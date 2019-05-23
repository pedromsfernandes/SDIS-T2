import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigInteger;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

class ChunkRequestThread implements Runnable{

    private DataOutputStream dos;
    private DataInputStream dis;
    private BigInteger chunkID;
    private Node node;
    private String file;

    public ChunkRequestThread(Node node, ExternalNode successor, BigInteger chunkID, String file) {

        this.node = node;
        this.chunkID = chunkID;
        this.file = file;

        SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        try {
            SSLSocket client = (SSLSocket) factory.createSocket(successor.ip, successor.port);
            dis = new DataInputStream(client.getInputStream());
            dos = new DataOutputStream(client.getOutputStream());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            dos.writeUTF("RESTORE\n");
            dos.writeUTF(chunkID.toString()); 
            dos.flush();

            int length = dis.readInt(); // read length of incoming message
            if (length > 0) {
                byte[] chunk = new byte[length];
                dis.readFully(chunk, 0, chunk.length); // read the message

                node.storage.addRestoredChunk(chunkID.toString(), file, chunk);
            }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}
