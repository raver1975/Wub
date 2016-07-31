package com.klemstinegroup.wub2.test;

import com.echonest.api.v4.Segment;

/**
 * Created by Paul on 7/31/2016.
 */
public class SegmentSong  {
    Segment segment;
    int song;
    SegmentSong(int song,Segment segment){
        this.segment=segment;
        this.song=song;
    }

    @Override public int hashCode(){
        return segment.hashCode();
    }


    @Override
    public boolean equals(Object o) {
        if (!(o instanceof SegmentSong))return false;
        SegmentSong ss=(SegmentSong)o;
        if (segment.hashCode()!=ss.segment.hashCode())return false;
        return true;
    }
}
