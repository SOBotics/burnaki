package fr.tunaki.stackoverflow.burnaki.bot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.sobotics.chatexchange.chat.Room;

public final class BurnRoom {

	private Room room;
	private List<String> tags;

	public BurnRoom(Room room) {
		this(room, new ArrayList<>());
	}

	public BurnRoom(Room room, String tag) {
		this(room, new ArrayList<>(Arrays.asList(tag)));
	}

	public BurnRoom(Room room, List<String> tags) {
		this.room = room;
		this.tags = tags;
	}

	public Room getRoom() {
		return room;
	}

	public List<String> getTags() {
		return tags;
	}

	public void addTag(String tag) {
		tags.add(tag);
	}

}