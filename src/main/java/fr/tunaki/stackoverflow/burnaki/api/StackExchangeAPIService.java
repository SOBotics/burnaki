package fr.tunaki.stackoverflow.burnaki.api;

import static fr.tunaki.stackoverflow.burnaki.util.Utils.unorderedBatchesWith;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.StreamSupport;

import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
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

@Service
@Transactional
public class StackExchangeAPIService {

	private static final Logger LOGGER = LoggerFactory.getLogger(StackExchangeAPIService.class);

	private StackExchangeAPIProperties properties;

	private int quota;

	@Autowired
	public StackExchangeAPIService(StackExchangeAPIProperties properties) {
		this.properties = properties;
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
				root = get("/questions", properties.getQuestionFilter(), "tagged", tag, "page", String.valueOf(page));
			} else {
				root = get("/questions", properties.getQuestionFilter(), "tagged", tag, "fromdate", String.valueOf(from.getEpochSecond()), "page", String.valueOf(page));
			}
			List<Question> questions = toQuestions(root, o -> itemToQuestion(o, new HashMap<>()));
			if (root.get("has_more").getAsBoolean()) {
				questions.addAll(getQuestionsWithTag(tag, from, page + 1));
			}
			return questions;
		} catch (IOException e) {
			throw new BurnakiException("Cannot fetch questions with tag", e);
		}
	}

	public List<Question> getQuestionsWithIds(Collection<Integer> ids) {
		LOGGER.debug("Retrieving all questions with ids '{}'", ids);
		List<String> idsStr = ids.stream().map(Object::toString).collect(unorderedBatchesWith(100, joining(";")));
		return idsStr.stream().flatMap(s -> getQuestionsWithIds(s, 1).stream()).collect(toList());
	}

	public boolean isValidTag(String tag) {
		LOGGER.debug("Checking if [{}] is a valid tag", tag);
		try {
			JsonObject root = get("/tags/" + tag + "/info", properties.getQuestionFilter());
			return root.get("items").getAsJsonArray().size() > 0;
		} catch (IOException e) {
			throw new BurnakiException("Cannot fetch tags", e);
		}

	}

	private List<Question> getQuestionsWithIds(String ids, int page) {
		LOGGER.debug("Retrieving all questions with ids '{}', page {}", ids, page);
		try {
			JsonObject root = get("/questions/" + ids, properties.getQuestionFilter(), "page", String.valueOf(page));
			Map<Integer, List<SuggestedEdit>> edits = getSuggestedEditsForPosts(ids);
			List<Question> questions = toQuestions(root, o -> itemToQuestion(o, edits));
			if (root.get("has_more").getAsBoolean()) {
				questions.addAll(getQuestionsWithIds(ids, page + 1));
			}
			return questions;
		} catch (IOException e) {
			throw new BurnakiException("Cannot fetch questions with ids", e);
		}
	}

	private Map<Integer, List<SuggestedEdit>> getSuggestedEditsForPosts(String ids) throws IOException {
		LOGGER.debug("Retrieving all suggested edits for posts with ids '{}'", ids);
		JsonObject root = get("/posts/" + ids + "/suggested-edits", properties.getSuggestedEditFilter());
		return StreamSupport.stream(root.get("items").getAsJsonArray().spliterator(), false).map(JsonElement::getAsJsonObject).collect(groupingBy(o -> o.get("post_id").getAsInt(), mapping(this::itemToSuggestedEdit, toList())));
	}

	private <T> List<T> toQuestions(JsonObject root, Function<JsonObject, T> mapper) {
		return StreamSupport.stream(root.get("items").getAsJsonArray().spliterator(), false).map(JsonElement::getAsJsonObject).map(mapper::apply).collect(toCollection(ArrayList::new));
	}

	private Question itemToQuestion(JsonObject object, Map<Integer, List<SuggestedEdit>> editsPosts) {
		Question question = new Question();
		question.setId(object.get("question_id").getAsInt());
		question.setLink(object.get("link").getAsString());
		question.setShareLink(object.get("share_link").getAsString());
		question.setTitle(Parser.unescapeEntities(object.get("title").getAsString(), false).trim());
		question.setTags(itemToTags(object));
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
		if (object.has("locked_date")) {
			question.setLockedDate(Instant.ofEpochSecond(object.get("locked_date").getAsLong()));
		}
		if (object.has("last_editor")) {
			question.setLastEditor(toShallowUser(object.get("last_editor").getAsJsonObject()));
		}
		if (object.has("accepted_answer_id")) {
			question.setAcceptedAnswerId(object.get("accepted_answer_id").getAsInt());
		}
		question.setAnswerCount(object.get("answer_count").getAsInt());
		question.setViewCount(object.get("view_count").getAsInt());
		question.setCommentCount(object.has("comment_count") ? object.get("comment_count").getAsInt() : 0); // bug?! field isn't marked possibly absent in doc
		question.setScore(object.get("score").getAsInt());
		question.setAnswered(object.get("is_answered").getAsBoolean());
		question.setMigrated(object.has("migrated_to"));
		if (object.has("closed_reason")) {
			question.setClosedReason(object.get("closed_reason").getAsString());
		}
		List<SuggestedEdit> edits = editsPosts.getOrDefault(question.getId(), Collections.emptyList());
		edits.stream().filter(se -> se.getApprovalDate() == null && se.getRejectionDate() == null).findFirst().ifPresent(question::setPendingSuggestedEdit);
		return question;
	}

	private SuggestedEdit itemToSuggestedEdit(JsonObject object) {
		SuggestedEdit se = new SuggestedEdit();
		se.setId(object.get("suggested_edit_id").getAsInt());
		se.setCreationDate(Instant.ofEpochSecond(object.get("creation_date").getAsLong()));
		if (object.has("approval_date")) {
			se.setApprovalDate(Instant.ofEpochSecond(object.get("approval_date").getAsLong()));
		}
		if (object.has("rejection_date")) {
			se.setRejectionDate(Instant.ofEpochSecond(object.get("rejection_date").getAsLong()));
		}
		if (object.has("tags")) {
			se.setTags(itemToTags(object));
		}
		return se;
	}

	private List<String> itemToTags(JsonObject object) {
		return StreamSupport.stream(object.get("tags").getAsJsonArray().spliterator(), false).map(JsonElement::getAsString).collect(toList());
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

	private JsonObject get(String method, String filter, String... data) throws IOException {
		Response response = Jsoup.connect(properties.getRootUrl() + method).data(data).data("site", properties.getSite(), "key", properties.getKey(), "filter", filter, "pageSize", properties.getPageSize()).method(Method.GET).ignoreContentType(true).ignoreHttpErrors(true).execute();
		String json = response.body();
		if (response.statusCode() != 200) {
			throw new IOException("HTTP " + response.statusCode() + " fetching URL " + (properties.getRootUrl() + method) + ". Body is: " + response.body());
		}
		JsonObject root = new JsonParser().parse(json).getAsJsonObject();
		quota = root.get("quota_remaining").getAsInt();
		handleBackoff(root);
		return root;
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

}
