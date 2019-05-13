import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

class TestApp {

    public static void printUsage() {
        System.err.println("Usage: java TestApp <node_ip> <node_port> <sub_protocol> <opnd_1> <opnd_2>");
        System.err.println("The IP address of the node that will receive the request.");
        System.err.println(
                "<sub_protocol>\nIs the operation the peer of the backup service must execute. It can be the triggering of the subprotocol to test and it must be one of: BACKUP, RESTORE, DELETE.");
        System.err.println(
                "<opnd_1>\nIs either the path name of the file to backup/restore/delete, for the respective 3 subprotocols.");
        System.err.println(
                "<opnd_2>\nThis operand is an integer that specifies the desired replication degree and applies only to the backup protocol.");
    }

    public static boolean validateArguments(String[] args) {
        if (args.length < 4 || args.length > 5) {
            System.err.println("ERROR: Invalid number of arguments!");
            printUsage();
            return false;
        }

        String operation = args[2];

        if (operation.equals("BACKUP")) {
            if (args.length != 5) {
                System.err.println("ERROR: BACKUP takes 2 operands!");
                printUsage();
                return false;
            }
            try {
                Integer.parseInt(args[4]);
            } catch (Exception e) {
                System.err.println("ERROR: Could not parse opnd_2!");
                printUsage();
                return false;
            }
        } else if (operation.equals("RESTORE")) {
            if (args.length != 4) {
                System.err.println("ERROR: RESTORE takes 1 operand!");
                printUsage();
                return false;
            }
        } else if (operation.contains("DELETE")) {
            if (args.length != 4) {
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

    private static byte[] encryptFileId(String fileName) {

        String dateModified = "", owner = "";

        try {
            Path file = Paths.get(fileName);
            BasicFileAttributes attr = Files.readAttributes(file, BasicFileAttributes.class);

            dateModified = attr.lastModifiedTime().toString();
            owner = Files.getOwner(file).getName();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return Utils.getSHA(fileName + "-" + dateModified + "-" + owner);
    }

    private static ArrayList<byte[]> splitFile(String fileName) {

        int chunkSize = 64 * 1000; // 64KByte
        ArrayList<byte[]> chunks = new ArrayList<byte[]>();

        FileInputStream fin = null;
        try {
            fin = new FileInputStream(fileName);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        FileChannel fc = fin.getChannel();
        ByteBuffer byteBuffer = ByteBuffer.allocate(chunkSize);
        int bytesAmount = 0;

        try {
            while ((bytesAmount = fc.read(byteBuffer)) > 0) {
                byte[] smaller = new byte[bytesAmount];

                byteBuffer.flip();
                byteBuffer.get(smaller);
                byteBuffer.clear();

                chunks.add(smaller);
            }

            fin.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return chunks;
    }

    public static byte[] buildBackupMessage(byte[] fileId, int chunkNo, int replicationDegree, byte[] chunkContent) {

        String file = Utils.bytesToHex(fileId);

        String header = "BACKUP " + file + " " + chunkNo + " " + replicationDegree + " \r\n\r\n";
        byte[] header_b = header.getBytes(StandardCharsets.US_ASCII);

        return Utils.concatenateArrays(header_b, chunkContent);
    }

    private static void backup(String fileName, int replicationDegree, DataOutputStream out, DataInputStream in)
            throws IOException {

        byte[] fileId = encryptFileId(fileName);
        ArrayList<byte[]> chunks = splitFile(fileName);

        out.writeUTF("BACKUP");
        out.writeInt(chunks.size());

        for (int i = 0; i < chunks.size(); i++) {
            byte[] message = buildBackupMessage(fileId, i, replicationDegree, chunks.get(i));
            out.writeInt(message.length);
            out.write(message);
            out.flush();
        }

        System.out.println(in.readUTF());
    }

    private static void restore(String fileName, DataOutputStream out, DataInputStream in) {

    }

    private static void delete(String fileName, DataOutputStream out, DataInputStream in) {

    }

    public static void main(String[] args) throws IOException {
        if (!validateArguments(args))
            return;

        String operation = args[2];
        String fileName = args[3];

        SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        SSLSocket socket = (SSLSocket) factory.createSocket(args[0], Integer.parseInt(args[1]));

        socket.startHandshake();

        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
        DataInputStream in = new DataInputStream(socket.getInputStream());

        switch (operation) {
        case "BACKUP":
            backup(fileName, Integer.parseInt(args[4]), out, in);
            break;
        case "RESTORE":
            restore(fileName, out, in);
            break;
        case "DELETE":
            delete(fileName, out, in);
            break;
        default:
            break;
        }

        in.close();
        out.close();
        socket.close();
    }
}