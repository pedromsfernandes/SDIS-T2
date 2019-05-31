import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import javax.net.ssl.SSLSocket;

class ClientRequestHandlerThread implements Runnable {

  private Node node;
  private SSLSocket s;

  /**
   * 
   * @param node
   * @param s
   */
  public ClientRequestHandlerThread(Node node, SSLSocket s) {
    this.node = node;
    this.s = s;
  }

  @Override
  public void run() {
    try {
      DataOutputStream out = new DataOutputStream(s.getOutputStream());
      DataInputStream in = new DataInputStream(s.getInputStream());

      String request = in.readUTF();
      String response = "";

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
      case "RECLAIM":
        response = node.reclaim(out, in);
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
      e.printStackTrace();
    }
  }
}