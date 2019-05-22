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
				System.out.print(node.successor.id);

			else System.out.print("null");

			// System.out.println("\nFingerTable:");

			// for(int i = 0; i < node.fingerTable.length; i++) {
			// 	if(node.fingerTable[i] == null)
			// 		System.out.println(i + ": null");

			// 	else System.out.println(i + ": " + node.fingerTable[i].id);
			// }
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}