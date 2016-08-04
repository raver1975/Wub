package com.klemstinegroup.wub2.test;

import com.echonest.api.v4.Segment;
import com.klemstinegroup.wub2.system.Audio;
import com.klemstinegroup.wub2.system.AudioInterval;
import com.klemstinegroup.wub2.system.LoadFromFile;
import com.klemstinegroup.wub2.system.Song;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import rnn.ExamplePaulGraham;
import weka.clusterers.SimpleKMeans;
import weka.core.*;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class Test3 {


    static String directory = "e:\\wub\\";
    private static final File[] list;

    static final int attLength = 28;
    public static Attribute[] attlist;

    //230
    //245 uk dubstep tutorial
    //246 good dub with voice
    //290 is good
    //296 bassnectar lights
    //298 vnv nation
    //300 icon of coil machines are us
    //301 zeds dead, eyes on fire
    //310 convenent bullet
    //316 bassnectar enter the chamber
    //323 bassnectar
    //404 NIN slave screams
    //407 rotersand almost wasted
    //423 vnv nation true life remix
    //430 gemini blue
    //439 bassnectar nothing has been broken
    //449 bassnectar paging sterophonic
    //456 bassnectar timestretch
    //1016 bassnectar basshead

    public static JFrame frame = new JFrame("test");

    static int playback = 430;
    static int stretch = 1;
    static int playbackStart = playback;
    static int playbackEnd = playback + stretch;


    public static final int numClusters = 256;

    static float pitchFactor = 17f;
    static float timbreFactor = 17f;
    static float loudFactor = 70f;
    static float durationFactor = 90f;
    public static ImagePanel tf;


    static {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        File[] list1 = new File(directory).listFiles();
        ArrayList<File> al = new ArrayList<>();
        for (File f : list1) {
            if (f.getAbsolutePath().endsWith(".au")) al.add(f);
        }
        list = al.toArray(new File[]{});

    }


    public static void main(String[] args) {


        frame.setSize(400, 300);
        tf = new ImagePanel();
        tf.setFont(new Font("Arial", Font.BOLD, 300));
        JScrollPane jscr = new JScrollPane(tf);

//        DefaultCaret caret = (DefaultCaret) tf.getCaret();
//        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        frame.add(jscr);
        frame.setVisible(true);

        for (int i = 0; i < 10; i++) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        //one time attribute setup
        FastVector attrs = new FastVector();
        attlist = new Attribute[attLength];
        for (int io = 0; io < attLength; io++) {
            attlist[io] = new Attribute("at" + io);
            attrs.addElement(attlist[io]);
        }


        //kmeans setup
        SimpleKMeans kmeans = new SimpleKMeans();
//        kmeans.setSeed(10);

        try {
            kmeans.setNumClusters(numClusters);
            kmeans.setDistanceFunction(new ManhattanDistance());
            String[] options = Utils.splitOptions("-I 100");
            kmeans.setOptions(options);

//            kmeans.setMaxIterations(100);
        } catch (Exception e) {
            e.printStackTrace();
        }
        ArrayList<SegmentSong> coll = new ArrayList<>();
        Instances dataset = new Instances("my_dataset", attrs, 0);
        SegmentSong[] lastSeen = new SegmentSong[numClusters];
        for (int songIter = playbackStart; songIter < list.length && songIter < playbackEnd; songIter++) {


//            datasets[songIter] = dataset;

            Song song = LoadFromFile.loadSong(list[songIter]);
            System.out.println("processing song #" + ((songIter - playbackStart) + 1) + "/" + Math.min(list.length, playbackEnd - playbackStart) + "\t" + list[songIter].getName());
            int cnt = 0;
            for (Segment s : song.analysis.getSegments()) {
                Instance inst = getInstance(attlist, s);
                coll.add(new SegmentSong(songIter, cnt++));
                inst.setDataset(dataset);
                dataset.add(inst);
            }
        }

        long time = System.currentTimeMillis();
        System.out.println("building cluster");
        try {
            kmeans.buildClusterer(dataset);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("cluster built: " + ((System.currentTimeMillis() - time) / 1000));

        // print out the cluster centroids
        Instances centroids = kmeans.getClusterCentroids();
        for (int io = 0; io < centroids.numInstances(); io++) {
            double dist = Float.MAX_VALUE;
            int best = -1;
            for (int j = 0; j < dataset.numInstances(); j++) {
                double dd = distance(centroids.instance(io), dataset.instance(j));
//                System.out.println("dist="+dd);
                if (dd < dist) {
                    dist = dd;
                    best = j;
                }
            }
            SegmentSong gg = coll.get(best);
            lastSeen[io] = gg;
            System.out.println("centroid io " + io + "\t" + gg);


        }
        // get cluster membership for each instance
        HashMap<SegmentSong, SegmentSong> map = new HashMap<>();
        for (int io = 0; io < dataset.numInstances(); io++) {
            try {
                int cluster = kmeans.clusterInstance(dataset.instance(io));
                SegmentSong tempSegmentSong = lastSeen[cluster];
                map.put(coll.get(io), tempSegmentSong);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        ObjectManager.write(map, "map-universal.ser");


        Audio audio = new Audio(tf,numClusters);
        Song song = SongManager.getRandom(playback);
        Song tempSong = null;
        int lastSong = -1;
        String out = "";
        ArrayList<Integer> tem = new ArrayList<>();
        for (int cnt = 0; cnt < song.analysis.getSegments().size(); cnt++) {
            SegmentSong pp = map.get(new SegmentSong(playback, cnt));
            if (!tem.contains(pp.segment)) {
                tem.add(pp.segment);
            }
            out += (char) tem.indexOf(pp.segment);

        }
        PrintWriter writer = null;
        try {
            writer = new PrintWriter("out.txt", "UTF-8");
            writer.println(out);
            writer.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        boolean flag=true;
        while(flag==true) {
            String get = null;
            try {
                get = ExamplePaulGraham.go("out",true);
            } catch (Exception e) {
                e.printStackTrace();
            }

            for (int i = 0; i < get.length(); i++) {
                char c = get.charAt(i);
                SegmentSong pp = new SegmentSong(playback, (int) c);
                SegmentSong play = map.get(pp);
                if (lastSong != play.song) {
                    tempSong = SongManager.getRandom(play.song);
                    lastSong = play.song;
                }
                AudioInterval ai = tempSong.getAudioInterval(tempSong.analysis.getSegments().get(play.segment));
                ai.payload = play;
                audio.play(ai);
            }
        }


        for (int cnt = 0; cnt < song.analysis.getSegments().size(); cnt++) {
            SegmentSong pp = new SegmentSong(playback, cnt);
            SegmentSong play = map.get(pp);
//            if (Math.random()<.5f){
//                cnt=play.segment-1;
//                System.out.println("ging back to "+(cnt));
//                continue;
//            }
            if (play == null) {
                System.out.println("null! " + pp);
                continue;
            }
            if (lastSong != play.song) {
                tempSong = SongManager.getRandom(play.song);
                lastSong = play.song;
            }
//            System.out.println("playing: "+play);
            AudioInterval ai = tempSong.getAudioInterval(tempSong.analysis.getSegments().get(play.segment));
            ai.payload = play;
            audio.play(ai);
        }
//        String meta = song.analysis.toString();
//        meta = meta.substring(0, 400);
//        meta = meta.replaceAll("(.{100})", "$1\n");
//        meta=meta.substring(0,meta.length()-1);
//        System.out.println(meta);

        JSONParser parser = new JSONParser();
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
        if (seconds == null || seconds == 0) seconds = new Long(-61);

        float scale = (int) (((float) numClusters / (float) song.analysis.getSegments().size()) * 1000) / 10f;
        System.out.println("segments size=" + song.analysis.getSegments().size() + "\t" + scale + "%");
        System.out.println("title\t" + title);
        System.out.println("artist\t" + artist);
        System.out.println("album\t" + album);
        System.out.println("genre\t" + genre);
        System.out.println("time\t" + seconds / 60 + ": " + seconds % 60);
        Iterator iter = song.analysis.getMap().entrySet().iterator();

//        while (iter.hasNext()){
//            Object bbb = iter.next();
//
//            Map.Entry<String, String> bbe = (Map.Entry<String, String>) bbb;
//            System.out.println(bbb.toString());
//
//        }
    }

    protected static double distance(Instance i1, Instance i2) {
        double tot = 0;
        for (int i = 0; i < attLength; i++) {
            double ta = i1.value(attlist[i]) - i2.value(attlist[i]);
            ta = Math.abs(ta);
            tot += ta;
        }
        return tot;
    }

    protected static Instance getInstance(Attribute[] attlist, Segment s) {

        int cnt = 0;
        Instance inst = new Instance(attLength);
        inst.setValue(attlist[cnt++], s.getDuration() * durationFactor);
        inst.setValue(attlist[cnt++], s.getLoudnessMax() * loudFactor);
        inst.setValue(attlist[cnt++], s.getLoudnessStart() * loudFactor);
        inst.setValue(attlist[cnt++], s.getLoudnessMaxTime() * loudFactor);
        inst.setValue(attlist[cnt++], timbreFactor * s.getTimbre()[0]);
        inst.setValue(attlist[cnt++], timbreFactor * s.getTimbre()[1]);
        inst.setValue(attlist[cnt++], timbreFactor * s.getTimbre()[2]);
        inst.setValue(attlist[cnt++], timbreFactor * s.getTimbre()[3]);
        inst.setValue(attlist[cnt++], timbreFactor * s.getTimbre()[4]);
        inst.setValue(attlist[cnt++], timbreFactor * s.getTimbre()[5]);
        inst.setValue(attlist[cnt++], timbreFactor * s.getTimbre()[6]);
        inst.setValue(attlist[cnt++], timbreFactor * s.getTimbre()[7]);
        inst.setValue(attlist[cnt++], timbreFactor * s.getTimbre()[8]);
        inst.setValue(attlist[cnt++], timbreFactor * s.getTimbre()[9]);
        inst.setValue(attlist[cnt++], timbreFactor * s.getTimbre()[10]);
        inst.setValue(attlist[cnt++], timbreFactor * s.getTimbre()[11]);
        inst.setValue(attlist[cnt++], s.getPitches()[0] * pitchFactor);
        inst.setValue(attlist[cnt++], s.getPitches()[1] * pitchFactor);
        inst.setValue(attlist[cnt++], s.getPitches()[2] * pitchFactor);
        inst.setValue(attlist[cnt++], s.getPitches()[3] * pitchFactor);
        inst.setValue(attlist[cnt++], s.getPitches()[4] * pitchFactor);
        inst.setValue(attlist[cnt++], s.getPitches()[5] * pitchFactor);
        inst.setValue(attlist[cnt++], s.getPitches()[6] * pitchFactor);
        inst.setValue(attlist[cnt++], s.getPitches()[7] * pitchFactor);
        inst.setValue(attlist[cnt++], s.getPitches()[8] * pitchFactor);
        inst.setValue(attlist[cnt++], s.getPitches()[9] * pitchFactor);
        inst.setValue(attlist[cnt++], s.getPitches()[10] * pitchFactor);
        inst.setValue(attlist[cnt++], s.getPitches()[11] * pitchFactor);
        return inst;
    }
}


