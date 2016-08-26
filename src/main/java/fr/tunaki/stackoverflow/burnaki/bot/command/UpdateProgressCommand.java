package fr.tunaki.stackoverflow.burnaki.bot.command;

import java.util.function.BiPredicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.tunaki.stackoverflow.burnaki.BurninationManager;
import fr.tunaki.stackoverflow.burnaki.bot.Burnaki;
import fr.tunaki.stackoverflow.chat.Message;
import fr.tunaki.stackoverflow.chat.Room;

@Component
public class UpdateProgressCommand implements Command {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(UpdateProgressCommand.class);
	
	private BurninationManager burninationManager;
	
	@Autowired
	public UpdateProgressCommand(BurninationManager burninationManager) {
		this.burninationManager = burninationManager;
	}
	
	@Override
	public String getName() {
		return "update progress";
	}

	@Override
	public String getDescription() {
		return "Forces an update of the progress of the burnination. The tag can be omitted if ran inside the dedicated burn room.";
	}

	@Override
	public String getUsage() {
		return "update progress [tag]";
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
		try {
			burninationManager.updateProgressNow(tag);
			room.send("Progress has been updated! Run `get progress` to get the current progress.");
		} catch (Exception e) {
			LOGGER.error("Cannot update progress of burnination for tag [{}]", tag, e);
			room.replyTo(message.getId(), "Cannot update progress of burnination for tag \\[" + tag + "\\]: " + e.getMessage());
		}
	}

}
