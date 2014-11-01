package com.klemstinegroup.wub;
import javax.swing.JFrame;

public class TEst {
	public static void main(String[] args) {
//		AudioObject au = new AudioObject("songs/plumber.mp3");
//		AudioObject au1 = new AudioObject("songs/heat.mp3");
		AudioObject au2 = new AudioObject("songs/mylittle.mp3");
		System.out.println("here");
	
//		JFrame frame = new JFrame(au.getFileName());
//		MusicCanvas mc=new MusicCanvas(au);
//		frame.getContentPane().add(mc);
//		frame.setBounds(200, 200, 500, 350);
//		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//
//		frame.show();
//		frame.validate();
//		frame.repaint();
//		
//		JFrame frame1 = new JFrame(au1.getFileName());
//		MusicCanvas mc1=new MusicCanvas(au1);
//		frame1.getContentPane().add(mc1);
//		frame1.setBounds(200, 200, 500, 350);
//		frame1.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//		frame1.show();
//		frame1.validate();
//		frame1.repaint();
		
		JFrame frame2 = new JFrame(au2.getFileName());
		MusicCanvas mc2=new MusicCanvas(au2);
		frame2.getContentPane().add(mc2);
		frame2.setBounds(200, 200, 500, 350);
		frame2.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame2.show();
		frame2.validate();
		frame2.repaint();
		
		
		
		
		// Player player = new Player();
		// player.play(au,0,10);
		//
		// try {
		// Thread.sleep(10000);
		// } catch (InterruptedException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// try {
		//
		// JFrame frame = new JFrame("Waveform Display Simulator");
		// frame.setBounds(200, 200, 500, 350);
		//
		// File file = new File("songs/heat.mp3");
		//
		// WaveformPanelContainer container = new WaveformPanelContainer();
		// container.setAudioToDisplay(file);
		//
		// frame.getContentPane().setLayout(new BorderLayout());
		// frame.getContentPane().add(container, BorderLayout.CENTER);
		//
		// frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//
		// frame.show();
		// frame.validate();
		// frame.repaint();
		//
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
	}
}
