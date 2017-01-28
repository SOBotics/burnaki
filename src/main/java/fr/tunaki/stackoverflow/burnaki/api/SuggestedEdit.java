package fr.tunaki.stackoverflow.burnaki.api;

import java.time.Instant;
import java.util.List;

public class SuggestedEdit {

	private int id;
	private List<String> tags;
	private Instant creationDate;
	private Instant approvalDate;
	private Instant rejectionDate;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public List<String> getTags() {
		return tags;
	}

	public void setTags(List<String> tags) {
		this.tags = tags;
	}

	public Instant getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Instant creationDate) {
		this.creationDate = creationDate;
	}

	public Instant getApprovalDate() {
		return approvalDate;
	}

	public void setApprovalDate(Instant approvalDate) {
		this.approvalDate = approvalDate;
	}

	public Instant getRejectionDate() {
		return rejectionDate;
	}

	public void setRejectionDate(Instant rejectionDate) {
		this.rejectionDate = rejectionDate;
	}

}
