import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.MessageDigest;

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
			return new BigInteger(encoded);
		} catch (Exception e) {
			e.printStackTrace();
			return new BigInteger("0");
		}
	}

	ExternalNode(String ip, int port) {
		this.id = getId(ip, port);
		this.ip = ip;
		this.port = port;
	}

	public ExternalNode findSuccessor(BigInteger id) {
		try {
			// SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
			// SSLSocket socket = (SSLSocket) factory.createSocket(ip, port);

			Socket socket = new Socket(ip, port);

			DataOutputStream out = new DataOutputStream(socket.getOutputStream());
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			String message = "FINDSUCCESSOR " + this.id + " " + id + " \n";
			System.out.print("[Node " + this.id + "] " + message);

			out.writeBytes(message);

			String response = in.readLine().trim();

			socket.close();

			System.out.print("[Node " + this.id + "] " + response);

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

	public ExternalNode getPredecessor() {
		try {
			// SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
			String response;
			// SSLSocket socket = (SSLSocket) factory.createSocket(ip, port);
			Socket socket = new Socket(ip, port);

			DataOutputStream out = new DataOutputStream(socket.getOutputStream());
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			String message = "GETPREDECESSOR " + this.id + " \n";
			System.out.print("[Node " + this.id + "] " + message);

			out.writeBytes(message);

			response = in.readLine().trim();

			socket.close();
			System.out.print("[Node " + this.id + "] " + response);

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

	public void notify(ExternalNode other) {
		try {
			// SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
			// SSLSocket socket = (SSLSocket) factory.createSocket(ip, port);
			Socket socket = new Socket(ip, port);

			DataOutputStream out = new DataOutputStream(socket.getOutputStream());

			String message = "NOTIFY " + this.id + " " + other.ip + " " + other.port + " \n";
			System.out.print("[Node " + this.id + "] " + message);

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

	public boolean failed() {
		try {
			// SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
			// SSLSocket socket = (SSLSocket) factory.createSocket(ip, port);
			Socket socket = new Socket(ip, port);

			DataOutputStream out = new DataOutputStream(socket.getOutputStream());
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			String message = "HI " + this.id + " \n";
			System.out.print("[Node " + this.id + "] " + message);

			out.writeBytes(message);

			in.readLine();

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

	public void getKeys() {
		try {
			// SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
			// SSLSocket socket = (SSLSocket) factory.createSocket(ip, port);

			Socket socket = new Socket(ip, port);

			DataOutputStream out = new DataOutputStream(socket.getOutputStream());

			String message = "GETKEYS " + this.id + " \n";
			System.out.print("[Node " + this.id + "] " + message);

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