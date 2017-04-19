package com.klemstinegroup.wub.system;

import java.util.ArrayList;
import java.util.List;

import com.echonest.api.v4.Segment;
import com.echonest.api.v4.TimedEvent;
import com.echonest.api.v4.TrackAnalysis;

public class Song {
    public byte[] data;
    public TrackAnalysis analysis;
    public int number=-1;


    public Song(byte[] data, TrackAnalysis ta) {
        this.data = data;
        this.analysis = ta;
    }

    public AudioInterval getAudioInterval(List<TimedEvent> list) {
        return new AudioInterval(list, data);
    }

    public AudioInterval getAudioInterval(TimedEvent timedEvent, int s, int snum) {
        return new AudioInterval(timedEvent, data,s,snum);
    }

    public ArrayList<Segment> getSegments(TimedEvent timedEvent) {
        ArrayList<Segment> al = new ArrayList<>();
        ArrayList<Integer> al2 = getSegmentsPosition(timedEvent);
        for (int i : al2) al.add(analysis.getSegments().get(i));
        return al;
    }

    public ArrayList<Integer> getSegmentsPosition(TimedEvent timedEvent) {
        ArrayList<Integer> al = new ArrayList<>();
        int closeToStart = -1;
        int closeToEnd = -1;
        double closeToStartDiff = Double.MAX_VALUE;
        double closeToEndDiff = Double.MAX_VALUE;
        for (int i = 0; i < analysis.getSegments().size(); i++) {
            Segment t = analysis.getSegments().get(i);
            if (Math.abs(t.start - timedEvent.start) < closeToStartDiff) {
                closeToStartDiff = Math.abs(t.start - timedEvent.start);
                closeToStart = i;
            }
            if (Math.abs((t.start + t.duration) - (timedEvent.start + timedEvent.duration)) < closeToEndDiff) {
                closeToEndDiff = Math.abs((t.start + t.duration) - (timedEvent.start + timedEvent.duration));
                closeToEnd = i;
            }
        }
        for (int i = closeToStart; i <= closeToEnd; i++) {
            al.add(i);
        }
        return al;
    }

    public AudioInterval getAudioInterval(Segment sem, SegmentSong segMapped) {
        return getAudioInterval(sem,segMapped.song,segMapped.segment);
    }
    @Override
    public String toString(){
        return this.analysis.toString();
    }
}
