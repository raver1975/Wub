package com.klemstinegroup.wub.ai.custom;

import com.echonest.api.v4.Segment;
import com.klemstinegroup.wub.system.*;
import weka.clusterers.SimpleKMeans;
import weka.core.*;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Created by Paul on 4/4/2017.
 */
public class Custom {

    static final int attLength = 28;
    public  Attribute[] attlist;
    int width = 1200;
    int height = 400;
    int numClusters=50;
    public Custom() {

        int sonu = (int) (Math.random() * 1300);
        Song song = SongManager.getRandom(sonu);
        ImagePanel tf = new ImagePanel();
//        JTextArea jta = new JTextArea(4, 20);
        JFrame jframe = new JFrame("Wub");
        jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jframe.setSize(width, height);
        jframe.setResizable(false);
        tf.setFont(new Font("Arial", Font.BOLD, 300));
        jframe.add("Center", tf);
        jframe.setVisible(true);
        Audio audio = new Audio(jframe, tf, numClusters);
        List<Segment> segments = song.analysis.getSegments();
        int i = 0;
        HashMap<SegmentSong, SegmentSong> map1 = makeMap(numClusters, song, false);
//        HashMap<SegmentSong, SegmentSong> mapr = makeMap(numClusters, song, true);
        HashMap<SegmentSong, Integer> count = new HashMap<>();
        for (Segment s : segments) {
            SegmentSong segOrig = new SegmentSong(song.number, i);
            SegmentSong segMapped = map1.get(segOrig);
            Segment sem=segments.get(segMapped.segment);
            audio.play(song.getAudioInterval(sem,segMapped));

//            System.out.println(segOrig + " maps to " + segMapped);
            if (count.containsKey(segMapped)) count.put(segMapped, count.get(segMapped) + 1);
            else count.put(segMapped, 1);
            i++;
        }
        i = 0;
        for (Segment s : segments) {
            System.out.println(i + "\t" + count.get(map1.get(new SegmentSong(song.number, i))));
            i++;
        }
    }

    private  HashMap<SegmentSong, SegmentSong> makeMap(int numClusters, Song song1, boolean reverseMap) {

        //one time attribute setup
        FastVector attrs = new FastVector();
        attlist = new Attribute[attLength];
        for (int io = 0; io < attLength; io++) {
            attlist[io] = new Attribute("at" + io);
            attrs.addElement(attlist[io]);
        }

        //kmeans setup
        SimpleKMeans kmeans = new SimpleKMeans();

        try {
            String[] options = Utils.splitOptions("-I 100");
            kmeans.setNumClusters(numClusters);
            kmeans.setDistanceFunction(new ManhattanDistance());
            kmeans.setOptions(options);
        } catch (Exception e) {
            e.printStackTrace();
        }
        ArrayList<SegmentSong> coll = new ArrayList<>();
        Instances dataset = new Instances("my_dataset", attrs, 0);
        SegmentSong[] lastSeen = new SegmentSong[numClusters];

        int cnt = 0;
        for (Segment s : song1.analysis.getSegments()) {
            Instance inst = getInstance(attlist, s);
            coll.add(new SegmentSong(song1.number, cnt++));
            inst.setDataset(dataset);
            dataset.add(inst);
        }


        long time = System.currentTimeMillis();
        System.out.println("building cluster " + numClusters);
        try {
            kmeans.buildClusterer(dataset);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("cluster found in " + ((System.currentTimeMillis() - time)) + " ms");

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
//            System.out.println("centroid io " + io + "\t" + gg);


        }
        // get cluster membership for each instance
        HashMap<SegmentSong, SegmentSong> map = new HashMap<>();
        for (int io = 0; io < dataset.numInstances(); io++) {
            try {
                int cluster = kmeans.clusterInstance(dataset.instance(io));
                SegmentSong tempSegmentSong = lastSeen[cluster];
                if (reverseMap) map.put(tempSegmentSong, coll.get(io));
                else map.put(coll.get(io), tempSegmentSong);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
//        ObjectManager.write(map, "map-universal.ser");
        return map;
    }

    protected double distance(Instance i1, Instance i2) {
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
        inst.setValue(attlist[cnt++], s.getDuration() * Settings.durationFactor);
        inst.setValue(attlist[cnt++], s.getLoudnessMax() * Settings.loudFactor);
        inst.setValue(attlist[cnt++], s.getLoudnessStart() * Settings.loudFactor);
        inst.setValue(attlist[cnt++], s.getLoudnessMaxTime() * Settings.loudFactor);
        inst.setValue(attlist[cnt++], Settings.timbreFactor * s.getTimbre()[0]);
        inst.setValue(attlist[cnt++], Settings.timbreFactor * s.getTimbre()[1]);
        inst.setValue(attlist[cnt++], Settings.timbreFactor * s.getTimbre()[2]);
        inst.setValue(attlist[cnt++], Settings.timbreFactor * s.getTimbre()[3]);
        inst.setValue(attlist[cnt++], Settings.timbreFactor * s.getTimbre()[4]);
        inst.setValue(attlist[cnt++], Settings.timbreFactor * s.getTimbre()[5]);
        inst.setValue(attlist[cnt++], Settings.timbreFactor * s.getTimbre()[6]);
        inst.setValue(attlist[cnt++], Settings.timbreFactor * s.getTimbre()[7]);
        inst.setValue(attlist[cnt++], Settings.timbreFactor * s.getTimbre()[8]);
        inst.setValue(attlist[cnt++], Settings.timbreFactor * s.getTimbre()[9]);
        inst.setValue(attlist[cnt++], Settings.timbreFactor * s.getTimbre()[10]);
        inst.setValue(attlist[cnt++], Settings.timbreFactor * s.getTimbre()[11]);
        inst.setValue(attlist[cnt++], s.getPitches()[0] * Settings.pitchFactor);
        inst.setValue(attlist[cnt++], s.getPitches()[1] * Settings.pitchFactor);
        inst.setValue(attlist[cnt++], s.getPitches()[2] * Settings.pitchFactor);
        inst.setValue(attlist[cnt++], s.getPitches()[3] * Settings.pitchFactor);
        inst.setValue(attlist[cnt++], s.getPitches()[4] * Settings.pitchFactor);
        inst.setValue(attlist[cnt++], s.getPitches()[5] * Settings.pitchFactor);
        inst.setValue(attlist[cnt++], s.getPitches()[6] * Settings.pitchFactor);
        inst.setValue(attlist[cnt++], s.getPitches()[7] * Settings.pitchFactor);
        inst.setValue(attlist[cnt++], s.getPitches()[8] * Settings.pitchFactor);
        inst.setValue(attlist[cnt++], s.getPitches()[9] * Settings.pitchFactor);
        inst.setValue(attlist[cnt++], s.getPitches()[10] * Settings.pitchFactor);
        inst.setValue(attlist[cnt++], s.getPitches()[11] * Settings.pitchFactor);
        return inst;
    }


    public static void main(String[] args) {
        new Custom();
    }
}
