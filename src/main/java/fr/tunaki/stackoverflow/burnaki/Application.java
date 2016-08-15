package fr.tunaki.stackoverflow.burnaki;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import fr.tunaki.stackoverflow.burnaki.api.StackExchangeAPIProperties;

@SpringBootApplication
@EnableConfigurationProperties(StackExchangeAPIProperties.class)
public class Application {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

	public static void main(String[] args) {
		System.out.println(Instant.now().getEpochSecond());
		SpringApplication.run(Application.class);
	}
	
//	@Bean
//	public CommandLineRunner demo(StackExchangeAPIService service) {
//		return args -> {
//			service.getQuestionsInTag("java", Instant.now().minus(5, ChronoUnit.MINUTES)).forEach(q -> LOGGER.info(""+q.getId()));
//		};
//	}
	
//	@Bean
//	public CommandLineRunner demo(BurninationRepository repository, BurninationService service) {
//		return args -> {
//			LOGGER.info("test():");
//			LOGGER.info("-------------------------------");
//			for (Burnination burnination : repository.findAll()) {
//				LOGGER.info(burnination.toString());
//			}
//			LOGGER.info("-------------------------------");
//			service.start("java", 123, "link");
//			LOGGER.info("-------------------------------");
//			for (Burnination burnination : repository.findAll()) {
//				LOGGER.info(burnination.getTag() +  " " + burnination.getStartDate() + " " + burnination.getEndDate());
//			}
//			
//			LOGGER.info("-------------------------------");
//			service.stop("java");
//			LOGGER.info("-------------------------------");
//			for (Burnination burnination : repository.findAll()) {
//				LOGGER.info(burnination.getTag() +  " " + burnination.getStartDate() + " " + burnination.getEndDate());
//			}
//			
//			LOGGER.info("-------------------------------");
//			service.start("java", 123, "link");
//			for (Burnination burnination : repository.findAll()) {
//				LOGGER.info(burnination.getTag() +  " " + burnination.getStartDate() + " " + burnination.getEndDate());
//			}
//			
//			LOGGER.info("-------------------------------");
//			service.stop("java");
//			LOGGER.info("-------------------------------");
//			for (Burnination burnination : repository.findAll()) {
//				LOGGER.info(burnination.getTag() +  " " + burnination.getStartDate() + " " + burnination.getEndDate());
//			}
//			
//			LOGGER.info("");
//		};
//	}

	// public static void main(String [] args) throws IOException {
	// Properties properties = new Properties();
	// try (InputStream inputStream =
	// Application.class.getResourceAsStream("/burnaki.properties")) {
	// properties.load(inputStream);
	// }
	// StackExchangeAPIService apiService = new
	// StackExchangeAPIService(properties.getProperty("API_KEY"),
	// properties.getProperty("FILTER"));
	// List<Long> ids =
	// apiService.getQuestionsWithTag("removing-whitespace").stream().map(Question::getId).collect(Collectors.toList());
	// System.out.println();
	// try {
	// Thread.sleep(5000);
	// } catch (InterruptedException e) { }
	// apiService.getQuestionsWithIds(ids).forEach(question ->
	// System.out.println(question.getId() + " - " + question.getLink()));
	// }

}
