package fr.tunaki.stackoverflow.burnaki.entity;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "burnination")
public class Burnination implements Serializable {

	private static final long serialVersionUID = -9046397626323921632L;

	@Id
	@GeneratedValue
	@Column(name = "id", unique = true, nullable = false)
	private long id;

	@Column(name = "tag", nullable = false, length = 35)
	private String tag;

	@Column(name = "start_date", nullable = false)
	private Instant startDate;

	@Column(name = "end_date")
	private Instant endDate;

	@Column(name = "meta_link", nullable = false)
	private String metaLink;

	@Column(name = "room_id", nullable = false)
	private int roomId;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "burnination", cascade = CascadeType.ALL)
	private List<BurninationProgress> progresses = new ArrayList<>();

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "burnination", cascade = CascadeType.ALL)
	private List<BurninationQuestion> questions = new ArrayList<>();

	public long getId() {
		return id;
	}

	@Column(name = "tag", nullable = false, length = 35)
	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public Instant getStartDate() {
		return startDate;
	}

	public void setStartDate(Instant startDate) {
		this.startDate = startDate;
	}

	public Instant getEndDate() {
		return endDate;
	}

	public void setEndDate(Instant endDate) {
		this.endDate = endDate;
	}

	public String getMetaLink() {
		return metaLink;
	}

	public void setMetaLink(String metaLink) {
		this.metaLink = metaLink;
	}

	public int getRoomId() {
		return roomId;
	}

	public void setRoomId(int roomId) {
		this.roomId = roomId;
	}

	public List<BurninationProgress> getProgresses() {
		return progresses;
	}

	public void addProgress(BurninationProgress progress) {
		progresses.add(progress);
		progress.setBurnination(this);
	}

	public List<BurninationQuestion> getQuestions() {
		return questions;
	}

	public void addQuestion(BurninationQuestion burninationQuestion) {
		questions.add(burninationQuestion);
		burninationQuestion.setBurnination(this);
	}

}
