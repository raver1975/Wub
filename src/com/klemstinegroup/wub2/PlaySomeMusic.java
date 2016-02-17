package com.klemstinegroup.wub2;

import java.util.Random;

public class PlaySomeMusic {

	public PlaySomeMusic() {
		Song song = LoadFromFile.loadSong("q:\\2a42n08tem87s4vs9cirt8d6t1.au");
		Audio audio = new Audio();
		System.out.println(song.analysis.getSections().size());
		//audio.play(song.getAudioInterval(song.analysis.getSections().get(13)));
//		for (int i = 0; i < song.analysis.getSections().size(); i++) {
//			audio.play(song.getAudioInterval(song.analysis.getSections().get(i)));
//		}
		Random rand=new Random();
		int n=song.analysis.getBars().size();
		for (int i=0;i<100;i++){
			int j=rand.nextInt(n);
			audio.play(song.getAudioInterval(song.analysis.getBars().get(j)));
		}
	}

	public static void main(String[] args) {
		new PlaySomeMusic();

	}

}
