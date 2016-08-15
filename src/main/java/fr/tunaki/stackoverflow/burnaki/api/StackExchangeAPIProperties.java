package fr.tunaki.stackoverflow.burnaki.api;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("stackexchange.api")
public class StackExchangeAPIProperties {
	
	/**
	 * The site to connect to. Example: <code>stackoverflow</code>.
	 */
	private String site;
	
	/**
	 * The API key to use.
	 */
	private String key;
	
	/**
	 * Search filter to use when querying the API.
	 */
	private String filter;
	
	/**
	 * Root URL of the API. Example: <code>https://api.stackexchange.com/2.2</code>.
	 */
	private String rootUrl;
	
	/**
	 * Page size of queried results.
	 */
	private String pageSize;

	public String getSite() {
		return site;
	}

	public void setSite(String site) {
		this.site = site;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getFilter() {
		return filter;
	}

	public void setFilter(String filter) {
		this.filter = filter;
	}

	public String getRootUrl() {
		return rootUrl;
	}

	public void setRootUrl(String rootUrl) {
		this.rootUrl = rootUrl;
	}

	public String getPageSize() {
		return pageSize;
	}

	public void setPageSize(String pageSize) {
		this.pageSize = pageSize;
	}

}
