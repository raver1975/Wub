package player;

import java.io.InputStream;
import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.BitstreamException;
import javazoom.jl.decoder.Header;
import maryb.player.io.SeekablePumpStream;

class PlayerBufferedResolverThread
  extends Thread
{
  private final Player parent;
  
  public PlayerBufferedResolverThread(Player parent)
  {
    super("player-buffered-time-resolver-thread");
    this.parent = parent;
  }
  
  private volatile boolean dieRequested = false;
  
  public void run()
  {
    try
    {
      SeekablePumpStream s = this.parent.getPumpStream();
      if (s != null)
      {
        InputStream is = s.openStream();
        Bitstream bs = new Bitstream(is);
        this.parent.currentBufferedTimeMcsec = 0L;
        double value = 0.0D;
        long lastUpdateTime = 0L;
        Header h;
        while ((!this.dieRequested) && ((h = bs.readFrame()) != null))
        {
          value = 1000.0D * h.ms_per_frame();
          
          this.parent.currentBufferedTimeMcsec += value;
          
          long now = System.currentTimeMillis();
          if (now - lastUpdateTime > 800L)
          {
            lastUpdateTime = now;
            synchronized (this.parent.osync)
            {
              this.parent.osync.notifyAll();
            }
          }
          bs.closeFrame();
        }
      }
    }
    catch (BitstreamException e)
    {
      e.printStackTrace();
    }
  }
  
  public void die()
  {
    this.dieRequested = true;
    interrupt();
  }
}
