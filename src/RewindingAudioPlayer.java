/*
 *	RewindingAudioPlayer.java
 *
 *	This file is part of jsresources.org
 */

/*
 * Copyright (c) 1999 - 2003 by Matthias Pfisterer
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/*
|<---            this code is formatted to fit into 80 columns             --->|
*/

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.DataLine;



/**	<titleabbrev>RewindingAudioPlayer</titleabbrev>
	<title>Playing an audio file multiple times</title>

	<formalpara><title>Purpose</title>

	<para>Plays an audio file repeatedly by resetting the
	AudioInputStream rather than using a Clip.</para>
	</formalpara>

	<formalpara><title>Usage</title>
	<para>
	<cmdsynopsis><command>java RewindingAudioPlayer</command>
	<arg choice="plain"><replaceable class="parameter">audiofile</replaceable></arg>
	</cmdsynopsis>
	</para>
	</formalpara>

	<formalpara><title>Parameters</title>
	<variablelist>
	<varlistentry>
	<term><replaceable class="parameter">audiofile</replaceable></term>
	<listitem><para>the name of the audio file to play</para></listitem>
	</varlistentry>
	</variablelist>
	</formalpara>

	<formalpara><title>Bugs, limitations</title>
	<para>Compressed formats cannot be handled.
	For a way to handle encoded as well as unencoded files,
	see <olink targetdocent="AudioPlayer">AudioPlayer</olink>.</para>
	</formalpara>

	<formalpara><title>Source code</title>
	<para>
	<ulink url="RewindingAudioPlayer.java.html">RewindingAudioPlayer.java</ulink>
	</para>
	</formalpara>

*/
public class RewindingAudioPlayer
{
	private static final boolean DEBUG = true;

	private static final int	EXTERNAL_BUFFER_SIZE = 128000;

	/** How often to play the audio file.
	 */
	private static final int	PLAY_COUNT = 5;


	public static void main(String[] args)
		throws Exception
	{
		if (args.length != 1)
		{
			printUsageAndExit();
		}

		/*
		  Now, that we're shure there is an argument, we
		  take it as the filename of the soundfile
		  we want to play.
		*/
		String	strFilename = args[0];
		InputStream fileInputStream = new FileInputStream(strFilename);
		InputStream inputStream = new BufferedInputStream(fileInputStream);

		/*
		  We have to read in the sound file.
		*/
		AudioInputStream	audioInputStream = null;
		try
		{
			audioInputStream = AudioSystem.getAudioInputStream(inputStream);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.exit(1);
		}
		if (! audioInputStream.markSupported())
		{
			out("###  resetting not supported");
			System.exit(1);
		}

		AudioFormat	audioFormat = audioInputStream.getFormat();

		SourceDataLine	line = null;
		DataLine.Info	info = new DataLine.Info(SourceDataLine.class,
												 audioFormat);
		try
		{
			line = (SourceDataLine) AudioSystem.getLine(info);

			/*
			  The line is there, but it is not yet ready to
			  receive audio data. We have to open the line.
			*/
			line.open(audioFormat);
		}
		catch (LineUnavailableException e)
		{
			e.printStackTrace();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		byte[]	abData = new byte[EXTERNAL_BUFFER_SIZE];
		int	nBytesRead = 0;
		int nPlayCount = 0;
		if (audioInputStream.getFrameLength() == AudioSystem.NOT_SPECIFIED ||
			audioFormat.getFrameSize() == AudioSystem.NOT_SPECIFIED)
		{
			out("cannot calculate length of AudioInputStream!");
			System.exit(1);
		}
		long lStreamLengthInBytes = audioInputStream.getFrameLength()
			* audioFormat.getFrameSize();
		if (lStreamLengthInBytes > Integer.MAX_VALUE)
		{
			out("length of AudioInputStream exceeds 2^31, cannot properly reset stream!");
			System.exit(1);
		}
		int nStreamLengthInBytes = (int) lStreamLengthInBytes;

		line.start();

		while (nPlayCount < PLAY_COUNT)
		{
			nPlayCount++;
			audioInputStream.mark(nStreamLengthInBytes);
			nBytesRead = 0;
			while (nBytesRead != -1)
			{
				try
				{
					nBytesRead = audioInputStream.read(abData, 0, abData.length);
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
				if (nBytesRead >= 0)
				{
					int	nBytesWritten = line.write(abData, 0, nBytesRead);
				}
			}
			audioInputStream.reset();
		}
		line.drain();
		line.close();
	}



	public static void printUsageAndExit()
	{
		out("RewindingAudioPlayer: usage:");
		out("\tjava RewindingAudioPlayer <soundfile>");
		System.exit(1);
	}



	private static void out(String strMessage)
	{
		System.out.println(strMessage);
	}
}



/*** RewindingAudioPlayer.java ***/