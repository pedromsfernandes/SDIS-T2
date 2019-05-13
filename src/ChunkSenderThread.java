import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

class TCPChunkSenderThread implements Runnable {

    private byte[] message;
    private DataOutputStream dos;
    private DataInputStream dis;
    
    public TCPChunkSenderThread(Node peer, byte[] message, String ip, int port){
        this.message = message;

        SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        try {
            SSLSocket client = (SSLSocket) factory.createSocket(ip, port);
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
            int oi = Integer.parseInt(Utils.getHeader(message)[4]);
            Thread.sleep(400);

            dos.writeInt(oi);

            if(!dis.readBoolean()){
                return;
            }

            dos.writeInt(message.length); // write length of the message
            dos.write(message); 
            dos.flush();
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
}