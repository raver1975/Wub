package com.klemstinegroup.wub2.test;

import com.echonest.api.v4.Segment;
import com.sun.xml.internal.ws.developer.Serialization;

import java.io.Serializable;

/**
 * Created by Paul on 7/31/2016.
 */
public class SegmentSong implements Serializable {
//    Segment segment;
    int song;
    int segment;

    SegmentSong(int song,int segment){
        this.segment=segment;
        this.song=song;
    }

    @Override public int hashCode(){
        return Integer.hashCode(song);
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
