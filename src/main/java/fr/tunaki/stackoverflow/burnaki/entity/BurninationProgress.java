package fr.tunaki.stackoverflow.burnaki.entity;

import java.io.Serializable;
import java.time.Instant;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;

@Entity
@Table(name = "burnination_progress")
public class BurninationProgress implements Serializable {

	private static final long serialVersionUID = 8174996252515252416L;

	@EmbeddedId
	@AttributeOverrides({
		@AttributeOverride(name = "progressDate", column = @Column(name = "progress_date", nullable = false, length = 19)),
	})
	private BurninationProgressId id;

	@MapsId("burninationId")
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "burnination_id", nullable = false)
	private Burnination burnination;

	@Column(name = "total_questions")
	private int totalQuestions;

	@Column(name = "closed")
	private int closed;

	@Column(name = "roombad")
	private int roombad;

	@Column(name = "manually_deleted")
	private int manuallyDeleted;

	@Column(name = "retagged")
	private int retagged;

	public BurninationProgress() { }

	public BurninationProgress(Burnination burnination, Instant progressDate) {
		id = new BurninationProgressId();
		id.setBurninationId(burnination.getId());
		id.setProgressDate(progressDate);
	}

	public BurninationProgressId getId() {
		return id;
	}

	public Burnination getBurnination() {
		return burnination;
	}

	public void setBurnination(Burnination burnination) {
		this.burnination = burnination;
	}

	public int getTotalQuestions() {
		return totalQuestions;
	}

	public void setTotalQuestions(int totalQuestions) {
		this.totalQuestions = totalQuestions;
	}

	public int getClosed() {
		return closed;
	}

	public void setClosed(int closed) {
		this.closed = closed;
	}

	public int getRoombad() {
		return roombad;
	}

	public void setRoombad(int roombad) {
		this.roombad = roombad;
	}

	public int getManuallyDeleted() {
		return manuallyDeleted;
	}

	public void setManuallyDeleted(int manuallyDeleted) {
		this.manuallyDeleted = manuallyDeleted;
	}

	public int getRetagged() {
		return retagged;
	}

	public void setRetagged(int retagged) {
		this.retagged = retagged;
	}

	public int getRemaining() {
		return totalQuestions - closed - retagged - manuallyDeleted - roombad;
	}

}
