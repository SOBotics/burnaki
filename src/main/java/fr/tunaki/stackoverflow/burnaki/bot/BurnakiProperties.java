package fr.tunaki.stackoverflow.burnaki.bot;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("burnaki.bot")
public class BurnakiProperties {
	
	private String restApi;

	public String getRestApi() {
		return restApi;
	}

	public void setRestApi(String restApi) {
		this.restApi = restApi;
	}
	
}
