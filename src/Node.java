import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

public class Node extends ExternalNode {
	ThreadPoolExecutor executor;
	ExternalNode predecessor;
	ExternalNode successor;

	Node(int port) throws UnknownHostException {
		super(Inet4Address.getLocalHost().getHostAddress(), port);

		this.predecessor = null;
		this.successor = this;

		executor = (ThreadPoolExecutor) Executors.newScheduledThreadPool(10);
		executor.execute(new NodeThread(this));
	}

	public void join(ExternalNode ringNode) throws UnknownHostException, IOException {
		this.predecessor = null;
		this.successor = ringNode.findSuccessor(this.id);
	}

	public ExternalNode findSuccessor(String id) throws UnknownHostException, IOException {
		if (id.compareTo(this.id) < 0 || id.compareTo(this.successor.id) >= 0)
			return this;

		if (this != successor)
			return successor.findSuccessor(id);

		return null;
	}

	private String backup(DataOutputStream out, DataInputStream in) throws IOException {
		String response = "";

		int chunks = in.readInt();

		for(int i = 0; i < chunks; i++){
			int length = in.readInt();
			byte[] message = new byte[length];
			in.readFully(message, 0, message.length); // read the message

			// TODO: iniciar thread para enviar cada chunk para replicationDegree nodes
			// executor.execute(new ChunkSenderThread(this, message, ip, port));
		}


		return response;
	}

	private String restore(DataOutputStream out, DataInputStream in) {
		String response = "";


		return response;
	}

	private String delete(DataOutputStream out, DataInputStream in) {
		String response = "";


		return response;
	}

	public static void main(String[] args) throws IOException {

		Node node = new Node(Integer.parseInt(args[0]));

		SSLServerSocketFactory ssf = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
		SSLServerSocket ss = (SSLServerSocket) ssf.createServerSocket(8081);

		SSLSocket s = (SSLSocket) ss.accept();

		DataOutputStream out = new DataOutputStream(s.getOutputStream());
		DataInputStream in = new DataInputStream(s.getInputStream());

		String request = in.readUTF();
		String response = "";

		switch (request) {
		case "BACKUP":
			response = node.backup(out, in);
			break;
		case "RESTORE":
			response = node.restore(out, in);
			break;
		case "DELETE":
			response = node.delete(out, in);
			break;
		default:
			break;
		}

		System.out.println(response);
		out.writeUTF("oioioi");

		in.close();
		out.close();
		s.close();
	}
}