import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.MessageDigest;

public class ExternalNode {
	String id;
	String ip;
	int port;

	private static String getId(String ip, int port) {
		String info = ip + ":" + port;
		try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] encoded = digest.digest(info.getBytes());
            return new String(Utils.hexString(encoded));
        } catch (Exception e) {
            return info;
        }
	}

	ExternalNode(String ip, int port) {
		this.id = getId(ip, port);
		this.ip = ip;
		this.port = port;
	}

	public ExternalNode findSuccessor(String id) throws UnknownHostException, IOException {
		Socket socket = new Socket(ip, port);
		DataOutputStream out = new DataOutputStream(socket.getOutputStream());
		BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		
		String message = "FINDSUCCESSOR " + this.id + " " + id + " \n";
		System.out.print("[Node " + this.id + "] "+ message);

		out.writeBytes(message);
		
		String response = in.readLine().trim();

		socket.close();

		System.out.print("[Node " + this.id + "] "+ response);

		String[] args = response.split(" ");

		if(args[2].equals("NOTFOUND"))
			return null;
		
		return new ExternalNode(args[2], Integer.parseInt(args[3]));
	}
}