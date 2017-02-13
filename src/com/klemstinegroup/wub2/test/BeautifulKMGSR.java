package com.klemstinegroup.wub2.test;

import com.echonest.api.v4.Segment;
import com.klemstinegroup.wub2.system.Audio;
import com.klemstinegroup.wub2.system.AudioInterval;
import com.klemstinegroup.wub2.system.LoadFromFile;
import com.klemstinegroup.wub2.system.Song;
import com.mxgraph.layout.mxFastOrganicLayout;
import org.bytedeco.javacv.FrameRecorder;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.ui.layout.springbox.implementations.SpringBox;
import org.graphstream.ui.view.View;
import org.graphstream.ui.view.Viewer;
import org.json.simple.JSONObject;
import weka.clusterers.SimpleKMeans;
import weka.core.*;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.swing.*;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class BeautifulKMGSR {


    //    static String directory = "e:\\wub\\";
    private static final File[] list;

    static final int attLength = 28;
    public static Attribute[] attlist;

    static boolean enableAudioDuringTraining = true;

    static int[] playback = new int[]{430};
    public static final int decreaseClustersBy = 50*playback.length;
    static int newSongLength = 25000;

    public static boolean makeVideo = true;
    private static boolean addTrackInfo=false;

    public static int numClusters = -1;


//    static int playbackStart = playback;
//    static int playbackEnd = playback + stretch;


    static float pitchFactor = 17f;
    static float timbreFactor = 17f;
    static float loudFactor = 70f;
    static float durationFactor = 90f;

    public static ImagePanel tf;
    public static MultiGraph graph;
    public static int maxValue;
    private static SegmentSong firstSaved = null;
    private static int width=1400;
    private static int height=900;

//    public static HashMap<String,Integer> hm;


    static {
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        File[] list1 = new File(SongManager.directory).listFiles();
        ArrayList<File> al = new ArrayList<>();
        for (File f : list1) {
            if (f.getAbsolutePath().endsWith(".au")) al.add(f);
        }
        list = al.toArray(new File[]{});

    }


    public static void main(String[] args) {
        int totsegm = 0;
        JTextArea jta = new JTextArea(4, 20);
        JFrame jframe = new JFrame("Wub");
        jframe.setSize(width, height);
        for (int v : playback) {
            Song song1 = SongManager.getRandom(v);
            JSONObject js = (JSONObject) song1.analysis.getMap().get("meta");
            String title = null;
            String artist = null;
            String album = null;
            String genre = null;
            Long seconds = null;

            try {
                title = (String) js.get("title");
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                artist = (String) js.get("artist");
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                album = (String) js.get("album");
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                genre = (String) js.get("genre");
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                seconds = (Long) js.get("seconds");
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (seconds == null || seconds == 0) seconds = new Long(-61);


            int segm = song1.analysis.getSegments().size();
            totsegm += segm;
            float scale = (int) (((float) numClusters / (float) song1.analysis.getSegments().size()) * 1000) / 10f;

            System.out.println("segments size=" + segm + "\t" + scale + "%");
            System.out.println("title\t" + title);
            System.out.println("artist\t" + artist);
            System.out.println("album\t" + album);
            System.out.println("genre\t" + genre);
            String secs=seconds%60+"";
            while (secs.length()<2)secs="0"+secs;
            System.out.println("time\t" + seconds / 60 + ":" + secs);
            jta.append("Title\t" + title);
            jta.append("\n");
            jta.append("Artist\t" + artist);
            jta.append("\n");
            jta.append("Album\t" + album);
            jta.append("\n");
            jta.append("Genre\t" + genre);
            jta.append("\n");
            jta.append("Time\t" + seconds / 60 + ": " + secs);
            jta.append("\n");
            jta.append("----------------------------");
            jta.append("\n");
        }
        numClusters = totsegm - decreaseClustersBy;
        System.out.println("total segments=" + totsegm);
        System.out.println("nnum clusters=" + numClusters);

//        frame.setSize(400, 300);
        tf = new ImagePanel();
        tf.setFont(new Font("Arial", Font.BOLD, 300));
//        JScrollPane jscr = new JScrollPane(tf);

//        DefaultCaret caret = (DefaultCaret) tf.getCaret();
//        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
//        frame.add(jscr);
//        frame.setVisible(true);

        //one time attribute setup
        FastVector attrs = new FastVector();
        attlist = new Attribute[attLength];
        for (int io = 0; io < attLength; io++) {
            attlist[io] = new Attribute("at" + io);
            attrs.addElement(attlist[io]);
        }


        //kmeans setup
        SimpleKMeans kmeans = new SimpleKMeans();
//        kmeans.setSeed(10);

        try {
            kmeans.setNumClusters(numClusters);
            kmeans.setDistanceFunction(new ManhattanDistance());
            String[] options = Utils.splitOptions("-I 100");
            kmeans.setOptions(options);

//            kmeans.setMaxIterations(100);
        } catch (Exception e) {
            e.printStackTrace();
        }
        ArrayList<SegmentSong> coll = new ArrayList<>();
        Instances dataset = new Instances("my_dataset", attrs, 0);
        SegmentSong[] lastSeen = new SegmentSong[numClusters];
        for (int songIter : playback) {


//            datasets[songIter] = dataset;

            Song song = LoadFromFile.loadSong(list[songIter]);
            int cnt = 0;
            for (Segment s : song.analysis.getSegments()) {
                Instance inst = getInstance(attlist, s);
                coll.add(new SegmentSong(songIter, cnt++));
                inst.setDataset(dataset);
                dataset.add(inst);
            }
        }

        long time = System.currentTimeMillis();
        System.out.println("building cluster");
        try {
            kmeans.buildClusterer(dataset);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("cluster built: " + ((System.currentTimeMillis() - time) / 1000));

        // print out the cluster centroids
        Instances centroids = kmeans.getClusterCentroids();
        for (int io = 0; io < centroids.numInstances(); io++) {
            double dist = Float.MAX_VALUE;
            int best = -1;
            for (int j = 0; j < dataset.numInstances(); j++) {
                double dd = distance(centroids.instance(io), dataset.instance(j));
//                System.out.println("dist="+dd);
                if (dd < dist) {
                    dist = dd;
                    best = j;
                }
            }
            SegmentSong gg = coll.get(best);
            lastSeen[io] = gg;
//            System.out.println("centroid io " + io + "\t" + gg);


        }
        // get cluster membership for each instance
        HashMap<SegmentSong, SegmentSong> map = new HashMap<>();
        for (int io = 0; io < dataset.numInstances(); io++) {
            try {
                int cluster = kmeans.clusterInstance(dataset.instance(io));
                SegmentSong tempSegmentSong = lastSeen[cluster];
                map.put(coll.get(io), tempSegmentSong);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        ObjectManager.write(map, "map-universal.ser");


        Audio audio = new Audio(jframe, tf, numClusters);


        graph = new MultiGraph("id");
        graph.addAttribute("ui.quality");
        graph.addAttribute("ui.antialias");
        Viewer viewer = new Viewer(graph, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
        SpringBox sb=new SpringBox();
        sb.setForce(1.5f);
//        sb.setQuality(0);
        viewer.enableAutoLayout(sb);
        View view = viewer.addDefaultView(false);
        final SegmentSong[] startNode = {new SegmentSong(playback[0], 0)};

//        for (int cnt=0;cnt<5000;cnt++){
//            graph.addNode(cnt+"");
//        }
        HashMap<Integer, SegmentSong> nodes = new HashMap<>();
        HashSet<Integer> nodeset = new HashSet<>();
        final Song[] tempSong = {null};
        final int[] lastSong = {-1};
//        HashSet<String> edges = new HashSet<>();
        int cnt2 = 0;
        for (int songToPlay : playback) {
            Song song = SongManager.getRandom(songToPlay);

            for (int cnt = 0; cnt < song.analysis.getSegments().size(); cnt++) {
                SegmentSong pp = new SegmentSong(songToPlay, cnt);
                SegmentSong play = map.get(pp);
//            }
                if (play == null) {
                    System.out.println("null! " + pp);
                    continue;
                }

                if (!nodeset.contains(startNode[0].hashCode())) {
                    Node n=graph.addNode(startNode[0].hashCode() + "");
                    nodeset.add(startNode[0].hashCode());
                }
                if (!nodeset.contains(play.hashCode())) {
                    graph.addNode(play.hashCode() + "");
                    nodeset.add(play.hashCode());
                }
                graph.addEdge((cnt2) + "", startNode[0].hashCode() + "", play.hashCode() + "", true);
                if (nodes.isEmpty()) firstSaved = play;
                nodes.put(play.hashCode(), play);
                startNode[0] = new SegmentSong(play.song, play.segment);
                cnt2++;
            }
            graph.addEdge((cnt2++) + "", startNode[0].hashCode() + "", firstSaved.hashCode() + "", true);
        }


        jframe.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                if (makeVideo) {
                    try {
                        Audio.recorder.stop();
                    } catch (FrameRecorder.Exception e) {
                        e.printStackTrace();
                    }
                }
                System.exit(0);

            }
        });

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        jframe.add(panel);


        panel.add("Center", viewer.getDefaultView());
        tf.setMinimumSize(new Dimension(100, 100));
        tf.setPreferredSize(new Dimension(100, 100));
        panel.add("North", tf);
        if (addTrackInfo)panel.add("West", jta);
        jframe.setVisible(true);
        for (int i=0;i<graph.getNodeCount();i++){
            graph.getNode(i).setAttribute("x",Math.random()*width);
            graph.getNode(i).setAttribute("y",Math.random()*height);
        }

        HashMap<String, Integer> hm = new HashMap<>();

        startNode[0] = new SegmentSong(playback[0], 0);

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (newSongLength-- > 0) {
                    if (lastSong[0] != startNode[0].song) {
                        tempSong[0] = SongManager.getRandom(startNode[0].song);
                        lastSong[0] = startNode[0].song;
                    }
                    AudioInterval ai = tempSong[0].getAudioInterval(tempSong[0].analysis.getSegments().get(startNode[0].segment));
                    ai.payload = new SegmentSong(startNode[0].song, startNode[0].segment);
                    audio.play(ai);

                    Iterator<Edge> adj = graph.getNode(startNode[0].hashCode() + "").getEachLeavingEdge().iterator();
                    ArrayList<Edge> temp = new ArrayList<>();
                    int lowest = 0;
                    int lowestValue = Integer.MAX_VALUE;
//            int cnt = 0;
                    while (adj.hasNext()) {
                        Edge bb = adj.next();
                        temp.add(bb);
                    }
                    int cnt1 = 0;
                    Collections.shuffle(temp);
                    for (Edge bb : temp) {
                        String key = bb.getNode1().getId();
                        if (!hm.containsKey(key)) {
                            hm.put(key, 0);
                        }
                        int val = hm.get(key);
                        if (val < lowestValue) {
                            lowestValue = val;
                            lowest = cnt1;

                        }
                        cnt1++;
                    }


//            int next = (int) (Math.random() * temp.size());
                    int next = lowest;

//            System.out.println("going down: " + next + " out of " + temp.size());
                    if (temp.size() == 0) startNode[0] = firstSaved;
                    else {
                        Edge selected = temp.get(next);
                        startNode[0] = nodes.get(Integer.parseInt(selected.getNode1().getId()));

                    }
                    String key = startNode[0].hashCode() + "";

                    if (!hm.containsKey(key)) {
                        hm.put(key, 0);
                    }
                    int val = hm.get(key);
                    hm.put(key, val + 1);
                    maxValue = Math.max(maxValue, val + 1);
//            System.out.println(Arrays.toString(bb));

                }

                byte[] output = Audio.baos.toByteArray();
                try {
                    AudioSystem.write(new AudioInputStream(new ByteArrayInputStream(output), Audio.audioFormat, output.length), AudioFileFormat.Type.WAVE, new File("out.wav"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

//        while (iter.hasNext()){
//            Object bbb = iter.next();
//
//            Map.Entry<String, String> bbe = (Map.Entry<String, String>) bbb;
//            System.out.println(bbb.toString());
//


    }

    protected static double distance(Instance i1, Instance i2) {
        double tot = 0;
        for (int i = 0; i < attLength; i++) {
            double ta = i1.value(attlist[i]) - i2.value(attlist[i]);
            ta = Math.abs(ta);
            tot += ta;
        }
        return tot;
    }

    protected static Instance getInstance(Attribute[] attlist, Segment s) {

        int cnt = 0;
        Instance inst = new Instance(attLength);
        inst.setValue(attlist[cnt++], s.getDuration() * durationFactor);
        inst.setValue(attlist[cnt++], s.getLoudnessMax() * loudFactor);
        inst.setValue(attlist[cnt++], s.getLoudnessStart() * loudFactor);
        inst.setValue(attlist[cnt++], s.getLoudnessMaxTime() * loudFactor);
        inst.setValue(attlist[cnt++], timbreFactor * s.getTimbre()[0]);
        inst.setValue(attlist[cnt++], timbreFactor * s.getTimbre()[1]);
        inst.setValue(attlist[cnt++], timbreFactor * s.getTimbre()[2]);
        inst.setValue(attlist[cnt++], timbreFactor * s.getTimbre()[3]);
        inst.setValue(attlist[cnt++], timbreFactor * s.getTimbre()[4]);
        inst.setValue(attlist[cnt++], timbreFactor * s.getTimbre()[5]);
        inst.setValue(attlist[cnt++], timbreFactor * s.getTimbre()[6]);
        inst.setValue(attlist[cnt++], timbreFactor * s.getTimbre()[7]);
        inst.setValue(attlist[cnt++], timbreFactor * s.getTimbre()[8]);
        inst.setValue(attlist[cnt++], timbreFactor * s.getTimbre()[9]);
        inst.setValue(attlist[cnt++], timbreFactor * s.getTimbre()[10]);
        inst.setValue(attlist[cnt++], timbreFactor * s.getTimbre()[11]);
        inst.setValue(attlist[cnt++], s.getPitches()[0] * pitchFactor);
        inst.setValue(attlist[cnt++], s.getPitches()[1] * pitchFactor);
        inst.setValue(attlist[cnt++], s.getPitches()[2] * pitchFactor);
        inst.setValue(attlist[cnt++], s.getPitches()[3] * pitchFactor);
        inst.setValue(attlist[cnt++], s.getPitches()[4] * pitchFactor);
        inst.setValue(attlist[cnt++], s.getPitches()[5] * pitchFactor);
        inst.setValue(attlist[cnt++], s.getPitches()[6] * pitchFactor);
        inst.setValue(attlist[cnt++], s.getPitches()[7] * pitchFactor);
        inst.setValue(attlist[cnt++], s.getPitches()[8] * pitchFactor);
        inst.setValue(attlist[cnt++], s.getPitches()[9] * pitchFactor);
        inst.setValue(attlist[cnt++], s.getPitches()[10] * pitchFactor);
        inst.setValue(attlist[cnt++], s.getPitches()[11] * pitchFactor);
        return inst;
    }


}


