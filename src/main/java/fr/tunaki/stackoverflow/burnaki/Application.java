package fr.tunaki.stackoverflow.burnaki;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import fr.tunaki.stackoverflow.burnaki.db.entities.Burnination;
import fr.tunaki.stackoverflow.burnaki.db.repository.BurninationRepository;

@SpringBootApplication
public class Application {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

	public static void main(String[] args) {
		SpringApplication.run(Application.class);
	}

	@Bean
	public CommandLineRunner demo(BurninationRepository repository) {
		return (args) -> {
			LOGGER.info("findAll():");
			LOGGER.info("-------------------------------");
			for (Burnination burnination : repository.findAll()) {
				LOGGER.info(burnination.toString());
			}
			LOGGER.info("");
		};
	}

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
