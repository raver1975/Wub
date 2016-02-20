package com.klemstinegroup.wub2;

import java.util.Arrays;
import java.util.List;

import com.echonest.api.v4.TimedEvent;

public class AudioData {

	//TimedEvent te;
	//public int startBytes;
	//public int lengthBytes;
	public byte[] data;
	//public int endBytes;
//	int newbytestart;

	public AudioData(TimedEvent te, byte[] fullData) {
		double start1 = te.getStart();
		double duration = te.getDuration();
		int startBytes = (int) (start1 * Audio.sampleRate * Audio.frameSize) - (int) (start1 * Audio.sampleRate * Audio.frameSize) % Audio.frameSize;
		double lengthInFrames = duration * Audio.sampleRate;
		int lengthBytes = (int) (lengthInFrames * Audio.frameSize) - (int) (lengthInFrames * Audio.frameSize) % Audio.frameSize;
		//int endBytes = startBytes + lengthBytes;
		data=new byte[lengthBytes];
		
		if (startBytes+lengthBytes>fullData.length)lengthBytes=fullData.length-startBytes;
		System.arraycopy(fullData, startBytes, data, 0, lengthBytes);
		//System.out.println((startBytes+lengthBytes)+"\t"+fullData.length+"\t"+data.length+"\t"+lengthBytes);
		//this.te = te;
		
	}

	public AudioData(List<TimedEvent> list, byte[] fullData) {
		double start1 = list.get(0).getStart();
		double duration=0;
		for (TimedEvent t:list)duration+=t.duration;
		//double duration = te.getDuration();
		int startBytes = (int) (start1 * Audio.sampleRate * Audio.frameSize) - (int) (start1 * Audio.sampleRate * Audio.frameSize) % Audio.frameSize;
		double lengthInFrames = duration * Audio.sampleRate;
		int lengthBytes = (int) (lengthInFrames * Audio.frameSize) - (int) (lengthInFrames * Audio.frameSize) % Audio.frameSize;
		//int endBytes = startBytes + lengthBytes;
		data=new byte[lengthBytes];

		if (startBytes+lengthBytes>fullData.length)lengthBytes=fullData.length-startBytes;
		System.arraycopy(fullData, startBytes, data, 0, lengthBytes);
	}

//	public String toString() {
//		return startBytes + ":" + lengthBytes;
//	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(data);
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof AudioData))
			return false;
		AudioData i = (AudioData) o;
		return this.hashCode()==i.hashCode()&&data.length==i.data.length;
	}
}
