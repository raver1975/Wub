package com.klemstinegroup.wub;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import com.echonest.api.v4.EchoNestAPI;
import com.echonest.api.v4.EchoNestException;
import com.echonest.api.v4.Track;
import com.echonest.api.v4.TrackAnalysis;

public class AudioObject {

	public byte[] data;
	public File file;
	TrackAnalysis analysis;
	
	public AudioObject(String file) {
		this(new File(file));
	}

	public AudioObject(File file) {
		this.file=file;
		convert(file);

	}

    void echoNest(final File file){
    	new Thread(new Runnable(){
    		public void run(){
    			try{
    				EchoNestAPI en=null;
    		    	Track track = en.uploadTrack(file);
    				track.waitForAnalysis(30000);
    				if (track.getStatus() == Track.AnalysisStatus.COMPLETE) {
    					analysis = track.getAnalysis();
    				}
    			}
    			catch(Exception e){
    				e.printStackTrace();
    			}
    		}
    	}).start();
    }

	void convert(File soundFile) {
		AudioInputStream mp3InputStream = null;
		try {
			mp3InputStream = AudioSystem.getAudioInputStream(soundFile);
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		AudioFormat baseFormat = mp3InputStream.getFormat();
		AudioInputStream convertedAudioInputStream = AudioSystem.getAudioInputStream(Player.audioFormat, mp3InputStream);
		File temp=new File("temp.mp3");
		try {
			AudioSystem.write(convertedAudioInputStream, AudioFileFormat.Type.WAVE,
					temp);
		} catch (IOException e) {
			e.printStackTrace();
		}
		data=FileToBytes(temp);
		temp.delete();
	}
	
	public static byte[] FileToBytes(File file){
		 byte[] b = new byte[(int) file.length()];
       try { 
             FileInputStream fileInputStream = new FileInputStream(file);
             fileInputStream.read(b);
             fileInputStream.close();
        } catch (FileNotFoundException e) {
                    System.out.println("File Not Found.");
                    e.printStackTrace();
        } 
        catch (IOException e1) {
                 System.out.println("Error Reading The File.");
                  e1.printStackTrace();
        } 
       
       return b;
	}
}
