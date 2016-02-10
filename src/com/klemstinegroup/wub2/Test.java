package com.klemstinegroup.wub2;

import java.io.File;

import com.echonest.api.v4.Segment;
import com.klemstinegroup.wub.AudioObject;
import com.klemstinegroup.wub.Interval;

public class Test {

	public static void main(String[] args) {
		new Thread(new Runnable(){
			public void run(){
				File file=new File("C:\\Users\\Paul\\Documents\\Vuze Downloads\\Zomboy – The Oubreak (2014) [LEAKED 320] [DUBSTEP, BROSTEP] [EDM RG]\\05. Skull 'n' Bones.mp3");
					AudioObject ao = AudioObject.factory(file);
					long bb=0;
					for (Segment s:ao.analysis.getSegments()){
						Interval i=new Interval(s,0);
						System.out.println(i.lengthBytes);
						bb+=i.lengthBytes;
					}
					System.out.println(bb+"="+ao.data.length);
			}
		}).start();
		
	}

}
