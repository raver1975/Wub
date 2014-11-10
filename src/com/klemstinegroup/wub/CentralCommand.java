package com.klemstinegroup.wub;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

public class CentralCommand {

	static ArrayList<AudioObject> aolist = new ArrayList<AudioObject>();
	static ArrayList<Node> nodes = new ArrayList<Node>();
	static PlayingField pf = new PlayingField();

	public static void add(AudioObject ao) {

		aolist.add(ao);
		// ao.PlayFieldPosition.y = y;
		// advanceY();
		addRectangle(ao);

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

	public static Rectangle2D.Double getRectangle(AudioObject audioObject) {

		double max = Double.MIN_VALUE;
		for (AudioObject au : CentralCommand.aolist) {
			if (au.analysis.getDuration() > max) {
				max = au.analysis.getDuration();
			}
		}
		Rectangle2D.Double r = new Rectangle2D.Double(
				0,
				0,
				(audioObject.analysis.getDuration() * (double) pf.oldWidth / max),
				40);
		top: while (true) {
			if (intersects(r)) {
				r.y += 1;
				continue top;
			}
			break;
		}
		return r;

	}

	public static Node addRectangle(AudioObject ao) {
		Node n = new Node(getRectangle(ao), ao);
		nodes.add(n);
		pf.makeImageResize();
		pf.makeData();
		return n;
	}

	public static void removeRectangle(Node mover) {
		nodes.remove(mover);
		pf.makeData();
	}

	public static boolean intersects(Rectangle2D.Double r) {
		
		for (Node n : nodes) {
			if (r.intersects(n.rect))
				return true;
		}
		return false;
	}

	public static boolean intersects(Node mover) {
		for (Node n : nodes) {
			if (mover != n && mover.rect.intersects(n.rect))
				return true;
		}
		return false;
	}

	// public static void advanceY() {
	// y += 40;
	// if (y > pf.getHeight())
	// y = 0;
	//
	// }
}
