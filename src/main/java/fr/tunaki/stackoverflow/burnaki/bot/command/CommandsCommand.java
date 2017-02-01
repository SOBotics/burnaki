package fr.tunaki.stackoverflow.burnaki.bot.command;

import java.util.Collection;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import fr.tunaki.stackoverflow.burnaki.bot.Burnaki;
import fr.tunaki.stackoverflow.chat.Message;
import fr.tunaki.stackoverflow.chat.Room;

@Component
public class CommandsCommand implements Command {

    @Autowired
    private ApplicationContext applicationContext;

	private Collection<Command> commands;

    @PostConstruct
    private void init() {
    	commands = applicationContext.getBeansOfType(Command.class).values();
    }

	@Override
	public String getName() {
		return "commands";
	}

	@Override
	public String getDescription() {
		return "Prints the list of commands.";
	}

	@Override
	public String getUsage() {
		return "commands";
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
		int maxUsageLength = commands.stream().map(Command::getUsage).mapToInt(String::length).max().orElse(0);
		room.replyTo(message.getId(), "Here's a list of commands:");
		room.send(commands.stream().map(c -> "    " + String.format("%1$-" + maxUsageLength + "s", c.getUsage()) + " - " + c.getDescription()).collect(Collectors.joining("\n")));
	}

}
