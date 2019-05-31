import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

class ClientRequestListenerThread implements Runnable {

    private Node node;

    public ClientRequestListenerThread(Node node) {
        this.node = node;
    }

    @Override
    public void run() {
        SSLServerSocketFactory ssf = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
        SSLServerSocket ss = null;

        try {
            ss = (SSLServerSocket) ssf.createServerSocket(node.port + 80);
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        while (true) {
            SSLSocket s;
    
            try {
                s = (SSLSocket) ss.accept();

                node.executor.execute(new ClientRequestHandlerThread(node, s));
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
    }

}