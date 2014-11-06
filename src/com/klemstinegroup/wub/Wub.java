package com.klemstinegroup.wub;

import com.echonest.api.v4.TrackAnalysis;

public class Wub {

	public Wub() {

		// MidiPumper midi = new MidiPumper();

		AudioObject au1 = AudioObject.factory("songs/Robert Miles - Children.mp3");

		// midi.add(au1);

	}

	public static void main(String[] args) {
		new Wub();
		// while(true){
		// try {
		// Thread.sleep(1000);
		// } catch (InterruptedException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// }

	}

}
