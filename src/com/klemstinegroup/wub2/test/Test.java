package com.klemstinegroup.wub2.test;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import com.echonest.api.v4.EchoNestException;
import com.echonest.api.v4.Segment;
import com.klemstinegroup.wub.Interval;

import com.klemstinegroup.wub2.system.Audio;
import com.klemstinegroup.wub2.system.Song;
import sun.plugin.dom.core.Attr;
import weka.clusterers.SimpleKMeans;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

public class Test {
static final int songNum=-1;
    static final int numClusters = 40;
    static final int attLength = 28;

    public static void main(String[] args) {
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
                } catch (Exception e) {
                    e.printStackTrace();
                }
                HashMap<Instance, Segment> hm = new HashMap<>();
                HashMap<Integer, Integer> hrm = new HashMap<>();
                ArrayList<Integer>[] lastSeen = new ArrayList[numClusters];
                Song song = SongManager.getRandom(songNum);

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

//                // print out the cluster centroids
//                Instances centroids = kmeans.getClusterCentroids();
//                for (int i = 0; i < centroids.numInstances(); i++) {
//                    System.out.println("Centroid " + (i + 1) + ": " + centroids.instance(i));
//                }

                // get cluster membership for each instance
                for (int i = 0; i < dataset.numInstances(); i++) {
                    try {
                        hrm.put(i, kmeans.clusterInstance(dataset.instance(i)));
                        int index = kmeans.clusterInstance(dataset.instance(i));
                        if (lastSeen[index] == null) {
                            lastSeen[index] = new ArrayList<Integer>();
                        }
//                        lastSeen[=i;
                        lastSeen[index].add(i);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                int cnt = 0;
                for (Segment s : song.analysis.getSegments()) {
                        int search = hrm.get(cnt);
                        //System.out.println("playing:" + lastSeen[search]);

                        //get random from same bin
                        ArrayList<Integer> bbb = lastSeen[search];
                        int ran = (int) (Math.random() * bbb.size());

                        audio.play(song.getAudioInterval(song.analysis.getSegments().get(bbb.get(ran))));
                    //only use each one once
                        lastSeen[search].remove(ran);
//
                    //or
//                    lastSeen[search].clear();
//                    lastSeen[search].add(ran);

                    cnt++;
                }
                String meta = song.analysis.toString();
                meta = meta.substring(0, 400);
                meta = meta.replaceAll("(.{100})", "$1\n");
                System.out.println(meta);
            }

            float pitchFactor=10f;
            float timbreFactor=10f;

            private Instance getInstance(Attribute[] attlist, Segment s) {

                int cnt = 0;
                Instance inst = new Instance(attLength);
                inst.setValue(attlist[cnt++], s.getDuration() * 1000f);
                inst.setValue(attlist[cnt++], s.getLoudnessMax() * 100f);
                inst.setValue(attlist[cnt++], s.getLoudnessStart() * 100f);
                inst.setValue(attlist[cnt++], s.getLoudnessMaxTime() * 100f);
                inst.setValue(attlist[cnt++], timbreFactor*s.getTimbre()[0]);
                inst.setValue(attlist[cnt++], timbreFactor*s.getTimbre()[1]);
                inst.setValue(attlist[cnt++], timbreFactor*s.getTimbre()[2]);
                inst.setValue(attlist[cnt++], timbreFactor*s.getTimbre()[3]);
                inst.setValue(attlist[cnt++], timbreFactor*s.getTimbre()[4]);
                inst.setValue(attlist[cnt++], timbreFactor*s.getTimbre()[5]);
                inst.setValue(attlist[cnt++], timbreFactor*s.getTimbre()[6]);
                inst.setValue(attlist[cnt++], timbreFactor*s.getTimbre()[7]);
                inst.setValue(attlist[cnt++], timbreFactor*s.getTimbre()[8]);
                inst.setValue(attlist[cnt++], timbreFactor*s.getTimbre()[9]);
                inst.setValue(attlist[cnt++], timbreFactor*s.getTimbre()[10]);
                inst.setValue(attlist[cnt++], timbreFactor*s.getTimbre()[11]);
                inst.setValue(attlist[cnt++], s.getPitches()[0]*pitchFactor);
                inst.setValue(attlist[cnt++], s.getPitches()[1]*pitchFactor);
                inst.setValue(attlist[cnt++], s.getPitches()[2]*pitchFactor);
                inst.setValue(attlist[cnt++], s.getPitches()[3]*pitchFactor);
                inst.setValue(attlist[cnt++], s.getPitches()[4]*pitchFactor);
                inst.setValue(attlist[cnt++], s.getPitches()[5]*pitchFactor);
                inst.setValue(attlist[cnt++], s.getPitches()[6]*pitchFactor);
                inst.setValue(attlist[cnt++], s.getPitches()[7]*pitchFactor);
                inst.setValue(attlist[cnt++], s.getPitches()[8]*pitchFactor);
                inst.setValue(attlist[cnt++], s.getPitches()[9]*pitchFactor);
                inst.setValue(attlist[cnt++], s.getPitches()[10]*pitchFactor);
                inst.setValue(attlist[cnt++], s.getPitches()[11]*pitchFactor);
                return inst;
            }
        }).start();

    }

}
