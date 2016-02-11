package com.klemstinegroup.wub2;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.klemstinegroup.wub.AudioObject;
import com.klemstinegroup.wub.CentralCommand;

public class DirectoryConvert {

	public static void main(String[] args) throws Exception {
		while (true) {
			File file = new File("Q:");
			Files.walk(Paths.get(file.getPath())).filter(Files::isRegularFile).forEach(DirectoryConvert::process);
		}

	}

	public static void process(Path path) {
		if (path.toString().toLowerCase().endsWith(".mp3")) {
			System.out.println("Converting:"+path);
			AudioObject au = AudioObject.factory(path.toFile());
			CentralCommand.remove(au);
			au.mc.frame.dispose();
			CentralCommand.ccn.nodes.clear();
			boolean del=path.toFile().delete();
			System.out.println("Deleted?"+del+"\t"+path);
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
