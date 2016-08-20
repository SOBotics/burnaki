package fr.tunaki.stackoverflow.burnaki.bot;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import fr.tunaki.stackoverflow.burnaki.BurninationManager;
import fr.tunaki.stackoverflow.burnaki.api.StackExchangeAPIService;
import fr.tunaki.stackoverflow.burnaki.entity.BurninationProgress;
import fr.tunaki.stackoverflow.burnaki.service.BurninationUpdateEvent;
import fr.tunaki.stackoverflow.burnaki.service.BurninationUpdateListener;
import fr.tunaki.stackoverflow.chat.Room;
import fr.tunaki.stackoverflow.chat.StackExchangeClient;
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
	public Burnaki(StackExchangeClient client, StackExchangeAPIService apiService, BurninationManager burninationScheduler, ConfigurableApplicationContext context) {
		this.client = client;
		this.apiService = apiService;
		this.burninationManager = burninationScheduler;
		this.context = context;
	}

	private void registerEventListeners(Room room) {
		room.addEventListener(EventType.USER_MENTIONED, event -> {
			String stripped = event.getMessage().getPlainContent().toLowerCase().replaceAll("\\s*@tun[^\\s$]*\\s*", "").trim();
			handleMessage(event.getMessage().getId(), event.getRoomId(), stripped);
		});
		room.addEventListener(EventType.MESSAGE_REPLY, event -> {
			String stripped = event.getMessage().getPlainContent().toLowerCase().replaceAll("^:[^\\s$]*\\s*", "").trim();
			handleMessage(event.getMessage().getId(), event.getRoomId(), stripped);
		});
	}

	private void handleMessage(long messageId, int roomId, String message) {
		if (message.startsWith("stop bot")) {
			context.close();
		} else if (message.startsWith("commands")) {
			commandsCommand(messageId, roomId);
		} else if (message.startsWith("start tag")) {
			startTagCommand(messageId, message.substring("start tag".length()).trim().split(" "));
		} else if (message.startsWith("stop tag")) {
			stopTagCommand(messageId, roomId, message.substring("stop tag".length()).trim().split(" "));
		} else if (message.startsWith("get progress")) {
			getProgressCommand(messageId, roomId , message.substring("get progress".length()).trim().split(" "));
		} else if (message.startsWith("update progress")) {
			updateProgressCommand(messageId, roomId , message.substring("update progress".length()).trim().split(" "));
		} else if (message.startsWith("quota")) {
			quotaCommand(messageId, roomId);
		} else {
			BurnRoom burnRoom = burnRooms.get(roomId);
			Room room = burnRoom == null ? hqRoom : burnRoom.room;
			room.send("Unknown command: " + sanitizeChatMessage(message) + ". Use `commands` to have a list of commands.");
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
				+ "    update progress [tag]                   - Force an update of the current progress of the tag's burnination.\n"
				+ "    quota                                   - Prints the remaining quota for the Stack Exchange API.";
		room.send(commands);
	}
	
	private void quotaCommand(long messageId, int roomId) {
		BurnRoom burnRoom = burnRooms.get(roomId);
		Room room = burnRoom == null ? hqRoom : burnRoom.room;
		room.replyTo(messageId, "Remaining quota is: " + apiService.getQuotaRemaining());
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
				String message = "Here's a recap of your efforts so far for \\[" + tag + "\\]: Total questions (" + progress.getTotalQuestions() + "), Retagged (" + progress.getRetagged() + "), Closed (" + progress.getClosed() + "), Roombad (" + progress.getRoombad() + "), Manually deleted (" + progress.getManuallyDeleted() + ").";
				hqRoom.replyTo(messageId, message);
			} catch (Exception e) {
				LOGGER.error("Cannot get progress of burnination for tag [{}]", tag, e);
				hqRoom.replyTo(messageId, "Cannot get progress of burnination for tag \\[" + tag + "\\]: " + e.getMessage());
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
				hqRoom.send("Progress has been updated! Run `get progress` to get the current progress.");
			} catch (Exception e) {
				LOGGER.error("Cannot update progress of burnination for tag [{}]", tag, e);
				hqRoom.replyTo(messageId, "Cannot update progress of burnination for tag \\[" + tag + "\\]: " + e.getMessage());
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
			List<String> messages = events.stream().collect(groupingBy(BurninationUpdateEvent::getEvent, mapping(e -> "[" + sanitizeChatMessage(e.getQuestion().getTitle()) + "](" + e.getQuestion().getShareLink() + ")", joining(", ", ": ", ".")))).entrySet().stream().map(e -> " - " + e.getKey().name() + e.getValue()).collect(toList());
			room.send("New notifications for the burn team!");
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
		burnRooms.forEach((k, v) -> registerEventListeners(v.room));
		burninationManager.addListener(this);
	}

	@Override
	public void close() throws IOException {
		client.close();
	}

}
