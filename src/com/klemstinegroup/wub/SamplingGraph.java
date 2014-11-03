package com.klemstinegroup.wub;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import javax.sound.sampled.AudioFormat;

import com.echonest.api.v4.Segment;

/**
 * Render a WaveForm.
 */
class SamplingGraph {
	static Vector<Line2D.Double> lines = new Vector<Line2D.Double>();
	static Color jfcBlue = new Color(255, 255, 0);
	static Color pink = new Color(255, 175, 175);

	public SamplingGraph() {
	}

	public static BufferedImage createWaveForm(List<Segment> segment,
			double duration, byte[] audioBytes, AudioFormat format, int w, int h) {
		lines.removeAllElements(); // clear the old vector

		int[] audioData = null;
		if (format.getSampleSizeInBits() == 16) {
			int nlengthInSamples = audioBytes.length / 2;
			audioData = new int[nlengthInSamples];
			if (format.isBigEndian()) {
				for (int i = 0; i < nlengthInSamples; i++) {
					/* First byte is MSB (high order) */
					int MSB = (int) audioBytes[2 * i];
					/* Second byte is LSB (low order) */
					int LSB = (int) audioBytes[2 * i + 1];
					audioData[i] = MSB << 8 | (255 & LSB);
				}
			} else {
				for (int i = 0; i < nlengthInSamples; i++) {
					/* First byte is LSB (low order) */
					int LSB = (int) audioBytes[2 * i];
					/* Second byte is MSB (high order) */
					int MSB = (int) audioBytes[2 * i + 1];
					audioData[i] = MSB << 8 | (255 & LSB);
				}
			}
		} else if (format.getSampleSizeInBits() == 8) {
			int nlengthInSamples = audioBytes.length;
			audioData = new int[nlengthInSamples];
			if (format.getEncoding().toString().startsWith("PCM_SIGN")) {
				for (int i = 0; i < audioBytes.length; i++) {
					audioData[i] = audioBytes[i];
				}
			} else {
				for (int i = 0; i < audioBytes.length; i++) {
					audioData[i] = audioBytes[i] - 128;
				}
			}
		}

		int frames_per_pixel = audioBytes.length / format.getFrameSize() / w;
		byte my_byte = 0;
		double y_last = 0;
		int numChannels = format.getChannels();
		for (double x = 0; x < w && audioData != null; x++) {
			int idx = (int) (frames_per_pixel * numChannels * x);
			byte min=Byte.MAX_VALUE;
			byte max=Byte.MIN_VALUE;
			for (int i=0;i<frames_per_pixel * numChannels;i++){
				if (format.getSampleSizeInBits() == 8) {
					my_byte = (byte) audioData[idx+i];
				} else {
					my_byte = (byte) (128 * audioData[idx+i] / 32768);
				}
				min=(byte) Math.min(min,my_byte);
				max=(byte) Math.max(max, my_byte);
			}
//			if (format.getSampleSizeInBits() == 8) {
//				my_byte = (byte) audioData[idx];
//			} else {
//				my_byte = (byte) (128 * audioData[idx] / 32768);
//			}
			double y_new = (double) (h * (128 - min) / 256);
			double y_new1 = (double) (h * (128 - max) / 256);
//			 lines.add(new Line2D.Double(x, y_last, x, y_new));
//			 lines.add(new Line2D.Double(x, h-y_last, x, h-y_new));
			lines.add(new Line2D.Double(x, y_new, x, y_new1));
			y_last = y_new;
		}
		// saveToFile(waveformFilename);
		return image(segment, duration, w, h);
	}

	public static BufferedImage image(List<Segment> segment, double duration,
			int w, int h) {
		int INFOPAD = 15;

		BufferedImage bufferedImage = new BufferedImage(w, h,
				BufferedImage.TYPE_INT_RGB);
		Graphics2D g2 = bufferedImage.createGraphics();

		createSampleOnGraphicsContext(segment, duration, w, h, INFOPAD, g2);
		g2.dispose();
		return bufferedImage;
	}

	private static void createSampleOnGraphicsContext(List<Segment> segment,
			double duration, int w, int h, int INFOPAD, Graphics2D g2) {
		g2.setBackground(Color.black);
		g2.clearRect(0, 0, w, h);
		g2.setColor(Color.black);

		HashMap<Integer, Color> hm = new HashMap<Integer, Color>();
		if (segment != null) {
			double[] min = new double[12];
			double[] max = new double[12];
			double[] range = new double[12];
			for (int i = 0; i < 12; i++) {
				min[i] = Double.MAX_VALUE;
				max[i] = Double.MIN_VALUE;
			}
			for (Segment s : segment) {
				double[] timbre = s.getTimbre();
				for (int i = 0; i < 12; i++) {
					min[i] = Math.min(min[i], timbre[i]);
					max[i] = Math.max(max[i], timbre[i]);
				}
			}
			for (int i = 0; i < 12; i++) {
				range[i] = max[i] - min[i];
			}

			for (Segment s : segment) {
				int x1 = (int) ((s.getStart() / duration) * (double) w + .5d);
				// Color c = ColorHelper
				// .numberToColorPercentage((s.getTimbre()[1] - min[1])
				// / range[1]);
				float hc=(float) ((s.getTimbre()[1] - min[1]) / range[1]);
				float sc=1.0f;
			    float lc=(float) ((s.getTimbre()[0] - min[0]) / range[0]);
			    
				Color c = HSLColor.toRGB(hc*360, sc*100, lc*100);
				hm.put(x1, c);
			}

		}
		// .. render sampling graph ..
		g2.setColor(jfcBlue);
		for (int i = 1; i < lines.size(); i++) {
			Line2D line = lines.get(i);

			if (hm.get((int) line.getP1().getX()) != null) {
				g2.setColor(hm.get((int) line.getP1().getX()));

			}
			g2.draw(line);
		}

		// // .. draw current position ..
		// if (seconds != 0) {
		// double loc = seconds/duration*w;
		// g2.setColor(pink);
		// g2.setStroke(new BasicStroke(3));
		// g2.draw(new Line2D.Double(loc, 0, loc, h-INFOPAD-2));
		// }
	}
}

// public void start() {
// thread = new Thread(this);
// thread.setName("SamplingGraph");
// thread.start();
// seconds = 0;
// }
//
// public void stop() {
// if (thread != null) {
// thread.interrupt();
// }
// thread = null;
// }
//
// public void run() {
// seconds = 0;
// while (thread != null) {
// if ( (capture.line != null) && (capture.line.isActive()) ) {
// long milliseconds = (long)(capture.line.getMicrosecondPosition() / 1000);
// seconds = milliseconds / 1000.0;
// }
// try { thread.sleep(100); } catch (Exception e) { break; }
// while ((capture.line != null && !capture.line.isActive()))
// {
// try { thread.sleep(10); } catch (Exception e) { break; }
// }
// }
// seconds = 0;
// }
// } // End class SamplingGraph