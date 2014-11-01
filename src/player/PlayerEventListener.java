package player;

public abstract interface PlayerEventListener
{
  public abstract void endOfMedia();
  
  public abstract void stateChanged();
  
  public abstract void buffer();
}
