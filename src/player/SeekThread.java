package player;

class SeekThread
  extends Thread
{
  private final Player parent;
  
  public SeekThread(Player parent)
  {
    super("player-seek-thread");
    this.parent = parent;
  }
  
  public void run()
  {
    try
    {
      for (;;)
      {
        synchronized (this.parent.seekSync)
        {
          if (this.parent.currentSeekTo == -1L)
          {
            this.parent.seekThread = null;
            this.parent.seekSync.notifyAll();
            return;
          }
        }
        PlayerState st = this.parent.getState();
        this.parent.stopSync();
        synchronized (this.parent.seekSync)
        {
          this.parent.currentSeekPositionMcsec = this.parent.currentSeekTo;
          this.parent.currentSeekTo = -1L;
        }
        switch (st)
        {
        case PAUSED_BUFFERING: 
        case PLAYING: 
          this.parent.play();
          break;
        case PAUSED: 
          synchronized (this.parent.osync)
          {
            this.parent.setState(st);
            this.parent.osync.notifyAll();
          }
        }
      }
    }
    catch (InterruptedException ignore)
    {
      ignore = ignore;
      
      return;
    }
    finally {}
  }
}
