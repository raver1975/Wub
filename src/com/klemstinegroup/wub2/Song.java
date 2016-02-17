package com.klemstinegroup.wub2;

import com.echonest.api.v4.TimedEvent;
import com.echonest.api.v4.TrackAnalysis;

public class Song {
	byte[] data;
	TrackAnalysis analysis;
	
	public Song(byte[] data, TrackAnalysis ta) {
		this.data=data;
		this.analysis=ta;
	}

	public AudioInterval getAudioInterval(TimedEvent timedEvent) {
		return new AudioInterval(timedEvent,data);
	}
}
