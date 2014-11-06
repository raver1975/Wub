package com.klemstinegroup.wub;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Queue;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import com.echonest.api.v4.Segment;
import com.echonest.api.v4.TimedEvent;
import com.echonest.api.v4.TrackAnalysis;

public class MusicCanvas extends JComponent implements MouseListener, MouseMotionListener, ComponentListener, KeyListener, MouseWheelListener {

	private AudioObject au;
	double duration;
	TrackAnalysis analysis;
	BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
	BufferedImage bufferedimage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
	public Interval tempTimedEvent;
	public Interval hovering;
	public double selectedStart;
	public int selectedStartX;
	int currPos;
	public boolean selectedPress;
	public int oldWidth;
	private JScrollBar jbar;
	private JScrollPane js;
	SamplingGraph samplingGraph = new SamplingGraph();
	Queue<Interval> tempQueue = new LinkedList<Interval>();
	public boolean mouseDown;

	public MusicCanvas(AudioObject au) {
		this.au = au;
		analysis = au.analysis;
		duration = au.analysis.getDuration();
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		this.addComponentListener(this);
		this.addKeyListener(this);
		this.addMouseWheelListener(this);
		startPosition();
		makeCanvas();
		// makeImage();
	}

	private void startPosition() {
		new Thread(new Runnable() {
			public void run() {
				while (true) {
					repaint();
					try {
						Thread.sleep(10);
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
		if (js != null && js.getHorizontalScrollBar() != null) {
			js.getHorizontalScrollBar().setUnitIncrement(js.getViewport().getWidth()/3);
			js.getHorizontalScrollBar().setBlockIncrement(js.getViewport().getWidth()/3);
		}
		paint1();
	}

	public void update(Graphics g) {
		paint(g);
	}

	@Override
	public void paint(Graphics g) {
		Graphics g1 = bufferedimage.getGraphics();
		g1.drawImage(image, 0, 0, null);

		int c = 1;
		LinkedList<Interval> temp = new LinkedList<Interval>();
		if (!au.queue.isEmpty())
			temp.addAll(au.queue);
		temp.addAll(tempQueue);
		for (Interval i : temp) {
			if (i != null) {
				double co = (double) c++ / ((double) temp.size() + 1d);
				g1.setColor(ColorHelper.numberToColorPercentage(1d - co));
				int x3 = (int) (((i.te.getStart() / duration) * (double) getWidth()) + .5d);
				int x4 = (int) (((i.te.getDuration() / duration) * (double) getWidth()) + .5d);
				g1.fillRect(x3 + 1, i.y + 1, x4, 18);
			}
		}
		// if (tempTimedEvent != null) {
		// g1.setColor(Color.WHITE);
		// int x3 = (int) ((tempTimedEvent.te.getStart() / duration) * (double)
		// getWidth() + .5d);
		// int x4 = (int) ((tempTimedEvent.te.getDuration() / duration) *
		// (double) getWidth() + .5d);
		// g1.fillRect(x3 + 1, tempTimedEvent.y + 1, x4 - 2, 18);
		// }

		if (hovering != null) {
			// if (hovering.y == 0)
			// g1.setColor(Color.red);
			// if (hovering.y == 20)
			// g1.setColor(Color.yellow);
			// if (hovering.y == 40)
			// g1.setColor(Color.green);
			// if (hovering.y == 60)
			// g1.setColor(Color.blue);
			// if (hovering.y == 80)
			// g1.setColor(Color.orange);
			g1.setColor(Color.white);
			int x3 = (int) (((hovering.te.getStart() / duration) * (double) getWidth()) + .5d);
			int x4 = (int) (((hovering.te.getDuration() / duration) * (double) getWidth()) + .5d);
			g1.fillRect(x3 + 1, hovering.y + 1, x4, 18);
			int x1 = (int) ((hovering.te.getStart() / duration) * (double) getWidth() + .5d);
			int x2 = (int) ((hovering.te.getDuration() / duration) * (double) getWidth() + .5d);
			g1.setColor(new Color(255, 255, 0, 127));
			g1.fillRect(x1 + 1, 100, x2 - 1, 200);
		}

		if (au.currentlyPlaying != null) {
			g1.setColor(Color.red.darker().darker().darker());
			if (hovering != null && hovering.equals(au.currentlyPlaying))
				g1.setColor(new Color(150, 100, 100));
			int x3 = (int) (((au.currentlyPlaying.te.getStart() / duration) * (double) getWidth()) + .5d);
			int x4 = (int) (((au.currentlyPlaying.te.getDuration() / duration) * (double) getWidth()) + .5d);
			g1.fillRect(x3 + 1, au.currentlyPlaying.y + 1, x4, 18);
			int x1 = (int) ((au.currentlyPlaying.te.getStart() / duration) * (double) getWidth() + .5d);
			int x2 = (int) ((au.currentlyPlaying.te.getDuration() / duration) * (double) getWidth() + .5d);
			g1.setColor(new Color(255, 0, 0, 127));
			g1.fillRect(x1 + 1, 100, x2 - 1, 200);
		}
		if (selectedPress) {
			g1.setColor(new Color(255, 0, 0, 127));
			int st = selectedStartX;
			int en = currPos;
			if (en < st) {
				st = currPos;
				en = selectedStartX;
			}
			g1.fillRect(st, 100, en - st, 200);
		}

		g1.setColor(Color.white);
		int x1 = (int) ((getWidth() * (double) au.position / (double) au.data.length) + .5d);
		g1.drawLine(x1, 0, x1, getHeight());

		if (au.loop) {
			g1.setColor(Color.CYAN);
			g1.drawString("loop", js.getHorizontalScrollBar().getValue() + 5, 14);
		}

		FontMetrics metrics = g1.getFontMetrics(g1.getFont());
		for (Entry<String, Interval> e : au.midiMap.entrySet()) {
			int x5 = (int) ((e.getValue().te.getStart() / duration) * (double) getWidth() + .5d);
			int x6 = (int) ((e.getValue().te.getDuration() / duration) * (double) getWidth() + .5d);
			g1.setColor(new Color(0, 255, 255, 80));
			g1.fillRect(x5 + 1, e.getValue().y + 1, x6, 18);
			if (x6 > 5) {
				g1.setColor(Color.CYAN);
				g1.drawString(e.getKey(), x5 + x6 / 2 - metrics.stringWidth(e.getKey()) / 2, e.getValue().y + 14);
			}
		}
		g1.setColor(Color.red);
		g1.drawLine(currPos, 0, currPos, getHeight());

		g.drawImage(bufferedimage, 0, 0, null);

	}

	public void paint1() {
		if (au.analysis == null)
			return;
		Graphics g = image.getGraphics();
		int x = this.getWidth();
		int y = this.getHeight();
		g.setColor(Color.black);
		g.fillRect(0, 0, x, y);
		g.drawImage(samplingGraph.createWaveForm(au.analysis.getSegments(), duration, au.data, AudioObject.audioFormat, getWidth(), 200), 0, 100, null);
		g.setColor(Color.white);
		g.drawLine(0, 200, getWidth(), 200);

		List<TimedEvent> list = au.analysis.getTatums();
		for (int i = 0; i < list.size(); i++) {
			TimedEvent te = list.get(i);
			int x1 = (int) ((te.getStart() / duration) * (double) x + .5d);
			int x2 = (int) ((te.getDuration() / duration) * (double) x + .5d);
			g.setColor(Color.red.darker().darker());
			g.fillRect(x1, 60, x2, 19);
			g.setColor(Color.red);
			g.drawRect(x1, 60, x2, 19);
		}

		list = au.analysis.getBeats();
		for (int i = 0; i < list.size(); i++) {
			TimedEvent te = list.get(i);
			int x1 = (int) ((te.getStart() / duration) * (double) x + .5d);
			int x2 = (int) ((te.getDuration() / duration) * (double) x + .5d);
			g.setColor(Color.yellow.darker().darker());
			g.fillRect(x1, 40, x2, 19);
			g.setColor(Color.yellow);
			g.drawRect(x1, 40, x2, 19);

		}

		list = au.analysis.getBars();
		for (int i = 0; i < list.size(); i++) {
			TimedEvent te = list.get(i);
			int x1 = (int) ((te.getStart() / duration) * (double) x + .5d);
			int x2 = (int) ((te.getDuration() / duration) * (double) x + .5d);
			g.setColor(Color.green.darker().darker());
			g.fillRect(x1, 20, x2, 19);
			g.setColor(Color.green);
			g.drawRect(x1, 20, x2, 19);
		}

		list = au.analysis.getSections();
		for (int i = 0; i < list.size(); i++) {
			TimedEvent te = list.get(i);
			int x1 = (int) ((te.getStart() / duration) * (double) x + .5d);
			int x2 = (int) ((te.getDuration() / duration) * (double) x + .5d);
			g.setColor(Color.blue.darker().darker());
			g.fillRect(x1, 0, x2, 19);
			g.setColor(Color.blue);
			g.drawRect(x1, 0, x2, 19);
		}

		g.setColor(Color.red);
		List<Segment> list1 = au.analysis.getSegments();
		double[] min = new double[12];
		double[] max = new double[12];
		double[] range = new double[12];
		for (int i = 0; i < 12; i++) {
			min[i] = Double.MAX_VALUE;
			max[i] = Double.MIN_VALUE;
		}
		for (Segment s : list1) {
			double[] timbre = s.getTimbre();
			for (int i = 0; i < 12; i++) {
				min[i] = Math.min(min[i], timbre[i]);
				max[i] = Math.max(max[i], timbre[i]);
			}
		}
		for (int i = 0; i < 12; i++) {
			range[i] = max[i] - min[i];
		}

		for (int i = 0; i < list1.size() - 1; i++) {
			Segment testart = list1.get(i);
			Segment teend = list1.get(i + 1);
			int x1 = (int) ((testart.getStart() / duration) * (double) x + .5d);
			int x2 = (int) ((teend.getStart() / duration) * (double) x + .5d);
			// double loudstart = testart.getLoudnessStart()*2d;
			// double loudend = teend.getLoudnessStart()*2d;
			// g.setColor(Color.red);
			// g.drawLine(x1, (int) (80 - loudstart), x2, (int) (80 - loudend));
			double[] pitch = testart.getPitches();
			float col = 0f;
			for (int j = 0; j < 12; j++) {
				// g.setColor(ColorHelper.numberToColorPercentage(pitch[j]));
				float hc = col += .083;
				float sc = 1.0f;
				float lc = ((float) (pitch[j]) / 2f);
				//
				Color c = HSLColor.toRGB(hc * 360, sc * 100, lc * 100);
				g.setColor(c);
				g.fillRect(x1, 320 + (j * 15), x2 - x1, 15);
			}

			double[] timbre = testart.getTimbre();
			for (int j = 0; j < 12; j++) {
				float hc = (float) ((timbre[j] - min[j]) / range[j]);
				float sc = 1.0f;
				float lc = .5f;

				Color c = HSLColor.toRGB(hc * 360, sc * 100, lc * 100);
				// g.setColor(ColorHelper
				// .numberToColorPercentage((timbre[j] - min[j])
				// / range[j]));
				g.setColor(c);
				g.fillRect(x1, 520 + (j * 15), x2 - x1, 15);
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
		mouseDown = true;
		int x = e.getX();
		int y = e.getY();
		double loc = ((double) x / (double) this.getWidth()) * duration;

		if (e.getButton() == MouseEvent.BUTTON3) {
			List<Interval> temp = new LinkedList<Interval>(au.queue);
			for (Interval i : temp) {
				int x3 = (int) (((i.te.getStart() / duration) * (double) getWidth()) + .5d);
				int x4 = (int) ((((i.te.getStart() + i.te.getDuration()) / duration) * (double) getWidth()) + .5d);
				if (x >= x3 && x <= x4 && y >= i.y && y <= i.y + 20) {
					au.queue.remove(i);
				}
			}
			if (au.currentlyPlaying != null) {
				int x3 = (int) (((au.currentlyPlaying.te.getStart() / duration) * (double) getWidth()) + .5d);
				int x4 = (int) ((((au.currentlyPlaying.te.getStart() + au.currentlyPlaying.te.getDuration()) / duration) * (double) getWidth()) + .5d);
				if (x >= x3 && x <= x4 && y >= au.currentlyPlaying.y && y <= au.currentlyPlaying.y + 20) {

					au.breakPlay = true;
				}
			}
			return;
		}

		if (y >= 0 && y < 20) {
			List<TimedEvent> list = au.analysis.getSections();
			Collections.reverse(list);
			for (int i = 0; i < list.size(); i++) {
				if (list.get(i).getStart() <= loc) {
					tempTimedEvent = new Interval(list.get(i), 0);
					// au.play(list.get(i), 0);
					tempQueue.add(tempTimedEvent);
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
					// au.play(list.get(i), 20);
					tempQueue.add(tempTimedEvent);
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
					// au.play(list.get(i), 40);
					tempQueue.add(tempTimedEvent);
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
					// au.play(list.get(i), 60);
					tempQueue.add(tempTimedEvent);
					break;
				}

			}
		}
		if (y > 80) {
			selectedStart = loc;
			selectedStartX = x;
			// selectedStartY = y-10;
			selectedPress = true;
		}

	}

	@Override
	public void mouseReleased(MouseEvent e) {
		mouseDown = false;
		int x = e.getX();
		int y = e.getY();
		double loc = ((double) x / (double) this.getWidth()) * duration;

		if (y > 80 && selectedPress) {
			double st = selectedStart;
			double en = loc;
			if (loc < st) {
				st = loc;
				en = selectedStart;
			}
			en = en - st;
			HashMap<String, Double> hm = new HashMap<String, Double>();
			hm.put("start", Math.max(st, 0));
			hm.put("duration", Math.min(duration - st, en));
			hm.put("confidence", 1d);
			au.play(new Interval(new TimedEvent(hm), 80));
		}
		selectedPress = false;

		while (!tempQueue.isEmpty()) {
			au.play(tempQueue.poll());
		}
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
		jbar.setMinimum(js.getViewport().getWidth() / 50);

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
		if (SwingUtilities.isRightMouseButton(e)) {
			List<Interval> temp = new LinkedList<Interval>(au.queue);
			for (Interval i : temp) {
				int x3 = (int) (((i.te.getStart() / duration) * (double) getWidth()) + .5d);
				int x4 = (int) ((((i.te.getStart() + i.te.getDuration()) / duration) * (double) getWidth()) + .5d);
				if (x >= x3 && x <= x4 && y >= i.y && y <= i.y + 20) {
					au.queue.remove(i);
				}
			}
			if (au.currentlyPlaying != null) {
				int x3 = (int) (((au.currentlyPlaying.te.getStart() / duration) * (double) getWidth()) + .5d);
				int x4 = (int) ((((au.currentlyPlaying.te.getStart() + au.currentlyPlaying.te.getDuration()) / duration) * (double) getWidth()) + .5d);
				if (x >= x3 && x <= x4 && y >= au.currentlyPlaying.y && y <= au.currentlyPlaying.y + 20) {
					au.breakPlay = true;
				}
			}
			return;
		}

		currPos = x;
		double loc = ((double) x / (double) this.getWidth()) * duration;

		if (y >= 0 && y < 20) {
			List<TimedEvent> list = au.analysis.getSections();
			Collections.reverse(list);
			for (int i = 0; i < list.size(); i++) {
				if (list.get(i).getStart() <= loc) {
					hovering = new Interval(list.get(i), 0);
					if (tempTimedEvent == null || tempTimedEvent.te.getStart() != list.get(i).getStart()) {
						tempTimedEvent = new Interval(list.get(i), 0);
						tempQueue.add(tempTimedEvent);
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
					hovering = new Interval(list.get(i), 20);
					if (tempTimedEvent == null || tempTimedEvent.te.getStart() != list.get(i).getStart()) {
						tempTimedEvent = new Interval(list.get(i), 20);
						tempQueue.add(tempTimedEvent);
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
					hovering = new Interval(list.get(i), 40);
					if (tempTimedEvent == null || tempTimedEvent.te.getStart() != list.get(i).getStart()) {
						tempTimedEvent = new Interval(list.get(i), 40);
						tempQueue.add(tempTimedEvent);
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
					hovering = new Interval(list.get(i), 60);
					if (tempTimedEvent == null || tempTimedEvent.te.getStart() != list.get(i).getStart()) {
						tempTimedEvent = new Interval(list.get(i), 60);
						tempQueue.add(tempTimedEvent);
					}
					break;
				}

			}
		}

	}

	@Override
	public void mouseMoved(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();
		currPos = x;
		double loc = ((double) x / (double) this.getWidth()) * duration;
		if (y >= 0 && y < 20) {
			hovering = null;
			List<TimedEvent> list = au.analysis.getSections();
			Collections.reverse(list);
			for (int i = 0; i < list.size(); i++) {
				if (list.get(i).getStart() <= loc) {
					hovering = new Interval(list.get(i), 0);
					break;

				}

			}
		}
		if (y >= 20 && y < 40) {
			List<TimedEvent> list = au.analysis.getBars();
			hovering = null;
			Collections.reverse(list);
			for (int i = 0; i < list.size(); i++) {
				if (list.get(i).getStart() <= loc) {
					hovering = new Interval(list.get(i), 20);
					break;
				}

			}
		}
		if (y >= 40 && y < 60) {
			List<TimedEvent> list = au.analysis.getBeats();
			hovering = null;
			Collections.reverse(list);
			for (int i = 0; i < list.size(); i++) {
				if (list.get(i).getStart() <= loc) {
					hovering = new Interval(list.get(i), 40);
					break;
				}

			}
		}

		if (y >= 60 && y < 80) {
			List<TimedEvent> list = au.analysis.getTatums();
			hovering = null;
			Collections.reverse(list);
			for (int i = 0; i < list.size(); i++) {
				if (list.get(i).getStart() <= loc) {
					hovering = new Interval(list.get(i), 60);
					break;
				}
			}
		}

		if (y >= 80 && y < 100) {
			List<Interval> list1 = new LinkedList<Interval>(au.queue);
			if (au.currentlyPlaying != null)
				list1.add(au.currentlyPlaying);
			hovering = null;
			for (int i = 0; i < list1.size(); i++) {
				if (list1.get(i).y == 80 && loc >= list1.get(i).te.getStart() && loc <= list1.get(i).te.getStart() + list1.get(i).te.getDuration()) {
					hovering = list1.get(i);
					break;
				}

			}
		}
		if (y >= 100) {
			hovering = null;
		}
	}

	public void makeCanvas() {
		JFrame frame = new JFrame(au.getFileName());
		// JMenuBar menuBar;
		// JMenu fileMenu;
		// JMenuItem fileMenuItem;
		// menuBar = new JMenuBar();
		// fileMenu = new JMenu("File");
		// menuBar.add(fileMenu);
		// fileMenuItem = new JMenuItem("Open");
		// fileMenu.add(fileMenuItem);
		// fileMenuItem.addActionListener(new ActionListener() {
		// @Override
		// public void actionPerformed(ActionEvent e) {
		// AudioObject.factory();
		// }
		//
		// });
		// frame.setJMenuBar(menuBar);

		oldWidth = 800;
		setSize(new Dimension(oldWidth, 760));

		js = new JScrollPane(this);
		frame.addKeyListener(this);
		js.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
		frame.getContentPane().add(js, "Center");
		jbar = new JScrollBar(JScrollBar.VERTICAL);
		jbar.setMinimum(oldWidth / 50);
		jbar.setMaximum(2000);
		jbar.setValue(oldWidth / 50);
		jbar.addAdjustmentListener(new AdjustmentListener() {
			public void adjustmentValueChanged(AdjustmentEvent ae) {
				if (ae.getValueIsAdjusting())
					return;
				double factor = ((double) (50 * ae.getValue()) / (double) oldWidth);
				double oldPos = js.getHorizontalScrollBar().getValue() + js.getViewport().getWidth() / 2d;
				int newPos = (int) (oldPos * factor);
				newPos -= js.getViewport().getWidth() / 2d;
				js.getHorizontalScrollBar().setValue(newPos);
				setSize(50 * ae.getValue(), getHeight());
				makeImage();
				oldWidth = 50 * ae.getValue();
				jbar.setUnitIncrement(jbar.getValue() / 5);
				jbar.setBlockIncrement(jbar.getValue() / 5);

			}
		});
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				au.pause = true;
				au.breakPlay = true;
				au.queue.clear();
			}
		});

		frame.getContentPane().add(jbar, "East");

		frame.setBounds(100, 100, oldWidth + 50, 760);
		frame.setVisible(true);
		frame.validate();
		frame.repaint();

	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyChar() == ' ')
			au.pause = !au.pause;
		else if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
			AudioObject.factory();
		else if (e.getKeyCode() == KeyEvent.VK_F12)
			System.exit(0);
		else if (e.getKeyCode() == KeyEvent.VK_F1)
			au.loop = !au.loop;
		else if (e.getKeyCode() == KeyEvent.VK_F3)
			au.breakPlay = true;
		else if (e.getKeyCode() == KeyEvent.VK_F4) {
			au.queue.clear();
			au.breakPlay = true;
		} else if (e.getKeyCode() == KeyEvent.VK_F5) {
			au.midiMap.clear();
		} else if (e.getKeyCode() == KeyEvent.VK_F6) {
			au.createAudioObject();
		} else if (e.getKeyCode() == KeyEvent.VK_F7) {
			au.createReverseAudioObject();
		} else if (e.getKeyCode() == KeyEvent.VK_F2) {
			Collections.reverse((LinkedList) au.queue);
		} else {
			au.sendMidi(e.getKeyChar() + "", 127);
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		au.sendMidi(e.getKeyChar() + "", 0);
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		int notches = e.getWheelRotation();
		// int x = e.getX();
		int x = currPos;
		double percentage = (double) x / (double) getWidth();
		int barvalue = js.getHorizontalScrollBar().getValue();
		js.getHorizontalScrollBar().setValue((int) (percentage * getWidth() - js.getViewport().getWidth() / 2d));
		jbar.setValue(jbar.getValue() + notches * -jbar.getValue() / 10);
		currPos = (int) (percentage * getWidth());
		js.getHorizontalScrollBar().setValue((int) (currPos - js.getViewport().getWidth() / 2d));
	}
}
