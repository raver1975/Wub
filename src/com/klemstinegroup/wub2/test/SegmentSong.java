package com.klemstinegroup.wub2.test;

import com.echonest.api.v4.Segment;

/**
 * Created by Paul on 7/31/2016.
 */
public class SegmentSong  {
    Segment segment;
    int song;
    int id;

    static int cnt;
    SegmentSong(int song,Segment segment){
        this.segment=segment;
        this.song=song;
        this.id=cnt++;
    }

    @Override public int hashCode(){
        return Integer.hashCode(id);
    }


    @Override
    public boolean equals(Object o) {
        if (!(o instanceof SegmentSong))return false;
        SegmentSong ss=(SegmentSong)o;
        if (id!=ss.id)return false;
        return true;
    }

    @Override
    public String toString() {
        return "SegmentSong{" +
                "song=" + song +
                ", id=" + id +
                '}';
    }
}
