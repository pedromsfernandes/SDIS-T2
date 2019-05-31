import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

class TestApp {

  public static void printUsage() {
    System.err.println(
        "Usage: java TestApp <node_ip> <node_port> <sub_protocol> <opnd_1> <opnd_2>");
    System.err.println(
        "<node_ip>\nThe IP address of the node that will receive the request.");
    System.err.println(
        "<node_port>\nThe port of the node that will receive the request.");
    System.err.println(
        "<sub_protocol>\nIs the operation the peer of the backup service must execute. It can be the triggering of the subprotocol to test and it must be one of: BACKUP, RESTORE, DELETE, RECLAIM.");
    System.err.println(
        "<opnd_1>\nIs either the path name of the file to backup/restore/delete, for the respective first 3 subprotocols, or the amount of KB of data the receiver node should have after a RECLAIM request.");
    System.err.println(
        "<opnd_2>\nThis operand is an integer that specifies the desired replication degree and applies only to the backup protocol.");
  }

  /**
   * 
   * @param args
   * @return
   */
  public static boolean validateArguments(String[] args) {
    if (args.length < 4 || args.length > 5) {
      System.err.println("ERROR: Invalid number of arguments!");
      printUsage();
      return false;
    }

    String operation = args[2];

    switch (operation.toUpperCase()) {
    case "BACKUP":
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
      break;
    case "RESTORE":
      if (args.length != 4) {
        System.err.println("ERROR: RESTORE takes 1 operand!");
        printUsage();
        return false;
      }
      break;
    case "DELETE":
      if (args.length != 4) {
        System.err.println("ERROR: DELETE takes 1 operand!");
        printUsage();
        return false;
      }
      break;
    case "RECLAIM":
      if (args.length != 4) {
        System.err.println("ERROR: RECLAIM takes 1 operand!");
        printUsage();
        return false;
      }
      try {
        Integer.parseInt(args[3]);
      } catch (Exception e) {
        System.err.println("ERROR: Could not parse opnd_1!");
        printUsage();
        return false;
      }
      break;
    default:
      System.err.println("ERROR: Invalid operation!");
      printUsage();
      return false;
    }

    return true;
  }

  /**
   * 
   * @param fileName
   * @param chunkNo
   * @param replicationDegree
   * @param chunkContent
   * @return
   */
  public static byte[] buildBackupMessage(String fileName, int chunkNo,
                                          int replicationDegree,
                                          byte[] chunkContent) {

    String header = "BACKUP " + fileName + " " + chunkNo + " " +
                    replicationDegree + " \r\n\r\n";
    byte[] header_b = header.getBytes(StandardCharsets.US_ASCII);

    return Utils.concatenateArrays(header_b, chunkContent);
  }

  /**
   * 
   * @param spaceToReclaim
   * @param out
   * @param in
   * @throws IOException
   */
  private static void reclaim(int spaceToReclaim, DataOutputStream out,
                              DataInputStream in) throws IOException {
    out.writeUTF("RECLAIM");
    out.writeInt(spaceToReclaim);
  }

  /**
   * 
   * @param fileName
   * @param replicationDegree
   * @param out
   * @param in
   * @throws IOException
   */
  private static void backup(String fileName, int replicationDegree,
                             DataOutputStream out, DataInputStream in)
      throws IOException {

    ArrayList<byte[]> chunks = Utils.splitFile(fileName);

    out.writeUTF("BACKUP");
    out.writeInt(chunks.size());

    for (int i = 0; i < chunks.size(); i++) {
      byte[] message =
          buildBackupMessage(fileName, i, replicationDegree, chunks.get(i));
      out.writeInt(message.length);
      out.write(message);
      out.flush();
    }
  }

  /**
   * 
   * @param chunks
   * @param fileName
   */
  private static void restoreFile(ArrayList<byte[]> chunks, String fileName) {
    int chunkSize = 64 * 1000; // 64KByte

    FileOutputStream out = null;
    try {
      out = new FileOutputStream(fileName);
    } catch (FileNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    FileChannel fout = out.getChannel();
    ByteBuffer byteBuffer = ByteBuffer.allocate(chunkSize);

    try {

      for (byte[] chunk : chunks) {
        byteBuffer.clear();
        byteBuffer.put(chunk);
        byteBuffer.flip();
        fout.write(byteBuffer);
      }

      fout.close();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  /**
   * 
   * @param fileName
   * @param out
   * @param in
   * @throws IOException
   */
  private static void restore(String fileName, DataOutputStream out,
                              DataInputStream in) throws IOException {
    out.writeUTF("RESTORE");
    out.writeUTF(fileName);

    ArrayList<byte[]> chunks = new ArrayList<byte[]>();

    int numChunks = in.readInt();

    for (int i = 0; i < numChunks; i++) {
      int length = in.readInt(); // read length of incoming message
      if (length > 0) {
        byte[] chunk = new byte[length];
        in.readFully(chunk, 0, chunk.length); // read the message
        chunks.add(chunk);
      }
    }

    restoreFile(chunks, fileName);
  }

  /**
   * 
   * @param fileName
   * @param out
   * @param in
   * @throws IOException
   */
  private static void delete(String fileName, DataOutputStream out,
                             DataInputStream in)throws IOException {
    out.writeUTF("DELETE");
    out.writeUTF(fileName);
  }

  /**
   * 
   * @param args
   * @throws IOException
   */
  public static void main(String[] args) throws IOException {
    if (!validateArguments(args))
      return;

    String operation = args[2];
    String fileName = args[3];

    SSLSocketFactory factory = (SSLSocketFactory)SSLSocketFactory.getDefault();
    SSLSocket socket =
        (SSLSocket)factory.createSocket(args[0], Integer.parseInt(args[1]));

    socket.startHandshake();

    DataOutputStream out = new DataOutputStream(socket.getOutputStream());
    DataInputStream in = new DataInputStream(socket.getInputStream());

    switch (operation.toUpperCase()) {
    case "BACKUP":
      backup(fileName, Integer.parseInt(args[4]), out, in);
      break;
    case "RESTORE":
      restore(fileName, out, in);
      break;
    case "DELETE":
      delete(fileName, out, in);
      break;
    case "RECLAIM":
      reclaim(Integer.parseInt(args[3]), out, in);
      break;
    default:
      break;
    }

    System.out.println(in.readUTF());

    in.close();
    out.close();
    socket.close();
  }
}