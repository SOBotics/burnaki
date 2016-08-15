package fr.tunaki.stackoverflow.burnaki.db.entities;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class BurninationQuestionHistoryId implements Serializable {

	private static final long serialVersionUID = -3304636598881447364L;
	
	private BurninationQuestionId burninationQuestionId;
	
	@Column(name = "event_date", nullable = false, length = 19)
	private Instant eventDate;

	public BurninationQuestionId getBurninationQuestionId() {
		return burninationQuestionId;
	}

	public void setBurninationQuestionId(BurninationQuestionId burninationQuestionId) {
		this.burninationQuestionId = burninationQuestionId;
	}

	public Instant getEventDate() {
		return eventDate;
	}

	public void setEventDate(Instant eventDate) {
		this.eventDate = eventDate;
	}

	@Override
	public boolean equals(Object that) {
		if (this == that) {
			return true;
		}
		if (that == null || !(that instanceof BurninationQuestionHistoryId)) {
			return false;
		}
		BurninationQuestionHistoryId other = (BurninationQuestionHistoryId) that;
		return burninationQuestionId.equals(other.getBurninationQuestionId()) && eventDate.equals(other.eventDate);
	}

	@Override
	public int hashCode() {
		return Objects.hash(burninationQuestionId, eventDate);
	}

}
