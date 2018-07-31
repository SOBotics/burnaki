package fr.tunaki.stackoverflow.burnaki.bot.command;

import java.util.function.BiPredicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.tunaki.stackoverflow.burnaki.BurninationManager;
import fr.tunaki.stackoverflow.burnaki.bot.Burnaki;
import org.sobotics.chatexchange.chat.Message;
import org.sobotics.chatexchange.chat.Room;
import org.sobotics.chatexchange.chat.User;

@Component
public class StopTagCommand implements Command {

	private static final Logger LOGGER = LoggerFactory.getLogger(StopTagCommand.class);

	private BurninationManager burninationManager;

	@Autowired
	public StopTagCommand(BurninationManager burninationManager) {
		this.burninationManager = burninationManager;
	}

	@Override
	public String getName() {
		return "stop tag";
	}

	@Override
	public String getDescription() {
		return "Stops the burnination of the given tag. The tag can be omitted if ran inside the dedicated burn room.";
	}

	@Override
	public String getUsage() {
		return "stop tag [tag]";
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
	public boolean requiresValidTag() {
		return false; // it could be the case that the tag was already purged from the system when running the command
	}

	@Override
	public void execute(Message message, Room room, Burnaki burnaki, String[] arguments) {
		String tag = arguments[0];
		long messageId = message.getId();
		User user = message.getUser();
		if (!user.isModerator() && !user.isRoomOwner()) {
			room.send("Nope. Only a moderator or a room owner can stop the burnination.");
			return;
		}
		try {
			burninationManager.stop(tag);
			room.replyTo(messageId, "Burnination of tag \\[" + tag + "\\] correctly stopped! I hope you had real fun!");
		} catch (Exception e) {
			LOGGER.error("Cannot stop burnination of tag [{}]", tag, e);
			room.replyTo(messageId, "Cannot stop burnination of tag \\[" + tag + "\\]: " + e.getMessage());
		}
		burnaki.removeBurnination(tag, room.getRoomId());
	}

}
