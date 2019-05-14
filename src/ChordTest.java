import java.io.IOException;

class ChordTest {
	public static void main(String[] args) throws IOException {
		if(args.length == 2) {
			Node node = new Node(args[0],Integer.parseInt(args[1]));
			System.out.println(node.ip + ":" + node.port);
		}

		else {
			Node node = new Node(args[0],Integer.parseInt(args[1]));
			System.out.println(node.id);
			node.join(new ExternalNode(args[2],Integer.parseInt(args[3])));
			System.out.println(node.successor.id);
		}
	}
}