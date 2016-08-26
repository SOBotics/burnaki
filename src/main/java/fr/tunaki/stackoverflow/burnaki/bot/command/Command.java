package fr.tunaki.stackoverflow.burnaki.bot.command;

import java.util.function.BiPredicate;

import fr.tunaki.stackoverflow.burnaki.bot.Burnaki;
import fr.tunaki.stackoverflow.chat.Message;
import fr.tunaki.stackoverflow.chat.Room;

public interface Command {
	
	String getName();
	
	String getDescription();
	
	String getUsage();
	
	BiPredicate<String, String> matches();
	
	int argumentCount();
	
	void execute(Message message, Room room, Burnaki burnaki, String[] arguments);

}
