package fr.tunaki.stackoverflow.burnaki.scheduler;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("burnination.scheduler")
public class BurninationSchedulerProperties {
	
	private int refreshQuestionsEvery;
	private int refreshProgressEvery;

	public int getRefreshQuestionsEvery() {
		return refreshQuestionsEvery;
	}

	public void setRefreshQuestionsEvery(int refreshQuestionsEvery) {
		this.refreshQuestionsEvery = refreshQuestionsEvery;
	}

	public int getRefreshProgressEvery() {
		return refreshProgressEvery;
	}

	public void setRefreshProgressEvery(int refreshProgressEvery) {
		this.refreshProgressEvery = refreshProgressEvery;
	}
	
}
