import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

class TestApp {

    public static void printUsage() {
        System.err.println("Usage: java TestApp <sub_protocol> <opnd_1> <opnd_2>");
        System.err.println(
                "<sub_protocol>\nIs the operation the peer of the backup service must execute. It can be the triggering of the subprotocol to test and it must be one of: BACKUP, RESTORE, DELETE.");
        System.err.println(
                "<opnd_1>\nIs either the path name of the file to backup/restore/delete, for the respective 3 subprotocols.");
        System.err.println(
                "<opnd_2>\nThis operand is an integer that specifies the desired replication degree and applies only to the backup protocol.");
    }

    public static boolean validateArguments(String[] args) {
        if (args.length < 2 || args.length > 3) {
            System.err.println("ERROR: Invalid number of arguments!");
            printUsage();
            return false;
        }

        String operation = args[0];

        if (operation.equals("BACKUP")) {
            if (args.length != 3) {
                System.err.println("ERROR: BACKUP takes 2 operands!");
                printUsage();
                return false;
            }
            try {
                Integer.parseInt(args[2]);
            } catch (Exception e) {
                System.err.println("ERROR: Could not parse opnd_2!");
                printUsage();
                return false;
            }
        } else if (operation.equals("RESTORE")) {
            if (args.length != 2) {
                System.err.println("ERROR: RESTORE takes 1 operand!");
                printUsage();
                return false;
            }
        } else if (operation.contains("DELETE")) {
            if (args.length != 2) {
                System.err.println("ERROR: DELETE takes 1 operand!");
                printUsage();
                return false;
            }
        } else {
            System.err.println("ERROR: Invalid operation!");
            printUsage();
            return false;
        }

        return true;
    }

    public static void main(String[] args) throws IOException {
        if (!validateArguments(args))
            return;

        try {
            SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            SSLSocket socket = (SSLSocket) factory.createSocket("127.0.0.1", 8081);

            socket.startHandshake();

            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())));
            String ip = InetAddress.getLocalHost().getHostAddress();

            String request = ip + " " + String.join(" ", args);
            out.println(request);
            out.println();
            out.flush();

            if (out.checkError())
                System.out.println("SSLSocketClient:  java.io.PrintWriter error");

            /* read response */
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String inputLine;
            while ((inputLine = in.readLine()) != null)
                System.out.println(inputLine);

            in.close();
            out.close();
            socket.close();

        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
    }
}