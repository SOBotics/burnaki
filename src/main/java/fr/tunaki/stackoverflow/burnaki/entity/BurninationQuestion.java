package fr.tunaki.stackoverflow.burnaki.entity;

import static java.time.temporal.ChronoUnit.DAYS;

import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalLong;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "burnination_question")
public class BurninationQuestion implements Serializable {

	private static final long serialVersionUID = -9015468418982024905L;

	@EmbeddedId
	@AttributeOverrides({
		@AttributeOverride(name = "questionId", column = @Column(name = "question_id", nullable = false))
	})
	private BurninationQuestionId id;

	@MapsId("burninationId")
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "burnination_id", nullable = false)
	private Burnination burnination;

	@Column(name = "title", nullable = false, length = 250)
	private String title;

	@Column(name = "link", nullable = false, length = 255)
	private String link;

	@Column(name = "share_link", nullable = false, length = 255)
	private String shareLink;

	@Column(name = "score", nullable = false)
	private int score;

	@Column(name = "view_count", nullable = false)
	private int viewCount;

	@Column(name = "answer_count", nullable = false)
	private int answerCount;

	@Column(name = "comment_count", nullable = false)
	private int commentCount;

	@Column(name = "locked", nullable = false)
	private boolean locked;

	@Column(name = "migrated", nullable = false)
	private boolean migrated;

	@Column(name = "answered", nullable = false)
	private boolean answered;

	@Column(name = "accepted_answer_id")
	private Integer acceptedAnswerId;

	@Column(name = "tags", nullable = false, length = 255)
	private String tags;

	@Column(name = "created_date", nullable = false, length = 19)
	private Instant createdDate;

	@Column(name = "closed_date", length = 19)
	private Instant closedDate;

	@Column(name = "closed_as_duplicate", nullable = false)
	private boolean closedAsDuplicate;

	@Column(name = "deleted_date", length = 19)
	private Instant deletedDate;

	@Column(name = "last_edit_date")
	private Instant lastEditDate;

	@Column(name = "close_vote_count", nullable = false)
	private int closeVoteCount;

	@Column(name = "reopen_vote_count", nullable = false)
	private int reopenVoteCount;

	@Column(name = "delete_vote_count", nullable = false)
	private int deleteVoteCount;

	@Column(name = "undelete_vote_count", nullable = false)
	private int undeleteVoteCount;

	@Column(name = "roombad", nullable = false)
	private boolean roombad;

	@Column(name = "manually_deleted", nullable = false)
	private boolean manuallyDeleted;

	@Column(name = "retagged", nullable = false)
	private boolean retagged;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "burninationQuestion", cascade = CascadeType.ALL)
	private List<BurninationQuestionHistory> histories = new ArrayList<>();

	public BurninationQuestion() { }

	public BurninationQuestion(Burnination burnination, int questionId) {
		id = new BurninationQuestionId();
		id.setBurninationId(burnination.getId());
		id.setQuestionId(questionId);
	}

	public BurninationQuestionId getId() {
		return id;
	}

	public Burnination getBurnination() {
		return burnination;
	}

	public void setBurnination(Burnination burnination) {
		this.burnination = burnination;
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

	public boolean isRoombad() {
		return roombad;
	}

	public void setRoombad(boolean roombad) {
		this.roombad = roombad;
	}

	public boolean isManuallyDeleted() {
		return manuallyDeleted;
	}

	public void setManuallyDeleted(boolean manuallyDeleted) {
		this.manuallyDeleted = manuallyDeleted;
	}

	public boolean isRetagged() {
		return retagged;
	}

	public void setRetagged(boolean retagged) {
		this.retagged = retagged;
	}

	public List<BurninationQuestionHistory> getHistories() {
		return histories;
	}

	public void addHistory(BurninationQuestionHistory burninationQuestionHistory) {
		histories.add(burninationQuestionHistory);
		burninationQuestionHistory.setBurninationQuestion(this);
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

	public int getAnswerCount() {
		return answerCount;
	}

	public void setAnswerCount(int answerCount) {
		this.answerCount = answerCount;
	}

	public Integer getAcceptedAnswerId() {
		return acceptedAnswerId;
	}

	public void setAcceptedAnswerId(Integer acceptedAnswerId) {
		this.acceptedAnswerId = acceptedAnswerId;
	}

	public String getShareLink() {
		return shareLink;
	}

	public void setShareLink(String shareLink) {
		this.shareLink = shareLink;
	}

	public int getCommentCount() {
		return commentCount;
	}

	public void setCommentCount(int commentCount) {
		this.commentCount = commentCount;
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

	public boolean isAnswered() {
		return answered;
	}

	public void setAnswered(boolean answered) {
		this.answered = answered;
	}

	public String getTags() {
		return tags;
	}

	public void setTags(String tags) {
		this.tags = tags;
	}

	public boolean isClosedAsDuplicate() {
		return closedAsDuplicate;
	}

	public void setClosedAsDuplicate(boolean closedAsDuplicate) {
		this.closedAsDuplicate = closedAsDuplicate;
	}

	public Instant getLastEditDate() {
		return lastEditDate;
	}

	public void setLastEditDate(Instant lastEditDate) {
		this.lastEditDate = lastEditDate;
	}

	public boolean wasProbablyRoombad() {
		return daysBeforeRoomba().orElse(Long.MAX_VALUE) <= 0;
	}

	public OptionalLong daysBeforeRoomba() {
		Instant now = Instant.now();
		long result = Long.MAX_VALUE;
		/* RemoveDeadQuestions and RemoveMigrationStubs */
		if (score <= -1 && answerCount == 0 && !locked || migrated) {
			int delta = 30;
			result = Math.min(result, daysBeforePossibleRoomba(now, toNextUTCSaturday(createdDate.plus(delta, DAYS))));
		}
		/* RemoveAbandonedQuestions */
		if (score == 0 && answerCount == 0 && !locked && commentCount <= 1) {
			int delta = Math.max(365, (int) (viewCount / 1.5) + 1);
			result = Math.min(result, daysBeforePossibleRoomba(now, toNextUTCSaturday(createdDate.plus(delta, DAYS))));
		}
		/* RemoveAbandonedClosed */
		if (closedDate != null && !closedAsDuplicate && score <= 0 && !locked && !answered && acceptedAnswerId == null && reopenVoteCount == 0) {
			Instant date = lastEditDate == null || closedDate.isAfter(lastEditDate) ? closedDate : lastEditDate;
			result = Math.min(result, daysBeforePossibleRoomba(now, date.plus(9, DAYS)));
		}
		return result == Long.MAX_VALUE ? OptionalLong.empty() : OptionalLong.of(result);
	}

	/**
	 * Returns the number of whole days to wait before a possible roomba event can happen, given the current date and another date.
	 * <p>
	 * This calculates the number of whole days between the two dates. Since this number is truncated to the day, we need to take into account
	 * whether the given date is before or after the current date (but in the same 24h interval); if it is after, then we'll need to wait
	 * another full day before a roomba can happen.
	 * <p>
	 * To clear this up, consider the case where the date is after the current date:
	 * <pre>
	 * --D----------------D+1----------
	 *      ^      ^
	 *     NOW    DATE
	 * </pre>
	 * <p>
	 * in which case, the roomba event can only happen the day after, to reach:
	 * <pre>
	 * --D----------------D+1----------
	 *             ^             ^
	 *            DATE          NOW
	 * </pre>
	 * @param now Current date for the check.
	 * @param date Date to test against.
	 * @return Number of whole days to wait before a roomba event, when checking about the given date. When this is negative or zero, it
	 * means the event already happened.
	 */
	private static long daysBeforePossibleRoomba(Instant now, Instant date) {
		long days = DAYS.between(now, date);
		return date.isAfter(now) ? days + 1 : days;
	}

	private static Instant toNextUTCSaturday(Instant instant) {
		return instant.atZone(ZoneOffset.UTC).with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY)).toInstant();
	}

}
