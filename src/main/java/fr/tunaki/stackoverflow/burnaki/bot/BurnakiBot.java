package fr.tunaki.stackoverflow.burnaki.bot;

import java.io.Closeable;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import fr.tunaki.stackoverflow.burnaki.api.StackExchangeAPIService;
import fr.tunaki.stackoverflow.burnaki.scheduler.BurninationScheduler;
import fr.tunaki.stackoverflow.chat.Room;
import fr.tunaki.stackoverflow.chat.StackExchangeClient;
import fr.tunaki.stackoverflow.chat.event.EventType;

@Component
public class BurnakiBot implements Closeable, InitializingBean {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(BurnakiBot.class);
	
	private StackExchangeClient client;
	private StackExchangeAPIService apiService;
	private BurninationScheduler burninationScheduler;
	private ConfigurableApplicationContext context;
	
	private Room hqRoom;
	
	@Autowired
	public BurnakiBot(StackExchangeClient client, StackExchangeAPIService apiService, BurninationScheduler burninationScheduler, ConfigurableApplicationContext context) {
		this.client = client;
		this.apiService = apiService;
		this.burninationScheduler = burninationScheduler;
		this.context = context;
	}

	private void registerEventListeners() {
		hqRoom.addEventListener(EventType.USER_MENTIONED, event -> {
			long messageId = event.getMessage().getId();
			String stripped = event.getMessage().getPlainContent().toLowerCase().replaceAll("@tun[^\\s$]*", "").trim();
			if (stripped.equals("stop bot")) {
				context.close();
			} else if (stripped.startsWith("start tag")) {
				String[] tokens = stripped.substring("start tag".length() + 1).split(" ");
				if (tokens.length != 3) {
					hqRoom.replyTo(messageId, "Cannot start burnination; 3 parameters required: \\[tag\\] \\[roomId\\] \\[link to Meta\\]");
					return;
				}
				String tag = tokens[0];
				if (tag.startsWith("[")) tag = tag.substring(1, tag.lastIndexOf(']'));
				if (!apiService.isValidTag(tag)) {
					LOGGER.warn("Tried to start a burnination on non-existing tag [{}]", tag);
					hqRoom.replyTo(messageId, "Cannot start burnination; tag \\[" + tag + "\\] doesn't exist");
					return;
				}
				int roomId;
				try {
					roomId = Integer.parseInt(tokens[1]);
				} catch (NumberFormatException e) {
					hqRoom.replyTo(messageId, "Cannot start burnination; incorrect value for roomId: " + tokens[1]);
					return;
				}
				try {
					burninationScheduler.start(tag, roomId, tokens[2]);
					hqRoom.replyTo(messageId, "Burnination of tag \\[" + tag + "\\] correctly started! Have fun!");
				} catch (Exception e) {
					LOGGER.error("Cannot start burnination of tag [{}]", tag, e);
					hqRoom.replyTo(messageId, "Cannot start burnination of tag \\[" + tag + "\\]: " + e.getMessage());
				}
			} else if (stripped.startsWith("stop tag")) {
				String[] tokens = stripped.substring("stop tag".length() + 1).split(" ");
				if (tokens.length != 1) {
					hqRoom.replyTo(messageId, "Cannot stop burnination; 1 parameters required: [tag]");
					return;
				}
				String tag = tokens[0];
				if (tag.startsWith("[")) tag = tag.substring(1, tag.lastIndexOf(']'));
				if (!apiService.isValidTag(tag)) {
					LOGGER.warn("Tried to start a burnination on non-existing tag [{}]", tag);
					hqRoom.replyTo(messageId, "Cannot stop burnination; tag \\[" + tag + "\\] doesn't exist");
					return;
				}
				try {
					burninationScheduler.stop(tag);
					hqRoom.replyTo(messageId, "Burnination of tag \\[" + tag + "\\] correctly stopped! I hope you had real fun!");
				} catch (Exception e) {
					LOGGER.error("Cannot stop burnination of tag [{}]", tag, e);
					hqRoom.replyTo(messageId, "Cannot stop burnination of tag \\[" + tag + "\\]: " + e.getMessage());
				}
			}
		});
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		hqRoom = client.joinRoom("stackoverflow.com", 111347);
		registerEventListeners();
	}

	@Override
	public void close() throws IOException {
		client.close();
	}

}
