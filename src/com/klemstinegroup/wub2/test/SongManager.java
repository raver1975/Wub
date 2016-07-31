package com.klemstinegroup.wub2.test;

import com.echonest.api.v4.Segment;
import com.klemstinegroup.wub2.system.LoadFromFile;
import com.klemstinegroup.wub2.system.Song;
import weka.clusterers.SimpleKMeans;
import weka.core.*;

import java.io.File;
import java.util.HashMap;

/**
 * Created by Paul on 7/30/2016.
 */
public class SongManager {
    static String directory = "e:\\wub\\";
    private static final File[] list;

    static final int attLength = 28;
    public static Attribute[] attlist;

    static final int numClusters = 255;

    static float pitchFactor = 2f;
    static float timbreFactor = 2f;
    static float loudFactor = 20f;
    static float durationFactor = 40f;


    static {
        list = new File(directory).listFiles();
        System.out.println(list.length + " wub files in directory");
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
        if (true)return ;
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


        HashMap<Instance, SegmentSong> hm = new HashMap<>();
        HashMap<SegmentSong, Segment> hrm = new HashMap<>();
        SegmentSong[] lastSeen = new SegmentSong[numClusters];

        for (int songIter = 0; songIter < list.length && songIter < 2; songIter++) {
            Song song = LoadFromFile.loadSong(list[songIter]);
            System.out.println("processing song #" + songIter);
            int cnt = 0;
            for (Segment s : song.analysis.getSegments()) {
                Instance inst = getInstance(attlist, s);
                hm.put(inst, new SegmentSong(cnt,s));
                inst.setDataset(dataset);
                dataset.add(inst);
            }


        }
        try {
            kmeans.buildClusterer(dataset);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // print out the cluster centroids
        Instances centroids = kmeans.getClusterCentroids();
        for (int io = 0; io < centroids.numInstances(); io++) {
            double dist = Float.MAX_VALUE;
            int best = -1;
            for (int j = 0; j < dataset.numInstances(); j++) {
                double dd = distance(centroids.instance(io), dataset.instance(j));
                if (dd < dist) {
                    dist = dd;
                    best = j;
                }
            }
            Song tempSong=SongManager.getRandom(best);
//            lastSeen[io] = song.analysis.getSegments().get(best);
            lastSeen[io]=new SegmentSong(best,tempSong.analysis.getSegments().get(best));

        }

        // get cluster membership for each instance
        for (int io = 0; io < dataset.numInstances(); io++) {
            try {
//                hrm.put(io, lastSeen[kmeans.clusterInstance(dataset.instance(io))]);
            } catch (Exception e) {
                e.printStackTrace();
            }
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
