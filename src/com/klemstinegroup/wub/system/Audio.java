package com.klemstinegroup.wub.system;

import com.echonest.api.v4.Segment;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.*;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.swing.*;

import org.bytedeco.javacv.*;
import org.bytedeco.javacv.Frame;
import org.graphstream.graph.Node;
import org.json.simple.JSONObject;

import static org.bytedeco.javacpp.avutil.AV_PIX_FMT_ARGB;

public class Audio {

    private Java2DFrameConverter converter;
    private Robot robot;
    public static FFmpegFrameRecorder recorder;
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
    public static double maxDuration=1;
    //    private Song cachedSong;
//    private int cachedSongIndex;
    public static ByteArrayOutputStream baos = new ByteArrayOutputStream();
    Node lastNode1 = null;
    Node lastNode2 = null;
    private int start;
    private int lastSeg;
    private Queue<AudioInterval> lastPlayedQueue = new LinkedList<>();

    public Audio() {
        this(null, null, 1);
    }

    public Audio(JFrame jframe, Canvas ip, int numClusters) {

        queue = new LinkedList<AudioInterval>();
        startPlaying(jframe, ip, numClusters);
        try {
            robot = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
        }
        if (Settings.makeVideo) {
            recorder = new FFmpegFrameRecorder(new File("out" + Settings.spotifyId + ".mp4"), jframe.getWidth(), jframe.getHeight(), 2);
            recorder.setSampleRate((int) audioFormat.getSampleRate());
            recorder.setAudioChannels(2);
            recorder.setInterleaved(true);
            recorder.setVideoQuality(0);
//            recorder.setVideoBitrate(10000000);
//            recorder.setAudioBitrate(10000000);
            recorder.setImageWidth(jframe.getWidth());
            recorder.setImageHeight(jframe.getHeight());
            try {
                recorder.start();
                Thread.sleep(2000);
                System.out.println("****recorder started");
            } catch (FrameRecorder.Exception e) {
                e.printStackTrace();
            } catch (InterruptedException e) {

            }
            converter = new Java2DFrameConverter();
        }
    }

    ArrayList<Integer> tem = new ArrayList<>();


    public static HashMap<Integer, Color> randomColor = new HashMap<>();

    static {
        ArrayList<Integer> gill = new ArrayList<>();
        for (int i = 0; i < AudioParams.numClusters; i++) {
            gill.add(i);
        }

        for (int i = 0; i < AudioParams.numClusters; i++) {
            int pos = (int) (Math.random() * gill.size());
            double normd = ((double) pos / (double) AudioParams.numClusters) * 100d;
//            Test3.tf.setBackground(ColorHelper.numberToColor(normd));
            randomColor.put(i, ColorHelper.numberToColor(normd));
            gill.remove(new Integer(pos));
        }

    }

