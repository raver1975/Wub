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

public class AudioObject {

	public AudioObject(String file) {
		this(new File(file));
	}

	public AudioObject(File file) {
		convert(file);
	}

	public byte[] data;

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
		System.out.println(baseFormat.getSampleRate());
//		AudioFormat decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, Player.sampleRate, Player.resolution, Player.channels, Player.frameSize, Player.sampleRate, false);
		AudioInputStream convertedAudioInputStream = AudioSystem.getAudioInputStream(Player.audioFormat, mp3InputStream);
//		ByteOutputStream bos=new ByteOutputStream();
//		try {
//			AudioSystem.write(convertedAudioInputStream, AudioFileFormat.Type.WAVE, bos);
//			data=bos.getBytes();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		try {
			AudioSystem.write(convertedAudioInputStream, AudioFileFormat.Type.WAVE,
					new File("temp.mp3"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		try {
//			mp3InputStream = AudioSystem.getAudioInputStream(new File("temp.mp3"));
//		} catch (UnsupportedAudioFileException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		data=FileToBytes(new File("temp.mp3"));
		
//		data=new byte[(int) (mp3InputStream.getFrameLength()*Player.frameSize)];
//		try {
//			convertedAudioInputStream.read(data);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		new File("temp.mp3").delete();
	}
	
	public static byte[] FileToBytes(File file){
		 byte[] b = new byte[(int) file.length()];
       try { 
             FileInputStream fileInputStream = new FileInputStream(file);
             fileInputStream.read(b);
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
