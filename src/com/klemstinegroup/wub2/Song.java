package com.klemstinegroup.wub2;

import java.util.ArrayList;

import com.echonest.api.v4.Segment;
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
	
	public ArrayList<Segment> getSegments(TimedEvent timedEvent){
		ArrayList<Segment> al=new ArrayList<>();
		for (int i=0;i<analysis.getSegments().size();i++){
			Segment t=analysis.getSegments().get(i);
			if (t.start+t.duration>=timedEvent.start && t.start<=timedEvent.start+timedEvent.duration){
				al.add(t);
			}
		}
		return al;
	}
}
