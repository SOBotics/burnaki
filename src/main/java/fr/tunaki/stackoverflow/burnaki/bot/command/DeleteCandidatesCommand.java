package fr.tunaki.stackoverflow.burnaki.bot.command;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import fr.tunaki.stackoverflow.burnaki.BurninationManager;
import fr.tunaki.stackoverflow.burnaki.api.StackExchangeAPIService;
import fr.tunaki.stackoverflow.burnaki.bot.Burnaki;
import fr.tunaki.stackoverflow.burnaki.bot.BurnakiProperties;
import fr.tunaki.stackoverflow.burnaki.entity.BurninationQuestion;
import fr.tunaki.stackoverflow.chat.Message;
import fr.tunaki.stackoverflow.chat.Room;

@Component
public class DeleteCandidatesCommand implements Command {

	private static final Logger LOGGER = LoggerFactory.getLogger(DeleteCandidatesCommand.class);

	private BurnakiProperties properties;
	private StackExchangeAPIService apiService;
	private BurninationManager burninationManager;

	@Autowired
	public DeleteCandidatesCommand(BurnakiProperties properties, StackExchangeAPIService apiService, BurninationManager burninationManager) {
		this.properties = properties;
		this.apiService = apiService;
		this.burninationManager = burninationManager;
	}

	@Override
	public String getName() {
		return "delete candidates";
	}

	@Override
	public String getDescription() {
		return "Returns delete candidates and posts having delete votes. The tag can be omitted if ran inside the dedicated burn room.";
	}

	@Override
	public String getUsage() {
		return "delete candidates [tag]";
	}

	@Override
	public BiPredicate<String, String> matches() {
		return String::startsWith;
	}

	@Override
	public int argumentCount() {
		return 1;
	}

	@Override
	public void execute(Message message, Room room, Burnaki burnaki, String[] arguments) {
		String tag = arguments[0];
		String json = burninationQuestionsToJson(burninationManager.getDeleteCandidates(tag), room.getRoomId(), tag).toString();
		LOGGER.debug("POSTing JSON data '{}' to {}", json, properties.getRestApi());
		try {
			HttpURLConnection connection = getConnection(properties.getRestApi());
			try (GZIPOutputStream gos = new GZIPOutputStream(connection.getOutputStream())) {
				StreamUtils.copy(new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)), gos);
				gos.flush();
			}
			int responseCode = connection.getResponseCode();
			if (responseCode != 200) {
				LOGGER.error("Couldn't contact server, status={}", responseCode);
				room.send("Looks like I couldn't contact Sam's server, feel free to poke him about it. Status code: " + responseCode);
				return;
			}
			try (GZIPInputStream gis = new GZIPInputStream(connection.getInputStream())) {
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				StreamUtils.copy(gis, bos);
				String response = bos.toString("UTF-8");
				room.send("Delete candidates available here: " + response);
			}
		} catch (IOException e) {
			LOGGER.error("Couldn't contact server", e);
			return;
		}
	}

	private HttpURLConnection getConnection(String url) throws IOException {
		HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
		conn.setConnectTimeout(10 * 1000);
		conn.setReadTimeout(10 * 1000);
		conn.setDoOutput(true);
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Content-Type", "application/json");
		conn.setRequestProperty("Content-Encoding", "gzip");
		conn.setRequestProperty("Accept-Encoding", "gzip");
		return conn;
	}

	private JsonObject burninationQuestionsToJson(List<BurninationQuestion> bqs, long roomId, String tag) {
		JsonObject json = new JsonObject();
		json.addProperty("timestamp", Instant.now().getEpochSecond());
		json.addProperty("room_id", roomId);
		json.addProperty("batch_nr", 1);
		json.addProperty("search_tag", tag);
		json.addProperty("is_filtered_duplicates", false);
		json.addProperty("api_quota", apiService.getQuotaRemaining());
		JsonArray questions = new JsonArray();
		for (BurninationQuestion bq : bqs) {
			JsonObject jsonBq = new JsonObject();
			jsonBq.addProperty("link", bq.getLink());
			jsonBq.addProperty("title", bq.getTitle());
			jsonBq.addProperty("score", bq.getScore());
			jsonBq.addProperty("creation_date", bq.getCreatedDate().getEpochSecond());
			jsonBq.addProperty("view_count", bq.getViewCount());
			jsonBq.addProperty("answer_count", bq.getAnswerCount());
			jsonBq.addProperty("accepted_answer_id", bq.getAcceptedAnswerId() == null ? 0 : bq.getAcceptedAnswerId());
			jsonBq.addProperty("close_vote_count", bq.getCloseVoteCount());
			questions.add(jsonBq);
		}
		json.add("questions", questions);
		return json;
	}

}
