import java.io.IOException;
import java.net.UnknownHostException;

public class StabilizeThread implements Runnable {
	private Node node;

	StabilizeThread(Node node) {
		this.node = node;
	}

	public void run() {
		node.stabilize();
		//node.fixFingers();
		node.checkPredecessor();
	}
}