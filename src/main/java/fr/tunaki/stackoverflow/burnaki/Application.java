package fr.tunaki.stackoverflow.burnaki;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;

import fr.tunaki.stackoverflow.burnaki.api.StackExchangeAPIProperties;
import fr.tunaki.stackoverflow.burnaki.bot.BurnakiProperties;
import org.sobotics.chatexchange.chat.StackExchangeClient;

@SpringBootApplication
@EnableConfigurationProperties({StackExchangeAPIProperties.class, BurninationManagerProperties.class, BurnakiProperties.class, ChatProperties.class})
@EnableScheduling
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class);
	}

	@Bean
	public ScheduledExecutorService scheduledExecutorService() {
		return Executors.newSingleThreadScheduledExecutor();
	}

	@Bean
	public StackExchangeClient stackExchangeClient(ChatProperties properties) {
		return new StackExchangeClient(properties.getEmail(), properties.getPassword());
	}

}
