package fr.tunaki.stackoverflow.burnaki.bot;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("burnaki.bot")
public class BurnakiProperties {
	
	private String restApi;
	private int hqRoomId;
	private String host;

	public String getRestApi() {
		return restApi;
	}

	public void setRestApi(String restApi) {
		this.restApi = restApi;
	}

	public int getHqRoomId() {
		return hqRoomId;
	}

	public void setHqRoomId(int hqRoomId) {
		this.hqRoomId = hqRoomId;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}
	
}
