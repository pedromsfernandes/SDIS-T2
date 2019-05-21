import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Iterator;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class ExternalNode {
	BigInteger id;
	String ip;
	int port;

	private static BigInteger getId(String ip, int port) {
		String info = ip + ":" + port;
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-1");
			byte[] encoded = digest.digest(info.getBytes());
			return new BigInteger(1,encoded);
		} catch (Exception e) {
			e.printStackTrace();
			return new BigInteger(1,"0".getBytes());
		}
	}

	ExternalNode(String ip, int port) {
		this.id = getId(ip, port);
		this.ip = ip;
		this.port = port;
	}

	public ExternalNode findSuccessor(BigInteger requestId, BigInteger id) {
		try {
			SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
			SSLSocket socket = (SSLSocket) factory.createSocket(ip, port);

			DataOutputStream out = new DataOutputStream(socket.getOutputStream());
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			String message = "FINDSUCCESSOR " + requestId + " " + id + " \n";
			out.writeBytes(message);

			String response = in.readLine().trim();

			socket.close();

			System.out.println("[Node " + requestId + "] " + response);

			String[] args = response.split(" ");

			if (args.length < 3 || args[2].equals("NOTFOUND"))
				return null;

			return new ExternalNode(args[2], Integer.parseInt(args[3]));
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	public ExternalNode getPredecessor(BigInteger requestId) {
		try {
			SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
			String response;
			SSLSocket socket = (SSLSocket) factory.createSocket(ip, port);

			DataOutputStream out = new DataOutputStream(socket.getOutputStream());
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			String message = "GETPREDECESSOR " + requestId + " \n";

			out.writeBytes(message);

			response = in.readLine().trim();

			socket.close();
			System.out.println("[Node " + requestId + "] " + response);

			String[] args = response.split(" ");

			if (args[2].equals("NOTFOUND"))
				return null;

			return new ExternalNode(args[2], Integer.parseInt(args[3]));
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	public void notify(BigInteger requestId, ExternalNode other) {
		try {
			SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
			SSLSocket socket = (SSLSocket) factory.createSocket(ip, port);

			DataOutputStream out = new DataOutputStream(socket.getOutputStream());
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			String message = "NOTIFY " + requestId + " " + other.ip + " " + other.port + " \n";

			out.writeBytes(message);

			in.readLine();
			socket.close();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public boolean failed(BigInteger requestId) {
		try {
			SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
			SSLSocket socket = (SSLSocket) factory.createSocket(ip, port);

			DataOutputStream out = new DataOutputStream(socket.getOutputStream());
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			String message = "HI " + requestId + " \n";

			out.writeBytes(message);
			System.out.println("[Node " + requestId + "] " + in.readLine());
			socket.close();

			return false;
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return true;
	}

	public void giveKeys(BigInteger requestId, HashMap<BigInteger,String> keys) {
		if(keys.size() == 0)
			return;

		try {
			SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
			SSLSocket socket = (SSLSocket) factory.createSocket(ip, port);

			DataOutputStream out = new DataOutputStream(socket.getOutputStream());

			String message = "GIVEKEYS " + requestId + " " + keys.size() + " \n";

			Iterator<BigInteger> it = keys.keySet().iterator();

			while(it.hasNext()) {
				BigInteger i = it.next();
				message += i + " " + keys.get(i) + "\n";
			}

			out.writeBytes(message);

			socket.close();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}