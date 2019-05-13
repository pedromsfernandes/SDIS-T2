import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;

class ChunkReceiverThread implements Runnable {

    private Node peer;
    private DataInputStream dis;
    private DataOutputStream dos;
    private byte[] file;
    private SSLServerSocket serverSocket;

    public ChunkReceiverThread(Node peer, SSLServerSocket serverSocket, byte[] file) {
        this.peer = peer;
        this.file = file;
        this.serverSocket = serverSocket;
    }

    @Override
    public void run() {

        while (!serverSocket.isClosed()) {
            try {

                SSLSocket server;

                try {
                    server = (SSLSocket) serverSocket.accept();
                    this.dis = new DataInputStream(server.getInputStream());
                    this.dos = new DataOutputStream(server.getOutputStream());

                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                int chunk = dis.readInt();
                // boolean acceptChunk = !peer.getStorage().hasRestoredChunk(Utils.bytesToHex(file) + "-" + chunk);

                // dos.writeBoolean(acceptChunk);
                // dos.flush();

                // if (!acceptChunk) {
                //     continue;
                // }

                int length = dis.readInt(); // read length of incoming message
                if (length > 0) {
                    byte[] message = new byte[length];
                    dis.readFully(message, 0, message.length); // read the message

                    String[] header = Utils.getHeader(message);
                    byte[] chunkContent = Utils.getChunkContent(message, length);

                }
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }
    }

}