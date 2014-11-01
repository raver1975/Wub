package com.klemstinegroup.wub;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.echonest.api.v4.EchoNestAPI;
import com.echonest.api.v4.EchoNestException;
import com.echonest.api.v4.TimedEvent;
import com.echonest.api.v4.Track;
import com.echonest.api.v4.TrackAnalysis;

/**
 * This example will demonstrate uploading an MP3, analyzing it and getting the
 * audio summary and the timing info for all of the beats
 * 
 * @author plamere
 */
public class TrackAnalysisExample {

	static String song = "songs/plumber.mp3";

	public static void main(String[] args) throws EchoNestException {
		EchoNestAPI en = new EchoNestAPI();

		String path = new File(song).getAbsolutePath();

		if (args.length > 2) {
			path = args[1];
		}

		File file = new File(path);

		if (!file.exists()) {
			System.err.println("Can't find " + path);
		} else {

			try {
				Track track = en.uploadTrack(file);
				track.waitForAnalysis(30000);
				if (track.getStatus() == Track.AnalysisStatus.COMPLETE) {
					System.out.println("Tempo: " + track.getTempo());
					System.out.println("Danceability: " + track.getDanceability());
					System.out.println("Speechiness: " + track.getSpeechiness());
					System.out.println("Liveness: " + track.getLiveness());
					System.out.println("Energy: " + track.getEnergy());
					System.out.println("Loudness: " + track.getLoudness());
					System.out.println("Signature: " + track.getTimeSignature());
					System.out.println();
					System.out.println("Beat start times:");
					TrackAnalysis analysis = track.getAnalysis()
							;
					List<TimedEvent> list = analysis.getSections();
					Collections.sort(list, new Comparator<TimedEvent>() {

						@Override
						public int compare(TimedEvent o1, TimedEvent o2) {
							return Double.compare(o1.getStart(), o2.getStart());
						}

					});
//					 Collections.reverse(list);
					List<TimedEvent> al=new ArrayList<TimedEvent>();
					 for (int i=0;i<list.size();i+=2){
//					 Collections.swap(list, i, i+2);
						 al.add(list.get(i));
					 }
					AudioObject au = new AudioObject(song);
					Player player = new Player();
					for (TimedEvent beat : al) {
						System.out.println("beat " + beat.getConfidence() + "\t" + beat.getStart() + "\t" + beat.getDuration());
						player.play(au, beat.getStart(), beat.getDuration());
					}
				} else {
					System.err.println("Trouble analysing track " + track.getStatus());
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			// Mp3Player mp3=new Mp3Player();
			// mp3.testPlay(path);
		}
	}
}