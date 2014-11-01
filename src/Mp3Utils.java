import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import com.klemstinegroup.wub.AudioObject;

public class Mp3Utils {
	static byte[] data = new byte[4096];

	static void convert(File soundFile, File outputFile)
			throws UnsupportedAudioFileException, IOException {
		AudioInputStream mp3InputStream = AudioSystem
				.getAudioInputStream(soundFile);
		AudioFormat baseFormat = mp3InputStream.getFormat();
		System.out.println(baseFormat.getSampleRate());
		AudioFormat decodedFormat = new AudioFormat(
				AudioFormat.Encoding.PCM_SIGNED, baseFormat.getSampleRate(),
				16, baseFormat.getChannels(), baseFormat.getChannels() * 2,
				baseFormat.getSampleRate(), false);
		AudioInputStream convertedAudioInputStream = AudioSystem
				.getAudioInputStream(decodedFormat, mp3InputStream);
		AudioSystem.write(convertedAudioInputStream, AudioFileFormat.Type.WAVE,
				outputFile);
	}

	public static AudioObject open(File audioFile) {
		AudioInputStream au = null;
		try {
			au = AudioSystem.getAudioInputStream(audioFile);
		} catch (UnsupportedAudioFileException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		BufferedInputStream bufferedInputStream = new BufferedInputStream(au);
		au = new AudioInputStream(bufferedInputStream, au.getFormat(),
				au.getFrameLength());
		au.mark(Integer.MAX_VALUE);
		// try {
		// InputStream audioSrc =
		// Mp3Utils.class.getResourceAsStream(audioFile.getAbsolutePath());
		// au = AudioSystem
		// .getAudioInputStream(new BufferedInputStream(audioSrc));
		// } catch (FileNotFoundException e1) {
		// e1.printStackTrace();
		// } catch (UnsupportedAudioFileException e1) {
		// e1.printStackTrace();
		// } catch (IOException e1) {
		// e1.printStackTrace();
		// }
		AudioFormat audioFormat = au.getFormat();
		SourceDataLine line = null;
		try {
			line = getLine(audioFormat);
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		}
		AudioObject ao = new AudioObject();
		ao.au = au;
		ao.line = line;
		ao.af = audioFormat;
		
		
		line.start();
		return ao;
	}

	//
	//
	public static void play(AudioObject ao, double d, double e)
			throws UnsupportedAudioFileException, IOException {

		double startInBytes = (d * ao.af.getSampleRate() * ao.af.getFrameSize());
		double lengthInFrames = ((e - d) * ao.af.getSampleRate());
		int lengthInBytes = (int) (lengthInFrames * ao.af.getFrameSize())
				-((int) (lengthInFrames * ao.af.getFrameSize()))
				% ao.af.getFrameSize();
		// System.out.println(startTimeinMs+"\t"+endTimeinMs+"\t"+startInBytes+"\t"+lengthInFrames);
		ao.au.reset();
		ao.au.skip((long) startInBytes);
		// AudioInputStream partAudioInputStream = new AudioInputStream(ao.au,
		// ao.af, (long) lengthInFrames);
		if (ao.line != null) {
			int nBytesRead = 0, nBytesWritten = 0;
			while (nBytesWritten < lengthInBytes && nBytesRead != -1) {
				if (lengthInBytes - nBytesWritten > data.length)
					nBytesRead = ao.au.read(data, 0, data.length);
				else
					nBytesRead = ao.au.read(data, 0, lengthInBytes
							- nBytesWritten);

				if (nBytesRead != -1)
					nBytesWritten += ao.line.write(data, 0, nBytesRead);
				// System.out.println(
				// nBytesRead+"\t"+nBytesWritten+"\t"+lengthInBytes);
			}

			// Stop
			// ao.line.drain();
			// line.stop();
			// line.close();
			// partAudioInputStream.close();
		}

	}

	public static SourceDataLine getLine(AudioFormat audioFormat)
			throws LineUnavailableException {
		SourceDataLine res = null;
		DataLine.Info info = new DataLine.Info(SourceDataLine.class,
				audioFormat);
		res = (SourceDataLine) AudioSystem.getLine(info);
		res.open(audioFormat);
		return res;
	}

//	private static AudioInputStream convertToPCM(
//			AudioInputStream audioInputStream) {
//		AudioFormat m_format = audioInputStream.getFormat();
//
//		if ((m_format.getEncoding() != AudioFormat.Encoding.PCM_SIGNED)
//				&& (m_format.getEncoding() != AudioFormat.Encoding.PCM_UNSIGNED)) {
//			AudioFormat targetFormat = new AudioFormat(
//					AudioFormat.Encoding.PCM_SIGNED, m_format.getSampleRate(),
//					16, m_format.getChannels(), m_format.getChannels() * 2,
//					m_format.getSampleRate(), m_format.isBigEndian());
//			audioInputStream = AudioSystem.getAudioInputStream(targetFormat,
//					audioInputStream);
//		}
	//
	// return audioInputStream;
	// }
	public static byte[] FileToBytes(File file){
		 byte[] b = new byte[(int) file.length()];
        try { 
              FileInputStream fileInputStream = new FileInputStream(file);
              fileInputStream.read(b);
              for (int i = 0; i < b.length; i++) {
                          System.out.print((char)b[i]);
               } 
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
