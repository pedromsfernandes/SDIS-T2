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
            DataOutputStream out = null;
            DataInputStream in = null;
            String request = "";
            String response = "";

            try {
                s = (SSLSocket) ss.accept();

                out = new DataOutputStream(s.getOutputStream());
                in = new DataInputStream(s.getInputStream());

                request = in.readUTF();

                switch (request) {
                case "BACKUP":
                    response = node.backup(out, in);
                    break;
                case "RESTORE":
                    response = node.restore(out, in);
                    break;
                case "DELETE":
                    response = node.delete(out, in);
                    break;
                default:
                    break;
                }

                System.out.println(response);
                out.writeUTF(response);

                in.close();
                out.close();
                s.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
    }

}