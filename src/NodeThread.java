import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class NodeThread implements Runnable {
	private Node node;

	NodeThread(Node node) {
		this.node = node;
	}

	public void interpretMessage(Socket connection) throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

		String message = in.readLine().trim();
		System.out.print("[Node " + node.id + "] "+ message);
		String[] args = message.split(" ");
		
		ExternalNode successor = node.findSuccessor(args[2]);

		String response = "SUCCESSOR " + node.id + " ";

		if(successor == null)
			response += "NOTFOUND \n";

		else response += successor.id + " \n";

		System.out.print("[Node " + node.id + "] "+ response);

		DataOutputStream out = new DataOutputStream(connection.getOutputStream());
		out.writeBytes(response);
	}

	public void run() {
		try {
			ServerSocket listenSocket = new ServerSocket(node.port);

			while(true) {
				Socket connection = listenSocket.accept();
				node.executor.execute(new Runnable(){
					public void run() {
						try {
							interpretMessage(connection);
						} catch (IOException e) {
						}
					}
				});		
			}
		} catch(Exception e) {}
	}
}