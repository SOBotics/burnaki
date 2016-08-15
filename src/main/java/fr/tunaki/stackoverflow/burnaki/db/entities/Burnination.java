package fr.tunaki.stackoverflow.burnaki.db.entities;

import static javax.persistence.GenerationType.IDENTITY;

import java.io.Serializable;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

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
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private long id;
	
	@Column(name = "tag", nullable = false, length = 35)
	private String tag;
	
	@Column(name = "start_date", nullable = false)
	private Instant startDate;
	
	@Column(name = "end_date", nullable = false)
	private Instant endDate;
	
	@Column(name = "meta_link", nullable = false)
	private String metaLink;
	
	@Column(name = "room_id", nullable = false)
	private int roomId;
	
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "burnination")
	private Set<BurninationProgress> burninationProgresses = new HashSet<BurninationProgress>();
	
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "burnination")
	private Set<BurninationQuestion> burninationQuestions = new HashSet<BurninationQuestion>();

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

	public Set<BurninationProgress> getBurninationProgresses() {
		return burninationProgresses;
	}

	public void setBurninationProgresses(Set<BurninationProgress> burninationProgresses) {
		this.burninationProgresses = burninationProgresses;
	}

	public Set<BurninationQuestion> getBurninationQuestions() {
		return burninationQuestions;
	}

	public void setBurninationQuestions(Set<BurninationQuestion> burninationQuestions) {
		this.burninationQuestions = burninationQuestions;
	}

}
