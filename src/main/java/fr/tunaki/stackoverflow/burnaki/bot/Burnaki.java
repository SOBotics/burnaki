package fr.tunaki.stackoverflow.burnaki.bot;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import fr.tunaki.stackoverflow.burnaki.BurninationManager;
import fr.tunaki.stackoverflow.burnaki.api.StackExchangeAPIService;
import fr.tunaki.stackoverflow.burnaki.entity.BurninationProgress;
import fr.tunaki.stackoverflow.burnaki.entity.BurninationQuestion;
import fr.tunaki.stackoverflow.burnaki.service.BurninationUpdateEvent;
import fr.tunaki.stackoverflow.burnaki.service.BurninationUpdateListener;
import fr.tunaki.stackoverflow.chat.Message;
import fr.tunaki.stackoverflow.chat.Room;
import fr.tunaki.stackoverflow.chat.StackExchangeClient;
import fr.tunaki.stackoverflow.chat.User;
import fr.tunaki.stackoverflow.chat.event.EventType;

@Component
public class Burnaki implements Closeable, InitializingBean, BurninationUpdateListener {
	
	private static final String HOST = "stackoverflow.com";
	private static final int HQ_ROOM_ID = 111347; 
	
	private static final Logger LOGGER = LoggerFactory.getLogger(Burnaki.class);
	
	private StackExchangeClient client;
	private StackExchangeAPIService apiService;
	private BurninationManager burninationManager;
	private ConfigurableApplicationContext context;
	private BurnakiProperties properties;
	
	private Room hqRoom;
	private Map<Integer, BurnRoom> burnRooms;
	private Map<String, Integer> tagsMap;
	
	static final class BurnRoom {
		
		private Room room;
		private String tag;
		
		public BurnRoom(Room room, String tag) {
			this.room = room;
			this.tag = tag;
		}
		
	}
	
	@Autowired
	public Burnaki(StackExchangeClient client, StackExchangeAPIService apiService, BurninationManager burninationScheduler, ConfigurableApplicationContext context, BurnakiProperties properties) {
		this.client = client;
		this.apiService = apiService;
		this.burninationManager = burninationScheduler;
		this.context = context;
		this.properties = properties;
	}

	private void registerEventListeners(Room room) {
		room.addEventListener(EventType.USER_MENTIONED, event -> {
			String stripped = event.getMessage().getPlainContent().toLowerCase().replaceAll("\\s*@bur[^\\s$]*\\s*", "").trim();
			handleMessage(event.getMessage(), event.getRoomId(), stripped);
		});
		room.addEventListener(EventType.MESSAGE_REPLY, event -> {
			String stripped = event.getMessage().getPlainContent().toLowerCase().replaceAll("^:[^\\s$]*\\s*", "").trim();
			handleMessage(event.getMessage(), event.getRoomId(), stripped);
		});
	}

	private void handleMessage(Message message, int roomId, String plainContent) {
		long messageId = message.getId();
		if (plainContent.equals("stop")) {
			stopCommand(message, roomId);
		} else if (plainContent.startsWith("commands")) {
			commandsCommand(messageId, roomId);
		} else if (plainContent.startsWith("start tag")) {
			startTagCommand(messageId, plainContent.substring("start tag".length()).trim().split(" "));
		} else if (plainContent.startsWith("stop tag")) {
			stopTagCommand(messageId, roomId, plainContent.substring("stop tag".length()).trim().split(" "));
		} else if (plainContent.startsWith("get progress")) {
			getProgressCommand(messageId, roomId , plainContent.substring("get progress".length()).trim().split(" "));
		} else if (plainContent.startsWith("update progress")) {
			updateProgressCommand(messageId, roomId , plainContent.substring("update progress".length()).trim().split(" "));
		} else if (plainContent.startsWith("quota")) {
			quotaCommand(messageId, roomId);
		} else if (plainContent.startsWith("delete candidates")) {
			deleteCandidatesCommand(messageId, roomId, plainContent.substring("delete candidates".length()).trim().split(" "));
		} else {
			BurnRoom burnRoom = burnRooms.get(roomId);
			Room room = burnRoom == null ? hqRoom : burnRoom.room;
			room.send("Unknown command: " + sanitizeChatMessage(plainContent) + ". Use `commands` to have a list of commands.");
		}
	}

