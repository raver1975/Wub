package com.klemstinegroup.wub;

import java.awt.Rectangle;
import java.util.ArrayList;

public class CentralCommand {

	static ArrayList<AudioObject> aolist = new ArrayList<AudioObject>();

	static PlayingField pf = new PlayingField();

	public static void add(AudioObject ao) {

		aolist.add(ao);
		// ao.PlayFieldPosition.y = y;
		// advanceY();
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

	public static Rectangle getRectangle(AudioObject audioObject) {

		double max = Double.MIN_VALUE;
		for (AudioObject au : CentralCommand.aolist) {
			if (au.analysis.getDuration() > max) {
				max = au.analysis.getDuration();
			}
		}
		Rectangle r = new Rectangle(0, 0, (int) (audioObject.analysis.getDuration() * (double) pf.oldWidth / max), 40);
		top:while (true) {
			for (AudioObject au : aolist) {
				for (Rectangle rect : au.playFieldPosition) {
					if (r.intersects(rect)) {
						r.y += 1;
						continue top;
					}
				}
			}
			break;
		}
		return r;

	}

	// public static void advanceY() {
	// y += 40;
	// if (y > pf.getHeight())
	// y = 0;
	//
	// }
}
