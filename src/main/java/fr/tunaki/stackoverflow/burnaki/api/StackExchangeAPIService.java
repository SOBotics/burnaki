package fr.tunaki.stackoverflow.burnaki.api;

import static fr.tunaki.stackoverflow.burnaki.util.Utils.unorderedBatchesWith;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.jsoup.Connection.Method;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class StackExchangeAPIService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(StackExchangeAPIService.class);
	private static final String ROOT_URL = "https://api.stackexchange.com/2.2";
	private static final String PAGE_SIZE = "100";
	
	private String site;
	private String apiKey;
	private String filter;
	
	private int quota;
	
	public StackExchangeAPIService(String apiKey, String filter) {
		this("stackoverflow", apiKey, filter);
	}
	
	public StackExchangeAPIService(String site, String apiKey, String filter) {
		this.site = site;
		this.apiKey = apiKey;
		this.filter = filter;
	}

	public List<Question> getQuestionsWithTag(String tag) {
		LOGGER.info("Retrieving all questions tagged [{}]", tag);
		return getQuestionsWithTag(tag, 1);
	}
	
	private List<Question> getQuestionsWithTag(String tag, int page) {
		LOGGER.debug("Retrieving all questions tagged [{}], page {}", tag, page);
		try {
			JsonObject root = get("/questions", "tagged", tag, "page", Integer.toString(page));
			List<Question> questions = toQuestions(root);
			System.out.println(questions.size());
			if (root.get("has_more").getAsBoolean()) {
				handleBackoff(root);
				questions.addAll(getQuestionsWithTag(tag, page + 1));
			}
			return questions;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public List<Question> getQuestionsWithIds(List<Long> ids) {
		LOGGER.info("Retrieving all questions with ids '{}'", ids);
		List<String> idsStr = ids.stream().map(Object::toString).collect(unorderedBatchesWith(100, Collectors.joining(";")));
		return idsStr.stream().flatMap(s -> getQuestionsWithIds(s, 1).stream()).collect(Collectors.toList());
	}
	
	private List<Question> getQuestionsWithIds(String ids, int page) {
		LOGGER.info("Retrieving all questions with ids '{}', page {}", ids, page);
		try {
			JsonObject root = get("/questions/" + ids, "page", Integer.toString(page));
			List<Question> questions = toQuestions(root);
			if (root.get("has_more").getAsBoolean()) {
				handleBackoff(root);
				questions.addAll(getQuestionsWithIds(ids, page + 1));
			}
			return questions;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	private ArrayList<Question> toQuestions(JsonObject root) {
		return StreamSupport.stream(root.get("items").getAsJsonArray().spliterator(), false).map(JsonElement::getAsJsonObject).map(this::itemToQuestion).collect(Collectors.toCollection(ArrayList::new));
	}
	
	private void handleBackoff(JsonObject root) {
		if (root.has("backoff")) {
			int backoff = root.get("backoff").getAsInt();
			LOGGER.info("Backing off {} seconds", backoff);
			try {
				Thread.sleep(1000 * backoff);
			} catch (InterruptedException e) {
				LOGGER.error("Couldn't backoff for {} seconds, was interrupted!", backoff, e);
			}
		}
	}

	private Question itemToQuestion(JsonObject object) {
		Question question = new Question();
		question.setId(object.get("question_id").getAsLong());
		question.setLink(object.get("link").getAsString());
		question.setTitle(object.get("title").getAsString());
		if (object.has("closed_date")) {
			question.setClosedDate(Instant.ofEpochSecond(object.get("closed_date").getAsLong()));
		}
		question.setCloseVoteCount(object.get("close_vote_count").getAsInt());
		question.setReopenVoteCount(object.get("reopen_vote_count").getAsInt());
		question.setDeleteVoteCount(object.get("delete_vote_count").getAsInt());
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
		String json = Jsoup.connect(ROOT_URL + method).data(data).data("site", site, "key", apiKey, "filter", filter, "pageSize", PAGE_SIZE).method(Method.GET).ignoreContentType(true).execute().body();
		JsonObject root = new JsonParser().parse(json).getAsJsonObject();
		quota = root.get("quota_remaining").getAsInt();
		return root;
	}

}
