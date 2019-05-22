import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigInteger;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

class ChunkSenderThread implements Runnable {

    private DataOutputStream dos;
    private DataInputStream dis;
    private BigInteger key;
    private String value;
    private byte[] content;
    
    public ChunkSenderThread(Node peer, ExternalNode successor, BigInteger key, String value, byte[] content){

        this.key = key;
        this.content = content;
        this.value = value;

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
            dos.writeUTF("BACKUP\n");
            dos.writeUTF(key.toString()); 
            dos.writeUTF(value); 
            dos.writeInt(content.length);
            dos.write(content);
            dos.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
}