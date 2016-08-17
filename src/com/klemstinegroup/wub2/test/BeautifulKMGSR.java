package com.klemstinegroup.wub2.test;

import com.echonest.api.v4.Segment;
import com.klemstinegroup.wub2.system.Audio;
import com.klemstinegroup.wub2.system.AudioInterval;
import com.klemstinegroup.wub2.system.LoadFromFile;
import com.klemstinegroup.wub2.system.Song;
import org.graphstream.graph.Edge;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.ui.view.View;
import org.graphstream.ui.view.Viewer;
import org.jgrapht.alg.EulerianCircuit;
import org.json.simple.JSONObject;
import weka.clusterers.SimpleKMeans;
import weka.core.*;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.swing.*;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class BeautifulKMGSR {


    static String directory = "e:\\wub\\";
    private static final File[] list;

    static final int attLength = 28;
    public static Attribute[] attlist;

    //230
    //245 uk dubstep tutorial
    //246 good dub with voice
    //290 is good
    //296 bassnectar lights
    //298 vnv nation
    //300 icon of coil machines are us
    //301 zeds dead, eyes on fire
    //310 convenent bullet
    //316 bassnectar enter the chamber
    //323 bassnectar
    //404 NIN slave screams
    //407 rotersand almost wasted
    //423 vnv nation true life remix
    //430 gemini blue
    //439 bassnectar nothing has been broken
    //449 bassnectar paging sterophonic
    //456 bassnectar timestretch
    //1016 bassnectar basshead

//    public static JFrame frame = new JFrame(BeautifulKMGSR.class.toString());

    //    static boolean rnn = true;
    static boolean enableAudioDuringTraining = true;
//    private static boolean loadPrevSavedModel = true;

    static int playback = 1016;
    static int stretch = 3;
    static int playbackStart = playback;
    static int playbackEnd = playback + stretch;


    public static final int numClusters = 3100;

    static float pitchFactor = 17f;
    static float timbreFactor = 17f;
    static float loudFactor = 70f;
    static float durationFactor = 90f;

    public static ImagePanel tf;
    public static MultiGraph graph;
    public static int maxValue;
    private static SegmentSong firstSaved = null;
//    public static HashMap<String,Integer> hm;


    static {
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        File[] list1 = new File(directory).listFiles();
        ArrayList<File> al = new ArrayList<>();
        for (File f : list1) {
            if (f.getAbsolutePath().endsWith(".au")) al.add(f);
        }
        list = al.toArray(new File[]{});

    }


    public static void main(String[] args) {


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
        for (int songIter = playbackStart; songIter < list.length && songIter < playbackEnd; songIter++) {


//            datasets[songIter] = dataset;

            Song song = LoadFromFile.loadSong(list[songIter]);
            System.out.println("processing song #" + ((songIter - playbackStart) + 1) + "/" + Math.min(list.length, playbackEnd - playbackStart) + "\t" + list[songIter].getName());
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
            System.out.println("centroid io " + io + "\t" + gg);


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


        Audio audio = new Audio(tf, numClusters);


        graph = new MultiGraph("id");
//        graph.addAttribute("ui.quality");
//        graph.addAttribute("ui.antialias");
        Viewer viewer = new Viewer(graph, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
        viewer.enableAutoLayout();
        View view = viewer.addDefaultView(false);

        SegmentSong startNode = new SegmentSong(playback, 0);

//        for (int cnt=0;cnt<5000;cnt++){
//            graph.addNode(cnt+"");
//        }
        HashMap<Integer, SegmentSong> nodes = new HashMap<>();
        HashSet<Integer> nodeset = new HashSet<>();
        Song tempSong = null;
        int lastSong = -1;
//        HashSet<String> edges = new HashSet<>();
        int cnt2 = 0;
        for (int jj = 0; jj < stretch; jj++) {
            Song song = SongManager.getRandom(playback + jj);

            for (int cnt = 0; cnt < song.analysis.getSegments().size(); cnt++) {
                SegmentSong pp = new SegmentSong(playback + jj, cnt);
                SegmentSong play = map.get(pp);
//            }
                if (play == null) {
                    System.out.println("null! " + pp);
                    continue;
                }

                if (!nodeset.contains(startNode.hashCode())) {
                    graph.addNode(startNode.hashCode() + "");
                    nodeset.add(startNode.hashCode());
                }
                if (!nodeset.contains(play.hashCode())) {
                    graph.addNode(play.hashCode() + "");
                    nodeset.add(play.hashCode());
                }
                graph.addEdge((cnt2) + "", startNode.hashCode() + "", play.hashCode() + "", true);
                if (nodes.isEmpty()) firstSaved = play;
                nodes.put(play.hashCode(), play);
                startNode = new SegmentSong(play.song, play.segment);
                cnt2++;
            }
            graph.addEdge((cnt2++) + "", startNode.hashCode() + "", firstSaved.hashCode() + "", true);
        }


        JFrame jframe = new JFrame("graphstream");
        jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jframe.setSize(800, 600);
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        jframe.add(panel);
        panel.add("Center", viewer.getDefaultView());
        tf.setMinimumSize(new Dimension(100, 100));
        tf.setPreferredSize(new Dimension(100, 100));
        panel.add("North", tf);
        jframe.setVisible(true);


//        JSONObject js = (JSONObject) song.analysis.getMap().get("meta");
//        String title = null;
//        String artist = null;
//        String album = null;
//        String genre = null;
//        Long seconds = null;
//
//        try {
//            title = (String) js.get("title");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        try {
//            artist = (String) js.get("artist");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        try {
//            album = (String) js.get("album");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        try {
//            genre = (String) js.get("genre");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        try {
//            seconds = (Long) js.get("seconds");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        if (seconds == null || seconds == 0) seconds = new Long(-61);
//
//        float scale = (int) (((float) numClusters / (float) song.analysis.getSegments().size()) * 1000) / 10f;
//        System.out.println("segments size=" + song.analysis.getSegments().size() + "\t" + scale + "%");
//        System.out.println("title\t" + title);
//        System.out.println("artist\t" + artist);
//        System.out.println("album\t" + album);
//        System.out.println("genre\t" + genre);
//        System.out.println("time\t" + seconds / 60 + ": " + seconds % 60);


        HashMap<String, Integer> hm = new HashMap<>();

        startNode = new SegmentSong(playback, 0);
        int cnto = 8000;
        while (cnto-- > 0) {
//            if (startNode == numClusters-1)startNode=0;
            if (lastSong != startNode.song) {
                tempSong = SongManager.getRandom(startNode.song);
                lastSong = startNode.song;
            }
            AudioInterval ai = tempSong.getAudioInterval(tempSong.analysis.getSegments().get(startNode.segment));
            ai.payload = new SegmentSong(startNode.song, startNode.segment);
            audio.play(ai);

            Iterator<Edge> adj = graph.getNode(startNode.hashCode() + "").getEachLeavingEdge().iterator();
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

            System.out.println("going down: " + next + " out of " + temp.size());
            if (temp.size() == 0) startNode = firstSaved;
            else {
                Edge selected = temp.get(next);
                startNode = nodes.get(Integer.parseInt(selected.getNode1().getId()));

            }
            String key = startNode.hashCode()+"";

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


