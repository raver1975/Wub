package com.klemstinegroup.wub2.test;

import com.echonest.api.v4.Segment;
import com.klemstinegroup.wub2.system.Audio;
import com.klemstinegroup.wub2.system.AudioInterval;
import com.klemstinegroup.wub2.system.LoadFromFile;
import com.klemstinegroup.wub2.system.Song;
import weka.clusterers.SimpleKMeans;
import weka.core.*;

import java.io.File;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Paul on 7/30/2016.
 */
public class SongManager {
    static String directory = "e:\\wub\\";
    private static final File[] list;

    static final int attLength = 28;
    public static Attribute[] attlist;

    static final int numClusters = 255*255;
    static final int songsToScan = 100;

    static float pitchFactor = 2f;
    static float timbreFactor = 2f;
    static float loudFactor = 20f;
    static float durationFactor = 40f;


    static {
        File[] list1 = new File(directory).listFiles();
        ArrayList<File> al=new ArrayList<>();
        for (File f:list1){
            if (f.getAbsolutePath().endsWith(".au"))al.add(f);
        }
        list=al.toArray(new File[]{});

    }

    public static Song getRandom(int i) {
        if (i < 0) return getRandom();
        if (i > -1 && i < list.length) return LoadFromFile.loadSong(list[i]);
        else return null;
    }

    public static int getSize() {
        return list.length;
    }

    public static void process() {
//        if (true)return ;
        SimpleKMeans kmeans = new SimpleKMeans();
        kmeans.setSeed(10);
        FastVector attrs = new FastVector();
        attlist = new Attribute[attLength];
        for (int io = 0; io < attLength; io++) {
            attlist[io] = new Attribute("at" + io);
            attrs.addElement(attlist[io]);
        }
        Instances dataset = new Instances("my_dataset", attrs, 0);
        try {
            kmeans.setNumClusters(numClusters);
            kmeans.setDistanceFunction(new ManhattanDistance());
        } catch (Exception e) {
            e.printStackTrace();
        }


//        HashMap<Instance, SegmentSong> hm = new HashMap<>();
        HashMap<Integer, SegmentSong> map = new HashMap<>();
        SegmentSong[] lastSeen = new SegmentSong[numClusters];

        ArrayList<SegmentSong> coll = new ArrayList<>();
        for (int songIter = 0; songIter < list.length && songIter < songsToScan; songIter++) {
            Song song = LoadFromFile.loadSong(list[songIter]);
            System.out.println("processing song #" + songIter +"/"+getSize()+"\t"+list[songIter].getName());
            int cnt = 0;
            for (Segment s : song.analysis.getSegments()) {
                Instance inst = getInstance(attlist, s);
                coll.add(new SegmentSong(songIter, s));
                inst.setDataset(dataset);
                dataset.add(inst);
            }
        }
        long time=System.currentTimeMillis();
        System.out.println("building cluster");
        try {
            kmeans.buildClusterer(dataset);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("cluster built: "+((System.currentTimeMillis()-time)/1000));
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
//                        SegmentSong ss=hm.get(dataset.instance(best));
//            Song tempSong=SongManager.getRandom(best.song);
//            lastSeen[io] = song.analysis.getSegments().get(best);
            SegmentSong gg = coll.get(best);
            lastSeen[io] = gg;
            System.out.println("centroid io " + io + "\t" + gg);

            Song tt = SongManager.getRandom(gg.song);
            AudioInterval ai = tt.getAudioInterval(gg.segment);
            ObjectManager.write(ai, "centroid" + io + ".ser");
        }

        // get cluster membership for each instance
        for (int io = 0; io < dataset.numInstances(); io++) {
            try {
                int cluster = kmeans.clusterInstance(dataset.instance(io));
                SegmentSong tempSegmentSong = lastSeen[cluster];
                map.put(io, tempSegmentSong);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        ObjectManager.write(map,"map.ser");
        Audio audio = new Audio();
        Song song = SongManager.getRandom(0);
        int cnt = 0;
        for (Segment s : song.analysis.getSegments()) {
//            Instance inst = getInstance(attlist, s);
            SegmentSong play = map.get(cnt++);
            if (play == null) {
                System.out.println("null~!!!");
                continue;
            }//            System.out.println("******" + play);
            Song tempSong = SongManager.getRandom(play.song);
            audio.play(tempSong.getAudioInterval(play.segment));
        }
    }

    private static Instance getInstance(Attribute[] attlist, Segment s) {

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

    private static double distance(Instance i1, Instance i2) {
        double tot = 0;
        for (int i = 0; i < attLength; i++) {
            double ta = i1.value(attlist[i]) - i2.value(attlist[i]);
            ta = Math.abs(ta);
            tot += ta;
        }
        return tot;
    }

    public static Song getRandom() {
        int sel = (int) (list.length * Math.random());
        System.out.println("playing song = " + sel);
        return LoadFromFile.loadSong(list[sel]);
    }
}
