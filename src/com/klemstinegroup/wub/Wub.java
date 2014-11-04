package com.klemstinegroup.wub;

import com.jsyn.JSyn;
import com.jsyn.Synthesizer;
import com.jsyn.data.FloatSample;
import com.jsyn.data.ShortSample;
import com.jsyn.unitgen.LineOut;
import com.jsyn.unitgen.SineOscillator;
import com.jsyn.unitgen.VariableRateStereoReader;
import com.softsynth.jsyn.Synth;

public class Wub {

	public Wub() {
		Synth.startEngine(0);
		LineOut lo = new LineOut();
		Synthesizer synth = JSyn.createSynthesizer();
		VariableRateStereoReader samplePlayer = new VariableRateStereoReader();
		synth.add(samplePlayer);
		synth.add(lo);
		// AudioObject au = AudioObject.factory("songs/plumber.mp3");
//		AudioObject au1 = AudioObject.factory("songs/heat.mp3");
		// AudioObject au2 =
		 AudioObject au1=AudioObject.factory("songs/Pendulum - The Island Pt. I (Dawn).mp3");
		// AudioObject.factory("songs/Zeds Dead - In The Beginning.mp3");
		// AudioObject
		// au1=AudioObject.factory("songs/Rotersand - Almost Wasted.mp3");
		// System.out.println("here");
		samplePlayer.output.connect(0, lo.input, 0);
		samplePlayer.output.connect(0, lo.input, 1);
		samplePlayer.start();
		lo.start();
		synth.start();

		System.out.println("dumping sample");
		ShortSample myStereoSample = new ShortSample(au1.data.length / AudioObject.frameSize, 2);
		byte LSB, MSB;
		short left, right;
		for (int i = 0; i < au1.data.length / AudioObject.frameSize; i += 1) {
			LSB = au1.data[4 * i];
			MSB = au1.data[4 * i + 1];
			left = (short) (((MSB & 0xFF) << 8) | (LSB & 0xFF));

			LSB = au1.data[4 * i + 2];
			MSB = au1.data[4 * i + 3];
			right = (short) (((MSB & 0xFF) << 8) | (LSB & 0xFF));
			myStereoSample.write(i, new short[] { left, right }, 0, 1);
		}
		// for( int i=0; i<data.length; i+=frameSize )
		// {
		// d[cnt]=data[i];
		// }
		// float value = 0.0f;
		// for (int i = 0; i < d.length; i++) {
		// d[i] = value;
		// value += 0.01;
		// if (value >= 1.0) {
		// value -= 2.0;
		// }
		// }

		samplePlayer.rate.set(44100);
		System.out.println("done");
		samplePlayer.dataQueue.queue(myStereoSample, 0, myStereoSample.getNumFrames());

		// // Create a context for the synthesizer.
		// synth = JSyn.createSynthesizer();
		//
		// SineOscillator osc;
		// // Add a tone generator.
		// synth.add( osc = new SineOscillator() );
		// LineOut lineOut;
		// // Add an output mixer.
		// synth.add( lineOut = new LineOut() );
		// // Connect the oscillator to the output.
		// osc.output.connect( 0, lineOut.input, 0 );
		//
		// // Start synthesizer using default stereo output at 44100 Hz.
		// synth.start();
		// // We only need to start the LineOut. It will pull data from the
		// oscillator.
		// lineOut.start();
		//
		// // Sleep while the sound is generated in the background.
		try {
			double time = synth.getCurrentTime();
			// Sleep for a few seconds.
			synth.sleepUntil(time + 400.0);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		//
		// // Stop everything.
		synth.stop();
	}

	public static void main(String[] args) {
		new Wub();

	}

}
