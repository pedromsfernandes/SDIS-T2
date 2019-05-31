public class StabilizeThread implements Runnable {
	private Node node;

	/**
	 * 
	 * @param node
	 */
	StabilizeThread(Node node) {
		this.node = node;
	}

	public void run() {
		try {
			node.stabilize();
			node.fixFingers();
			node.checkPredecessor();

			System.out.print("Pre: ");

			if (node.predecessor != null)
				System.out.print(node.predecessor.id);

			else
				System.out.print("null");

			System.out.print(" ID:" + node.id + " Suc: ");

			if (node.successor != null)
				System.out.println(node.successor.id);

			else
				System.out.println("null");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}