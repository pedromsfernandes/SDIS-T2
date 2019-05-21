import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Node extends ExternalNode {
	ScheduledExecutorService executor;
	ExternalNode predecessor;
	ExternalNode successor;
	ExternalNode[] fingerTable;
	ConcurrentHashMap<BigInteger,String> keys;

	Node(String ip,int port) throws UnknownHostException {
		super(ip, port);

		keys = new ConcurrentHashMap<>();

		this.predecessor = null;
		this.successor = this;

		fingerTable = new ExternalNode[160];
		for(int i = 0; i < fingerTable.length; i++)
			fingerTable[i] = this;

		executor = Executors.newScheduledThreadPool(25);
		executor.execute(new NodeThread(this));
		executor.scheduleAtFixedRate(new StabilizeThread(this), 15, 15, TimeUnit.SECONDS);
	}
	public void join(ExternalNode ringNode) throws UnknownHostException, IOException {
		this.predecessor = null;
		this.successor = ringNode.findSuccessor(this.id, this.id);
		fingerTable[0] = this.successor;
	}

	public static boolean idBetween(BigInteger id, BigInteger lhs, BigInteger rhs) {
		if(lhs.compareTo(rhs) > 0)
			return (id.compareTo(lhs) >= 0) || (id.compareTo(rhs) <= 0);
		
		return (id.compareTo(lhs) >= 0) && (id.compareTo(rhs) <= 0);
	}

	public ExternalNode findSuccessor(BigInteger requestId, BigInteger id) {
		if(id.equals(this.id))
			return this;

		if(idBetween(id, this.id, this.successor.id))
			return this.successor;

		else {
			ExternalNode n0 = closestPrecedingNode(id);

			if(n0.id.equals(this.id))
				return this;

			return n0.findSuccessor(this.id, id);
		}
	}

	public ExternalNode closestPrecedingNode(BigInteger id) {
		for(int i = fingerTable.length - 1; i >= 0; i++)
			if(fingerTable[i] != null && idBetween(fingerTable[i].id, this.id, id))
				return fingerTable[i];

		return this;
	}

	public ExternalNode getPredecessor(BigInteger requestId) {
		return this.predecessor;
	}

	public void notify(BigInteger requestId, ExternalNode other) {
		if(this.predecessor == null || !this.predecessor.id.equals(this.id) || idBetween(other.id, this.predecessor.id, this.id)) {			
			if(other.id.equals(this.id))
				return;

			this.predecessor = other;
			//this.predecessor.giveKeys(this.id); Calculate keys to give first
		}
	}

	public void stabilize() {
		ExternalNode x = this.successor.getPredecessor(this.id);

		if(x != null && !this.id.equals(x.id) && (this.id.equals(this.successor.id) || idBetween(x.id, this.id, successor.id))) {
			this.successor = x;
			fingerTable[0] = x;
		}

		this.successor.notify(this.id, this);
	}

	public void fixFingers() {
		for(int i = 0; i < fingerTable.length; i++) {
			fingerTable[i] = findSuccessor(this.id, this.id.add(new BigInteger(1,(Integer.toString((int) Math.pow(2,i))).getBytes())).mod(new BigInteger(1,(Integer.toString((int) Math.pow(2,this.id.bitLength()))).getBytes())));

			if(i == 0) {
				this.successor = fingerTable[0];
			}
		}
	}

	public boolean failed(BigInteger requestId) {
		return false;
	}

	public void checkPredecessor() {
		if(this.predecessor != null && this.predecessor.failed(this.id))
			this.predecessor = null;
	}

	public String backup(DataOutputStream out, DataInputStream in) throws IOException {
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

	public String restore(DataOutputStream out, DataInputStream in) {
		String response = "";


		return response;
	}

	public String delete(DataOutputStream out, DataInputStream in) {
		String response = "";


		return response;
	}

	public static void main(String[] args) throws IOException {

		Node node = new Node("10.227.155.126",Integer.parseInt(args[0]));
		node.executor.execute(new ClientRequestListenerThread(node));
	}
}