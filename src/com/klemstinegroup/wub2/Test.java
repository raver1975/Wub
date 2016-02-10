package com.klemstinegroup.wub2;

import java.io.File;

import com.echonest.api.v4.Segment;
import com.klemstinegroup.wub.AudioObject;
import com.klemstinegroup.wub.Interval;

import weka.clusterers.SimpleKMeans;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

public class Test {

	public static void main(String[] args) {
		new Thread(new Runnable() {
			public void run() {
				File file = new File(
						"C:\\Users\\Paul\\Documents\\Vuze Downloads\\Zomboy – The Oubreak (2014) [LEAKED 320] [DUBSTEP, BROSTEP] [EDM RG]\\05. Skull 'n' Bones.mp3");
				AudioObject ao = AudioObject.factory(file);
				SimpleKMeans kmeans = new SimpleKMeans();
				kmeans.setSeed(10);
				FastVector attrs = new FastVector();
				Attribute timbre1 = new Attribute("timbre1");
				Attribute timbre2 = new Attribute("timbre2");
				Attribute timbre3 = new Attribute("timbre3");
				Attribute timbre4 = new Attribute("timbre4");
				Attribute timbre5 = new Attribute("timbre5");
				Attribute timbre6 = new Attribute("timbre6");
				Attribute timbre7 = new Attribute("timbre7");
				Attribute timbre8 = new Attribute("timbre8");
				Attribute timbre9 = new Attribute("timbre9");
				Attribute timbre10 = new Attribute("timbre10");
				Attribute timbre11 = new Attribute("timbre11");
				Attribute timbre12 = new Attribute("timbre12");
				attrs.addElement(timbre1);
				attrs.addElement(timbre2);
				attrs.addElement(timbre3);
				attrs.addElement(timbre4);
				attrs.addElement(timbre5);
				attrs.addElement(timbre6);
				attrs.addElement(timbre7);
				attrs.addElement(timbre8);
				attrs.addElement(timbre9);
				attrs.addElement(timbre10);
				attrs.addElement(timbre11);
				attrs.addElement(timbre12);
				Instances dataset = new Instances("my_dataset", attrs, 0);
				try {
					kmeans.setNumClusters(2000);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				for (Segment s : ao.analysis.getSegments()) {
					Instance inst = new Instance(12);
					inst.setValue(timbre1, s.getTimbre()[0]);
					inst.setValue(timbre2, s.getTimbre()[1]);
					inst.setValue(timbre3, s.getTimbre()[2]);
					inst.setValue(timbre4, s.getTimbre()[3]);
					inst.setValue(timbre5, s.getTimbre()[4]);
					inst.setValue(timbre6, s.getTimbre()[5]);
					inst.setValue(timbre7, s.getTimbre()[6]);
					inst.setValue(timbre8, s.getTimbre()[7]);
					inst.setValue(timbre9, s.getTimbre()[8]);
					inst.setValue(timbre10, s.getTimbre()[9]);
					inst.setValue(timbre11, s.getTimbre()[10]);
					inst.setValue(timbre12, s.getTimbre()[11]);
					inst.setDataset(dataset);
					dataset.add(inst);

					Interval i = new Interval(s, 0);
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
					System.out.println("Centroid " + (i + 1) + ": " + centroids.instance(i));
				}

				// get cluster membership for each instance
				for (int i = 0; i < dataset.numInstances(); i++) {
					try {
						System.out.println(dataset.instance(i) + " is in cluster "
								+ (kmeans.clusterInstance(dataset.instance(i)) + 1));
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

			}
		}).start();

	}

}
