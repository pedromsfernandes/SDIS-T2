import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigInteger;

import javax.net.ssl.SSLSocket;

class ChunkReceiverThread implements Runnable {

    private Node peer;
    private DataInputStream dis;
    private DataOutputStream dos;
    private SSLSocket server;

    public ChunkReceiverThread(Node peer, SSLSocket server) {
        this.peer = peer;
        this.server = server;
    }

    @Override
    public void run() {

            try {


                try {
                    this.dis = new DataInputStream(server.getInputStream());
                    this.dos = new DataOutputStream(server.getOutputStream());

                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                System.out.println("CHEGUEI CRL");
                String fileName = dis.readUTF();
                String numChunks = dis.readUTF();
                String key = dis.readUTF();

                int length = dis.readInt(); // read length of incoming message
                if (length > 0) {
                    byte[] chunk = new byte[length];
                    dis.readFully(chunk, 0, chunk.length); // read the message

                    peer.storeChunk(new BigInteger(fileName), numChunks, new BigInteger(key), chunk);
                }
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }

    }

}