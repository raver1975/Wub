package com.klemstinegroup.wub2.test;

import oracle.jrockit.jfr.JFR;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Paul on 2/14/2017.
 */
public final class JavaProcess {

//    private static boolean timerOn=true;

    public static void main(String[] args) {
        JFrame jframe = new JFrame("exit");
        Button cancel = new Button("exit");
        jframe.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {

            }

            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }

            @Override
            public void windowClosed(WindowEvent e) {
                System.exit(0);
            }

            @Override
            public void windowIconified(WindowEvent e) {

            }

            @Override
            public void windowDeiconified(WindowEvent e) {

            }

            @Override
            public void windowActivated(WindowEvent e) {

            }

            @Override
            public void windowDeactivated(WindowEvent e) {

            }
        });
        cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        jframe.getContentPane().add(cancel);
        jframe.pack();
        jframe.setVisible(true);

        while (true) {
            try {
                exec(BeautifulKMGSRandReduce.class,jframe);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private JavaProcess() {
    }

    public static int exec(Class klass, JFrame jframe) throws IOException,
            InterruptedException {
        String javaHome = System.getProperty("java.home");
        String javaBin = javaHome +
                File.separator + "bin" +
                File.separator + "java";
        String classpath = System.getProperty("java.class.path");
        String className = klass.getCanonicalName();

        ProcessBuilder builder = new ProcessBuilder(
                javaBin,"-Xmx4000m", "-cp", classpath, className);

        Process process = builder.start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(BeautifulKMGSRandReduce.makeVideo ? (45 * 60 * 1000) : (5 * 60 * 1000));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (process.isAlive()) {
//                    process.destroyForcibly();
                    jframe.dispose();
                }

            }
        }).start();
        InputStream is = process.getInputStream();
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (process.isAlive()) {
                    try {
                        while (is.available() > 0) {
                            System.out.print((char) is.read());
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        }).start();
        process.waitFor();
        return process.exitValue();
    }

}
