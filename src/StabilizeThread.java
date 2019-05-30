import java.math.BigInteger;

public class StabilizeThread implements Runnable {
	private Node node;

	StabilizeThread(Node node) {
		this.node = node;
	}

	public void run() {
		try {
			node.stabilize();
			node.fixFingers();
			node.checkPredecessor(); 

			System.out.print("Pre: ");

			if(node.predecessor != null)
				System.out.print(node.predecessor.id);

			else System.out.print("null");

			System.out.print(" ID:" + node.id + " Suc: ");
			
			if(node.successor != null)
				System.out.println(node.successor.id);

			else System.out.println("null");

			// System.out.println("\nFingerTable:");

			// for(int i = 0; i < node.fingerTable.length; i++) {
			// 	BigInteger fingerID = (node.id.add(new BigInteger("2").pow(i)))
			// 	.mod(new BigInteger("2").pow(node.fingerTable.length));

			// 	if(node.fingerTable[i] == null)
			// 		System.out.println(i + ": " + fingerID + " null");

			// 	else System.out.println(i + ": " + fingerID + " " + node.fingerTable[i].id);
			// }
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}