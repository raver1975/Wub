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

public class PlayingField extends Canvas implements MouseListener,
		MouseMotionListener, KeyListener, ComponentListener, MouseWheelListener {

	int oldWidth;
	private JFrame frame;
	private JScrollBar jverticalbar;
	private JScrollBar jhorizontalbar;
	private Image bufferedImage;
	private double offset;
	private Node mover;
	private double movex;
	private double movey;
	private int movex1;
	private int movey1;
	private int currPos;
	private int playPos;
	public transient SourceDataLine line;
	byte[] data;
	private double lengthInPixels;
	private double bytesPerPixel;
	private int lengthInBytes;
	static private int bufferSize = 8192;
	boolean pause = true;
	protected int playByte;

	@Override
	public void update(Graphics g) {
		paint(g);
	}

	@Override
	public void paint(Graphics g) {
		Graphics g1 = bufferedImage.getGraphics();
		g1.setColor(Color.black);
		g1.fillRect(0, 0, getWidth(), getHeight());

		for (Node node : CentralCommand.nodes) {
			g1.drawImage(node.image, (int) (node.rect.x - offset),
					(int) node.rect.y, null);
			g1.setColor(Color.cyan);
			g1.drawRect((int) (node.rect.x - offset), (int) node.rect.y,
					(int) node.rect.width + 1, (int) node.rect.height + 1);
		}
		if (mover != null) {
			g1.setColor(Color.red);
			g1.drawRect((int) (mover.rect.x - offset), (int) mover.rect.y,
					(int) mover.rect.width + 1, (int) mover.rect.height + 1);
		}
		g1.setColor(Color.red);
		g1.drawLine(currPos, 0, currPos, getHeight());
		g1.setColor(Color.white);
		g1.drawLine(playPos, 0, playPos, getHeight());
		g.drawImage(bufferedImage, 0, 0, null);
	}

	public PlayingField() {
		oldWidth = 800;
		setSize(new Dimension(oldWidth, 760));
		frame = new JFrame("Field Of Play");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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
		// frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		// frame.addWindowListener(new WindowAdapter() {
		// @Override
		// public void windowClosing(WindowEvent e) {
		// // au.pause = true;
		// // au.breakPlay = true;
		// // au.queue.clear();
		// // CentralCommand.remove(au);
		// }
		// });

		frame.getContentPane().add(jverticalbar, "East");
		frame.getContentPane().add(jhorizontalbar, "South");

		frame.setBounds(100, 100, oldWidth + 50, 760);
		frame.setVisible(true);
		frame.validate();
		frame.repaint();
		// makeImageResize();
		// makeData();

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
					if (pause) {
						try {
							Thread.sleep(10);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						continue top;
					}
					playPos = (int) (((double) playByte / (double) lengthInBytes)
							* lengthInPixels - offset);
					playByte += bufferSize;

					if (playByte + bufferSize >= data.length) {
						line.write(data, playByte, data.length - playByte);
						pause = true;
						continue top;
					}

					line.write(data, playByte, bufferSize);
				}
			}
		}).start();
	}

	protected void makeImageResize() {
		System.out.println("makeimage");
		double max = Double.MIN_VALUE;
		for (Node node : CentralCommand.nodes) {
			if (node.ao.data.length > max) {
				max = node.ao.data.length;
			}
		}
		for (Node node : CentralCommand.nodes) {
			node.image = new SamplingGraph().createWaveForm(
					node.ao.analysis.getSegments(),
					node.ao.analysis.getDuration(), node.ao.data,
					AudioObject.audioFormat, (int) (node.ao.data.length
							* (double) oldWidth / (max)), 40);
			double oldbb = node.rect.width;
			node.rect.width = (node.ao.data.length * (double) oldWidth / max);
			node.rect.x /= oldbb / node.rect.width;
		}
		makeData();
	}

	public void makeData() {
		if (CentralCommand.nodes.size() == 0)
			return;
		double minx = Double.MAX_VALUE;
		double maxx = Double.MIN_VALUE;
		for (Node node : CentralCommand.nodes) {
			if (node.rect.x < minx)
				minx = node.rect.x;
			if (node.rect.width + node.rect.x > maxx)
				maxx = node.rect.width + node.rect.x;

		}
		// minx--;
		// maxx--;
		lengthInPixels = maxx - minx;
		bytesPerPixel = CentralCommand.nodes.get(0).ao.data.length
				/ CentralCommand.nodes.get(0).rect.width;
		lengthInBytes = (int) (lengthInPixels * bytesPerPixel);
		lengthInBytes += lengthInBytes % AudioObject.frameSize;
		data = new byte[lengthInBytes];
		System.out.println("length=" + lengthInBytes);
		for (Node node : CentralCommand.nodes) {
			System.out.println(node.ao.data.length);
			System.out.println(node.rect.x);
			System.out.println((node.rect.x - minx));
			int start = (int) ((double) (node.rect.x - minx)
					/ (double) lengthInPixels * (double) lengthInBytes);
			start -= start % AudioObject.frameSize;
			System.out.println(node.rect.x + "\t" + "start=" + start);
			for (int i = 0; i < node.ao.data.length; i++) {
				short g = data[i + start];
				g += node.ao.data[i];
				if (g > 128)
					g = 128;
				else if (g < -127)
					g = -127;
				data[i + start] = (byte) g;
			}
			// System.arraycopy(node.ao.data, 0, data, start,
			// node.ao.data.length);

			// System.out.println("dur=" + duration + "\t"
			// + node.ao.analysis.getDuration());
		}
		// for (AudioObject au : CentralCommand.aolist) {
		// for (Rectangle r : au.playFieldPosition) {
		//
		// }
		// }

	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_UP) {
			if (mover != null)
				mover.rect.y--;
			// jverticalbar.setValue(jverticalbar.getValue() -
			// jverticalbar.getUnitIncrement());
		} else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
			// jverticalbar.setValue(jverticalbar.getValue() +
			// jverticalbar.getUnitIncrement());
			if (mover != null)
				mover.rect.y++;
		} else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
			// jhorizontalbar.setValue(jhorizontalbar.getValue() -
			// jhorizontalbar.getUnitIncrement());
			if (mover != null)
				mover.rect.x--;
		} else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
			// jhorizontalbar.setValue(jhorizontalbar.getValue() +
			// jhorizontalbar.getUnitIncrement());
			if (mover != null)
				mover.rect.x++;
		} else if (e.getKeyCode() == KeyEvent.VK_INSERT && mover.ao != null) {
			Node n = CentralCommand.addRectangle(mover.ao);
			n.rect.y = mover.rect.y;
			n.rect.x = mover.rect.x + mover.rect.width;
			makeImageResize();
			while (CentralCommand.intersects(n)) {
				n.rect.x++;
			}
		}

		else if (e.getKeyCode() == KeyEvent.VK_DELETE) {
			CentralCommand.removeRectangle(mover);
			mover = null;
			makeImageResize();
		} else if (e.getKeyCode() == KeyEvent.VK_SPACE) {
			pause = !pause;
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
			mover.rect.x = x - movex1 + movex;
			mover.rect.y = y - movey1 + movey;
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
		for (Node node : CentralCommand.nodes) {
			if (node.rect.contains(p)) {
				mover = node;
				movex = mover.rect.x;
				movey = mover.rect.y;
				movex1 = x;
				movey1 = y;
				break;
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
		if (e.getButton() == MouseEvent.BUTTON3) {
			pause = false;
			// playPos = (int) (((double) playByte / (double) lengthInBytes) *
			// getWidth());
			// lengthinbytes*playpos/width=playbyte
			playByte = (int) (lengthInBytes * (double) (x + offset) / lengthInPixels);
			playByte += playByte % AudioObject.frameSize;
			if (playByte < 0)
				playByte = 0;
			pause = false;
		}
		mover = null;
		for (Node node : CentralCommand.nodes) {
			if (node.rect.contains(p)) {
				mover = node;
				movex = mover.rect.x;
				movey = mover.rect.y;
				movex1 = x;
				movey1 = y;
				if (e.getClickCount() == 2) {
					mover.ao.mc.frame.setVisible(true);
				}
				break;
			}
		}

	}

	@Override
	public void mouseReleased(MouseEvent e) {
		makeData();

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
		bufferedImage = new BufferedImage(this.getWidth(), this.getHeight(),
				BufferedImage.TYPE_INT_ARGB);
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
		jverticalbar.setValue(jverticalbar.getValue()
				+ e.getWheelRotation()
				* ((-jverticalbar.getValue() / 10) + -jverticalbar.getValue()
						/ jverticalbar.getValue()));
	}

	public SourceDataLine getLine() {
		SourceDataLine res = null;
		DataLine.Info info = new DataLine.Info(SourceDataLine.class,
				AudioObject.audioFormat);
		try {
			res = (SourceDataLine) AudioSystem.getLine(info);
			res.open(AudioObject.audioFormat);
			res.start();
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		}
		return res;
	}

	public int convertTimeToByte(double time) {
		int c = (int) (time * AudioObject.sampleRate * AudioObject.frameSize);
		c += c % AudioObject.frameSize;
		return c;
	}

	// public int convertByteToPixel(int loc){
	// (double)loc/double()getWidth
	// }
}
