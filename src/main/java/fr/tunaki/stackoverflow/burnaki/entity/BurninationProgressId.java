package fr.tunaki.stackoverflow.burnaki.entity;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class BurninationProgressId implements Serializable {

	private static final long serialVersionUID = -6820510250055476794L;

	@Column(name = "burnination_id", nullable = false)
	private long burninationId;
	
	@Column(name = "progress_date", nullable = false, length = 19)
	private Instant progressDate;
	
	public long getBurninationId() {
		return this.burninationId;
	}

	public void setBurninationId(long burninationId) {
		this.burninationId = burninationId;
	}

	public Instant getProgressDate() {
		return this.progressDate;
	}

	public void setProgressDate(Instant progressDate) {
		this.progressDate = progressDate;
	}

	@Override
	public boolean equals(Object that) {
		if (this == that) {
			return true;
		}
		if (that == null || !(that instanceof BurninationProgressId)) {
			return false;
		}
		BurninationProgressId other = (BurninationProgressId) that;
		return burninationId == other.getBurninationId() && progressDate.equals(other.progressDate);
	}

	@Override
	public int hashCode() {
		return Objects.hash(burninationId, progressDate);
	}

}
