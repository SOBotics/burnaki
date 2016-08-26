package fr.tunaki.stackoverflow.burnaki.bot.command;

import java.util.function.BiPredicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.tunaki.stackoverflow.burnaki.BurninationManager;
import fr.tunaki.stackoverflow.burnaki.bot.BurnRoom;
import fr.tunaki.stackoverflow.burnaki.bot.Burnaki;
import fr.tunaki.stackoverflow.burnaki.bot.BurnakiProperties;
import fr.tunaki.stackoverflow.chat.Message;
import fr.tunaki.stackoverflow.chat.Room;
import fr.tunaki.stackoverflow.chat.StackExchangeClient;

@Component
public class StartTagCommand implements Command {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(StartTagCommand.class);
	
	private BurnakiProperties properties;
	private BurninationManager burninationManager;
	private StackExchangeClient client;
	
	@Autowired
	public StartTagCommand(BurnakiProperties properties, BurninationManager burninationManager, StackExchangeClient client) {
		this.properties = properties;
		this.burninationManager = burninationManager;
		this.client = client;
	}

	@Override
	public String getName() {
		return "start tag";
	}

	@Override
	public String getDescription() {
		return "Starts the burnination of the given tag.";
	}

	@Override
	public String getUsage() {
		return "start tag [tag] [roomId] [Meta]";
	}

	@Override
	public BiPredicate<String, String> matches() {
		return String::startsWith;
	}

	@Override
	public int argumentCount() {
		return 3;
	}

	@Override
	public void execute(Message message, Room room, Burnaki burnaki, String[] arguments) {
		int roomId;
		long messageId = message.getId();
		String tag = arguments[0];
		String metaLink = arguments[2];
		try {
			roomId = Integer.parseInt(arguments[1]);
		} catch (NumberFormatException e) {
			room.replyTo(messageId , "Cannot start burnination; incorrect value for roomId: " + arguments[1]);
			return;
		}
		try {
			int size = burninationManager.start(tag, roomId, metaLink);
			room.replyTo(messageId, "Burnination of tag \\[" + tag + "\\] correctly started! Have fun, " + size + " questions to go!");
		} catch (Exception e) {
			LOGGER.error("Cannot start burnination of tag [{}]", tag, e);
			room.replyTo(messageId, "Cannot start burnination of tag \\[" + tag + "\\]: " + e.getMessage());
		}
		if (roomId != properties.getHqRoomId()) {
			BurnRoom burnRoom = new BurnRoom(client.joinRoom(properties.getHost(), roomId), tag);
			burnaki.getBurnRooms().put(roomId, burnRoom);
			burnaki.registerEventListeners(burnRoom.getRoom());
		}
	}

}
