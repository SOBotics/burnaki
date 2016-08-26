package fr.tunaki.stackoverflow.burnaki.bot;

import fr.tunaki.stackoverflow.chat.Room;

public final class BurnRoom {
	
	private Room room;
	private String tag;
	
	public BurnRoom(Room room, String tag) {
		this.room = room;
		this.tag = tag;
	}

	public Room getRoom() {
		return room;
	}

	public String getTag() {
		return tag;
	}
	
}