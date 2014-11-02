package com.klemstinegroup.wub;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JFrame;

import com.echonest.api.v4.EchoNestAPI;
import com.echonest.api.v4.Track;
import com.echonest.api.v4.TrackAnalysis;

//-Xbootclasspath/p:./libs/tritonus_share.jar;./libs/tritonus_aos-0.3.6.jar;./libs/tritonus_remaining-0.3.6.jar

public class AudioObject {

	public byte[] data;
	public File file;
	TrackAnalysis analysis;
	// Player player = new Player();
	MusicCanvas mc;
	public SourceDataLine line;
	// PipedInputStream input;
	// PipedOutputStream output;
	Queue<Interval> queue = new LinkedList<Interval>();
	int position = 0;

	static final int resolution = 16;
	static final int channels = 2;
	static final int frameSize = channels * resolution / 8;
	static final int sampleRate = 44100;
	static final AudioFormat audioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, sampleRate, resolution, channels, frameSize, sampleRate, false);
	static final int bufferSize=4096;

	public AudioObject(String file) {
		this(new File(file));
	}

	public AudioObject(File file) {
		this.file = file;
		convert(file);
		analysis = echoNest(file);
		makeCanvas();
		startPlaying();
	}

	private void startPlaying() {
		line = getLine();
		new Thread(new Runnable() {
			public void run() {
				System.out.println("Thread");
				while (true) {
					// System.out.println(queue.size());
					if (!queue.isEmpty()) {
						Interval i = queue.poll();
						System.out.println(i);
						int j = 0;
						for (j = i.min; j <= i.max-bufferSize; j += bufferSize) {
							line.write(data, j, bufferSize);
							position = j;
							// System.out.println(j);
						}
						if (j < i.max)
							line.write(data, j, i.max - j);

					} else
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					// if (!off)line.write(data, 0, read);
				}
			}
		}).start();
	}

	private void makeCanvas() {
		JFrame frame = new JFrame(getFileName());
		mc = new MusicCanvas(this);
		// player.setCanvas(mc);
		frame.getContentPane().add(mc);
		frame.setBounds(200, 200, 1500, 350);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.show();
		frame.validate();
		frame.repaint();

	}

	public static TrackAnalysis echoNest(File file) {
		try {
			EchoNestAPI en = new EchoNestAPI();
			Track track = en.uploadTrack(file);
			track.waitForAnalysis(30000);
			if (track.getStatus() == Track.AnalysisStatus.COMPLETE) {
				return track.getAnalysis();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public void convert(File soundFile) {
		AudioInputStream mp3InputStream = null;
		try {
			mp3InputStream = AudioSystem.getAudioInputStream(soundFile);
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		File temp = new File("temp.wav");
		mp3InputStream = AudioSystem.getAudioInputStream(new AudioFormat(mp3InputStream.getFormat().getSampleRate(), resolution, AudioObject.channels, true, false), mp3InputStream);
		try {
			AudioSystem.write(mp3InputStream, AudioFileFormat.Type.WAVE, temp);
		} catch (IOException e) {
			e.printStackTrace();
		}
		// try {
		// data = Files.readAllBytes(temp.toPath());
		try {
			mp3InputStream = AudioSystem.getAudioInputStream(temp);
		} catch (UnsupportedAudioFileException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		mp3InputStream = AudioSystem.getAudioInputStream(AudioObject.audioFormat, mp3InputStream);

		ByteArrayOutputStream bo = new ByteArrayOutputStream();
		try {
			AudioSystem.write(mp3InputStream, AudioFileFormat.Type.WAVE, bo);
		} catch (IOException e) {
			e.printStackTrace();
		}
		data = bo.toByteArray();

		try {
			mp3InputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		temp.delete();
	}

	public String getFileName() {
		return file.getName();
	}

	public SourceDataLine getLine() {
		SourceDataLine res = null;
		DataLine.Info info = new DataLine.Info(SourceDataLine.class, AudioObject.audioFormat);
		try {
			res = (SourceDataLine) AudioSystem.getLine(info);
			res.open(AudioObject.audioFormat);
			res.start();
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		}
		return res;
	}

	public void play(double start, double duration) {
		int startInBytes = (int) (start * AudioObject.sampleRate * AudioObject.frameSize) - (int) (start * AudioObject.sampleRate * AudioObject.frameSize) % AudioObject.frameSize;
		double lengthInFrames = duration * AudioObject.sampleRate;
		int lengthInBytes = (int) (lengthInFrames * AudioObject.frameSize) - (int) (lengthInFrames * AudioObject.frameSize) % AudioObject.frameSize;
		queue.add(new Interval(startInBytes, Math.min(startInBytes + lengthInBytes, data.length)));
		System.out.println(data.length);

	}
}
