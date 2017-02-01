package fr.tunaki.stackoverflow.burnaki.bot.command;

import java.util.function.BiPredicate;

import fr.tunaki.stackoverflow.burnaki.bot.Burnaki;
import fr.tunaki.stackoverflow.chat.Message;
import fr.tunaki.stackoverflow.chat.Room;

public interface Command {

	/**
	 * Name of the command. This is the keywords needed to launch it.
	 * @return Name of the command.
	 */
	String getName();

	/**
	 * Description of the command. This will be used to display textual information about what it does.
	 * @return Description of the command.
	 */
	String getDescription();

	/**
	 * Usage of the command. This returns how to use the commands, with its expected arguments.
	 * @return Usage of the command.
	 */
	String getUsage();

	/**
	 * Tells how to match a given message with the name of the command.
	 * @return Function taking a message and the name of the command, returning if it matches this command or not.
	 */
	BiPredicate<String, String> matches();

	/**
	 * Expected number of arguments. This always corresponds to the maximum expected number.
	 * @return Expected number of arguments.
	 */
	int argumentCount();

	/**
	 * Whether this command requires a valid tag as first parameter. When it doesn't require a tag, or can take a tag that doesn't
	 * exist remotely, this returns <code>false</code>.
	 * @return <code>true</code> if this command requires a valid tag as first parameter.
	 */
	boolean requiresValidTag();

	/**
	 * Execute the command.
	 * @param message Message that triggered the command.
	 * @param room Room where the message was posted.
	 * @param burnaki Instance of the running bot.
	 * @param arguments Arguments to the command.
	 */
	void execute(Message message, Room room, Burnaki burnaki, String[] arguments);

}
