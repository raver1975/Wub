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
		addRectangle(new Node(new Rectangle2D.Double(0,0,1,40),ao));
		pf.makeImageResize();

	}

	public static void remove(AudioObject ao) {
		aolist.remove(ao);
		// if (aolist.size() == 0)
		// System.exit(0);
	}

	public static void key(String s) {
		for (AudioObject au : aolist) {
			if (au.midiMap.containsKey(s)) {
				au.queue.add(au.midiMap.get(s));
			}
		}
	}

	// public static Rectangle2D.Double getRectangle(AudioObject audioObject) {
	//
	// double minx = Double.MAX_VALUE;
	// double maxx = Double.MIN_VALUE;
	// for (Node node : CentralCommand.nodes) {
	// if (node.rect.x < minx)
	// minx = node.rect.x;
	// if (node.rect.width + node.rect.x > maxx)
	// maxx = node.rect.width + node.rect.x;
	//
	// }
	// double lengthInPixels = maxx - minx;
	// double bytesPerPixel = audioObject.data.length
	// / CentralCommand.nodes.get(0).rect.width;
	// double lengthInBytes = (int) (lengthInPixels * bytesPerPixel);
	// lengthInBytes += lengthInBytes % AudioObject.frameSize;
	// // minx--;
	// // maxx--;
	// Rectangle2D.Double r = new Rectangle2D.Double(
	// 0,
	// 0,
	// (audioObject.data.length * (double) pf.oldWidth / lengthInBytes),
	// 40);
	// top: while (true) {
	// if (intersects(r)) {
	// r.y += 1;
	// continue top;
	// }
	// break;
	// }
	// return r;
	//
	// }

	public static void addRectangle(Node n) {
		nodes.add(n);
		while (intersects(n)){
			n.rect.y++;
		}
		pf.makeImageResize();
	}

	public static void removeRectangle(Node mover) {
		nodes.remove(mover);
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
