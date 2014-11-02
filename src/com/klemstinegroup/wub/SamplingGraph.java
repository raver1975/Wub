package com.klemstinegroup.wub;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.util.Vector;

import javax.sound.sampled.AudioFormat;

/**
 * Render a WaveForm.
 */
class SamplingGraph {
	static Vector<Line2D.Double> lines = new Vector<Line2D.Double>();
	private Thread thread;
	private Font font10 = new Font("serif", Font.PLAIN, 10);
	private static Font font12 = new Font("serif", Font.PLAIN, 12);
	static Color jfcBlue = new Color(255, 255, 0);
	static Color pink = new Color(255, 175, 175);

	public SamplingGraph() {
	}

	public static BufferedImage createWaveForm(byte[] audioBytes, AudioFormat format,int w,int h) {

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
			if (format.getSampleSizeInBits() == 8) {
				my_byte = (byte) audioData[idx];
			} else {
				my_byte = (byte) (128 * audioData[idx] / 32768);
			}
			double y_new = (double) (h * (128 - my_byte) / 256);
			lines.add(new Line2D.Double(x, y_last, x, y_new));
			y_last = y_new;
		}
		// saveToFile(waveformFilename);
		return image(w,h);
	}

	public static BufferedImage image(int w, int h) {
		int INFOPAD = 15;

		BufferedImage bufferedImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2 = bufferedImage.createGraphics();

		createSampleOnGraphicsContext(w, h, INFOPAD, g2);
		g2.dispose();
		return bufferedImage;
	}

	private static void createSampleOnGraphicsContext(int w, int h, int INFOPAD, Graphics2D g2) {
		g2.setBackground(Color.black);
		g2.clearRect(0, 0, w, h);
		g2.setColor(Color.white);
		g2.fillRect(0, h - INFOPAD, w, INFOPAD);

		g2.setColor(Color.black);
		g2.setFont(font12);
		// g2.drawString("File: " + fileName + "  Length: " +
		// String.valueOf(duration) + "  Position: " + String.valueOf(seconds),
		// 3, h-4);

		// .. render sampling graph ..
		g2.setColor(jfcBlue);
		for (int i = 1; i < lines.size(); i++) {
			g2.draw((Line2D) lines.get(i));
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