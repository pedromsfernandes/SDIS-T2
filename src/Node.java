import java.io.IOException;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

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
		if(id.compareTo(this.id) < 0 || id.compareTo(this.successor.id) >= 0)
			return this;

		if(this != successor)
			return successor.findSuccessor(id);

		return null;
	}
}