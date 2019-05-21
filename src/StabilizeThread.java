import java.io.IOException;
import java.net.UnknownHostException;

public class StabilizeThread implements Runnable {
	private Node node;

	StabilizeThread(Node node) {
		this.node = node;
	}

	public void run() {
		try {
			node.stabilize();
			//node.fixFingers();
			node.checkPredecessor(); 

			System.out.print("Pre: ");

			if(node.predecessor != null)
				System.out.print(node.predecessor.id);

			else System.out.print("null");

			System.out.print(" ID:" + node.id + " Suc: ");
			
			if(node.successor != null)
				System.out.print(node.successor.id);

			else System.out.print("null");

			System.out.print("\n");
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}