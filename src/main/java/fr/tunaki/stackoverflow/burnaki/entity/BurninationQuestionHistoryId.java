package fr.tunaki.stackoverflow.burnaki.entity;

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

	@Column(name = "event_type", nullable = false, length = 45)
	private String eventType;

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

	public String getEventType() {
		return eventType;
	}

	public void setEventType(String eventType) {
		this.eventType = eventType;
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
		return burninationQuestionId.equals(other.getBurninationQuestionId()) && eventDate.equals(other.eventDate) && eventType.equals(other.eventType);
	}

	@Override
	public int hashCode() {
		return Objects.hash(burninationQuestionId, eventDate, eventType);
	}

	@Override
	public String toString() {
		return "[burninationQuestionId=" + burninationQuestionId + ";eventDate=" + eventDate + ";eventType=" + eventType + "]";
	}

}
