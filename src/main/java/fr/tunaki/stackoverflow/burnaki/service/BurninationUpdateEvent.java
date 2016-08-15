package fr.tunaki.stackoverflow.burnaki.service;

import fr.tunaki.stackoverflow.burnaki.api.Question;

public class BurninationUpdateEvent {
	
	public static enum Event {
		CLOSED, REOPEN_VOTE, RETAGGED_WITHOUT, DELETED, UNDELETE_VOTE, NEW;
	}
	
	private Event event;
	private String tag;
	private Question question;
	
	public BurninationUpdateEvent(Event event, String tag, Question question) {
		this.event = event;
		this.tag = tag;
		this.question = question;
	}

	public Event getEvent() {
		return event;
	}

	public String getTag() {
		return tag;
	}

	public Question getQuestion() {
		return question;
	}
	
	@Override
	public String toString() {
		return event + " on question " + question.getId();
	}

}
