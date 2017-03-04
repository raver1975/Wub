package com.klemstinegroup.wub3.old;

import com.echonest.api.v4.TrackAnalysis;
import com.klemstinegroup.wub.AudioObject;
import com.klemstinegroup.wub3.SpotifyUtils;
import com.wrapper.spotify.models.Track;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;

/**
 * Created by Paul on 2/25/2017.
 */
public class SpotifyToWub {

    private static boolean firstSongIsLoadedYet;

    public SpotifyToWub() throws IOException, ParseException {
    }

    public static void main(String[] args)  {
        String spotifyId = "spotify:track:6z0zyXMTA0ans4OoTAO2Bm";
        if (args.length == 0) {
            System.out.println("Please enter a spotify track url:");
            Scanner sc = new Scanner(System.in);
            String s=sc.nextLine();
            if (s!=null&&s.length()>0)spotifyId = s;
        }
        if (args.length == 1) {
            spotifyId = args[0];
        }
        spotifyId = spotifyId.replace("spotify:track:", "");

        try {
            grab(spotifyId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void grab(String id) throws Exception {
        System.out.println(id);
        Track track = SpotifyUtils.getTrack(id);
        System.out.println("got track info");
        JSONObject js1 = SpotifyUtils.getDownloadList(track.getArtists().get(0).getName() + " " + track.getName());
        JSONArray js2 = (JSONArray) js1.get("data");
        System.out.println(js2.toString());

        TrackAnalysis ta = SpotifyUtils.getAnalysis(id);
//        File dir=new File("mp3");
//        if (!dir.exists()||dir.isDirectory()){
//            dir.mkdir();
//        }
        System.out.println("Searching for duration " + ta.getDuration());
        for (int i = 0; i < js2.size(); i++) {
            JSONObject data = (JSONObject) js2.get(i);
            long duration = (Long) data.get("duration");
            String artist=(String)data.get("artist");
            String title=(String)data.get("title");
            if (Math.abs((int) duration - (double) ta.getDuration()) < 2d) {
                System.out.println("*****" + duration + "\t" + data.toString());

                String downloadUrl = (String) data.get("download");

                System.out.println("Downloading song from: " + downloadUrl);
                URLConnection conn = SpotifyUtils.getConnection(new URL(downloadUrl));
                InputStream is = conn.getInputStream();
//                String outputFile = track.getArtists().get(0).getName()+"-"+track.getName()+"-"+name+ "-" + i + ".mp3";
                String outputFile = "mp3/"+artist+"-"+title+ "-" + i + ".mp3";
                outputFile=outputFile.replaceAll("[^a-zA-Z0-9.-/]", "_");
                File file = new File(outputFile);
                if (!file.exists()) {
                    OutputStream outstream = new FileOutputStream(file);
                    byte[] buffer = new byte[4096];
                    int len;
                    int tot = 0;
                    int cnt = 0;
                    int rows = 0;
                    System.out.print(tot + ":");
                    while ((len = is.read(buffer)) > 0) {
                        System.out.print("*");
                        if (cnt++ > 40) {
                            tot++;
                            System.out.println();

                            System.out.print(tot + ":");
                            cnt = 0;

                        }
                        outstream.write(buffer, 0, len);
                    }
                    outstream.close();

                }
                System.out.println("\nDone!");
                if (!firstSongIsLoadedYet) {
                    firstSongIsLoadedYet=true;
                    AudioObject au = AudioObject.factory(new File(outputFile), ta);
                    //  AudioObject ao=new AudioObject(new File(outputFile),ta);

                }
            } else {
                System.out.println(duration + "\t" + data.toString());
            }
        }

    }


}
