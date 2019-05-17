import java.io.IOException;

class ChordTest {
	public static void main(String[] args) throws IOException {
		Node node = new Node(args[0],Integer.parseInt(args[1]));
		if(args.length == 2) {
			System.out.println(node.ip + ":" + node.port);
		}

		else {
			node.join(new ExternalNode(args[2],Integer.parseInt(args[3])));
			System.out.println("joined " + node.successor.id);
		}
	}
}