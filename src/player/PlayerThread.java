package player;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.DataLine.Info;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.BitstreamException;
import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.DecoderException;
import javazoom.jl.decoder.Header;
import javazoom.jl.decoder.SampleBuffer;
import maryb.player.io.EmulateSlowInputStream;
import maryb.player.io.SeekablePumpStream;

class PlayerThread
  extends Thread
{
  private static final Logger LOG = Logger.getLogger(PlayerThread.class.getName());
  private final Player parent;
  private volatile boolean dieRequested = false;
  private Bitstream bstream;
  private SourceDataLine line;
  private ByteBuffer bb = ByteBuffer.allocate(88200);
  private InputStream stream;
  private PlayerState requestedState = null;
  private Decoder decoder;
  
  private AudioFormat getAudioFormatValue()
  {
    return new AudioFormat(this.decoder.getOutputFrequency(), 16, this.decoder.getOutputChannels(), true, false);
  }
  
  public PlayerThread(Player parent)
  {
    super("player-playback-thread");
    setPriority(10);
    this.parent = parent;
  }
  
  private void createOpenLine()
    throws LineUnavailableException
  {
    this.line = (this.parent.currentDataLine = createLine());
    this.line.open(getAudioFormatValue(), 88200);
    LOG.info("Line opened for the format " + this.line.getFormat());
    
    this.parent.populateVolume(this.line);
    this.line.start();
  }
  
  private boolean decodeFrame()
    throws BitstreamException, DecoderException, LineUnavailableException, InterruptedException
  {
    this.bb.clear();
    
    float buffered = 0.0F;
    do
    {
      Header h = this.bstream.readFrame();
      if (h == null) {
        break;
      }
      SampleBuffer sb = (SampleBuffer)this.decoder.decodeFrame(h, this.bstream);
      if (this.line == null)
      {
        createOpenLine();
        this.parent.totalPlayTimeMcsec = (long) ((1000.0D * h.total_ms(this.parent.realInputStreamLength)));
        LOG.log(Level.FINE, "fq: " + this.decoder.getOutputFrequency());
        LOG.log(Level.FINE, "sr: " + this.line.getFormat().getSampleRate());
        
        this.bb.order(this.line.getFormat().isBigEndian() ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);
      }
      short[] samples = sb.getBuffer();
      int sz = sb.getBufferLength() * 2;
      if (sz > this.bb.remaining())
      {
        int olsSize = this.bb.position() + sz;
        int limit = this.bb.capacity() + 1024;
        while (olsSize > limit) {
          limit += 1024;
        }
        ByteBuffer newBb = ByteBuffer.allocate(limit);
        newBb.clear();
        this.bb.flip();
        newBb.put(this.bb);
        this.bb = newBb;
        this.bb.order(this.line.getFormat().isBigEndian() ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);
      }
      int idx = 0;
      for (int m = sb.getBufferLength(); idx < m; idx++) {
        this.bb.putShort(samples[idx]);
      }
      buffered += h.ms_per_frame();
      this.bstream.closeFrame();
    } while (buffered < 200.0F);
    if (this.bb.position() == 0) {
      return false;
    }
    synchronized (this.parent.osync)
    {
      while (this.parent.getState() == PlayerState.PAUSED_BUFFERING) {
        this.parent.osync.wait();
      }
      if (this.parent.getState() == PlayerState.STOPPED) {
        return !this.dieRequested;
      }
    }
    this.bb.flip();
    int wasWritten;
    while ((this.bb.remaining() > 0) && (!this.dieRequested) && (this.line.isOpen()) && (this.parent.getState() != PlayerState.STOPPED) && 
      ((wasWritten = this.line.write(this.bb.array(), 0, this.bb.remaining())) != -1)) {
      this.bb.position(this.bb.position() + wasWritten);
    }
    return true;
  }
  
  private void openInputStream()
    throws IOException
  {
    String location = this.parent.getSourceLocation();
    if (location == null) {
      throw new IllegalArgumentException();
    }
    if (location.startsWith("http://"))
    {
      HttpURLConnection.setFollowRedirects(true);
      URLConnection c = new URL(location).openConnection();
      c.connect();
      
      this.parent.realInputStreamLength = c.getContentLength();
      this.parent.realInputStream = c.getInputStream();
    }
    else
    {
      boolean slow = false;
      if (location.startsWith("!"))
      {
        location = location.substring(1);
        slow = true;
      }
      File f = new File(location);
      if ((f.exists()) && (!f.isDirectory()) && (f.canRead())) {
        this.parent.realInputStream = new FileInputStream(f);
      } else {
        throw new IllegalArgumentException("Bad path to file: '" + location + "'");
      }
      this.parent.realInputStreamLength = ((int)f.length());
      if (slow) {
        this.parent.realInputStream = new EmulateSlowInputStream(this.parent.realInputStream, 0.3D);
      }
    }
  }
  
  private void processID3v2()
    throws IOException
  {}
  
  private SourceDataLine createLine()
  {
    Line newLine = null;
    try
    {
      newLine = AudioSystem.getLine(getSourceLineInfo());
    }
    catch (LineUnavailableException ex)
    {
      LOG.log(Level.SEVERE, null, ex);
    }
    if ((newLine == null) || (!(newLine instanceof SourceDataLine))) {
      return null;
    }
    return (SourceDataLine)newLine;
  }
  
  private Line.Info getSourceLineInfo()
  {
    DataLine.Info info = new DataLine.Info(SourceDataLine.class, getAudioFormatValue());
    Line.Info[] infos = AudioSystem.getSourceLineInfo(info);
    if (infos.length == 0)
    {
      LOG.warning("There are no suitable lines for format " + getAudioFormatValue());
      return null;
    }
    Line.Info selected = infos[0];
    LOG.info("Selected channel: " + selected.toString());
    
    return selected;
  }
  
  private void skipFrames(long timeMcsec)
    throws BitstreamException
  {
    long skipped = 0L;
    Header h;
    while ((!this.dieRequested) && (skipped < timeMcsec) && ((h = this.bstream.readFrame()) != null))
    {
      skipped += (1000.0D * h.ms_per_frame());
      this.bstream.closeFrame();
    }
  }
  
  private void stopAndCloseLine()
  {
    try
    {
      this.line.stop();
    }
    catch (Throwable ignore) {}
    try
    {
      this.line.close();
    }
    catch (Throwable ignore) {}
  }
  
  public void run()
  {
    try
    {
      if (this.parent.getPumpStream() == null)
      {
        openInputStream();
        this.parent.setPumpStream(new SeekablePumpStream(this.parent.realInputStream));
        
        new Thread("player-stream-pump-thread")
        {
          public void run()
          {
            PlayerThread.this.parent.getPumpStream().pumpLoop();
            synchronized (PlayerThread.this.parent.osync)
            {
              PlayerThread.this.parent.osync.notifyAll();
            }
          }
        }.start();
        new PlayerBufferedResolverThread(this.parent).start();
      }
      this.parent.endOfMediaReached = false;
      
      this.stream = this.parent.getPumpStream().openStream();
      this.bstream = new Bitstream(this.stream);
      processID3v2();
      
      this.decoder = new Decoder();
      
      this.line = (this.parent.currentDataLine = null);
      synchronized (this.parent.seekSync)
      {
        while (this.parent.seekThread != null) {
          this.parent.seekSync.wait();
        }
      }
      synchronized (this.parent.osync)
      {
        this.parent.setState(PlayerState.PLAYING);
        this.parent.osync.notifyAll();
      }
      new PlayerListenerNotificatorThread(this.parent, this).start();
      new PlayerHelperThread(this.parent, this).start();
      
      skipFrames(this.parent.currentSeekPositionMcsec);
      
      long lastUpdate = 0L;
      while ((!this.dieRequested) && 
        (decodeFrame()))
      {
        this.parent.currentPlayTimeMcsec = this.line.getMicrosecondPosition();
        
        long now = System.currentTimeMillis();
        this.parent.lastPlayerActivity = now;
        if (now - lastUpdate > 250L)
        {
          synchronized (this.parent.osync)
          {
            this.parent.osync.notifyAll();
          }
          lastUpdate = now;
        }
      }
      if (this.line != null)
      {
        this.line.flush();
        this.line.drain();
        this.parent.currentSeekPositionMcsec += this.line.getMicrosecondPosition();
        this.parent.currentPlayTimeMcsec = 0L;
        this.parent.endOfMediaReached = (!this.dieRequested);
      }
    }
    catch (BitstreamException e)
    {
      e.printStackTrace();
    }
    catch (InterruptedIOException ignore) {}catch (IOException e)
    {
      e.printStackTrace();
    }
    catch (DecoderException e)
    {
      e.printStackTrace();
    }
    catch (LineUnavailableException e)
    {
      e.printStackTrace();
    }
    catch (InterruptedException ignore) {}finally
    {
      try
      {
        if (this.bstream != null) {
          this.bstream.close();
        }
      }
      catch (BitstreamException ignore) {}catch (Throwable t)
      {
        t.printStackTrace();
      }
      if (this.line != null)
      {
        stopAndCloseLine();
        this.line = null;
        this.parent.currentDataLine = null;
      }
      if (this.parent.currentPlayerThread.compareAndSet(this, null)) {
        synchronized (this.parent.osync)
        {
          if (this.requestedState != null)
          {
            switch (this.requestedState)
            {
            case STOPPED: 
              this.parent.currentSeekPositionMcsec = 0L;
              this.parent.setState(this.requestedState);
              break;
            case PAUSED: 
              this.parent.setState(this.requestedState);
              break;
            default: 
              this.parent.setState(PlayerState.STOPPED);
              break;
            }
          }
          else
          {
            this.parent.currentSeekPositionMcsec = 0L;
            this.parent.currentPlayTimeMcsec = 0L;
            this.parent.setState(PlayerState.STOPPED);
          }
          this.parent.osync.notifyAll();
        }
      } else {
        synchronized (this.parent.osync)
        {
          this.parent.osync.notifyAll();
        }
      }
    }
  }
  
  public void die(PlayerState requestedState)
  {
    if ((requestedState != PlayerState.STOPPED) && (requestedState != PlayerState.PAUSED)) {
      throw new IllegalArgumentException("player thread may die only with stopped or paused state");
    }
    this.requestedState = requestedState;
    this.dieRequested = true;
    interrupt();
  }
  
  public void die()
  {
    die(PlayerState.STOPPED);
  }
}
