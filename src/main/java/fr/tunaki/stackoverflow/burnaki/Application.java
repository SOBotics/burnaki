package fr.tunaki.stackoverflow.burnaki;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;

import fr.tunaki.stackoverflow.burnaki.api.StackExchangeAPIProperties;
import fr.tunaki.stackoverflow.chat.StackExchangeClient;

@SpringBootApplication
@EnableConfigurationProperties({StackExchangeAPIProperties.class, BurninationManagerProperties.class})
@EnableScheduling
public class Application {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(Application.class);
//		BurninationScheduler scheduler = context.getBean(BurninationScheduler.class);
//		scheduler.start("godaddy", 123, "http");
//		scheduler.stop("godaddy");
//		context.close();
	}
	
	@Bean
	public ScheduledExecutorService scheduledExecutorService() {
		return Executors.newSingleThreadScheduledExecutor();
	}
	
	@Bean
	public StackExchangeClient stackExchangeClient(Environment environment) {
		return new StackExchangeClient(environment.getProperty("stachexchange.chat.email"), environment.getProperty("stachexchange.chat.password"));
	}

}
