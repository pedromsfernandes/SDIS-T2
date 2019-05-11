import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

class Server {
    public static void main(String[] args) throws IOException {
        SSLServerSocketFactory ssf = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
        SSLServerSocket ss = (SSLServerSocket) ssf.createServerSocket(8081);
        
        SSLSocket s = (SSLSocket) ss.accept();

        OutputStream rawOut = s.getOutputStream();

        PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(rawOut)));

        BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));

        String request = in.readLine();

        System.out.println(request);
        out.println("oioioi");

        in.close();
        out.close();
        s.close();

    }
}