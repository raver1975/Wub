package com.klemstinegroup.wub;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.List;

import javax.swing.JComponent;

import com.echonest.api.v4.Segment;
import com.echonest.api.v4.TimedEvent;
import com.echonest.api.v4.TrackAnalysis;

public class MusicCanvas extends JComponent implements MouseListener, MouseMotionListener, ComponentListener {

	private AudioObject au;
	double duration;
	TrackAnalysis analysis;
	BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
	BufferedImage bufferedimage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
	public Interval tempTimedEvent;
	private int currPos;

	public MusicCanvas(AudioObject au) {
		this.au = au;
		analysis = au.analysis;
		duration = au.analysis.getDuration();
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		this.addComponentListener(this);
		startPosition();
		// makeImage();
	}

	private void startPosition() {
		new Thread(new Runnable() {
			public void run() {
				while (true) {
					repaint();
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();

	}

	void makeImage() {
		image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
		bufferedimage = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
		Graphics g = image.getGraphics();
		paint1(g);
	}

	public void update(Graphics g) {
		paint(g);
	}

	@Override
	public void paint(Graphics g) {
		Graphics g1 = bufferedimage.getGraphics();
		g1.drawImage(image, 0, 0, null);

		if (!au.queue.isEmpty()) {
			g1.setColor(Color.gray);
			for (Interval i : au.queue) {
				int x3 = (int) ((i.te.getStart() / duration) * (double) getWidth() + .5d);
				int x4 = (int) ((i.te.getDuration() / duration) * (double) getWidth() + .5d);
				g1.fillRect(x3 + 1, i.y + 1, x4 - 1, 18);
			}
		}
		if (tempTimedEvent != null) {
			g1.setColor(Color.LIGHT_GRAY);
			int x3 = (int) ((tempTimedEvent.te.getStart() / duration) * (double) getWidth() + .5d);
			int x4 = (int) ((tempTimedEvent.te.getDuration() / duration) * (double) getWidth() + .5d);
			g1.fillRect(x3 + 1, tempTimedEvent.y + 1, x4 - 1, 18);
		}
		if (au.positionInterval != null) {
			g1.setColor(Color.cyan);
			int x3 = (int) ((au.positionInterval.te.getStart() / duration) * (double) getWidth() + .5d);
			int x4 = (int) ((au.positionInterval.te.getDuration() / duration) * (double) getWidth() + .5d);
			g1.fillRect(x3 + 1, au.positionInterval.y + 1, x4 - 1, 18);
		} 
		g1.setColor(Color.white);
		int x1 = (int) (getWidth() * (double) au.position / (double) au.data.length + .5d);
		g1.drawLine(x1, 0, x1, getHeight());

		g1.setColor(Color.cyan);
		g1.drawLine(currPos, 0, currPos, getHeight());
		g.drawImage(bufferedimage, 0, 0, null);
	}

	public void paint1(Graphics g) {
		if (au.analysis == null)
			return;
		int x = this.getWidth();
		int y = this.getHeight();
		g.setColor(Color.black);
		g.fillRect(0, 0, x, y);
		g.drawImage(SamplingGraph.createWaveForm(au.data, au.audioFormat, getWidth(), 200), 0, 80, null);
		g.setColor(Color.magenta);
		List<TimedEvent> list = au.analysis.getTatums();
		for (int i = 0; i < list.size(); i++) {
			TimedEvent te = list.get(i);
			int x1 = (int) ((te.getStart() / duration) * (double) x + .5d);
			int x2 = (int) ((te.getDuration() / duration) * (double) x + .5d);
			g.drawRect(x1, 60, x2, 19);
		}

		g.setColor(Color.green);
		list = au.analysis.getBeats();
		for (int i = 0; i < list.size(); i++) {
			TimedEvent te = list.get(i);
			int x1 = (int) ((te.getStart() / duration) * (double) x + .5d);
			int x2 = (int) ((te.getDuration() / duration) * (double) x + .5d);
			g.drawRect(x1, 40, x2, 19);
		}
		g.setColor(Color.red);
		list = au.analysis.getBars();
		for (int i = 0; i < list.size(); i++) {
			TimedEvent te = list.get(i);
			int x1 = (int) ((te.getStart() / duration) * (double) x + .5d);
			int x2 = (int) ((te.getDuration() / duration) * (double) x + .5d);
			g.drawRect(x1, 20, x2, 19);
		}
		g.setColor(Color.yellow);
		list = au.analysis.getSections();
		for (int i = 0; i < list.size(); i++) {
			TimedEvent te = list.get(i);
			int x1 = (int) ((te.getStart() / duration) * (double) x + .5d);
			int x2 = (int) ((te.getDuration() / duration) * (double) x + .5d);
			g.drawRect(x1, 0, x2, 19);
		}

		g.setColor(Color.red);
		List<Segment> list1 = au.analysis.getSegments();
		for (int i = 0; i < list1.size() - 1; i++) {
			Segment testart = list1.get(i);
			Segment teend = list1.get(i + 1);
			int x1 = (int) ((testart.getStart() / duration) * (double) x + .5d);
			int x2 = (int) ((teend.getStart() / duration) * (double) x + .5d);
			double loudstart = testart.getLoudnessStart();
			double loudend = teend.getLoudnessStart();
			g.setColor(Color.red);
			g.drawLine(x1, (int) (260 + loudstart), x2, (int) (260 + loudend));
			double[] pitch = testart.getPitches();
			for (int j = 0; j < 12; j++) {
				g.setColor(ColorHelper.numberToColorPercentage(pitch[j]));
				g.fillRect(x1, 260 + (j * 15), x2 - x1, 15);
			}
			// g.drawLine(x3, (int)(70-loudmax), x2, (int)(70-loudend));
		}

	}

	public Dimension getPreferredSize() {
		return new Dimension(this.getSize().width, this.getSize().height);
	}

	// public MusicCanvas(GraphicsConfiguration config) {
	// super(config);
	// // TODO Auto-generated constructor stub
	// }

	@Override
	public void mouseClicked(MouseEvent e) {

	}

	@Override
	public void mousePressed(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();
		double loc = ((double) x / (double) this.getWidth()) * duration;

		if (y >= 0 && y < 20) {
			List<TimedEvent> list = au.analysis.getSections();
			Collections.reverse(list);
			for (int i = 0; i < list.size(); i++) {
				if (list.get(i).getStart() <= loc) {
					tempTimedEvent = new Interval(list.get(i), 0);
					au.play(list.get(i), 0);
					break;
				}

			}
		}
		if (y >= 20 && y < 40) {
			List<TimedEvent> list = au.analysis.getBars();
			Collections.reverse(list);
			for (int i = 0; i < list.size(); i++) {
				if (list.get(i).getStart() <= loc) {
					tempTimedEvent = new Interval(list.get(i), 20);
					au.play(list.get(i), 20);
					break;
				}

			}
		}
		if (y >= 40 && y < 60) {
			List<TimedEvent> list = au.analysis.getBeats();
			Collections.reverse(list);
			for (int i = 0; i < list.size(); i++) {
				if (list.get(i).getStart() <= loc) {
					tempTimedEvent = new Interval(list.get(i), 40);
					au.play(list.get(i), 40);
					break;
				}

			}
		}

		if (y >= 60 && y < 80) {
			List<TimedEvent> list = au.analysis.getTatums();
			Collections.reverse(list);
			for (int i = 0; i < list.size(); i++) {
				if (list.get(i).getStart() <= loc) {
					tempTimedEvent = new Interval(list.get(i), 60);
					au.play(list.get(i), 60);
					break;
				}

			}
		}

	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void componentResized(ComponentEvent e) {
		makeImage();

	}

	@Override
	public void componentMoved(ComponentEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void componentShown(ComponentEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void componentHidden(ComponentEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseDragged(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();
		currPos = x;
		double loc = ((double) x / (double) this.getWidth()) * duration;

		if (y >= 0 && y < 20) {
			List<TimedEvent> list = au.analysis.getSections();
			Collections.reverse(list);
			for (int i = 0; i < list.size(); i++) {
				if (list.get(i).getStart() <= loc) {
					if (tempTimedEvent == null || tempTimedEvent.te.getStart() != list.get(i).getStart()) {
						tempTimedEvent = new Interval(list.get(i), 0);
						au.play(list.get(i), 0);
					}
					break;

				}

			}
		}
		if (y >= 20 && y < 40) {
			List<TimedEvent> list = au.analysis.getBars();
			Collections.reverse(list);
			for (int i = 0; i < list.size(); i++) {
				if (list.get(i).getStart() <= loc) {
					if (tempTimedEvent == null || tempTimedEvent.te.getStart() != list.get(i).getStart()) {
						tempTimedEvent = new Interval(list.get(i), 20);
						au.play(list.get(i), 20);
					}
					break;
				}

			}
		}
		if (y >= 40 && y < 60) {
			List<TimedEvent> list = au.analysis.getBeats();
			Collections.reverse(list);
			for (int i = 0; i < list.size(); i++) {
				if (list.get(i).getStart() <= loc) {
					if (tempTimedEvent == null || tempTimedEvent.te.getStart() != list.get(i).getStart()) {
						tempTimedEvent = new Interval(list.get(i), 40);
						au.play(list.get(i), 40);
					}
					break;
				}

			}
		}

		if (y >= 60 && y < 80) {
			List<TimedEvent> list = au.analysis.getTatums();
			Collections.reverse(list);
			for (int i = 0; i < list.size(); i++) {
				if (list.get(i).getStart() <= loc) {
					if (tempTimedEvent == null || tempTimedEvent.te.getStart() != list.get(i).getStart()) {
						tempTimedEvent = new Interval(list.get(i), 60);
						au.play(list.get(i), 60);
					}
					break;
				}

			}
		}

	}

	@Override
	public void mouseMoved(MouseEvent e) {
		currPos = e.getX();

	}

}
