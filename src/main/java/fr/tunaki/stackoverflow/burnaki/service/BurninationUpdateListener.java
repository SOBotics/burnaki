package fr.tunaki.stackoverflow.burnaki.service;

import java.util.List;

public interface BurninationUpdateListener {
	
	void onUpdate(List<BurninationUpdateEvent> events);

}
