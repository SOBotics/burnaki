package fr.tunaki.stackoverflow.burnaki.bot.command;

import java.util.function.BiPredicate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.tunaki.stackoverflow.burnaki.api.StackExchangeAPIService;
import fr.tunaki.stackoverflow.burnaki.bot.Burnaki;
import fr.tunaki.stackoverflow.chat.Message;
import fr.tunaki.stackoverflow.chat.Room;

@Component
public class QuotaCommand implements Command {

	private StackExchangeAPIService apiService;

	@Autowired
	public QuotaCommand(StackExchangeAPIService apiService) {
		this.apiService = apiService;
	}

	@Override
	public String getName() {
		return "quota";
	}

	@Override
	public String getDescription() {
		return "Prints the remaining quota for the Stack Exchange API.";
	}

	@Override
	public String getUsage() {
		return "quota";
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
		room.replyTo(message.getId(), "Remaining quota is: " + apiService.getQuotaRemaining());
	}

}
