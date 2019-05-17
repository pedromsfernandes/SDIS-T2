import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

public class NodeThread implements Runnable {
	private Node node;

	NodeThread(Node node) {
		this.node = node;
	}

	public void findSuccessor(SSLSocket connection, String[] args) {
		ExternalNode successor = node.findSuccessor(new BigInteger(1, new BigInteger(args[2],16).toString(16).getBytes()));

		String response = "SUCCESSOR " + node.id + " ";

		if(successor == null)
			response += "NOTFOUND \n";

		else response += successor.ip + " " + successor.port + " \n";

		try {
			DataOutputStream out = new DataOutputStream(connection.getOutputStream());
			out.writeBytes(response);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void getPredecessor(SSLSocket connection, String[] args) {
		ExternalNode predecessor = node.getPredecessor();

		String response = "PREDECESSOR " + node.id + " ";

		if(predecessor == null)
			response += "NOTFOUND \n";

		else response += predecessor.ip + " " + predecessor.port + " \n";

		try {
			DataOutputStream out = new DataOutputStream(connection.getOutputStream());
			out.writeBytes(response);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void notify(SSLSocket connection, String[] args) {
		node.notify(new ExternalNode(args[2], Integer.parseInt(args[3])));

		try {
			DataOutputStream out = new DataOutputStream(connection.getOutputStream());

			out.writeBytes("OK");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void hi(SSLSocket connection, String[] args) {
		String response = "HI " + node.id + " \n";

		try {
			DataOutputStream out = new DataOutputStream(connection.getOutputStream());

			out.writeBytes(response);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void getKeys(SSLSocket connection, String[] args) {
		//TODO
	}

	public void interpretMessage(SSLSocket connection) throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

		String message = in.readLine().trim();
		System.out.println("[Node " + node.id + "] " + message);
		String[] args = message.split(" ");

		switch(args[0]) {
			case "FINDSUCCESSOR": findSuccessor(connection, args);
			case "GETPREDECESSOR": getPredecessor(connection, args);
			case "NOTIFY": notify(connection, args);
			case "HI": hi(connection, args);
			case "GETKEYS": getKeys(connection, args);
		}
	}

	public void run() {
		try {
			SSLServerSocketFactory ssf = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
			SSLServerSocket listenSocket = (SSLServerSocket) ssf.createServerSocket(node.port);
			
			while(true) {
				SSLSocket connection = (SSLSocket) listenSocket.accept();
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