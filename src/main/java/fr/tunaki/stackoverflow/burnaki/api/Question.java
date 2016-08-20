package fr.tunaki.stackoverflow.burnaki.api;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class Question {
	
	private int id;
	private String link;
	private String shareLink;
	private String title;
	private List<String> tags;
	private int closeVoteCount;
	private int reopenVoteCount;
	private int deleteVoteCount;
	private int undeleteVoteCount;
	private Instant createdDate;
	private Instant closedDate;
	private Instant deletedDate; // not returned by API, calculated when it is noticed that question was deleted
	private boolean roombad;
	private Instant lastEditDate;
	private ShallowUser lastEditor;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getShareLink() {
		return shareLink;
	}

	public void setShareLink(String shareLink) {
		this.shareLink = shareLink;
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

	public int getUndeleteVoteCount() {
		return undeleteVoteCount;
	}

	public void setUndeleteVoteCount(int undeleteVoteCount) {
		this.undeleteVoteCount = undeleteVoteCount;
	}

	public Instant getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Instant createdDate) {
		this.createdDate = createdDate;
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

	public boolean isRoombad() {
		return roombad;
	}

	public void setRoombad(boolean roombad) {
		this.roombad = roombad;
	}
	
	public void setTags(List<String> tags) {
		this.tags = new ArrayList<>(tags);
	}

	public boolean hasTag(String tag) {
		return tags.contains(tag);
	}

}
