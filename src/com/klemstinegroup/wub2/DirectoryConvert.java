package com.klemstinegroup.wub2;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.klemstinegroup.wub.AudioObject;
import com.klemstinegroup.wub.CentralCommand;

import sun.tools.jar.Main;

public class DirectoryConvert {

	public DirectoryConvert() {
		File file = new File("Q:");
		try {
			Files.walk(Paths.get(file.getPath())).filter(Files::isRegularFile).forEach(DirectoryConvert::process);

		} catch (IOException e)

		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args1) throws Exception {
		new DirectoryConvert();

	}

	public static void restart() throws IOException {
		StringBuilder cmd = new StringBuilder();
		cmd.append(System.getProperty("java.home") + File.separator + "bin" + File.separator + "java ");
		for (String jvmArg : ManagementFactory.getRuntimeMXBean().getInputArguments()) {
			cmd.append(jvmArg + " ");
		}
		cmd.append("-jar ").append(ManagementFactory.getRuntimeMXBean().getClassPath()).append(" ");
		cmd.append(DirectoryConvert.class.getName()).append(" ");
		// for (String arg : args) {
		// cmd.append(arg).append(" ");
		// }
		System.out.println(cmd);
		Runtime.getRuntime().exec(cmd.toString());
		System.exit(0);
	}

	public static void process(Path path) {
		if (path.toString().toLowerCase().endsWith(".mp3")) {
			System.out.println("Converting:" + path);
			AudioObject au = AudioObject.factory(path.toFile());
			CentralCommand.remove(au);
			au.mc.frame.dispose();
			CentralCommand.ccn.nodes.clear();
			boolean del = path.toFile().delete();
			System.out.println("Deleted?" + del + "\t" + path);

			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			try {
				restart();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}
}
