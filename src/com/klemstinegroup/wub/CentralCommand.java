package com.klemstinegroup.wub;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

public class CentralCommand {

	static ArrayList<AudioObject> aolist = new ArrayList<AudioObject>();
	static ArrayList<Node> nodes = new ArrayList<Node>();
	static PlayingField pf = new PlayingField();
	static int yOffset=40;

	public synchronized static void add(AudioObject ao) {

		aolist.add(ao);
		addRectangle(new Node(new Rectangle2D.Double(0, 0, 1,  yOffset), ao));

	}

	public synchronized static void remove(AudioObject ao) {
		aolist.remove(ao);
	}

	public synchronized static void key(String s) {
		for (AudioObject au : aolist) {
			if (au.midiMap.containsKey(s)) {
				au.queue.add(au.midiMap.get(s));
			}
		}
	}

	public synchronized static void addRectangleNoMoveY(Node n) {
		nodes.add(n);
		//pf.makeImageResize();
	}

	public synchronized static void addRectangle(Node n) {
		nodes.add(n);
		pf.makeImageResize();
		while (CentralCommand.intersects(n)) {
			n.rect.y +=  yOffset;
		}
	}

	public synchronized static void removeRectangle(Node mover) {
		nodes.remove(mover);
	}

	public synchronized static boolean intersects(Rectangle2D.Double r) {

		for (Node n : nodes) {
			if (r.intersects(n.rect))
				return true;
		}
		return false;
	}

	public synchronized static boolean intersects(Node mover) {
		for (Node n : nodes) {
			if (mover != n && mover.rect.intersects(n.rect))
				return true;
		}
		return false;
	}

	public synchronized static Node whichIntersects(Node mover, ArrayList<Node> copy) {
		for (Node n : nodes) {
			if (mover != n && mover.rect.intersects(n.rect) && !copy.contains(n))
				return n;
		}
		return null;
	}
}
