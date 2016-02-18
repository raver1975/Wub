package com.klemstinegroup.wub2;

import java.util.Random;

import com.echonest.api.v4.Segment;
import com.echonest.api.v4.TimedEvent;
import com.klemstinegroup.wub.Interval;

import weka.clusterers.SimpleKMeans;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

public class PlaySomeMusic2 {

	public PlaySomeMusic2() {
		Song song1 = LoadFromFile.loadSong("q:\\5doprumaifi4p93b8qs7p0pari.an");
		//Song song2 = LoadFromFile.loadSong("q:\\7mgcakup2rr54vt3oqv1bp882s.an");
		Audio audio = new Audio();
		//audio.play(song.getAudioInterval(song.analysis.getSections().get(13)));
//		for (int i = 0; i < song.analysis.getSections().size(); i++) {
//			audio.play(song.getAudioInterval(song.analysis.getSections().get(i)));
//		}
		Random rand=new Random();
		int n1=song1.analysis.getTatums().size();
		//int n2=song2.analysis.getTatums().size();
		for (int i=0;i<800;i+=4){
			audio.play(song1.getAudioInterval(song1.analysis.getTatums().get(i)));
			//audio.play(song1.getAudioInterval(song1.analysis.getTatums().get(i+1)));
			audio.play(song1.getAudioInterval(song1.analysis.getTatums().get(i+2)));
			//audio.play(song1.getAudioInterval(song1.analysis.getTatums().get(i+3)));
			//else audio.play(song2.getAudioInterval(song2.analysis.getBars().get(rand.nextInt(n2))));
		}
		
		SimpleKMeans kmeans = new SimpleKMeans();
		kmeans.setSeed(10);
		FastVector attrs = new FastVector();
		Attribute size = new Attribute("size");
		attrs.addElement(size);
		Instances dataset = new Instances("my_dataset", attrs, 0);
		
		try {
			kmeans.setNumClusters(1);
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
			System.out.println("Centroid " + (i + 1) + ": " + centroids.instance(i)+"\t"+centroids.instance(i).value(size));
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

	public static void main(String[] args) {
		new PlaySomeMusic2();

	}

}
