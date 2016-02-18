package com.klemstinegroup.wub2;

import java.util.Random;

public class PlaySomeMusic {

	public PlaySomeMusic() {
		Song song1 = LoadFromFile.loadSong("q:\\5doprumaifi4p93b8qs7p0pari.an");
		//Song song2 = LoadFromFile.loadSong("q:\\7mgcakup2rr54vt3oqv1bp882s.an");
		Audio audio = new Audio();
		System.out.println(song1.analysis.getSections().size());
		//audio.play(song.getAudioInterval(song.analysis.getSections().get(13)));
//		for (int i = 0; i < song.analysis.getSections().size(); i++) {
//			audio.play(song.getAudioInterval(song.analysis.getSections().get(i)));
//		}
		Random rand=new Random();
		int n1=song1.analysis.getTatums().size();
		//int n2=song2.analysis.getTatums().size();
		for (int i=0;i<1000;i++){
			audio.play(song1.getAudioInterval(song1.analysis.getTatums().get(rand.nextInt(n1))));
			//else audio.play(song2.getAudioInterval(song2.analysis.getBars().get(rand.nextInt(n2))));
		}
	}

	public static void main(String[] args) {
		new PlaySomeMusic();

	}

}
