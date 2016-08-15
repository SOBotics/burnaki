package fr.tunaki.stackoverflow.burnaki.entity;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

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
	
	@Column(name = "created_date", nullable = false, length = 19)
	private Instant createdDate;
	
	@Column(name = "close_vote_count", nullable = false)
	private int closeVoteCount;
	
	@Column(name = "reopen_vote_count", nullable = false)
	private int reopenVoteCount;
	
	@Column(name = "delete_vote_count", nullable = false)
	private int deleteVoteCount;
	
	@Column(name = "undelete_vote_count", nullable = false)
	private int undeleteVoteCount;
	
	@Column(name = "closed", nullable = false)
	private boolean closed;
	
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

	public boolean isClosed() {
		return closed;
	}

	public void setClosed(boolean closed) {
		this.closed = closed;
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

}
