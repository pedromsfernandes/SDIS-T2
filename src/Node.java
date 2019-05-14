import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.Hashtable;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

public class Node extends ExternalNode {
	ScheduledExecutorService executor;
	ExternalNode predecessor;
	ExternalNode successor;
	ExternalNode[] fingerTable;
	Hashtable<BigInteger,String> keys;

	Node(int port) throws UnknownHostException {
		super(Inet4Address.getLocalHost().getHostAddress(), port);

		keys = new Hashtable<>();

		this.predecessor = this;
		this.successor = this;

		fingerTable = new ExternalNode[this.id.bitLength()];
		for(int i = 0; i < fingerTable.length; i++)
			fingerTable[i] = this;

		executor = Executors.newScheduledThreadPool(25);
		executor.execute(new NodeThread(this));
		executor.scheduleAtFixedRate(new StabilizeThread(this), 0, 5, TimeUnit.SECONDS);
	}

	public void join(ExternalNode ringNode) throws UnknownHostException, IOException {
		this.predecessor = null;
		this.successor = ringNode.findSuccessor(this.id);
		//this.successor.getKeys();
		fingerTable[0] = this.successor;
	}

	public static boolean idBetween(BigInteger id, BigInteger lhs, BigInteger rhs) {
		if(lhs.compareTo(rhs) > 0)
			return (id.compareTo(lhs) >= 0) || (id.compareTo(rhs) <= 0);
		
		return (id.compareTo(lhs) >= 0) && (id.compareTo(rhs) <= 0);
	}

	public ExternalNode findSuccessor(BigInteger id) {
		if(id.equals(this.id))
			return this;

		if(idBetween(id, this.id, this.successor.id))
			return this.successor;

		else {
			ExternalNode n0 = closestPrecedingNode(id);

			if(n0.id.equals(this.id))
				return this;

			return n0.findSuccessor(id);
		}
	}

	public ExternalNode closestPrecedingNode(BigInteger id) {
		for(int i = fingerTable.length - 1; i >= 0; i++)
			if(fingerTable[i] != null && idBetween(fingerTable[i].id, this.id, id))
				return fingerTable[i];

		return this;
	}

	public ExternalNode getPredecessor() {
		return this.predecessor;
	}

	public void notify(ExternalNode other) {
		if(this.predecessor == null || idBetween(other.id, this.predecessor.id, this.id))
			this.predecessor = other;
	}

	public void stabilize() {
		ExternalNode x = this.successor.getPredecessor();

		if(x != null && idBetween(x.id, this.id, successor.id)) {
			this.successor = x;
			//this.successor.getKeys();
			fingerTable[0] = x;
		}

		this.successor.notify(this);
	}

	public void fixFingers() {
		for(int i = 0; i < fingerTable.length; i++) {
			fingerTable[i] = findSuccessor(this.id.add(new BigInteger(Integer.toString((int) Math.pow(2,i)))).mod(new BigInteger(Integer.toString((int) Math.pow(2,this.id.bitLength())))));

			if(i == 0) {
				this.successor = fingerTable[0];
				//this.successor.getKeys();
			}
		}
	}

	public boolean failed() {
		return false;
	}

	public void checkPredecessor() {
		if(this.predecessor != null && this.predecessor.failed())
			this.predecessor = null;
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