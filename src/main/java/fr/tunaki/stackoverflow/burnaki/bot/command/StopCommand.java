package fr.tunaki.stackoverflow.burnaki.bot.command;

import java.util.function.BiPredicate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import fr.tunaki.stackoverflow.burnaki.bot.Burnaki;
import fr.tunaki.stackoverflow.burnaki.bot.BurnakiProperties;
import org.sobotics.chatexchange.chat.Message;
import org.sobotics.chatexchange.chat.Room;
import org.sobotics.chatexchange.chat.User;

@Component
public class StopCommand implements Command {

	private BurnakiProperties properties;
	private ConfigurableApplicationContext context;

	@Autowired
	public StopCommand(BurnakiProperties properties, ConfigurableApplicationContext context) {
		this.properties = properties;
		this.context = context;
	}

	@Override
	public String getName() {
		return "stop";
	}

	@Override
	public String getDescription() {
		return "When ran in a dedicated burn room, makes the bot leave the room. Otherwise, stops it.";
	}

	@Override
	public String getUsage() {
		return "stop";
	}

	@Override
	public BiPredicate<String, String> matches() {
		return String::equals;
	}

	@Override
	public int argumentCount() {
		return 0;
	}

	@Override
	public boolean requiresValidTag() {
		return false;
	}

	@Override
	public void execute(Message message, Room room, Burnaki burnaki, String[] arguments) {
		User user = message.getUser();
		if (!user.isModerator() && !user.isRoomOwner() && user.getId() != 1743880) {
			room.send("Nope. Only a moderator, a room owner or Tunaki can stop me.");
			return;
		}
		int roomId = room.getRoomId();
		if (roomId == properties.getHqRoomId()) {
			room.send("Bye.").thenRun(context::close);
		} else {
			room.send("Okay, I'm leaving this room.").thenRun(room::leave).thenRun(() -> burnaki.removeBurnRoom(roomId));
		}
	}

}
