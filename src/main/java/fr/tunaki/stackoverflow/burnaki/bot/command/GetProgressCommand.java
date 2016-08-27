package fr.tunaki.stackoverflow.burnaki.bot.command;

import java.util.function.BiPredicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.tunaki.stackoverflow.burnaki.BurninationManager;
import fr.tunaki.stackoverflow.burnaki.bot.Burnaki;
import fr.tunaki.stackoverflow.burnaki.entity.BurninationProgress;
import fr.tunaki.stackoverflow.chat.Message;
import fr.tunaki.stackoverflow.chat.Room;

@Component
public class GetProgressCommand implements Command {

	private static final Logger LOGGER = LoggerFactory.getLogger(GetProgressCommand.class);

	private BurninationManager burninationManager;

	@Autowired
	public GetProgressCommand(BurninationManager burninationManager) {
		this.burninationManager = burninationManager;
	}

	@Override
	public String getName() {
		return "get progress";
	}

	@Override
	public String getDescription() {
		return "Prints the current progress of the tag's burnination. The tag can be omitted if ran inside the dedicated burn room.";
	}

	@Override
	public String getUsage() {
		return "get progress [tag]";
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
			BurninationProgress progress = burninationManager.getProgress(tag);
			room.send("Here's a recap of the efforts so far for \\[" + tag + "\\]: Total questions (" + progress.getTotalQuestions() + "), Retagged (" + progress.getRetagged() + "), Closed (" + progress.getClosed() + "), Roombad (" + progress.getRoombad() + "), Manually deleted (" + progress.getManuallyDeleted() + ").");
		} catch (Exception e) {
			LOGGER.error("Cannot get progress of burnination for tag [{}]", tag, e);
			room.replyTo(message.getId(), "Cannot get progress of burnination for tag \\[" + tag + "\\]: " + e.getMessage());
		}
	}

}
