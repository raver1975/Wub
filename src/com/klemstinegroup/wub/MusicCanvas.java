package com.klemstinegroup.wub;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Collections;
import java.util.List;

import com.echonest.api.v4.Segment;
import com.echonest.api.v4.TimedEvent;
import com.echonest.api.v4.TrackAnalysis;

public class MusicCanvas extends Canvas implements MouseListener {

	private AudioObject au;
	double duration;
	TrackAnalysis analysis;
	private Player player;

	public MusicCanvas(AudioObject au) {
		this.au = au;
		this.player = au.player;
		analysis = au.analysis;
		duration = au.analysis.getDuration();
		this.addMouseListener(this);
	}

	@Override
	public void paint(Graphics g) {
		if (au.analysis == null)
			return;
		int x = this.getWidth();
		int y = this.getHeight();
		g.setColor(Color.white);
		g.fillRect(0, 0, x, y);

		g.setColor(Color.green);
		List<TimedEvent> list = au.analysis.getBeats();
		for (int i = 0; i < list.size(); i++) {
			TimedEvent te = list.get(i);
			int x1 = (int) ((te.getStart() / duration) * (double) x);
			int x2 = (int) ((te.getDuration() / duration) * (double) x);
			g.drawRect(x1, 40, x2, 20);
		}
		g.setColor(Color.red);
		list = au.analysis.getBars();
		for (int i = 0; i < list.size(); i++) {
			TimedEvent te = list.get(i);
			int x1 = (int) ((te.getStart() / duration) * (double) x);
			int x2 = (int) ((te.getDuration() / duration) * (double) x);
			g.drawRect(x1, 20, x2, 20);
		}
		g.setColor(Color.blue);
		list = au.analysis.getSections();
		for (int i = 0; i < list.size(); i++) {
			TimedEvent te = list.get(i);
			int x1 = (int) ((te.getStart() / duration) * (double) x);
			int x2 = (int) ((te.getDuration() / duration) * (double) x);
			g.drawRect(x1, 0, x2, 20);
		}
		g.setColor(Color.red);
		g.drawLine(0, 110, x, 110);
		List<Segment> list1 = au.analysis.getSegments();
		for (int i = 0; i < list1.size() - 1; i++) {
			Segment testart = list1.get(i);
			Segment teend = list1.get(i + 1);
			int x1 = (int) ((testart.getStart() / duration) * (double) x);
			int x2 = (int) ((teend.getStart() / duration) * (double) x);
			// int x3 = (int) ((testart.getLoudnessMaxTime() / duration) *
			// (double) x);
			double loudstart = testart.getLoudnessStart();
			double loudend = teend.getLoudnessStart();
			// double loudmax=testart.getLoudnessMax();
			// System.out.println(x1+"\t"+x3+"\t"+x2);
			g.setColor(Color.red);
			g.drawLine(x1, (int) (110 + loudstart), x2, (int) (110 + loudend));
			double[] pitch = testart.getPitches();
			for (int j = 0; j < 12; j++) {
				g.setColor(ColorHelper.numberToColorPercentage(pitch[j]));
				g.fillRect(x1, 110+ (j * 10), x2 - x1, 10);
			}
			// g.drawLine(x3, (int)(70-loudmax), x2, (int)(70-loudend));
		}

	}

	public MusicCanvas(GraphicsConfiguration config) {
		super(config);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		System.out.println(e.getX() + "\t" + e.getY());
		int x = e.getX();
		int y = e.getY();
		double loc = ((double) x / (double) this.getWidth()) * duration;

		if (y >= 0 && y < 20) {
			List<TimedEvent> list = au.analysis.getSections();
			Collections.reverse(list);
			for (int i = 0; i < list.size(); i++) {
				if (list.get(i).getStart() <= loc) {
					player.play(au, list.get(i).getStart(), list.get(i).getDuration());
					break;
				}

			}
		}
		if (y >= 20 && y < 40) {
			List<TimedEvent> list = au.analysis.getBars();
			Collections.reverse(list);
			for (int i = 0; i < list.size(); i++) {
				if (list.get(i).getStart() <= loc) {
					player.play(au, list.get(i).getStart(), list.get(i).getDuration());
					break;
				}

			}
		}
		if (y >= 40 && y < 60) {
			List<TimedEvent> list = au.analysis.getBeats();
			Collections.reverse(list);
			for (int i = 0; i < list.size(); i++) {
				if (list.get(i).getStart() <= loc) {
					player.play(au, list.get(i).getStart(), list.get(i).getDuration());
					break;
				}

			}
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub

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

}
