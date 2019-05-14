import java.io.IOException;

class ChordTest {
	public static void main(String[] args) throws IOException {
		if(args.length == 0) {
			Node node = new Node(8080);
			System.out.println(node.ip + ":" + node.port);
		}

		else {
			Node node = new Node(8081);
			System.out.println(node.id);
			node.join(new ExternalNode(args[0],Integer.parseInt(args[1])));
			System.out.println(node.successor.id);
		}
	}
}