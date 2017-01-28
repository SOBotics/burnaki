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
	 * Search filter to use when querying the API for list of questions.
	 */
	private String questionFilter;

	/**
	 * Search filter to use when querying the API for list of suggested edits on given posts.
	 */
	private String suggestedEditFilter;

	/**
	 * Root URL of the API. Example: <code>https://api.stackexchange.com/2.2</code>.
	 */
	private String rootUrl;

	/**
	 * Page size of queried results.
	 */
	private String pageSize;

	/**
	 * Maximum number of pages of questions to return.
	 */
	private int maxPage;

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

	public String getQuestionFilter() {
		return questionFilter;
	}

	public void setQuestionFilter(String filter) {
		this.questionFilter = filter;
	}

	public String getSuggestedEditFilter() {
		return suggestedEditFilter;
	}

	public void setSuggestedEditFilter(String suggestedEditFilter) {
		this.suggestedEditFilter = suggestedEditFilter;
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

	public int getMaxPage() {
		return maxPage;
	}

	public void setMaxPage(int maxPage) {
		this.maxPage = maxPage;
	}

}
