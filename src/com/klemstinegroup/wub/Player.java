package com.klemstinegroup.wub;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

public class Player {
	boolean isRunning = true;
	SourceDataLine line;
	PipedInputStream input;
	PipedOutputStream output;
	static final int sampleRate = 44100;
	static final int channels = 2;
	static final int resolution = 16;
	static final int frameSize = channels * resolution/8;
	static final AudioFormat audioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, sampleRate,  resolution, channels, frameSize, sampleRate, false);
	boolean off=false;

	public Player() {
		output = new PipedOutputStream();
		try {
			input = new PipedInputStream(output,10000000);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		line = getLine();
		new Thread(new Runnable() {
			public void run() {
				byte[] data = new byte[4096];
				int read = 0;

				while (isRunning) {
					try {
						read = input.read(data);
						if (!off)line.write(data, 0, read);
					} catch (IOException e) {
					}
				}
			}
		}).start();
	}

	public void play(AudioObject ai, double d, double e) {

		int startInBytes = (int)(d * sampleRate * frameSize)-( (int)(d * sampleRate * frameSize))%4;
		double lengthInFrames = (e* sampleRate);
		int lengthInBytes = (int) (lengthInFrames * frameSize) - (((int) (lengthInFrames * frameSize)) % frameSize);
		System.out.println(startInBytes+"\t"+lengthInBytes);
		if (line != null) {
			try {
				output.write(ai.data, startInBytes, lengthInBytes);
			} catch (IOException e1) {
				e1.printStackTrace();
				System.exit(0);
			}
		}

	}
	
	public void stop(){
		off=true;
	}

	public void start(){
		off=false;
	}
	
	public SourceDataLine getLine() {
		SourceDataLine res = null;
		DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
		try {
			res = (SourceDataLine) AudioSystem.getLine(info);
			res.open(audioFormat);
			res.start();
			System.out.println(res.isOpen());
			System.out.println(res.isRunning());
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		}
		return res;
	}
}
