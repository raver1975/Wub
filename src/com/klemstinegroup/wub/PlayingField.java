package com.klemstinegroup.wub;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
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

import javax.swing.JFrame;
import javax.swing.JScrollBar;

public class PlayingField extends Canvas implements MouseListener, MouseMotionListener, KeyListener, ComponentListener, MouseWheelListener {

	private int oldWidth;
	private JFrame frame;
	private JScrollBar jverticalbar;
	private JScrollBar jhorizontalbar;
	private Image bufferedImage;
	private int offset;

	@Override
	public void update(Graphics g) {
		paint(g);
	}

	@Override
	public void paint(Graphics g) {
		g.drawImage(bufferedImage, 0, 0, null);
	}

	public PlayingField() {
		oldWidth = 800;
		setSize(new Dimension(oldWidth, 760));
		frame = new JFrame("Field Of Play");
		// js = new JScrollPane(this);
		frame.getContentPane().add(this, "Center");
		frame.addKeyListener(this);
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		this.addComponentListener(this);
		this.addKeyListener(this);
		this.addMouseWheelListener(this);
		// js.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
		// frame.getContentPane().add(js, "Center");
		jverticalbar = new JScrollBar(JScrollBar.VERTICAL);
		jverticalbar.setMinimum(oldWidth / 50);
		jverticalbar.setMaximum(2000);
		jverticalbar.setValue(oldWidth / 50);
		jverticalbar.addAdjustmentListener(new AdjustmentListener() {
			public void adjustmentValueChanged(AdjustmentEvent ae) {
				if (ae.getValueIsAdjusting())
					return;
				// double factor = ((double) (50 * ae.getValue()) / (double)
				// oldWidth);
				// double oldPos = js.getHorizontalScrollBar().getValue() +
				// js.getViewport().getWidth() / 2d;
				// int newPos = (int) (oldPos * factor);
				// newPos -= js.getViewport().getWidth() / 2d;
				// js.getHorizontalScrollBar().setValue(newPos);
				// setSize(50 * ae.getValue(), getHeight());
				oldWidth = 50 * ae.getValue();
				jverticalbar.revalidate();
				PlayingField.this.revalidate();

				jverticalbar.setUnitIncrement(jverticalbar.getValue() / 5);
				jverticalbar.setBlockIncrement(jverticalbar.getValue() / 5);
				jhorizontalbar.setMinimum(-oldWidth);
				jhorizontalbar.setMaximum(oldWidth);
				makeImageResize();

			}
		});

		jhorizontalbar = new JScrollBar(JScrollBar.HORIZONTAL);
		jhorizontalbar.setMinimum(-oldWidth);
		jhorizontalbar.setMaximum(oldWidth);
		jhorizontalbar.setValue(0);
		jhorizontalbar.addAdjustmentListener(new AdjustmentListener() {
			public void adjustmentValueChanged(AdjustmentEvent ae) {
				if (ae.getValueIsAdjusting())
					return;
				offset = ae.getValue();
				System.out.println(offset);
				makeImage();
			}
		});
		// jhorizontalbar.addAdjustmentListener(new AdjustmentListener() {
		// public void adjustmentValueChanged(AdjustmentEvent ae) {
		// if (ae.getValueIsAdjusting())
		// return;
		//
		// }
		// });
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				// au.pause = true;
				// au.breakPlay = true;
				// au.queue.clear();
				// CentralCommand.remove(au);
			}
		});

		frame.getContentPane().add(jverticalbar, "East");
		frame.getContentPane().add(jhorizontalbar, "South");

		frame.setBounds(100, 100, oldWidth + 50, 760);
		frame.setVisible(true);
		frame.validate();
		frame.repaint();
		makeImageResize();

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

	protected void makeImageResize() {
		double max = Double.MIN_VALUE;
		for (AudioObject au : CentralCommand.aolist) {
			if (au.analysis.getDuration() > max) {
				max = au.analysis.getDuration();
			}
		}
		for (AudioObject au : CentralCommand.aolist) {
			au.playFieldImage = new SamplingGraph().createWaveForm(au.analysis.getSegments(), au.analysis.getDuration(), au.data, AudioObject.audioFormat, (int) (au.analysis.getDuration() * (double)oldWidth / (max)), 40);
			au.PlayFieldPosition.width = (int) (au.analysis.getDuration() * (double)oldWidth / max);
		}
		makeImage();
	}

	public void makeImage() {
		System.out.println(this.getWidth());
		bufferedImage = new BufferedImage(this.getWidth(), this.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics g1 = bufferedImage.getGraphics();
		g1.setColor(Color.black);
		g1.fillRect(0, 0, getWidth(), getHeight());

		for (AudioObject au : CentralCommand.aolist) {
			if (au.playFieldImage != null) {
				g1.drawImage(au.playFieldImage, au.PlayFieldPosition.x - offset, au.PlayFieldPosition.y, null);
				g1.setColor(Color.cyan);
				g1.drawRect(au.PlayFieldPosition.x - offset, au.PlayFieldPosition.y,au.PlayFieldPosition.width,au.PlayFieldPosition.height);
			}
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseDragged(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseMoved(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub

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
	public void mouseWheelMoved(MouseWheelEvent e) {
		// TODO Auto-generated method stub
		jverticalbar.setValue(jverticalbar.getValue() + e.getWheelRotation() * (-jverticalbar.getValue() / 10));
	}

}
