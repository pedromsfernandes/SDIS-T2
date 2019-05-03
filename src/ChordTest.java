import java.io.IOException;

class ChordTest {
	public static void main(String[] args) throws IOException {
		if(args.length == 0)
			System.out.println(new Node(8080).id);

		else {
			Node node = new Node(8080);
			System.out.println(node.id);
			node.join(new ExternalNode(args[0],Integer.parseInt(args[1])));
			System.out.println(node.successor);
		}
	}
}