package fr.tunaki.stackoverflow.burnaki.api;

import java.time.Instant;
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
	private Instant lockedDate;
	private Instant lastEditDate;
	private ShallowUser lastEditor;
	private Integer acceptedAnswerId;
	private int answerCount;
	private int commentCount;
	private int score;
	private int viewCount;
	private boolean answered;
	private String closedReason;
	private boolean migrated;
	private SuggestedEdit pendingSuggestedEdit;

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

	public void setTags(List<String> tags) {
		this.tags = tags;
	}

	public List<String> getTags() {
		return tags;
	}

	public Integer getAcceptedAnswerId() {
		return acceptedAnswerId;
	}

	public void setAcceptedAnswerId(Integer acceptedAnswerId) {
		this.acceptedAnswerId = acceptedAnswerId;
	}

	public int getAnswerCount() {
		return answerCount;
	}

	public void setAnswerCount(int answerCount) {
		this.answerCount = answerCount;
	}

	public int getCommentCount() {
		return commentCount;
	}

	public void setCommentCount(int commentCount) {
		this.commentCount = commentCount;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public int getViewCount() {
		return viewCount;
	}

	public void setViewCount(int viewCount) {
		this.viewCount = viewCount;
	}

	public boolean isAnswered() {
		return answered;
	}

	public void setAnswered(boolean answered) {
		this.answered = answered;
	}

	public String getClosedReason() {
		return closedReason;
	}

	public void setClosedReason(String closedReason) {
		this.closedReason = closedReason;
	}

	public Instant getLockedDate() {
		return lockedDate;
	}

	public void setLockedDate(Instant lockedDate) {
		this.lockedDate = lockedDate;
	}

	public boolean isMigrated() {
		return migrated;
	}

	public void setMigrated(boolean migrated) {
		this.migrated = migrated;
	}

	public SuggestedEdit getPendingSuggestedEdit() {
		return pendingSuggestedEdit;
	}

	public void setPendingSuggestedEdit(SuggestedEdit pendingSuggestedEdit) {
		this.pendingSuggestedEdit = pendingSuggestedEdit;
	}

}
