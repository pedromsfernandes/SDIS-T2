import java.math.BigInteger;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

class CheckChunkReceiveThread implements Runnable {
    private Node node;
    private ExternalNode successor;
    private String key;
    private String fileName;
    private ArrayList<String> keys;

    public CheckChunkReceiveThread(Node node, ExternalNode successor, String key, ArrayList<String> keys, String fileName) {
        this.node = node;
        this.successor = successor;
        this.key = key;
        this.fileName = fileName;
        this.keys = keys;
    }

    @Override
    public void run() {
        if(!node.keys.contains(Utils.getSHA1(key))){
            int index = key.indexOf("-");
            String begin = key.substring(0, index + 1);
            int repDegree = Integer.parseInt(key.substring(index + 1)) + 1;
            key = begin + repDegree;
            
			BigInteger chunkID = Utils.getSHA1(key);
			keys.add(chunkID.toString());
			successor = this.node.findSuccessor(this.node.id, chunkID);

            if (successor.id == this.node.id) {
				node.storage.addRestoredChunk(chunkID.toString(), fileName, node.storage.readChunk(chunkID));
			} else {
				node.executor.execute(new ChunkRequestThread(node, successor, chunkID, fileName));
				node.executor.schedule(new CheckChunkReceiveThread(node, successor, key, keys, fileName), 5, TimeUnit.SECONDS);
			}
        }
    }
}