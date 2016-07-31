package com.klemstinegroup.wub2.test;

import java.util.ArrayList;
import java.util.List;

import com.echonest.api.v4.Segment;
import com.echonest.api.v4.TimedEvent;

import be.tarsos.lsh.Vector;
import be.tarsos.lsh.families.EuclidianHashFamily;
import be.tarsos.lsh.LSH;
import com.klemstinegroup.wub2.system.Audio;
import com.klemstinegroup.wub2.system.LoadFromFile;
import com.klemstinegroup.wub2.system.Song;
import weka.clusterers.SimpleKMeans;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

public class PlaySomeMusic2 {

    public PlaySomeMusic2() {
        Audio audio = new Audio();

        // Song song1 =
        // LoadFromFile.loadSong("i:\\wub\\5doprumaifi4p93b8qs7p0pari.an");
//		Song song1 = LoadFromFile.loadSong("e:\\wub\\c906k5i2co4d8om7qiegdaoh3d.an");
//		Song song2 = LoadFromFile.loadSong("e:\\wub\\9ulos27ngac2hegsjot4971al5.an");


        Song song1 = SongManager.getRandom();
        Song song2 = SongManager.getRandom();

        // Song song2 =
        // LoadFromFile.loadSong("q:\\7mgcakup2rr54vt3oqv1bp882s.an");

        // audio.play(song.getAudioInterval(song.analysis.getSections().get(13)));
        // for (int i = 0; i < song.analysis.getSections().size(); i++) {
        // audio.play(song.getAudioInterval(song.analysis.getSections().get(i)));
        // }
        // Random rand = new Random();
        // int n1 = song1.analysis.getTatums().size();
        // int n2=song2.analysis.getTatums().size();
        // for (int i=0;i<800;i+=4){
        // audio.play(song1.getAudioInterval(song1.analysis.getTatums().get(i)));
        // //audio.play(song1.getAudioInterval(song1.analysis.getTatums().get(i+1)));
        // audio.play(song1.getAudioInterval(song1.analysis.getTatums().get(i+2)));
        // //audio.play(song1.getAudioInterval(song1.analysis.getTatums().get(i+3)));
        // //else
        // audio.play(song2.getAudioInterval(song2.analysis.getBars().get(rand.nextInt(n2))));
        // }

        // new AudioObject(song1.data, song1.analysis, new File("temp.mp3"));
        String meta = song1.analysis.toString();
        meta = meta.substring(0, 400);
        meta = meta.replaceAll("(.{100})", "$1\n");
        System.out.println(meta);
        System.out.println("time    =" + song1.analysis.getTimeSignature() + "/4");
        System.out.println("sections=" + song1.analysis.getSections().size());
        System.out.println("bars    =" + song1.analysis.getBars().size());
        System.out.println("beats   =" + song1.analysis.getBeats().size());
        System.out.println("tatums  =" + song1.analysis.getTatums().size());
        System.out.println("segments=" + song1.analysis.getSegments().size());

        meta = song2.analysis.toString();
        meta = meta.substring(0, 400);
        meta = meta.replaceAll("(.{100})", "$1\n");
        System.out.println(meta);
        System.out.println("time    =" + song2.analysis.getTimeSignature() + "/4");
        System.out.println("sections=" + song2.analysis.getSections().size());
        System.out.println("bars    =" + song2.analysis.getBars().size());
        System.out.println("beats   =" + song2.analysis.getBeats().size());
        System.out.println("tatums  =" + song2.analysis.getTatums().size());
        System.out.println("segments=" + song2.analysis.getSegments().size());

        ArrayList<Vector> segmentVectors = new ArrayList<>();
        for (int i = 0; i < song2.analysis.getSegments().size(); i += 1) {
            Segment s = song2.analysis.getSegments().get(i);
            Vector v = setVector(s);
            v.setKey(i + "");
            segmentVectors.add(v);

        }

        LSH lsh = new LSH(segmentVectors, new EuclidianHashFamily(1000, 28));
        lsh.buildIndex(4, 40);

        /*for (TimedEvent t : song1.analysis.getTatums()) {
            ArrayList<Integer> al = song1.getSegmentsPosition(t);
            if (al.size() > 0) {
                // System.out.println(al.size() + "\t" + al.get(0) + "\t" +
                // al.get(al.size() - 1) + "\t" + t.duration);
                for (int g : al) {

                    Segment s = song1.analysis.getSegments().get(g);
                    Vector v = setVector(s);
                    v.setKey(g + "");
                    List<Vector> list = lsh.query(v, 1);
                    if (list.size() > 0) {
                        int j = Integer.parseInt(list.get(0).getKey());
                        if (j < song2.analysis.getSegments().size()) {
                            //audio.play(song2.getAudioInterval(song2.analysis.getSegments().get(j)));
                            //audio.play(song2.getAudioInterval(song2.analysis.getSegments().get(j+1)));
                            System.out.println(g + "\t" + j);
                        }
                    } // System.out.println(song2.analysis.getSegments().get(j).duration);
                }
            }
            // try {
            // Thread.sleep(800);
            // } catch (InterruptedException e) {
            // // TODO Auto-generated catch block
            // e.printStackTrace();
            // }
        }
*/
        SimpleKMeans kmeans = new SimpleKMeans();
        kmeans.setSeed(10);
        FastVector attrs = new FastVector();
        Attribute size = new Attribute("size");
        attrs.addElement(size);
        Instances dataset = new Instances("my_dataset", attrs, 0);

        try {
            kmeans.setNumClusters(10000);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        for (TimedEvent s : song1.analysis.getBeats()) {
            Instance inst = new Instance(1);
            inst.setValue(size, s.getDuration());
            inst.setDataset(dataset);
            dataset.add(inst);
        }
        try {
            kmeans.buildClusterer(dataset);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // print out the cluster centroids
        Instances centroids = kmeans.getClusterCentroids();
        for (int i = 0; i < centroids.numInstances(); i++) {
            System.out.println(
                    "Centroid " + (i + 1) + ": " + centroids.instance(i) + "\t" + centroids.instance(i).value(size));
        }

        // get cluster membership for each instance
        for (int i = 0; i < dataset.numInstances(); i++) {
            try {
                System.out.println(
                        dataset.instance(i) + " is in cluster " + (kmeans.clusterInstance(dataset.instance(i)) + 1));
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }

    float[] vectorWeights = new float[]{1000f,  //length
            1f, //loudness start
            1f,//loadness max
            1f,//loudness max time
            1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, //timbre
            10f, 10f, 10f, 10f, 10f, 10f, 10f, 10f, 10f, 10f, 10f, 10f  //Pitches
    };

    private Vector setVector(Segment s) {
        if (vectorWeights.length != 28) {
            System.out.println(vectorWeights.length);
            System.exit(1);
        }
        Vector v = new Vector(28);
        v.set(0, s.getDuration() * vectorWeights[0]);
        v.set(1, s.getLoudnessStart() * vectorWeights[1]);
        v.set(2, s.getLoudnessMax() * vectorWeights[2]);
        v.set(3, s.getLoudnessMaxTime() * vectorWeights[3]);
        v.set(4, s.getTimbre()[0] * vectorWeights[4]);
        v.set(5, s.getTimbre()[1] * vectorWeights[5]);
        v.set(6, s.getTimbre()[2] * vectorWeights[6]);
        v.set(7, s.getTimbre()[3] * vectorWeights[7]);
        v.set(8, s.getTimbre()[4] * vectorWeights[8]);
        v.set(9, s.getTimbre()[5] * vectorWeights[9]);
        v.set(10, s.getTimbre()[6] * vectorWeights[10]);
        v.set(11, s.getTimbre()[7] * vectorWeights[11]);
        v.set(12, s.getTimbre()[8] * vectorWeights[12]);
        v.set(13, s.getTimbre()[9] * vectorWeights[13]);
        v.set(14, s.getTimbre()[10] * vectorWeights[14]);
        v.set(15, s.getTimbre()[11] * vectorWeights[15]);

        v.set(16, s.getPitches()[0] * vectorWeights[16]);
        v.set(17, s.getPitches()[1] * vectorWeights[17]);
        v.set(18, s.getPitches()[2] * vectorWeights[18]);
        v.set(19, s.getPitches()[3] * vectorWeights[19]);
        v.set(20, s.getPitches()[4] * vectorWeights[20]);
        v.set(21, s.getPitches()[5] * vectorWeights[21]);
        v.set(22, s.getPitches()[6] * vectorWeights[22]);
        v.set(23, s.getPitches()[7] * vectorWeights[23]);
        v.set(24, s.getPitches()[8] * vectorWeights[24]);
        v.set(25, s.getPitches()[9] * vectorWeights[25]);
        v.set(26, s.getPitches()[10] * vectorWeights[26]);
        v.set(27, s.getPitches()[11] * vectorWeights[27]);
        return v;
    }

    public static void main(String[] args) {
        new PlaySomeMusic2();

    }

}
