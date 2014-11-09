package com.klemstinegroup.wub;

import java.awt.event.KeyEvent;
import java.util.ArrayList;

public class CentralCommand {

	static ArrayList<AudioObject> aolist = new ArrayList<AudioObject>();

	static PlayingField pf = new PlayingField();
	static int y = 0;

	public static void add(AudioObject ao) {

		aolist.add(ao);
		ao.PlayFieldPosition.y = y;
		y += 40;
		if (y > pf.getHeight())
			y = 0;

		pf.makeImageResize();

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