	private void stopCommand(Message message, int roomId) {
		BurnRoom burnRoom = burnRooms.get(roomId);
		Room room = burnRoom == null ? hqRoom : burnRoom.room;
		User user = message.getUser();
		if (!user.isModerator() && !user.isRoomOwner() && user.getId() != 1743880) {
			room.send("Nope. Only a moderator, a room owner or Tunaki can stop me.");
			return;
		}
		if (roomId == HQ_ROOM_ID) {
			burnRooms.forEach((id, r) -> r.room.send("I'm stopping, see you guys later!"));
			hqRoom.send("Bye.").thenRun(context::close);
		} else {
			room.send("Okay, I'm leaving this room.").thenRun(room::leave).thenRun(() -> burnRooms.remove(roomId));
		}
	}
	
	private void commandsCommand(long messageId, int roomId) {
		BurnRoom burnRoom = burnRooms.get(roomId);
		Room room = burnRoom == null ? hqRoom : burnRoom.room;
		room.replyTo(messageId, "Here's a list of commands:");
		String commands = ""
				+ "    commands                                - Prints the list of commands.\n"
				+ "    start tag [tag] [roomId] [link to Meta] - Starts the burnination of the given tag.\n"
				+ "    stop tag [tag]                          - Stops the burnination of the given tag. Can be omitted if ran inside the dedicated burn room.\n"
				+ "    get progress [tag]                      - Prints the current progress of the tag's burnination. Can be omitted if ran inside the dedicated burn room.\n"
				+ "    update progress [tag]                   - Force an update of the current progress of the tag's burnination. Can be omitted if ran inside the dedicated burn room.\n"
				+ "    quota                                   - Prints the remaining quota for the Stack Exchange API.";
		room.send(commands);
	}
	
	private void quotaCommand(long messageId, int roomId) {
		BurnRoom burnRoom = burnRooms.get(roomId);
		Room room = burnRoom == null ? hqRoom : burnRoom.room;
		room.replyTo(messageId, "Remaining quota is: " + apiService.getQuotaRemaining());
	}
	
