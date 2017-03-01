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
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Paul on 2/25/2017.
 */
public class Main2 {

    //    public static String artist="Zomboy";
//    public static String song="Like a bitch";
//public static String spotifyId="4xbRzFWVo7etLSA0ZCBMQG";
    public static String spotifyId = "spotify:track:6z0zyXMTA0ans4OoTAO2Bm";

    static {
        spotifyId = spotifyId.replace("spotify:track:", "");
    }


    public Main2() throws IOException, ParseException {
    }

    public static void main(String[] args) throws IOException, ParseException, NoSuchAlgorithmException, KeyManagementException {

        Track track = SpotifyUtils.getTrack(spotifyId);
        JSONObject js1 = SpotifyUtils.getDownloadList(track.getArtists().get(0).getName() + " " + track.getName());
        JSONArray js2 = (JSONArray) js1.get("data");
        System.out.println(js2.toString());

        TrackAnalysis ta = SpotifyUtils.getAnalysis(spotifyId);

        System.out.println("Searching for duration " + ta.getDuration());
        for (int i = 0; i < js2.size(); i++) {
            JSONObject data = (JSONObject) js2.get(i);
            long duration = (Long) data.get("duration");
            if (Math.abs((int) duration - (double) ta.getDuration()) < 2d) {
                System.out.println("*****" + duration + "\t" + data.toString());

                String downloadUrl = (String) data.get("download");

                System.out.println("Downloading song from: " + downloadUrl);
                HttpsURLConnection conn = SpotifyUtils.getConnection(new URL(downloadUrl));
                InputStream is = conn.getInputStream();
                String outputFile = "mp3/" + spotifyId + "-" + i + ".mp3";

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
                            System.out.println();

                            System.out.print(tot + ":");
                            cnt = 0;

                        }
                        outstream.write(buffer, 0, len);
                    }
                    outstream.close();

                }
                System.out.println("\nDone!");
                   AudioObject au = AudioObject.factory(new File(outputFile),ta);
                //  AudioObject ao=new AudioObject(new File(outputFile),ta);

            } else {
                System.out.println(duration + "\t" + data.toString());
            }
        }

    }


}
