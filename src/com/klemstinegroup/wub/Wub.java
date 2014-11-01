package com.klemstinegroup.wub;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class Wub {

	public Wub() {
		JPanel panel = new JPanel();
		panel.setLayout(null);
		JFrame f = new JFrame();

		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		f.setSize(400, 400);
		f.setLocation(100, 100);
		JLabel lab1 = new JLabel("t1");
		JLabel lab2 = new JLabel("t2");
		JLabel lab3 = new JLabel("t3");
		lab1.setBounds(10, 10, 100, 100);
		lab2.setBounds(30, 10, 100, 100);
		lab3.setBounds(60, 10, 100, 100);
		panel.add(lab1);
		panel.add(lab2);
		panel.add(lab3);
		f.add(panel);
		ComponentMover cm = new ComponentMover();
		cm.registerComponent(lab1, lab2, lab3);
		f.setVisible(true);
	}

	public static void main(String[] args) {
		new Wub();

	}

}