    private void startPlaying(JFrame jframe, Canvas tf, int numClusters) {
        HashMap<String, Integer> hm = new HashMap<>();
        line = getLine();

        new Thread(new Runnable() {
            public void run() {
                int cnt = 25000000;
                top:
                while (cnt-- > 0) {
                    if (!queue.isEmpty()) {
                        cnt = 25000000;
                        AudioInterval audioInterval = queue.poll();
                        currentlyPlaying = audioInterval;

                        if (tf != null) {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {


                                    ArrayList<Segment> list = new ArrayList<>();
                                    Segment bbbb1 = audioInterval.te;
                                    list.add(bbbb1);
//

//                                        double seconds = 1;
//                                        for (Segment s : cachedSong.analysis.getSegments()) {
//                                            seconds = Math.max(seconds, s.getStart() + s.getDuration());
//                                        }
                                    double duration = 1;
                                    duration = audioInterval.te.duration;

                                    if (bbbb1.getStart()+bbbb1.getDuration()>maxDuration){
                                        maxDuration=bbbb1.getDuration()+bbbb1.getStart();
                                    }

                                    BufferedImage bi = new SamplingGraph().createWaveForm(list, duration, audioInterval.data, audioFormat, tf.getWidth(), tf.getHeight());
                                    Graphics g = bi.getGraphics();




//                                        JSONObject js = (JSONObject) cachedSong.analysis.getMap().get("meta");
//                                        String title = null;
//                                        String artist = null;
//                                        String album = null;
//                                        String genre = null;
//
//
//                                        try {
//                                            title = (String) js.get("title");
//                                        } catch (Exception e) {
//                                            e.printStackTrace();
//                                        }
//                                        try {
//                                            artist = (String) js.get("artist");
//                                        } catch (Exception e) {
//                                            e.printStackTrace();
//                                        }
//                                        try {
//                                            album = (String) js.get("album");
//                                        } catch (Exception e) {
//                                            e.printStackTrace();
//                                        }
//                                        try {
//                                            genre = (String) js.get("genre");
//                                        } catch (Exception e) {
//                                            e.printStackTrace();
//                                        }
//                                            try {
//                                                seconds = (Long) js.get("seconds");
//                                            } catch (Exception e) {
//                                                e.printStackTrace();
//                                            }

//                                            String sonTit = "Title: " + artist;
//                                            String sonArt = "Artist: " + title;
//                                        String sonSeq = "#" + i.payloadString.segment;

//                                        Segment seg = cachedSong.analysis.getSegments().get(i.payloadString.segment);
                                    lastPlayedQueue.add(audioInterval);
                                    LinkedList<AudioInterval> lastPlayedQueue1 = new LinkedList();
                                    lastPlayedQueue1.addAll(lastPlayedQueue);
                                    Iterator<AudioInterval> quit = lastPlayedQueue1.iterator();
                                    int cnt = 0;
                                    int qsize = 15;
                                    g.setFont(new Font("Arial", Font.BOLD, 20));
                                    while (quit.hasNext()) {
                                        Segment seg1 = quit.next().te;
                                        g.setColor(ColorHelper.numberToColor((cnt * 100) / lastPlayedQueue.size()));
                                        //System.out.println(seg1+"\t"+seg1.getDuration());
                                        int x = (int) ((bi.getWidth() * seg1.getStart()) / maxDuration) - cnt;
                                        int y = bi.getHeight() / 2 - (bi.getHeight() / 2) * cnt / qsize;
                                        int w = (int) ((bi.getWidth() * seg1.getDuration() * cnt * 2) / maxDuration);
                                        int h = (bi.getHeight()) * cnt / qsize;
                                        g.fillRect(x, y, w, h);
                                        cnt++;
                                    }
                                    while (lastPlayedQueue1.size() > qsize) {
                                        lastPlayedQueue1.removeFirst();
                                    }
                                    lastPlayedQueue = new LinkedList<AudioInterval>();
                                    lastPlayedQueue.addAll(lastPlayedQueue1);

                                    g.setColor(Color.YELLOW);
                                    for (int xi = -1; xi < 2; xi++) {
                                        for (int yi = -1; yi < 2; yi++) {
                                            g.drawString("#"+audioInterval.label, 10 - xi, 25 + yi);
                                        }
                                    }
                                    g.setColor(Color.RED);
                                    g.drawString("#"+audioInterval.label, 10, 25);

                                    //------------------------------------------------
                                    int val = 0;
                                    if (hm.get(lastSeg + "") == null) {
                                        hm.put(lastSeg + "", 0);
                                    } else {
                                        Integer bbbb = hm.get(lastSeg + "");
                                        if (bbbb != null) val = bbbb + 1;
                                        else val = 1;
                                    }
                                    hm.put(lastSeg + "", val);
                                    lastSeg = audioInterval.label;
                                    Color color = ColorHelper.numberToColorPercentage((double) val / (double) AudioParams.maxValue);
                                    if (lastNode1 != null) {
                                        lastNode1.addAttribute("ui.style", "fill-color: rgb(" + color.getRed() + "," + color.getGreen() + "," + color.getBlue() + ");");
                                        lastNode1.addAttribute("ui.style", "size: 15;");
                                    }
                                    if (lastNode2 != null) {
                                        lastNode2.addAttribute("ui.style", "fill-color: rgb(" + color.getRed() + "," + color.getGreen() + "," + color.getBlue() + ");");
                                        lastNode2.addAttribute("ui.style", "size: 15;");
                                    }
                                    if (AudioParams.graph != null) {
                                        Node node1 = AudioParams.graph.getNode(audioInterval.hashCode() + "");
                                        if (node1 != null) {
                                            node1.addAttribute("ui.style", "fill-color: rgb(255,0,0);");
                                            node1.addAttribute("ui.style", "size:25;");
                                        }
                                        lastNode1 = node1;

                                            Node node2 = AudioParams.graph.getNode(audioInterval.hashCode() + "");
                                            if (node2 != null) {
                                                //node2.addAttribute("ui.style", "fill-color: rgb(255,0,0);");
                                                node2.addAttribute("ui.style", "size:20;");
                                            }

                                            lastNode2 = node2;
                                    }
                                    //------------------------------------------------

                                    Graphics gra = tf.getGraphics();
                                    gra.setColor(new Color(0,0,0));
                                    gra.clearRect(0,0,tf.getWidth(),tf.getHeight());
                                    gra.drawImage(bi, 0, 0, null);
                                }
//                                System.out.println("hetre2");


                            }).start();
                        }


                        int j = 0;
                        for (j = 0; j <= audioInterval.data.length - bufferSize; j += bufferSize) {
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
                            line.write(audioInterval.data, j, bufferSize);

                        }

                        if (j < audioInterval.data.length) {
                            position = j;
                            line.write(audioInterval.data, j, audioInterval.data.length - j);
                            // line.drain();
                        }
                        if (loop)
                            queue.add(audioInterval);
                    } else {
                        currentlyPlaying = null;
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                stop();
            }
        }).

                start();

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
        try {
            baos.write(i.data);
        } catch (IOException e) {
            e.printStackTrace();
        }
        queue.add(i);
    }

    public static void stop() {
        if (Settings.makeVideo) {
            try {
                Audio.recorder.stop();
            } catch (FrameRecorder.Exception e) {
                e.printStackTrace();
            }
        }
        System.exit(0);
    }
}