	private void deleteCandidatesCommand(long messageId, int roomId, String[] tokens) {
		BurnRoom burnRoom = burnRooms.get(roomId);
		Room room = burnRoom == null ? hqRoom : burnRoom.room;
		String tag = (burnRoom == null || tokens.length == 0) ? cleanTag(tokens[0]) : burnRoom.tag;
		String json = burninationQuestionsToJson(burninationManager.getDeleteCandidates(tag), roomId, tag).toString();
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

	private void startTagCommand(long messageId, String[] tokens) {
		if (tokens.length != 3) {
			hqRoom.replyTo(messageId, "Cannot start burnination; 3 parameters required: \\[tag\\] \\[roomId\\] \\[link to Meta\\]");
			return;
		}
		String tag = cleanTag(tokens[0]);
		if (validateTag(messageId, tag)) {
			int roomId;
			try {
				roomId = Integer.parseInt(tokens[1]);
			} catch (NumberFormatException e) {
				hqRoom.replyTo(messageId, "Cannot start burnination; incorrect value for roomId: " + tokens[1]);
				return;
			}
			try {
				int size = burninationManager.start(tag, roomId, tokens[2]);
				hqRoom.replyTo(messageId, "Burnination of tag \\[" + tag + "\\] correctly started! Have fun, " + size + " questions to go!");
			} catch (Exception e) {
				LOGGER.error("Cannot start burnination of tag [{}]", tag, e);
				hqRoom.replyTo(messageId, "Cannot start burnination of tag \\[" + tag + "\\]: " + e.getMessage());
			}
			if (roomId != HQ_ROOM_ID) {
				Room newBurnRoom = client.joinRoom(HOST, roomId);
				burnRooms.put(roomId, new BurnRoom(newBurnRoom, tag));
				registerEventListeners(newBurnRoom);
			}
		}
	}

	private void stopTagCommand(long messageId, int roomId, String[] tokens) {
		BurnRoom burnRoom = burnRooms.get(roomId);
		Room room = burnRoom == null ? hqRoom : burnRoom.room;
		if (burnRoom == null && tokens.length == 0) {
			room.replyTo(messageId, "Cannot stop burnination; 1 parameters required: \\[tag\\]");
			return;
		}
		String tag = (burnRoom == null || tokens.length == 0) ? cleanTag(tokens[0]) : burnRoom.tag;
		if (validateTag(messageId, tag)) {
			try {
				burninationManager.stop(tag);
				room.replyTo(messageId, "Burnination of tag \\[" + tag + "\\] correctly stopped! I hope you had real fun!");
			} catch (Exception e) {
				LOGGER.error("Cannot stop burnination of tag [{}]", tag, e);
				room.replyTo(messageId, "Cannot stop burnination of tag \\[" + tag + "\\]: " + e.getMessage());
			}
		}
	}
	
	private void getProgressCommand(long messageId, int roomId, String[] tokens) {
		BurnRoom burnRoom = burnRooms.get(roomId);
		Room room = burnRoom == null ? hqRoom : burnRoom.room;
		if (burnRoom == null && tokens.length == 0) {
			room.replyTo(messageId, "Cannot get progress of burnination; 1 parameters required: \\[tag\\]");
			return;
		}
		String tag = (burnRoom == null || tokens.length == 0) ? cleanTag(tokens[0]) : burnRoom.tag;
		if (validateTag(messageId, tag)) {
			try {
				BurninationProgress progress = burninationManager.getProgress(tag);
				String message = "Here's a recap of the efforts so far for \\[" + tag + "\\]: Total questions (" + progress.getTotalQuestions() + "), Retagged (" + progress.getRetagged() + "), Closed (" + progress.getClosed() + "), Roombad (" + progress.getRoombad() + "), Manually deleted (" + progress.getManuallyDeleted() + ").";
				room.send(message);
			} catch (Exception e) {
				LOGGER.error("Cannot get progress of burnination for tag [{}]", tag, e);
				room.replyTo(messageId, "Cannot get progress of burnination for tag \\[" + tag + "\\]: " + e.getMessage());
			}
		}
	}
	
	private void updateProgressCommand(long messageId, int roomId, String[] tokens) {
		BurnRoom burnRoom = burnRooms.get(roomId);
		Room room = burnRoom == null ? hqRoom : burnRoom.room;
		if (burnRoom == null && tokens.length == 0) {
			room.replyTo(messageId, "Cannot update progress of burnination; 1 parameters required: \\[tag\\]");
			return;
		}
		String tag = (burnRoom == null || tokens.length == 0) ? cleanTag(tokens[0]) : burnRoom.tag;
		if (validateTag(messageId, tag)) {
			try {
				burninationManager.updateProgressNow(tag);
				room.send("Progress has been updated! Run `get progress` to get the current progress.");
			} catch (Exception e) {
				LOGGER.error("Cannot update progress of burnination for tag [{}]", tag, e);
				room.replyTo(messageId, "Cannot update progress of burnination for tag \\[" + tag + "\\]: " + e.getMessage());
			}
		}
	}

	private static String cleanTag(String tag) {
		if (tag.startsWith("[tag:")) tag = tag.substring(5, tag.lastIndexOf(']'));
		else if (tag.startsWith("[")) tag = tag.substring(1, tag.lastIndexOf(']'));
		return tag;
	}
	
	private static String sanitizeChatMessage(String message) {
		return message.replaceAll("(\\[|\\]|_|\\*|`)", "\\\\$1");
	}

	private boolean validateTag(long messageId, String tag) {
		if (!apiService.isValidTag(tag)) {
			LOGGER.warn("Tag [{}] doesn't exist", tag);
			hqRoom.replyTo(messageId, "Tag \\[" + tag + "\\] doesn't exist");
			return false;
		}
		return true;
	}

	@Override
	public void onUpdate(List<BurninationUpdateEvent> events) {
		if (!events.isEmpty()) {
			BurnRoom burnRoom = burnRooms.get(tagsMap.get(events.get(0).getTag()));
			Room room = burnRoom == null ? hqRoom : burnRoom.room;
			List<String> messages = events.stream().collect(groupingBy(BurninationUpdateEvent::getEvent, mapping(e -> "[" + sanitizeChatMessage(e.getQuestion().getTitle()) + "](" + e.getQuestion().getShareLink() + ")", joining(", ", ": ", ".")))).entrySet().stream().map(e -> e.getKey().getDisplay() + e.getValue()).collect(toList());
			messages.forEach(room::send);
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		hqRoom = client.joinRoom(HOST, HQ_ROOM_ID);
		Map<Integer, String> idToTag = burninationManager.getBurnRooms();
		burnRooms = idToTag.entrySet().stream().filter(e -> e.getKey() != HQ_ROOM_ID).collect(toMap(Map.Entry::getKey, e -> new BurnRoom(client.joinRoom(HOST, e.getKey()), e.getValue())));
		tagsMap = idToTag.entrySet().stream().collect(toMap(Map.Entry::getValue, Map.Entry::getKey));
		registerEventListeners(hqRoom);
		hqRoom.send("Hiya o/");
		burnRooms.forEach((k, v) -> {
			registerEventListeners(v.room);
			v.room.send("Hiya.");
		});
		burninationManager.addListener(this);
	}

	@Override
	public void close() throws IOException {
		client.close();
	}

}
