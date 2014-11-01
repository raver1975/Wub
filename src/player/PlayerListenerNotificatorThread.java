package player;

import java.util.concurrent.atomic.AtomicLong;
import maryb.player.io.SeekablePumpStream;

class PlayerListenerNotificatorThread
  extends Thread
{
  private volatile boolean dieRequest = false;
  private final Player parent;
  private final PlayerThread creator;
  private AtomicLong eventCount = new AtomicLong();
  private final Object osync = new Object();
  private NotificationWrapper w = null;
  private long lastTime = 0L;
  
  public PlayerListenerNotificatorThread(Player parent, PlayerThread creator)
  {
    super("player-notification-dispatcher-thread");
    this.parent = parent;
    this.creator = creator;
  }
  
  private void notifyListenerState()
    throws InterruptedException
  {
    long old = this.eventCount.getAndIncrement();
    if ((this.w == null) || (!this.w.isAlive()))
    {
      this.w = new NotificationWrapper();
      this.w.start();
    }
    else if ((old > 0L) && (System.currentTimeMillis() - this.lastTime > 15000L))
    {
      this.w.interrupt();
      this.w = null;
      notifyListenerState();
      return;
    }
    synchronized (this.osync)
    {
      this.osync.notifyAll();
    }
    this.lastTime = System.currentTimeMillis();
  }
  
  private boolean isStopped()
  {
    return (this.parent.getState() == PlayerState.STOPPED) || (this.parent.getState() == PlayerState.PAUSED);
  }
  
  private boolean aliveCriterion()
  {
    return (!this.dieRequest) && ((isStopped()) || (this.creator.isAlive())) && (((this.parent.getPumpStream() != null) && (!this.parent.getPumpStream().isAllDownloaded())) || ((!isStopped()) && (this.creator.isAlive())));
  }
  
  public void run()
  {
    PlayerState lastState = this.parent.getState();
    long lastPosition = this.parent.getCurrentPosition();
    long lastBufferedPosition = this.parent.getCurrentBufferedTimeMcsec();
    long lastTotalTime = this.parent.getTotalPlayTimeMcsec();
    try
    {
      notifyListenerState();
      
      boolean chk = false;
      while (aliveCriterion())
      {
        while (aliveCriterion())
        {
          chk = (lastState != this.parent.getState()) || (lastPosition != this.parent.getCurrentPosition()) || (lastBufferedPosition != this.parent.getCurrentBufferedTimeMcsec()) || (lastTotalTime != this.parent.getTotalPlayTimeMcsec());
          if (chk) {
            break;
          }
          synchronized (this.parent.osync)
          {
            this.parent.osync.wait();
          }
        }
        if (chk)
        {
          notifyListenerState();
          sleep(100L);
        }
        lastState = this.parent.getState();
        lastPosition = this.parent.getCurrentPosition();
        lastBufferedPosition = this.parent.getCurrentBufferedTimeMcsec();
        lastTotalTime = this.parent.getTotalPlayTimeMcsec();
        if (isStopped())
        {
          this.dieRequest = (!aliveCriterion());
          synchronized (this.osync)
          {
            this.osync.notifyAll();
          }
        }
      }
    }
    catch (InterruptedException ignore) {}finally
    {
      this.dieRequest = true;
      synchronized (this.osync)
      {
        this.osync.notifyAll();
      }
    }
  }
  
  public void die()
  {
    this.dieRequest = true;
    interrupt();
  }
  
  private class NotificationWrapper
    extends Thread
  {
    public NotificationWrapper()
    {
      super();
    }
    
    private void call()
    {
      try
      {
        PlayerEventListener l = PlayerListenerNotificatorThread.this.parent.getListener();
        if (l != null)
        {
          l.stateChanged();
          if (PlayerListenerNotificatorThread.this.parent.getState() == PlayerState.PAUSED_BUFFERING) {
            l.buffer();
          }
        }
      }
      catch (Throwable t)
      {
        t.printStackTrace();
      }
    }
    
    private void callEOM()
    {
      try
      {
        PlayerEventListener l = PlayerListenerNotificatorThread.this.parent.getListener();
        if (l != null) {
          l.endOfMedia();
        }
      }
      catch (Throwable t)
      {
        t.printStackTrace();
      }
    }
    
    public void run()
    {
      try
      {
        while ((!PlayerListenerNotificatorThread.this.dieRequest) && (!isInterrupted()))
        {
          synchronized (PlayerListenerNotificatorThread.this.osync)
          {
            while ((!PlayerListenerNotificatorThread.this.dieRequest) && (PlayerListenerNotificatorThread.this.eventCount.get() == 0L)) {
              PlayerListenerNotificatorThread.this.osync.wait();
            }
          }
          do
          {
            call();
            if ((PlayerListenerNotificatorThread.this.parent.getState() == PlayerState.STOPPED) && (PlayerListenerNotificatorThread.this.parent.isEndOfMediaReached())) {
              callEOM();
            }
          } while (PlayerListenerNotificatorThread.this.eventCount.decrementAndGet() > 0L);
          synchronized (PlayerListenerNotificatorThread.this.osync)
          {
            PlayerListenerNotificatorThread.this.osync.notifyAll();
          }
        }
        synchronized (PlayerListenerNotificatorThread.this.osync)
        {
          PlayerListenerNotificatorThread.this.osync.notifyAll();
        }
      }
      catch (InterruptedException ignore) {}
    }
  }
}
