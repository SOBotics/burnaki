package fr.tunaki.stackoverflow.burnaki.entity;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class BurninationQuestionId implements Serializable {

	private static final long serialVersionUID = 4619622582007916405L;
	
	@Column(name = "burnination_id", nullable = false)
	private long burninationId;
	
	@Column(name = "question_id", nullable = false)
	private int questionId;

	public long getBurninationId() {
		return burninationId;
	}

	public void setBurninationId(long burninationId) {
		this.burninationId = burninationId;
	}

	public int getQuestionId() {
		return questionId;
	}

	public void setQuestionId(int questionId) {
		this.questionId = questionId;
	}

	@Override
	public boolean equals(Object that) {
		if (this == that) {
			return true;
		}
		if (that == null || !(that instanceof BurninationQuestionId)) {
			return false;
		}
		BurninationQuestionId other = (BurninationQuestionId) that;
		return burninationId == other.getBurninationId() && questionId == other.questionId;
	}

	@Override
	public int hashCode() {
		return Objects.hash(burninationId, questionId);
	}

}
