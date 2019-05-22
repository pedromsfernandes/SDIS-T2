import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Node extends ExternalNode {
	ScheduledExecutorService executor;
	ExternalNode predecessor;
	ExternalNode successor;
	ExternalNode[] fingerTable;
	ConcurrentHashMap<BigInteger, String> keys;
	Storage storage;

	Node(String ip,int port) throws UnknownHostException {
		super(ip, port);

		keys = new ConcurrentHashMap<>();
		storage = new Storage(id.toString());

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

		for(int i = 0; i < fingerTable.length; i++)
			fingerTable[i] = this.successor;
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

	public HashMap<BigInteger,String> computeKeys(BigInteger otherId) {
		HashMap<BigInteger,String> keysToGive = new HashMap<>();

		Enumeration<BigInteger> mapKeys = keys.keys();

		while(mapKeys.hasMoreElements()) {
			BigInteger i = mapKeys.nextElement();

			if(i.compareTo(otherId) < 0)
			{
				keysToGive.put(i, keys.get(i));
				keys.remove(i);
			} 
		}

		return keysToGive;
	}

	public void notify(BigInteger requestId, ExternalNode other) {
		if(this.predecessor == null || !this.predecessor.id.equals(this.id) || idBetween(other.id, this.predecessor.id, this.id)) {			
			if(other.id.equals(this.id))
				return;

			this.predecessor = other;

			this.predecessor.giveKeys(this.id, computeKeys(other.id));
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
		for(int i = 1; i < fingerTable.length; i++) {
			BigInteger fingerID = (this.id.add(new BigInteger("2").pow(i))).mod(new BigInteger("2").pow(fingerTable.length));
			fingerTable[i] = findSuccessor(this.id, fingerID);
		}
	}

	public boolean failed(BigInteger requestId) {
		return false;
	}

	public void checkPredecessor() {
		if(this.predecessor != null && this.predecessor.failed(this.id))
			this.predecessor = null;
	}

	public void storeKey(BigInteger requestId, BigInteger encrypted, String value) {
		keys.put(encrypted, value);
	}

	public String getKey(BigInteger requestId, BigInteger encrypted) {
		return keys.get(encrypted);
	}

	public void deleteKey(BigInteger requestId, BigInteger encrypted) {
		keys.remove(encrypted);
	}

	public void deleteChunk(BigInteger requestId, BigInteger encrypted) {
		keys.remove(encrypted);
		storage.delete(encrypted);
	}

	public String backup(DataOutputStream out, DataInputStream in) throws IOException {
		int chunks = in.readInt();

		for(int i = 0; i < chunks; i++){
			int length = in.readInt();
			byte[] message = new byte[length];
			in.readFully(message, 0, message.length); // read the message

			String[] header = Utils.getHeader(message);
			byte[] content = Utils.getChunkContent(message, length);

			if(i == 0) {
				String key = header[1];
				BigInteger encrypted = Utils.getSHA1(key);

				ExternalNode successor = this.findSuccessor(this.id, encrypted);

				if(successor.id == this.id) {
					this.storeKey(this.id, encrypted, chunks + ":" + header[3]);
				} else {
					successor.storeKey(this.id, encrypted, chunks + ":" + header[3]);
				}
			}

			for(int j = 0; j < Integer.parseInt(header[3]); j++){
				String key = header[1] + "-" + header[2] + "-" + j;
				BigInteger encrypted = Utils.getSHA1(key);

				ExternalNode successor = this.findSuccessor(this.id, encrypted);
				
				if(successor.id == this.id){
					storeChunk(encrypted, key, content);
				}
				else{
					executor.execute(new ChunkSenderThread(this, successor, encrypted, key, content));
				}

			}
		}

		return "BACKED UP";
	}

	public String restore(DataOutputStream out, DataInputStream in) throws IOException {
		String fileName = in.readUTF();
		BigInteger encrypted = Utils.getSHA1(fileName);

		ExternalNode successor = this.findSuccessor(this.id, encrypted);

		String value;

		if(successor.id == this.id){
			value = this.getKey(this.id, encrypted);
		}
		else {
			value = successor.getKey(this.id, encrypted);
		}

		//PEDIR CHUNKS, E MANDAR DE VOLTA PARA O TESTAPP

		// String[] args = value.split(":",2);
		// int chunks = Integer.parseInt(args[0]);

		// for(int i = 0; i < chunks; i++) {
		// 	String key = fileName + "-" + i + "-0";
		// 	BigInteger chunkID = Utils.getSHA1(key);

		// 	ExternalNode successor = this.findSuccessor(this.id, encrypted);
		// }


		return "RESTORED";
	}

	public String delete(DataOutputStream out, DataInputStream in) throws IOException {
		String fileName = in.readUTF();
		BigInteger encrypted = Utils.getSHA1(fileName);

		ExternalNode successor = this.findSuccessor(this.id, encrypted);

		String value;

		if(successor.id == this.id){
			value = this.getKey(this.id, encrypted);
		}
		else {
			value = successor.getKey(this.id, encrypted);
		}

		String[] args = value.split(":",2);
		int chunks = Integer.parseInt(args[0]);
		int repDegree = Integer.parseInt(args[1]);

		for(int i = 0; i < chunks; i++){
			for(int j = 0; j < repDegree; j++){
				String key = fileName + "-" + i + "-" + j;
				BigInteger chunkID = Utils.getSHA1(key);

				ExternalNode chunkSuccessor = this.findSuccessor(this.id, encrypted);
				
				if(successor.id == this.id) {
					deleteChunk(this.id, chunkID);
				}
				else {
					chunkSuccessor.deleteChunk(this.id, chunkID);
				}
			}
		}

		if(successor.id == this.id){
			this.deleteKey(this.id, encrypted);
		}
		else {
			successor.deleteKey(this.id, encrypted);
		}

		return "DELETED";
	}

	public static void main(String[] args) throws IOException {
		Node node = new Node(args[0],Integer.parseInt(args[1]));
		node.executor.execute(new ClientRequestListenerThread(node));

		if(args.length == 4)
			node.join(new ExternalNode(args[2],Integer.parseInt(args[3])));
	}

	public Storage getStorage() {
		return storage;
	}

	public void setStorage(Storage storage) {
		this.storage = storage;
	}

	public void storeChunk(BigInteger key, String value, byte[] chunk) {
		this.keys.put(key, value);
		this.storage.storeChunk(key, chunk);
	}
}