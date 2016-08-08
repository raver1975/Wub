package com.klemstinegroup.wub2.test;

import com.klemstinegroup.wub2.system.LoadFromFile;
import com.klemstinegroup.wub2.system.Song;
import org.json.simple.JSONObject;

import javax.swing.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

/**
 * Created by Paul on 8/8/2016.
 */
public class ListDirectory {
    private static final File[] list;
    static String directory = "e:\\wub\\";

    static {
        File[] list1 = new File(directory).listFiles();
        ArrayList<File> al = new ArrayList<>();
        for (File f : list1) {
            if (f.getAbsolutePath().endsWith(".au")) al.add(f);
        }
        list = al.toArray(new File[]{});
    }
    public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException {
        PrintWriter writer = new PrintWriter("catalog.txt", "UTF-8");
        int cnt=0;
        for (File file:list){
            if (!file.getName().endsWith(".au"))continue;
            Song song = LoadFromFile.loadSong(file);
            JSONObject js = (JSONObject) song.analysis.getMap().get("meta");
            String title = null;
            String artist = null;
            String album = null;
            String genre = null;
            Long seconds = null;

            try {
                title = (String) js.get("title");
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                artist = (String) js.get("artist");
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                album = (String) js.get("album");
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                genre = (String) js.get("genre");
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                seconds = (Long) js.get("seconds");
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (seconds == null || seconds == 0) seconds = new Long(-1);
            String s=(cnt++)+"\t"+file.getName().substring(0,file.getName().length()-3)+"\t"+artist+"\t"+title+"\t"+album+"\t"+genre+"\t"+seconds;
            System.out.println(s);
            writer.println(s);

        }
        writer.close();
        System.out.println("done!");
    }
}
