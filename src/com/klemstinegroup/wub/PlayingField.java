package com.klemstinegroup.wub;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
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

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.swing.JFrame;
import javax.swing.JScrollBar;

public class PlayingField extends Canvas implements MouseListener, MouseMotionListener, KeyListener, ComponentListener, MouseWheelListener {

	int oldWidth;
	private JFrame frame;
	private JScrollBar jverticalbar;
	private JScrollBar jhorizontalbar;
	private Image bufferedImage;
	private int offset;
	private Rectangle mover;
	private int movex;
	private int movey;
	private int movex1;
	private int movey1;
	private int currPos;
	public transient SourceDataLine line;
	byte[] data;
	static private int bufferSize=8192;

	@Override
	public void update(Graphics g) {
		paint(g);
	}

	@Override
	public void paint(Graphics g) {
		bufferedImage = new BufferedImage(this.getWidth(), this.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics g1 = bufferedImage.getGraphics();
		g1.setColor(Color.black);
		g1.fillRect(0, 0, getWidth(), getHeight());

		for (AudioObject au : CentralCommand.aolist) {
			for (Rectangle r : au.playFieldPosition) {
				g1.drawImage(au.playFieldImage, r.x - offset, r.y, null);
				g1.setColor(Color.cyan);
				g1.drawRect(r.x - offset, r.y, r.width, r.height);
			}
		}
		g1.setColor(Color.red);
		g1.drawLine(currPos, 0, currPos, getHeight());
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
		jverticalbar.setMinimum(1);
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
        startPlaying();
	}
	
	private void startPlaying() {
		line = getLine();
		new Thread(new Runnable() {


			public void run() {
				top: while (true) {

						
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
			au.playFieldImage = new SamplingGraph().createWaveForm(au.analysis.getSegments(), au.analysis.getDuration(), au.data, AudioObject.audioFormat, (int) (au.analysis.getDuration() * (double) oldWidth / (max)), 40);
			for (Rectangle r : au.playFieldPosition) {
				r.width = (int) (au.analysis.getDuration() * (double) oldWidth / max);
			}
		}
		makeData();
	}
	
	public void makeData(){
		int min=Integer.MAX_VALUE;
		int max=Integer.MIN_VALUE;
		for (AudioObject au : CentralCommand.aolist) {
			for (Rectangle r:au.playFieldPosition){
				if (r.x<min)min=r.x;
				if (r.y>max)max=r.y;
			}
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_UP) {
			if (mover != null)
				mover.y--;
			// jverticalbar.setValue(jverticalbar.getValue() -
			// jverticalbar.getUnitIncrement());
		} else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
			// jverticalbar.setValue(jverticalbar.getValue() +
			// jverticalbar.getUnitIncrement());
			if (mover != null)
				mover.y++;
		} else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
			// jhorizontalbar.setValue(jhorizontalbar.getValue() -
			// jhorizontalbar.getUnitIncrement());
			if (mover != null)
				mover.x--;
		} else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
			// jhorizontalbar.setValue(jhorizontalbar.getValue() +
			// jhorizontalbar.getUnitIncrement());
			if (mover != null)
				mover.x++;
		}

	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseDragged(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();
		Point p = new Point(x, y);
		if (mover != null) {
			mover.x = x - movex1 + movex;
			mover.y = y - movey1 + movey;
		}
		// for (AudioObject au : CentralCommand.aolist) {
		// for (Rectangle r : au.playFieldPosition) {
		// if (r.contains(p)) {
		// mover = r;
		// break;
		// }
		// }
		// }

	}

	@Override
	public void mouseMoved(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();
		Point p = new Point(x, y);
		if (!frame.isActive()) {
			frame.requestFocus();
			frame.toFront();
			try {
				Thread.sleep(10);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		for (AudioObject au : CentralCommand.aolist) {
			for (Rectangle r : au.playFieldPosition) {
				if (r.contains(p)) {
					mover = r;
					movex = mover.x;
					movey = mover.y;
					movex1 = x;
					movey1 = y;
					break;
				}
			}
		}

		currPos = e.getX();

	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mousePressed(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();
		Point p = new Point(x, y);
		mover = null;
		for (AudioObject au : CentralCommand.aolist) {
			for (Rectangle r : au.playFieldPosition) {
				if (r.contains(p)) {
					mover = r;
					movex = mover.x;
					movey = mover.y;
					movex1 = x;
					movey1 = y;
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
}
