package fr.tunaki.stackoverflow.burnaki.api;

import java.time.Instant;

public class Question {
	
	private long id;
	private String link;
	private String title;
	private int closeVoteCount;
	private int reopenVoteCount;
	private int deleteVoteCount;
	private Instant closedDate;
	private Instant deletedDate; // not returned by API, calculated when it is noticed that question was deleted
	private Instant lastEditDate;
	private ShallowUser lastEditor;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getCloseVoteCount() {
		return closeVoteCount;
	}

	public void setCloseVoteCount(int closeVoteCount) {
		this.closeVoteCount = closeVoteCount;
	}

	public int getReopenVoteCount() {
		return reopenVoteCount;
	}

	public void setReopenVoteCount(int reopenVoteCount) {
		this.reopenVoteCount = reopenVoteCount;
	}

	public int getDeleteVoteCount() {
		return deleteVoteCount;
	}

	public void setDeleteVoteCount(int deleteVoteCount) {
		this.deleteVoteCount = deleteVoteCount;
	}

	public Instant getClosedDate() {
		return closedDate;
	}

	public void setClosedDate(Instant closedDate) {
		this.closedDate = closedDate;
	}

	public Instant getDeletedDate() {
		return deletedDate;
	}

	public void setDeletedDate(Instant deletedDate) {
		this.deletedDate = deletedDate;
	}

	public Instant getLastEditDate() {
		return lastEditDate;
	}

	public void setLastEditDate(Instant lastEditDate) {
		this.lastEditDate = lastEditDate;
	}

	public ShallowUser getLastEditor() {
		return lastEditor;
	}

	public void setLastEditor(ShallowUser lastEditor) {
		this.lastEditor = lastEditor;
	}

}
