package com.klemstinegroup.wub2.test;

import be.tarsos.dsp.*;
import be.tarsos.dsp.io.TarsosDSPAudioFormat;
import be.tarsos.dsp.io.jvm.AudioDispatcherFactory;
import be.tarsos.dsp.resample.RateTransposer;
import com.klemstinegroup.wub2.system.Audio;
import com.klemstinegroup.wub2.system.AudioInterval;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.util.concurrent.CountDownLatch;

/**
 * Created by Paul on 2/21/2016.
 */
public class AudioUtils implements Handler {

    private static float[] buffer;
    private static AudioDispatcher d;
    AudioInterval ad;
    CountDownLatch cdl;

    public AudioUtils(AudioInterval ad, CountDownLatch cdl) {
        this.ad = ad;
        this.cdl = cdl;
    }

//    public static void timeStretch(AudioInterval ad, double stretch){
//        AudioDispatcher adp= null;
//        WaveformSimilarityBasedOverlapAdd wsola = new WaveformSimilarityBasedOverlapAdd(WaveformSimilarityBasedOverlapAdd.Parameters.automaticDefaults(stretch, Audio.audioFormat.getSampleRate()));
//        try {
//            adp = AudioDispatcherFactory.fromByteArray(ad.data, Audio.audioFormat,wsola.getInputBufferSize(),wsola.getOverlap());
//        } catch (UnsupportedAudioFileException e) {
//            e.printStackTrace();
//        }
//        TarsosDSPAudioFormat format = adp.getFormat();
//        System.out.println(format);
////        AudioPlayer audioPlayer = null;
////        try {
////            audioPlayer = new AudioPlayer(format);
////        } catch (LineUnavailableException e) {
////            e.printStackTrace();
////        }
//       // GainProcessor gain = new GainProcessor(1.0);
//        CountDownLatch cdl=new CountDownLatch(1);
//        ByteProcessor bp = new ByteProcessor(new AudioUtils(ad,cdl),format);
//
//        //wsola.setDispatcher(adp);
//        //adp.addAudioProcessor(wsola);
//       // adp.addAudioProcessor(gain);
//        adp.addAudioProcessor(bp);
//
////        Thread t = new Thread(adp);
////        t.start();
//        adp.run();
//        try {
//            cdl.await();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
//
//    }


    public static void pitchShift(AudioInterval ad, double shift) {
        AudioDispatcher adp = null;
        RateTransposer rateTransposer = new RateTransposer(shift);
        WaveformSimilarityBasedOverlapAdd wsola = new WaveformSimilarityBasedOverlapAdd(WaveformSimilarityBasedOverlapAdd.Parameters.musicDefaults(shift, Audio.audioFormat.getSampleRate()));
        try {
            adp = AudioDispatcherFactory.fromByteArray(ad.data, Audio.audioFormat, wsola.getInputBufferSize(), wsola.getOverlap());
        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
        }
        TarsosDSPAudioFormat format = adp.getFormat();
        System.out.println(format);
//        AudioPlayer audioPlayer = null;
//        try {
//            audioPlayer = new AudioPlayer(format);
//        } catch (LineUnavailableException e) {
//            e.printStackTrace();
//        }
        // GainProcessor gain = new GainProcessor(1.0);
        CountDownLatch cdl = new CountDownLatch(1);
        ByteProcessor bp = new ByteProcessor(new AudioUtils(ad, cdl), format);

        wsola.setDispatcher(adp);

        adp.addAudioProcessor(wsola);
        adp.addAudioProcessor(rateTransposer);
        // adp.addAudioProcessor(gain);
        adp.addAudioProcessor(bp);

//        Thread t = new Thread(adp);
//        t.start();
        adp.run();
        try {
            cdl.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }


    @Override
    public void handleTimeStretch(byte[] data) {
        for (int i = 495000; i < 500000; i++) System.out.print(ad.data[i]);
        System.out.println();
        ad.data = data;
        for (int i = 495000; i < 500000; i++) System.out.print(data[i]);
        System.out.println();

        cdl.countDown();
    }
}
