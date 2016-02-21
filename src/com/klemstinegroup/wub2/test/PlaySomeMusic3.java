package com.klemstinegroup.wub2.test;

import com.klemstinegroup.wub2.system.*;

public class PlaySomeMusic3 {

	public PlaySomeMusic3() {
		Audio audio = new Audio();
		Song song1 = LoadFromFile.loadSong("i:\\wub\\c906k5i2co4d8om7qiegdaoh3d.an");
		Song song2 = LoadFromFile.loadSong("i:\\wub\\9ulos27ngac2hegsjot4971al5.an");

		AudioInterval ai=song1.getAudioInterval(song1.analysis.getSections().get(2));
		System.out.println(ai.data.length);
		AudioUtils.timeStretch(ai,2d);
		System.out.println(ai.data.length);
		audio.play(ai);

	}


	public static void main(String[] args) {
		new PlaySomeMusic3();

	}

}
