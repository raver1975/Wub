package com.klemstinegroup.wub;

import java.awt.Dimension;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
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
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;

import com.echonest.api.v4.EchoNestAPI;
import com.echonest.api.v4.TimedEvent;
import com.echonest.api.v4.Track;
import com.echonest.api.v4.TrackAnalysis;

public class AudioObject implements Serializable {

	public byte[] data;
	public File file;
	TrackAnalysis analysis;
	public transient MusicCanvas mc;
	public transient SourceDataLine line;
	public transient Queue<Interval> queue;

	transient int position = 0;
	transient Interval positionInterval;

	public static final int resolution = 16;
	public static final int channels = 2;
	public static final int frameSize = channels * resolution / 8;
	public static final int sampleRate = 44100;
	public static final AudioFormat audioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, sampleRate, resolution, channels, frameSize, sampleRate, false);
	static final int bufferSize = 8192;

	public AudioObject(String file) {
		this(new File(file));
	}

	public static AudioObject factory(String file) {
		return factory(new File(file));
	}

	public static AudioObject factory(File file) {
		File newFile = new File(file.getAbsolutePath() + ".save");
		if (newFile.exists()) {
			try {
				AudioObject au = (AudioObject) Serializer.load(newFile);
				au.init();
				return au;
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		AudioObject au = new AudioObject(file);
		try {
			Serializer.store(au, newFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return au;
	}

	public AudioObject(File file) {

		this.file = file;
		convert(file);
		analysis = echoNest(file);
		init();
	}

	private void init() {
		queue = new LinkedList<Interval>();
		makeCanvas();
		startPlaying();
	}

	private void startPlaying() {
		line = getLine();
		new Thread(new Runnable() {
			public void run() {
				while (true) {
					// System.out.println(queue.size());
					if (!queue.isEmpty()) {
						Interval i = queue.poll();
						positionInterval = i;
						int j = 0;
						for (j = i.startBytes; j <= i.endBytes - bufferSize; j += bufferSize) {
							position = j;
							line.write(data, j, bufferSize);

						}

						if (j < i.endBytes) {
							position = j;
							line.write(data, j, i.endBytes - j);
							// line.drain();
						}
					} else
						try {
							positionInterval=null;
							mc.tempTimedEvent=null;
							line.drain();
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
		mc.setSize(new Dimension(4500, 440));
		final JScrollPane js = new JScrollPane(mc);
		js.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
		frame.getContentPane().add(js, "Center");
		JScrollBar jbar = new JScrollBar(JScrollBar.VERTICAL);
		jbar.setMinimum(1);
		jbar.setMaximum(1000);
		jbar.setValue(100);
		jbar.addAdjustmentListener(new AdjustmentListener() {
			public void adjustmentValueChanged(AdjustmentEvent ae) {
				if (ae.getValueIsAdjusting())
					return;
				mc.setSize(50 * ae.getValue(), mc.getHeight());
				mc.makeImage();
			}
		});

		frame.getContentPane().add(jbar, "East");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setBounds(100, 100, 800, 500);
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

	public void play(TimedEvent te, int y) {
		queue.add(new Interval(te, y));
	}

	// public void play(double start, double duration) {
	// int startInBytes = (int) (start * AudioObject.sampleRate *
	// AudioObject.frameSize) - (int) (start * AudioObject.sampleRate *
	// AudioObject.frameSize) % AudioObject.frameSize;
	// double lengthInFrames = duration * AudioObject.sampleRate;
	// int lengthInBytes = (int) (lengthInFrames * AudioObject.frameSize) -
	// (int) (lengthInFrames * AudioObject.frameSize) % AudioObject.frameSize;
	// queue.add(new Interval(startInBytes, Math.min(startInBytes +
	// lengthInBytes, data.length)));
	//
	// }
}
