package com.klemstinegroup.wub2.system;

import com.klemstinegroup.wub2.test.Test3;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class Audio {

    public transient SourceDataLine line;
    public transient Queue<AudioInterval> queue;

    transient int position = 0;
    transient AudioInterval currentlyPlaying;
    protected transient boolean breakPlay;
    public transient boolean pause = false;
    public transient boolean loop = false;

    public static final int resolution = 16;
    public static final int channels = 2;
    public static final int frameSize = channels * resolution / 8;
    public static final int sampleRate = 44100;
    public static final AudioFormat audioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, sampleRate, resolution, channels, frameSize, sampleRate, false);
    public static final AudioFormat audioFormatMono = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, sampleRate, resolution, channels / 2, frameSize / 2, sampleRate, false);
    public static final int bufferSize = 8192;

    public Audio() {
        queue = new LinkedList<AudioInterval>();
        startPlaying();
    }

    ArrayList<Integer> tem = new ArrayList<>();

    private void startPlaying() {
        line = getLine();
        new Thread(new Runnable() {
            public void run() {
                top:
                while (true) {
                    if (!queue.isEmpty()) {
                        AudioInterval i = queue.poll();

                        currentlyPlaying = i;
                        if (i.payload != null) {
                            System.out.println("now playing " + i.payload);

                            if (Test3.tf != null) {

                                if (!tem.contains(i.payload.segment))
                                    tem.add(i.payload.segment);
                                Test3.tf.append(tem.indexOf(i.payload.segment) + "\n");

//								Test3.tf.setText(i.payload.segment+"");
                            }


                        }
                        int j = 0;
                        for (j = 0; j <= i.data.length - bufferSize; j += bufferSize) {
                            while (pause || breakPlay) {
                                if (breakPlay) {
                                    breakPlay = false;
                                    try {
                                        Thread.sleep(10);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    continue top;
                                }
                                try {
                                    Thread.sleep(10);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                            position = j;
                            line.write(i.data, j, bufferSize);

                        }

                        if (j < i.data.length) {
                            position = j;
                            line.write(i.data, j, i.data.length - j);
                            // line.drain();
                        }
                        if (loop)
                            queue.add(i);
                    } else

                        currentlyPlaying = null;
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public SourceDataLine getLine() {
        SourceDataLine res = null;
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, Audio.audioFormat);
        try {
            res = (SourceDataLine) AudioSystem.getLine(info);
            res.open(Audio.audioFormat);
            res.start();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
        return res;
    }

    public void play(AudioInterval i) {
        queue.add(i);
    }

}
