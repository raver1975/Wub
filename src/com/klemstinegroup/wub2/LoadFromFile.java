package com.klemstinegroup.wub2;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import com.echonest.api.v4.TrackAnalysis;

public class LoadFromFile {
	
	public static Song loadSong(String file){
		byte[] data;
		TrackAnalysis ta;
		if (file.endsWith(".au")){
			data=loadData(file);
			ta=loadAnalysis(file.replace(".au", ".an"));
			return new Song(data,ta);
		}
		if (file.endsWith(".an")){
			data=loadData(file.replace(".an", ".au"));
			ta=loadAnalysis(file);
			return new Song(data,ta);
		}
		return null;
	}

	public static byte[] loadData(String file) {
		Path path = new File(file).toPath();
		try {
			byte[] data = Files.readAllBytes(path);
			return data;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static TrackAnalysis loadAnalysis(String file) {
		Path path = new File(file).toPath();
		try {
			byte[] data = Files.readAllBytes(path);
			ByteArrayInputStream bas = new ByteArrayInputStream(data);
			ObjectInputStream in = new ObjectInputStream(bas);
			return (TrackAnalysis) in.readObject();
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

}
