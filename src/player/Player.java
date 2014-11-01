package player;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicReference;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.SourceDataLine;
import maryb.player.io.SeekablePumpStream;

public final class Player {
	private float currentVolume = 0.5F;
	private String sourceLocation = null;
	private volatile PlayerState state = PlayerState.STOPPED;
	volatile long currentBufferedTimeMcsec = 0L;
	volatile long currentPlayTimeMcsec = 0L;
	volatile long currentSeekPositionMcsec = 0L;
	volatile long totalPlayTimeMcsec = 0L;
	private PlayerEventListener listener;
	volatile long lastPlayerActivity = 0L;
	boolean endOfMediaReached = false;

	public float getCurrentVolume() {
		return this.currentVolume;
	}

	public void setCurrentVolume(float currentVolume) {
		if ((currentVolume > 1.0D) || (currentVolume < 0.0F)) {
			throw new IllegalArgumentException(
					"volume should be non-negative and less or equal to 1.0");
		}
		this.currentVolume = currentVolume;

		SourceDataLine line = this.currentDataLine;
		if (line != null) {
			populateVolume(line);
		}
	}

	public String getSourceLocation() {
		return this.sourceLocation;
	}

	public void setSourceLocation(String sourceLocation) {
		if (this.sourceLocation == null) {
			this.sourceLocation = sourceLocation;
		} else if (this.sourceLocation.equals(sourceLocation)) {
			return;
		}
		this.sourceLocation = sourceLocation;

		stop();
		SeekablePumpStream pump;
		InputStream is;
		synchronized (this.osync) {
			this.currentBufferedTimeMcsec = 0L;
			this.currentPlayTimeMcsec = 0L;
			this.currentSeekPositionMcsec = 0L;

			pump = this.pumpStream;
			is = this.realInputStream;

			this.pumpStream = null;
			is = null;

			this.osync.notifyAll();
		}
		close(pump);
		close(is);
	}

	public PlayerState getState() {
		return this.state;
	}

	void setState(PlayerState state) {
		this.state = state;
	}

	public long getCurrentBufferedTimeMcsec() {
		return this.currentBufferedTimeMcsec;
	}

	public long getCurrentPosition() {
		return this.currentPlayTimeMcsec + this.currentSeekPositionMcsec;
	}

	public long getTotalPlayTimeMcsec() {
		return this.totalPlayTimeMcsec;
	}

	public void setListener(PlayerEventListener listener) {
		this.listener = listener;
	}

	public PlayerEventListener getListener() {
		return this.listener;
	}

	public boolean isEndOfMediaReached() {
		return this.endOfMediaReached;
	}

	final Object osync = new Object();
	final Object seekSync = new Object();
	volatile SourceDataLine currentDataLine;
	final AtomicReference<PlayerThread> currentPlayerThread = new AtomicReference<PlayerThread>();
	SeekThread seekThread = null;
	long currentSeekTo = -1L;
	int realInputStreamLength;
	InputStream realInputStream;
	private SeekablePumpStream pumpStream;

	SourceDataLine getCurrentDataLine() {
		return this.currentDataLine;
	}

	SeekablePumpStream getPumpStream() {
		return this.pumpStream;
	}

	void setPumpStream(SeekablePumpStream pumpStream) {
		this.pumpStream = pumpStream;
	}

	void populateVolume(SourceDataLine line) {
		try {
			if ((line != null)
					&& (line.isControlSupported(FloatControl.Type.VOLUME))) {
				FloatControl c = (FloatControl) line
						.getControl(FloatControl.Type.VOLUME);
				float interval = c.getMaximum() - c.getMinimum();
				float cv = this.currentVolume;

				interval *= cv;
				c.setValue(c.getMinimum() + interval);
			}
			return;
		} catch (Throwable t) {
			try {
				if ((line != null)
						&& (line.isControlSupported(FloatControl.Type.MASTER_GAIN))) {
					FloatControl c = (FloatControl) line
							.getControl(FloatControl.Type.MASTER_GAIN);
					float interval = c.getMaximum() - c.getMinimum();
					float cv = this.currentVolume;

					interval *= cv;
					c.setValue(c.getMinimum() + interval);
				}
				return;
			} catch (Throwable t1) {
			}
		}
	}

	public void play() {
		if (this.state == PlayerState.PLAYING) {
			return;
		}
		PlayerThread th = (PlayerThread) this.currentPlayerThread.get();
		if (th == null) {
			th = new PlayerThread(this);
			if (this.currentPlayerThread.compareAndSet(null, th)) {
				th.start();
			}
		}
	}

	private void waitForState(PlayerState st) throws InterruptedException {
		synchronized (this.osync) {
			while (this.state != st) {
				this.osync.wait();
			}
		}
	}

	void setStateNotify(PlayerState pst) {
		synchronized (this.osync) {
			this.state = pst;
			this.osync.notifyAll();
		}
	}

	public void playSync() throws InterruptedException {
		play();
		waitForState(PlayerState.PLAYING);
	}

	private void interruptPlayback(PlayerState st) {
		if (this.state == st) {
			return;
		}
		PlayerThread th = (PlayerThread) this.currentPlayerThread.get();
		if (th != null) {
			if (th.isAlive()) {
				th.die(st);
			} else if (this.currentPlayerThread.compareAndSet(th, null)) {
				setStateNotify(st);
			}
		} else {
			setStateNotify(st);
		}
	}

	public void stop() {
		interruptPlayback(PlayerState.STOPPED);
		this.currentSeekPositionMcsec = 0L;
		this.currentPlayTimeMcsec = 0L;
		SourceDataLine l = this.currentDataLine;
		if (l != null) {
			try {
				l.stop();
			} catch (Throwable ignoredT) {
			}
		}
	}

	public void stopSync() throws InterruptedException {
		stop();
		waitForState(PlayerState.STOPPED);
	}

	public void pause() {
		interruptPlayback(PlayerState.PAUSED);
	}

	public void pauseSync() throws InterruptedException {
		pause();
		waitForState(PlayerState.PAUSED);
	}

	public void seek(long newPos) {
		synchronized (this.seekSync) {
			this.currentSeekTo = newPos;
			if (this.seekThread == null) {
				(this.seekThread = new SeekThread(this)).start();
			}
		}
	}

	private static void close(Closeable c) {
		try {
			if (c != null) {
				c.close();
			}
		} catch (IOException ignore) {
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	private static void close(SeekablePumpStream sps) {
		try {
			if (sps != null) {
				sps.realClose();
			}
		} catch (IOException ignore) {
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
}
