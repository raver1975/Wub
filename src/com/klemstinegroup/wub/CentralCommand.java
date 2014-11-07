package com.klemstinegroup.wub;

import java.awt.event.KeyEvent;
import java.util.ArrayList;

public class CentralCommand {

	static ArrayList<AudioObject> aolist = new ArrayList<AudioObject>();

	public static void add(AudioObject ao) {
		aolist.add(ao);
	}

	public static void remove(AudioObject ao) {
		aolist.remove(ao);
		if (aolist.size() == 0)
			System.exit(0);
	}

	public static void key(String s) {
		for (AudioObject au : aolist) {
			if (au.midiMap.containsKey(s)) {
				au.queue.add(au.midiMap.get(s));
			}
		}
	}
}
