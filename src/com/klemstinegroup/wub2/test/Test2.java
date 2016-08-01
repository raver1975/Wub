package com.klemstinegroup.wub2.test;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import com.echonest.api.v4.Segment;

import com.klemstinegroup.wub2.system.Audio;
import com.klemstinegroup.wub2.system.LoadFromFile;
import com.klemstinegroup.wub2.system.Song;
import weka.clusterers.SimpleKMeans;
import weka.core.*;

public class Test2 {



    static String directory = "e:\\wub\\";
    private static final File[] list;

    static final int attLength = 28;
    public static Attribute[] attlist;


    static int playback = 101;
    static int stretch = 4;
    static int playbackStart = playback;
    static int playbackEnd = playback + stretch;


    static final int numClusters = 2000;

    static float pitchFactor = 2f;
    static float timbreFactor =5f;
    static float loudFactor = 20f;
    static float durationFactor = 40f;

    static {
        File[] list1 = new File(directory).listFiles();
        ArrayList<File> al = new ArrayList<>();
        for (File f : list1) {
            if (f.getAbsolutePath().endsWith(".au")) al.add(f);
        }
        list = al.toArray(new File[]{});

    }


    public static void main(String[] args) {


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
            System.out.println("processing song #" + ((songIter-playbackStart)+1) + "/" + Math.min(list.length, playbackEnd-playbackStart) + "\t" + list[songIter].getName());
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


        Audio audio = new Audio();
        Song song = SongManager.getRandom(playback);

        for (int cnt=0;cnt<song.analysis.getSegments().size();cnt++) {
            SegmentSong pp = new SegmentSong(playback, cnt);
            SegmentSong play = map.get(pp);
            if (play == null) {
                System.out.println("null! " + pp);
                continue;
            }
            Song tempSong = SongManager.getRandom(play.song);
            audio.play(tempSong.getAudioInterval(tempSong.analysis.getSegments().get(play.segment)));
        }
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


