package fr.tunaki.stackoverflow.burnaki.service;

import fr.tunaki.stackoverflow.burnaki.entity.BurninationQuestion;

public class BurninationUpdateEvent {

	public static enum Event {

		CLOSED("Closed"), REOPEN_VOTE("Reopen vote"), RETAGGED_WITHOUT("Tag removed"), SUGGESTED_EDIT_WITHOUT("Suggested edit"), DELETED("Deleted"), UNDELETED("Undeleted"), NEW("New");

		private final String display;

		private Event(String display) {
			this.display = display;
		}

		public String getDisplay() {
			return display;
		}

	}

	private Event event;
	private String tag;
	private BurninationQuestion question;

	public BurninationUpdateEvent(Event event, String tag, BurninationQuestion question) {
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

	public BurninationQuestion getQuestion() {
		return question;
	}

	@Override
	public String toString() {
		return event + " on question " + question.getId();
	}

}
