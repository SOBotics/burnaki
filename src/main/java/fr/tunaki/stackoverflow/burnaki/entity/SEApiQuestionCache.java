package fr.tunaki.stackoverflow.burnaki.entity;

import java.io.Serializable;
import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "se_api_question_cache")
public class SEApiQuestionCache implements Serializable {

	private static final long serialVersionUID = -9015468418982024905L;
	
	@Id
	@Column(name = "id", unique = true, nullable = false)
	private int id;
	
	@Column(name = "title", nullable = false, length = 250)
	private String title;
	
	@Column(name = "share_link", nullable = false, length = 255)
	private String shareLink;
	
	@Column(name = "created_date", nullable = false)
	private Instant createdDate;
	
	@Column(name = "deleted_date")
	private Instant deletedDate;
	
	@Column(name = "score", nullable = false)
	private int score;
	
	@Column(name = "answer_count", nullable = false)
	private int answerCount;
	
	@Column(name = "locked", nullable = false)
	private boolean locked;
	
	@Column(name = "migrated", nullable = false)
	private boolean migrated;
	
	@Column(name = "view_count", nullable = false)
	private int viewCount;
	
	@Column(name = "comment_count", nullable = false)
	private int commentCount;
	
	@Column(name = "closed_date")
	private Instant closedDate;
	
	@Column(name = "closed_as_duplicate", nullable = false)
	private boolean closedAsDuplicate;
	
	@Column(name = "answered", nullable = false)
	private boolean answered;
	
	@Column(name = "with_accepted_answer", nullable = false)
	private boolean withAcceptedAnswer;
	
	@Column(name = "close_vote_count", nullable = false)
	private int closeVoteCount;
	
	@Column(name = "reopen_vote_count", nullable = false)
	private int reopenVoteCount;
	
	@Column(name = "tags", nullable = false, length = 255)
	private String tags;
	
	@Column(name = "last_edit_date")
	private Instant lastEditDate;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getShareLink() {
		return shareLink;
	}

	public void setShareLink(String shareLink) {
		this.shareLink = shareLink;
	}

	public Instant getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Instant createdDate) {
		this.createdDate = createdDate;
	}

	public Instant getDeletedDate() {
		return deletedDate;
	}

	public void setDeletedDate(Instant deletedDate) {
		this.deletedDate = deletedDate;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public int getAnswerCount() {
		return answerCount;
	}

	public void setAnswerCount(int answerCount) {
		this.answerCount = answerCount;
	}

	public boolean isLocked() {
		return locked;
	}

	public void setLocked(boolean locked) {
		this.locked = locked;
	}

	public boolean isMigrated() {
		return migrated;
	}

	public void setMigrated(boolean migrated) {
		this.migrated = migrated;
	}

	public int getViewCount() {
		return viewCount;
	}

	public void setViewCount(int viewCount) {
		this.viewCount = viewCount;
	}

	public int getCommentCount() {
		return commentCount;
	}

	public void setCommentCount(int commentCount) {
		this.commentCount = commentCount;
	}

	public Instant getClosedDate() {
		return closedDate;
	}

	public void setClosedDate(Instant closedDate) {
		this.closedDate = closedDate;
	}

	public boolean isClosedAsDuplicate() {
		return closedAsDuplicate;
	}

	public void setClosedAsDuplicate(boolean closedAsDuplicate) {
		this.closedAsDuplicate = closedAsDuplicate;
	}

	public boolean isAnswered() {
		return answered;
	}

	public void setAnswered(boolean answered) {
		this.answered = answered;
	}

	public boolean isWithAcceptedAnswer() {
		return withAcceptedAnswer;
	}

	public void setWithAcceptedAnswer(boolean withAcceptedAnswer) {
		this.withAcceptedAnswer = withAcceptedAnswer;
	}

	public int getReopenVoteCount() {
		return reopenVoteCount;
	}

	public void setReopenVoteCount(int reopenVoteCount) {
		this.reopenVoteCount = reopenVoteCount;
	}

	public Instant getLastEditDate() {
		return lastEditDate;
	}

	public void setLastEditDate(Instant lastEditDate) {
		this.lastEditDate = lastEditDate;
	}

	public int getCloseVoteCount() {
		return closeVoteCount;
	}

	public void setCloseVoteCount(int closeVoteCount) {
		this.closeVoteCount = closeVoteCount;
	}

	public String getTags() {
		return tags;
	}

	public void setTags(String tags) {
		this.tags = tags;
	}
	
}
