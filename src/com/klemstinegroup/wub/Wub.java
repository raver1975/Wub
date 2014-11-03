package com.klemstinegroup.wub;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class Wub {

	public Wub() {
//		AudioObject au = AudioObject.factory("songs/plumber.mp3");
//		AudioObject au1 = AudioObject.factory("songs/heat.mp3");
//		AudioObject au2 = AudioObject.factory("songs/Pendulum - The Island Pt. I (Dawn).mp3");
		AudioObject.factory("songs/Zeds Dead - In The Beginning.mp3");
		// System.out.println("here");
	}

	public static void main(String[] args) {
		new Wub();

	}

}
