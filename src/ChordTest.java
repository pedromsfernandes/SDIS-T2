class ChordTest {
	public static void main(String[] args) {
		try {
			Node node = new Node(args[0],Integer.parseInt(args[1]));

			if(args.length != 2)
				node.join(new ExternalNode(args[2],Integer.parseInt(args[3])));
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}