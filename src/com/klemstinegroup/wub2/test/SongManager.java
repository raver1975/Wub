package com.klemstinegroup.wub2.test;

import com.klemstinegroup.wub2.system.LoadFromFile;
import com.klemstinegroup.wub2.system.Song;

import java.io.File;

/**
 * Created by Paul on 7/30/2016.
 */
public class SongManager {
    static String directory = "e:\\wub\\";
    private static final File[] list;

    static {
        list = new File(directory).listFiles();
        System.out.println(list.length + " wub files in directory");
    }

    public static Song getRandom(int i) {
        if (i<0)return getRandom();
        if (i > -1 && i < list.length) return LoadFromFile.loadSong(list[i]);
        else return null;
    }

    public static Song getRandom() {
        int sel = (int) (list.length * Math.random());
        System.out.println("playing song = "+sel);
        return LoadFromFile.loadSong(list[sel]);
    }
}
