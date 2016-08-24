package fr.tunaki.stackoverflow.burnaki.api;

import static fr.tunaki.stackoverflow.burnaki.util.Utils.unorderedBatchesWith;
import static java.time.temporal.ChronoUnit.DAYS;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.jsoup.Connection.Method;
import org.jsoup.Jsoup;
import org.jsoup.parser.Parser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import fr.tunaki.stackoverflow.burnaki.BurnakiException;
import fr.tunaki.stackoverflow.burnaki.entity.SEApiQuestionCache;
import fr.tunaki.stackoverflow.burnaki.repository.SEApiQuestionCacheRepository;

@Service
@Transactional
public class StackExchangeAPIService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(StackExchangeAPIService.class);
	
	private StackExchangeAPIProperties properties;
	private SEApiQuestionCacheRepository questionCacheRepository;
	
	private int quota;
	
	@Autowired
	public StackExchangeAPIService(StackExchangeAPIProperties properties, SEApiQuestionCacheRepository questionCacheRepository) {
		this.properties = properties;
		this.questionCacheRepository = questionCacheRepository;
	}
	
	public List<Question> getQuestionsInTag(String tag, Instant from) {
		LOGGER.debug("Retrieving all questions tagged [{}]", tag);
		return getQuestionsWithTag(tag, from, 1);
	}
	
	private List<Question> getQuestionsWithTag(String tag, Instant from, int page) {
		if (page > properties.getMaxPage()) {
			throw new BurnakiException("Too many pages for tag [" + tag + "], stopped at page " + page);
		}
		LOGGER.debug("Retrieving all questions tagged [{}], page {}", tag, page);
		try {
			JsonObject root;
			if (from == null) {
				root = get("/questions", "tagged", tag, "page", String.valueOf(page));
			} else {
				root = get("/questions", "tagged", tag, "fromdate", String.valueOf(from.getEpochSecond()), "page", String.valueOf(page));
			}
			List<Question> questions = toQuestions(root, this::itemToQuestion);
			if (root.get("has_more").getAsBoolean()) {
				handleBackoff(root);
				questions.addAll(getQuestionsWithTag(tag, from, page + 1));
			}
			return questions;
		} catch (IOException e) {
			throw new BurnakiException("Cannot fetch questions with tag", e);
		}
	}

	public List<Question> getQuestionsWithIds(Collection<Integer> ids) {
		LOGGER.debug("Retrieving all questions with ids '{}'", ids);
		List<String> idsStr = ids.stream().map(Object::toString).collect(unorderedBatchesWith(100, Collectors.joining(";")));
		List<Tuple<Question, SEApiQuestionCache>> results = idsStr.stream().flatMap(s -> getQuestionsWithIds(s, 1).stream()).collect(Collectors.toList());
		
		// identify deleted questions (those for which the ID was given but not returned by API)
		Set<Integer> returnedIds = results.stream().map(t -> t.a.getId()).collect(Collectors.toSet());
		List<Integer> deltaIds = new ArrayList<>(ids);
		deltaIds.removeAll(returnedIds);
		Iterable<SEApiQuestionCache> deltaCaches = deltaIds.isEmpty() ? Collections.emptyList() : questionCacheRepository.findAll(deltaIds);
		List<Question> questions = results.stream().map(t -> t.a).collect(Collectors.toCollection(ArrayList::new));
		for (SEApiQuestionCache cached : deltaCaches) {
			if (cached.getDeletedDate() == null) {
				cached.setDeletedDate(Instant.now());
				questionCacheRepository.save(cached);
			}
			questions.add(questionCacheToQuestion(cached));
		}
		questionCacheRepository.save(results.stream().map(t -> t.b).collect(Collectors.toList()));
		return questions;
	}

	private boolean wasProbablyRoombad(SEApiQuestionCache cached) {
		long age = DAYS.between(cached.getCreatedDate(), Instant.now());
		return /* RemoveDeadQuestions */
				(age >= 30 && cached.getScore() <= -1 && cached.getAnswerCount() == 0 && !cached.isLocked()) ||
			   /* RemoveMigrationStubs */
				(age >= 30 && cached.isMigrated()) ||
			   /* RemoveAbandonedQuestions */
				(age >= 365 && cached.getScore() == 0 && cached.getAnswerCount() == 0 && !cached.isLocked() && 
				 cached.getViewCount() <= 1.5 * age && cached.getCommentCount() <= 1
				) ||
			   /* RemoveAbandonedClosed */
				(cached.getClosedDate() != null && DAYS.between(cached.getClosedDate(), Instant.now()) >= 9 && 
				 !cached.isClosedAsDuplicate() && cached.getScore() <= 0 && !cached.isLocked() && !cached.isAnswered() && 
				 !cached.isWithAcceptedAnswer() && cached.getReopenVoteCount() == 0 && 
				 (cached.getLastEditDate() == null || DAYS.between(cached.getLastEditDate(), Instant.now()) >= 9)
				);
	}
	
	public boolean isValidTag(String tag) {
		LOGGER.debug("Checking if [{}] is a valid tag", tag);
		try {
			JsonObject root = get("/tags/" + tag + "/info");
			return root.get("items").getAsJsonArray().size() > 0;
		} catch (IOException e) {
			throw new BurnakiException("Cannot fetch tags", e);
		}
		
	}
	
	private List<Tuple<Question, SEApiQuestionCache>> getQuestionsWithIds(String ids, int page) {
		LOGGER.debug("Retrieving all questions with ids '{}', page {}", ids, page);
		try {
			JsonObject root = get("/questions/" + ids, "page", String.valueOf(page));
			List<Tuple<Question, SEApiQuestionCache>> questions = toQuestions(root, o -> new Tuple<>(itemToQuestion(o), itemToQuestionCache(o)));
			if (root.get("has_more").getAsBoolean()) {
				handleBackoff(root);
				questions.addAll(getQuestionsWithIds(ids, page + 1));
			}
			return questions;
		} catch (IOException e) {
			throw new BurnakiException("Cannot fetch questions with ids", e);
		}
	}

	private <T> List<T> toQuestions(JsonObject root, Function<JsonObject, T> mapper) {
		return StreamSupport.stream(root.get("items").getAsJsonArray().spliterator(), false).map(JsonElement::getAsJsonObject).map(mapper::apply).collect(Collectors.toCollection(ArrayList::new));
	}
	
	private void handleBackoff(JsonObject root) {
		if (root.has("backoff")) {
			int backoff = root.get("backoff").getAsInt();
			LOGGER.warn("Backing off {} seconds", backoff);
			try {
				Thread.sleep(1000 * backoff);
			} catch (InterruptedException e) {
				LOGGER.error("Couldn't backoff for {} seconds, was interrupted!", backoff, e);
			}
		}
	}

	private Question questionCacheToQuestion(SEApiQuestionCache cached) {
		Question question = new Question();
		question.setClosedDate(cached.getClosedDate());
		question.setCloseVoteCount(cached.getCloseVoteCount());
		question.setCreatedDate(cached.getCreatedDate());
		question.setDeletedDate(cached.getDeletedDate());
		question.setId(cached.getId());
		question.setLastEditDate(cached.getLastEditDate());
		question.setReopenVoteCount(cached.getReopenVoteCount());
		question.setRoombad(wasProbablyRoombad(cached));
		question.setShareLink(cached.getShareLink());
		question.setTags(Arrays.asList(cached.getTags().split(",")));
		question.setTitle(cached.getTitle());
		return question;
	}

	private Question itemToQuestion(JsonObject object) {
		Question question = new Question();
		question.setId(object.get("question_id").getAsInt());
		question.setLink(object.get("link").getAsString());
		question.setShareLink(object.get("share_link").getAsString());
		question.setTitle(Parser.unescapeEntities(object.get("title").getAsString(), false));
		question.setTags(StreamSupport.stream(object.get("tags").getAsJsonArray().spliterator(), false).map(JsonElement::getAsString).collect(Collectors.toList()));
		question.setCloseVoteCount(object.get("close_vote_count").getAsInt());
		question.setReopenVoteCount(object.get("reopen_vote_count").getAsInt());
		question.setDeleteVoteCount(object.get("delete_vote_count").getAsInt());
		question.setCreatedDate(Instant.ofEpochSecond(object.get("creation_date").getAsLong()));
		if (object.has("closed_date")) {
			question.setClosedDate(Instant.ofEpochSecond(object.get("closed_date").getAsLong()));
		}
		if (object.has("last_edit_date")) {
			question.setLastEditDate(Instant.ofEpochSecond(object.get("last_edit_date").getAsLong()));
		}
		if (object.has("last_editor")) {
			question.setLastEditor(toShallowUser(object.get("last_editor").getAsJsonObject()));
		}
		return question;
	}
	
	private SEApiQuestionCache itemToQuestionCache(JsonObject object) {
		SEApiQuestionCache questionCache = new SEApiQuestionCache();
		questionCache.setAnswerCount(object.get("answer_count").getAsInt());
		questionCache.setAnswered(object.get("is_answered").getAsBoolean());
		if (object.has("closed_reason")) {
			questionCache.setClosedAsDuplicate("duplicate".equals(object.get("closed_reason").getAsString()));
		}
		if (object.has("closed_date")) {
			questionCache.setClosedDate(Instant.ofEpochSecond(object.get("closed_date").getAsLong()));
		}
		questionCache.setCloseVoteCount(object.get("close_vote_count").getAsInt());
		questionCache.setCommentCount(object.has("comment_count") ? object.get("comment_count").getAsInt() : 0); // bug?! field isn't marked possibly absent in doc
		questionCache.setCreatedDate(Instant.ofEpochSecond(object.get("creation_date").getAsLong()));
		questionCache.setId(object.get("question_id").getAsInt());
		if (object.has("last_edit_date")) {
			questionCache.setLastEditDate(Instant.ofEpochSecond(object.get("last_edit_date").getAsLong()));
		}
		questionCache.setLocked(object.has("locked_date"));
		questionCache.setMigrated(object.has("migrated_to"));
		questionCache.setReopenVoteCount(object.get("reopen_vote_count").getAsInt());
		questionCache.setScore(object.get("score").getAsInt());
		questionCache.setShareLink(object.get("share_link").getAsString());
		questionCache.setTags(StreamSupport.stream(object.get("tags").getAsJsonArray().spliterator(), false).map(JsonElement::getAsString).collect(Collectors.joining(",")));
		questionCache.setTitle(Parser.unescapeEntities(object.get("title").getAsString(), false));
		questionCache.setViewCount(object.get("view_count").getAsInt());
		questionCache.setWithAcceptedAnswer(object.has("accepted_answer_id"));
		return questionCache;
	}
	
	private ShallowUser toShallowUser(JsonObject object) {
		ShallowUser user = new ShallowUser();
		user.setDisplayName(object.get("display_name").getAsString());
		user.setLink(object.get("link").getAsString());
		return user;
	}

	public int getQuotaRemaining() {
		return quota;
	}

	private JsonObject get(String method, String... data) throws IOException {
		String json = Jsoup.connect(properties.getRootUrl() + method).data(data).data("site", properties.getSite(), "key", properties.getKey(), "filter", properties.getFilter(), "pageSize", properties.getPageSize()).method(Method.GET).ignoreContentType(true).execute().body();
		JsonObject root = new JsonParser().parse(json).getAsJsonObject();
		quota = root.get("quota_remaining").getAsInt();
		return root;
	}

	private static final class Tuple<A,B> {
		A a; B b;
		public Tuple(A a, B b) {
			this.a = a;
			this.b = b;
		}
	}

}
