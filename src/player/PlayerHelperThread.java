package player;

import java.io.PrintStream;
import javax.sound.sampled.SourceDataLine;
import maryb.player.io.SeekablePumpStream;

class PlayerHelperThread
  extends Thread
{
  private final Player parent;
  private final PlayerThread creator;
  
  public PlayerHelperThread(Player parent, PlayerThread creator)
  {
    super("player-helper-thread");
    this.parent = parent;
    this.creator = creator;
  }
  
  private volatile boolean dieRequested = false;
  
  private boolean isStopped()
  {
    return (this.parent.getState() == PlayerState.PAUSED) || (this.parent.getState() == PlayerState.STOPPED);
  }
  
  private boolean aliveCriterion()
  {
    return (!this.dieRequested) && (!isStopped()) && (this.creator.isAlive());
  }
  
  public void run()
  {
    try
    {
      while (aliveCriterion())
      {
        long now = System.currentTimeMillis();
        long lpa = this.parent.lastPlayerActivity;
        long timeToSleep = 400L - (now - lpa);
        if (timeToSleep < 200L) {
          timeToSleep = 200L;
        }
        sleep(timeToSleep);
        SourceDataLine line;
        if ((this.parent.lastPlayerActivity == lpa) && 
          ((line = this.parent.getCurrentDataLine()) != null))
        {
          int size = line.getBufferSize();
          int available = line.available();
          
          float rate = available / size;
          PlayerState state = this.parent.getState();
          if ((state == PlayerState.PAUSED_BUFFERING) || (rate > 0.9D))
          {
            long time = this.parent.getCurrentPosition();
            synchronized (this.parent.osync)
            {
              if ((rate > 0.9D) && 
                (state == PlayerState.PLAYING) && (!this.parent.getPumpStream().isAllDownloaded()))
              {
                System.out.println("low speed detected.. lets pause");
                this.parent.setState(PlayerState.PAUSED_BUFFERING);
                this.parent.osync.notifyAll();
              }
              else if ((state == PlayerState.PAUSED_BUFFERING) && (
                (this.parent.currentBufferedTimeMcsec - time > 10000000L) || (this.parent.getPumpStream().isAllDownloaded())))
              {
                System.out.println("lets resume");
                this.parent.osync.wait(500L);
                this.parent.setState(PlayerState.PLAYING);
                this.parent.osync.notifyAll();
                this.parent.osync.wait(500L);
              }
            }
          }
        }
      }
    }
    catch (InterruptedException ignore) {}finally
    {
      if (this.creator.isAlive()) {
        this.creator.die();
      }
    }
  }
  
  public void die()
  {
    this.dieRequested = true;
    interrupt();
  }
}
