package fr.tunaki.stackoverflow.burnaki.bot;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.io.Closeable;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.tunaki.stackoverflow.burnaki.BurninationManager;
import fr.tunaki.stackoverflow.burnaki.api.StackExchangeAPIService;
import fr.tunaki.stackoverflow.burnaki.bot.command.Command;
import fr.tunaki.stackoverflow.burnaki.service.BurninationUpdateEvent;
import fr.tunaki.stackoverflow.burnaki.service.BurninationUpdateListener;
import fr.tunaki.stackoverflow.chat.Message;
import fr.tunaki.stackoverflow.chat.Room;
import fr.tunaki.stackoverflow.chat.StackExchangeClient;
import fr.tunaki.stackoverflow.chat.event.EventType;

@Component
public class Burnaki implements Closeable, InitializingBean, BurninationUpdateListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(Burnaki.class);

	private List<Command> commands;
	private StackExchangeClient client;
	private StackExchangeAPIService apiService;
	private BurninationManager burninationManager;
	private BurnakiProperties properties;

	private BurnRoom hqRoom;
	private Map<Integer, BurnRoom> burnRooms;
	private Map<String, Integer> tagsMap;

	@Autowired
	public Burnaki(List<Command> commands, StackExchangeClient client, StackExchangeAPIService apiService, BurninationManager burninationScheduler, BurnakiProperties properties) {
		this.commands = commands;
		this.client = client;
		this.apiService = apiService;
		this.burninationManager = burninationScheduler;
		this.properties = properties;
	}

	public void registerEventListeners(Room room) {
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
		BurnRoom room = burnRooms.getOrDefault(roomId, hqRoom);
		boolean wasCommandMatched = false;
		for (Command command : commands) {
			if (command.matches().test(plainContent, command.getName())) {
				wasCommandMatched = true;
				String[] arguments = plainContent.substring(command.getName().length()).trim().split(" ");
				arguments = Arrays.stream(arguments).filter(s -> !s.isEmpty()).toArray(String[]::new);
				if (burnRooms.containsKey(roomId) && room.getTags().size() == 1) {
					if (arguments.length == command.argumentCount() - 1) {
						arguments = Stream.concat(Stream.of(room.getTags().get(0)), Arrays.stream(arguments)).toArray(String[]::new);
					} else if (arguments.length < command.argumentCount()) {
						room.getRoom().send("Not enough arguments for command `" + command.getName() + "`. Usage is: `" + command.getUsage() + "`.");
						return;
					}
				} else {
					if (arguments.length < command.argumentCount()) {
						room.getRoom().send("Not enough arguments for command `" + command.getName() + "`. Usage is: `" + command.getUsage() + "`.");
						return;
					} else if (arguments.length > 0) {
						arguments[0] = cleanTag(arguments[0]);
						if (!apiService.isValidTag(arguments[0])) {
							LOGGER.warn("Tag [{}] doesn't exist", arguments[0]);
							room.getRoom().replyTo(messageId, "Tag \\[" + arguments[0] + "\\] doesn't exist");
							return;
						}
					}
				}
				command.execute(message, room.getRoom(), this, arguments);
				break;
			}
		}
		if (!wasCommandMatched) {
			room.getRoom().send("Unknown command: " + sanitizeChatMessage(plainContent) + ". Use `commands` to have a list of commands.");
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

	@Override
	public void onUpdate(List<BurninationUpdateEvent> events) {
		if (!events.isEmpty()) {
			String tag = events.get(0).getTag();
			BurnRoom burnRoom = burnRooms.getOrDefault(tagsMap.get(tag), hqRoom);
			boolean singleTag = burnRoom.getTags().size() == 1;
			List<String> messages = events.stream().collect(groupingBy(BurninationUpdateEvent::getEvent, mapping(e -> "[" + sanitizeChatMessage(e.getQuestion().getTitle()) + "](" + e.getQuestion().getShareLink() + ")", joining(", ", ": ", ".")))).entrySet().stream().map(e -> (singleTag ? "" : "\\[" + tag + "\\] ") + e.getKey().getDisplay() + e.getValue()).collect(toList());
			messages.forEach(burnRoom.getRoom()::send);
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		hqRoom = new BurnRoom(client.joinRoom(properties.getHost(), properties.getHqRoomId()));
		Map<Integer, List<String>> idToTags = burninationManager.getBurnRooms();
		burnRooms = idToTags.entrySet().stream().collect(toMap(Map.Entry::getKey, e -> new BurnRoom(client.joinRoom(properties.getHost(), e.getKey()), e.getValue())));
		tagsMap = idToTags.entrySet().stream().flatMap(e -> e.getValue().stream().map(t -> new AbstractMap.SimpleEntry<>(e.getKey(), t))).collect(toMap(Map.Entry::getValue, Map.Entry::getKey));
		registerEventListeners(hqRoom.getRoom());
		hqRoom.getRoom().send("Hiya o/");
		burnRooms.forEach((k, v) -> registerEventListeners(v.getRoom()));
		burninationManager.addListener(this);
	}

	@Override
	public void close() throws IOException {
		client.close();
	}

	public Map<Integer, BurnRoom> getBurnRooms() {
		return burnRooms;
	}

}
