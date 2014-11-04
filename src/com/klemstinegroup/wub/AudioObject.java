package com.klemstinegroup.wub;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
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
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileNameExtensionFilter;

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
	transient Interval currentlyPlaying;
	protected transient boolean breakPlay;

	public static final int resolution = 16;
	public static final int channels = 2;
	public static final int frameSize = channels * resolution / 8;
	public static final int sampleRate = 44100;
	public static final AudioFormat audioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, sampleRate, resolution, channels, frameSize, sampleRate, false);
	static final int bufferSize = 8192;

	public transient boolean pause = false;
	public transient boolean loop = false;

	public AudioObject(String file) {
		this(new File(file));
	}

	public static AudioObject factory() {
		JFileChooser chooser = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Audio", "mp3", "wav", "wub");
		chooser.setFileFilter(filter);
		int returnVal = chooser.showOpenDialog(new JFrame());
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			// System.out.println("You chose to open this file: " +
			return factory(chooser.getSelectedFile());
		}
		return null;
	}

	public static AudioObject factory(String file) {
		return factory(new File(file));
	}

	public static AudioObject factory(File file) {
		File newFile = file;
		String fileName = file.getName();
		String extension = "";
		String filePrefix = "";
		int i = fileName.lastIndexOf('.');
		if (i > 0) {
			extension = fileName.substring(i + 1);
			filePrefix = fileName.substring(0, i);
		}
		if (!extension.equals("wub")) {
			newFile = new File(file.getParent() + File.separator + filePrefix + ".wub");
			System.out.println(newFile.getAbsolutePath());
		}
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

	public AudioObject(final File file) {

		this.file = file;
		convert(file);
		JTextArea msgLabel;
		JProgressBar progressBar;
		final int MAXIMUM = 100;
		JPanel panel;

		progressBar = new JProgressBar(0, MAXIMUM);
		progressBar.setIndeterminate(true);
		msgLabel = new JTextArea(file.getName());
		msgLabel.setEditable(false);

		panel = new JPanel(new BorderLayout(5, 5));
		panel.add(msgLabel, BorderLayout.PAGE_START);
		panel.add(progressBar, BorderLayout.CENTER);
		panel.setBorder(BorderFactory.createEmptyBorder(11, 11, 11, 11));

		final JDialog dialog = new JDialog();
		dialog.setTitle("Analyzing audio...");
		dialog.getContentPane().add(panel);
		dialog.setResizable(false);
		dialog.pack();
		dialog.setSize(500, dialog.getHeight());
		dialog.setLocationRelativeTo(null);
		dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		dialog.setAlwaysOnTop(false);
		dialog.setVisible(true);
		msgLabel.setBackground(panel.getBackground());
		analysis = echoNest(file);
		init();
		dialog.dispose();
	}

	private void init() {
		queue = new LinkedList<Interval>();
		mc = new MusicCanvas(this);
		startPlaying();
	}

	private void startPlaying() {
		line = getLine();
		new Thread(new Runnable() {
			public void run() {
				top: while (true) {

					// System.out.println(queue.size());
					if (!queue.isEmpty()) {
						Interval i = queue.poll();
						
						currentlyPlaying = i;
						int j = 0;
						for (j = i.startBytes; j <= i.endBytes - bufferSize; j += bufferSize) {
							while (pause || breakPlay) {
								if (breakPlay) {
									breakPlay = false;
//									if (loop)
//										queue.add(i);
//									queue.clear();
									continue top;
								}
								try {
									Thread.sleep(10);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
							}
							position = j;
							line.write(data, j, bufferSize);

						}

						if (j < i.endBytes) {
							position = j;
							line.write(data, j, i.endBytes - j);
							// line.drain();
						}
						if (loop)
							queue.add(i);
					} else
						try {
							currentlyPlaying = null;
							mc.tempTimedEvent = null;
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

	public static TrackAnalysis echoNest(File file) {
		try {
			EchoNestAPI en = new EchoNestAPI();
			Track track = en.uploadTrack(file);
			System.out.println(track);
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
			System.out.println(soundFile.getAbsolutePath());
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
