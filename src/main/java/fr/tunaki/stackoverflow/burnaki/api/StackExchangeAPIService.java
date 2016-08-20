package fr.tunaki.stackoverflow.burnaki.api;

import static fr.tunaki.stackoverflow.burnaki.util.Utils.unorderedBatchesWith;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.jsoup.Connection.Method;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import fr.tunaki.stackoverflow.burnaki.BurnakiException;

@Component
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
				root = get("/questions", "tagged", tag, "page", String.valueOf(page));
			} else {
				root = get("/questions", "tagged", tag, "fromdate", String.valueOf(from.getEpochSecond()), "page", String.valueOf(page));
			}
			List<Question> questions = toQuestions(root);
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
		return idsStr.stream().flatMap(s -> getQuestionsWithIds(s, 1).stream()).collect(Collectors.toList());
		// post process returned list and compared with given list of ids to identity deleted questions
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
	
	private List<Question> getQuestionsWithIds(String ids, int page) {
		LOGGER.debug("Retrieving all questions with ids '{}', page {}", ids, page);
		try {
			JsonObject root = get("/questions/" + ids, "page", String.valueOf(page));
			List<Question> questions = toQuestions(root);
			if (root.get("has_more").getAsBoolean()) {
				handleBackoff(root);
				questions.addAll(getQuestionsWithIds(ids, page + 1));
			}
			return questions;
		} catch (IOException e) {
			throw new BurnakiException("Cannot fetch questions with ids", e);
		}
	}

	private ArrayList<Question> toQuestions(JsonObject root) {
		return StreamSupport.stream(root.get("items").getAsJsonArray().spliterator(), false).map(JsonElement::getAsJsonObject).map(this::itemToQuestion).collect(Collectors.toCollection(ArrayList::new));
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

	private Question itemToQuestion(JsonObject object) {
		Question question = new Question();
		question.setId(object.get("question_id").getAsInt());
		question.setLink(object.get("link").getAsString());
		question.setShareLink(object.get("share_link").getAsString());
		question.setTitle(object.get("title").getAsString());
		question.setTags(StreamSupport.stream(object.get("tags").getAsJsonArray().spliterator(), false).map(JsonElement::getAsString).collect(Collectors.toList()));
		question.setCloseVoteCount(object.get("close_vote_count").getAsInt());
		question.setReopenVoteCount(object.get("reopen_vote_count").getAsInt());
		question.setDeleteVoteCount(object.get("delete_vote_count").getAsInt());
		// FIXME: question.setUndeleteVoteCount(undeleteVoteCount);
		question.setCreatedDate(Instant.ofEpochSecond(object.get("creation_date").getAsLong()));
		// FIXME: question.setDeletedDate(deletedDate); there is "accepted_answer_id", "answer_count" and "is_answered".
		// FIXME: question.setRoombad(roombad);
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

}
