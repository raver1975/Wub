package com.klemstinegroup.wub2.test;

import java.util.HashMap;

import com.echonest.api.v4.Segment;

import com.klemstinegroup.wub2.system.Audio;
import com.klemstinegroup.wub2.system.Song;
import weka.clusterers.SimpleKMeans;
import weka.core.*;

public class Test {

    static final int attLength = 28;

    //933  good bassy dubstep
    //2441 Crissy Criss & Youngman - Kick Snare
    //713 kathy's song
    //2088 bassnectar mesmerizing the ultra
    //1670 covenant like tears in rain
    //2344 saw wave bassicles

    static final int songNum = 1670;

    static final int numClusters = 255;
    static final int start= 200;




    public static void main(String[] args) {

        SongManager.process();

        new Thread(new Runnable() {
            public Attribute[] attlist;

            public void run() {
                Audio audio = new Audio();
                SimpleKMeans kmeans = new SimpleKMeans();
                kmeans.setSeed(10);
                FastVector attrs = new FastVector();
                attlist = new Attribute[attLength];
                for (int i = 0; i < attLength; i++) {
                    attlist[i] = new Attribute("at" + i);
                    attrs.addElement(attlist[i]);
                }
                Instances dataset = new Instances("my_dataset", attrs, 0);
                try {
                    kmeans.setNumClusters(numClusters);
                    kmeans.setDistanceFunction(new ManhattanDistance());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                HashMap<Instance, Segment> hm = new HashMap<>();
                HashMap<Integer, Segment> hrm = new HashMap<>();
                Segment[] lastSeen = new Segment[numClusters];
                Song song = SongManager.getRandom(songNum);

                int cnt=0;
                for (Segment s : song.analysis.getSegments()) {
                    Instance inst = getInstance(attlist, s);
                    hm.put(inst, s);
                    inst.setDataset(dataset);
                    dataset.add(inst);
                }


                try {
                    kmeans.buildClusterer(dataset);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // print out the cluster centroids
                Instances centroids = kmeans.getClusterCentroids();
                for (int i = 0; i < centroids.numInstances(); i++) {
                    double dist = Float.MAX_VALUE;
                    int best = -1;
                    for (int j = 0; j < dataset.numInstances(); j++) {
                        double dd = distance(centroids.instance(i), dataset.instance(j));
                        if (dd < dist) {
                            dist = dd;
                            best = j;
                        }
                    }
                    lastSeen[i] = song.analysis.getSegments().get(best);

                }

                // get cluster membership for each instance
                for (int i = 0; i < dataset.numInstances(); i++) {
                    try {
                        hrm.put(i, lastSeen[kmeans.clusterInstance(dataset.instance(i))]);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

//



                for (Segment s : song.analysis.getSegments()) {
                    if (cnt<start){
                        cnt++;
                        continue;
                    }
                    Segment search = hrm.get(cnt);
                    if (search!=null)
                    audio.play(song.getAudioInterval(search));
//                    only use each one once
//                    lastSeen[search].remove(ran);
//
                    //or
//                    lastSeen[search].clear();
//                    lastSeen[search].add(new Integer(ran));

                    cnt++;
                }
                String meta = song.analysis.toString();
                meta = meta.substring(0, 400);
                meta = meta.replaceAll("(.{100})", "$1\n");
                System.out.println(meta);

                System.out.println("segments size="+song.analysis.getSegments().size());

                //--------------------------------------------------------------------------------------------------------------
            }

            private double distance(Instance i1, Instance i2) {
                double tot = 0;
                for (int i = 0; i < attLength; i++) {
                    double ta = i1.value(attlist[i]) - i2.value(attlist[i]);
                    ta = Math.abs(ta);
                    tot += ta;
                }
                return tot;
            }

            float pitchFactor = 2f;
            float timbreFactor = 2f;
            float loudFactor = 2f;
            float durationFactor = 4f;

            private Instance getInstance(Attribute[] attlist, Segment s) {

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
        }).start();

    }

}
