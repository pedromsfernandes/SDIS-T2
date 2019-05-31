import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigInteger;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

class ChunkSenderThread implements Runnable {
    private Node peer;
    private DataOutputStream dos;
    private BigInteger key;
    private String value;
    private byte[] content;
    private boolean deleteAfter;
    
    /**
     * 
     * @param peer
     * @param successor
     * @param key
     * @param value
     * @param content
     * @param deleteAfter
     */
    public ChunkSenderThread(Node peer, ExternalNode successor, BigInteger key, String value, byte[] content, boolean deleteAfter){
        this.peer = peer;
        this.key = key;
        this.content = content;
        this.value = value;
        this.deleteAfter = deleteAfter;

        SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        try {
            SSLSocket client = (SSLSocket) factory.createSocket(successor.ip, successor.port);
            dos = new DataOutputStream(client.getOutputStream());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            dos.writeUTF("BACKUP\n");
            dos.writeUTF(key.toString()); 
            dos.writeUTF(value); 
            dos.writeInt(content.length);
            dos.write(content);
            dos.flush();
            if(this.deleteAfter)
                peer.deleteChunk(peer.id, key);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
}