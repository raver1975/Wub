package com.klemstinegroup.wub2.test;

import java.io.Serializable;

/**
 * Created by Paul on 7/31/2016.
 */
public class SegmentSong implements Serializable {
//    Segment segment;
public int song;
    public int segment;

    public SegmentSong(int song, int segment){
        this.segment=segment;
        this.song=song;
    }

    @Override public int hashCode(){
        return Integer.hashCode(song)*1000121+Integer.hashCode(segment)*457848421;
    }


    @Override
    public boolean equals(Object o) {
        if (!(o instanceof SegmentSong))return false;
        SegmentSong ss=(SegmentSong)o;
        if (segment!=ss.segment)return false;
        if (song!=ss.song)return false;
        return true;
    }

    @Override
    public String toString() {
        return "SegmentSong{" +
                "song=" + song +
                ", segment=" + segment +
                '}';
    }
}
