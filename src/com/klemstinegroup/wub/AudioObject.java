package com.klemstinegroup.wub;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import com.echonest.api.v4.EchoNestAPI;
import com.echonest.api.v4.Track;
import com.echonest.api.v4.TrackAnalysis;
import com.jssrc.resample.JSSRCResampler;

//-Xbootclasspath/p:./libs/tritonus_share.jar;./libs/tritonus_aos-0.3.6.jar;./libs/tritonus_remaining-0.3.6.jar

public class AudioObject {

	public byte[] data;
	public File file;
	TrackAnalysis analysis;
	Player player = new Player();

	public AudioObject(String file) {
		this(new File(file));
	}

	public AudioObject(File file) {
		this.file = file;
		convert(file);
		analysis = echoNest(file);
	}

	public static TrackAnalysis echoNest(File file) {
		try {
			EchoNestAPI en = new EchoNestAPI();
			Track track = en.uploadTrack(file);
			track.waitForAnalysis(30000);
			if (track.getStatus() == Track.AnalysisStatus.COMPLETE) {
				return track.getAnalysis();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public void convert(File soundFile) {
		AudioInputStream mp3InputStream = null;
		try {
			mp3InputStream = AudioSystem.getAudioInputStream(soundFile);
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		File temp = new File("temp.wav");
		mp3InputStream = AudioSystem.getAudioInputStream(new AudioFormat(mp3InputStream.getFormat().getSampleRate(), Player.resolution, Player.channels, true, false), mp3InputStream);
		try {
			AudioSystem.write(mp3InputStream, AudioFileFormat.Type.WAVE, temp);
		} catch (IOException e) {
			e.printStackTrace();
		}
		// try {
		// data = Files.readAllBytes(temp.toPath());
		try {
			mp3InputStream = AudioSystem.getAudioInputStream(temp);
		} catch (UnsupportedAudioFileException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		

		mp3InputStream = AudioSystem.getAudioInputStream(Player.audioFormat, mp3InputStream);

		ByteArrayOutputStream bo = new ByteArrayOutputStream();
		try {
			AudioSystem.write(mp3InputStream, AudioFileFormat.Type.WAVE, bo);
		} catch (IOException e) {
			e.printStackTrace();
		}
		data = bo.toByteArray();
		
		try {
			mp3InputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		temp.delete();
	}

	public String getFileName() {
		return file.getName();
	}

}
