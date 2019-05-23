import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigInteger;

import javax.net.ssl.SSLSocket;

class ChunkRequestListenerThread implements Runnable {

    private DataOutputStream dos;
    private DataInputStream dis;
    private Node node;
    private SSLSocket server;

    public ChunkRequestListenerThread(Node node, SSLSocket server) {
        this.node = node;
        this.server = server;
        try {
            this.dis = new DataInputStream(server.getInputStream());
            this.dos = new DataOutputStream(server.getOutputStream());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void run() {

        try {
            String chunkID = dis.readUTF();
            byte[] chunk = node.storage.readChunk(new BigInteger(chunkID));

            dos.writeInt(chunk.length);
            dos.write(chunk);
            dos.flush();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

}