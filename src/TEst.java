import com.klemstinegroup.wub.AudioObject;
import com.klemstinegroup.wub.Player;


public class TEst {
	public static void main(String[] args) {
		AudioObject au=new AudioObject("songs/heat.mp3");
		Player player = new Player();
		player.play(au,0,10);
		
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
