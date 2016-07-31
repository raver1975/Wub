package com.klemstinegroup.wub2.test;

import com.klemstinegroup.wub2.system.Audio;
import com.klemstinegroup.wub2.system.LoadFromFile;
import com.klemstinegroup.wub2.system.Song;

import java.util.Random;

public class PlaySomeMusic {

	public PlaySomeMusic() {
		//Song song1 = LoadFromFile.loadSong("i:\\wub\\5doprumaifi4p93b8qs7p0pari.an");
//		Song song1 = LoadFromFile.loadSong("i:\\wub\\c906k5i2co4d8om7qiegdaoh3d.an");
		//Song song1 = LoadFromFile.loadSong("i:\\wub\\c906k5i2co4d8om7qiegdaoh3d.an");
		Song song1= SongManager.getRandom();
//		Song song1= LoadFromFile.loadSong("e:\\wub\\2ii25uloipqee4tq40b65cr56.an");

		//Song song2 = LoadFromFile.loadSong("q:\\7mgcakup2rr54vt3oqv1bp882s.an");
		Audio audio = new Audio();
		System.out.println(song1.analysis.getSections().size());
		//audio.play(song.getAudioInterval(song.analysis.getSections().get(13)));
//		for (int i = 0; i < song.analysis.getSections().size(); i++) {
//			audio.play(song.getAudioInterval(song.analysis.getSections().get(i)));
//		}
		Random rand=new Random();
		int n1=song1.analysis.getBeats().size();
		//int n2=song2.analysis.getTatums().size();
		for (int i=0;i<1000;i++){
			audio.play(song1.getAudioInterval(song1.analysis.getBeats().get(i)));
			//else audio.play(song2.getAudioInterval(song2.analysis.getBars().get(rand.nextInt(n2))));
		}
	}

	public static void main(String[] args) {
		new PlaySomeMusic();

	}

}
