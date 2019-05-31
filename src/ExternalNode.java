import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Iterator;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class ExternalNode {
	BigInteger id;
	String ip;
	int port;

	/**
	 * 
	 * @param ip
	 * @param port
	 * @return
	 */
	private static BigInteger getId(String ip, int port) {
		String info = ip + ":" + port;
		return Utils.getSHA1(info);
	}

	/**
	 * 
	 * @param ip
	 * @param port
	 */
	ExternalNode(String ip, int port) {
		this.id = getId(ip, port);
		this.ip = ip;
		this.port = port;
	}

	/**
	 * 
	 * @param requestId
	 * @param id
	 * @return
	 */
	public ExternalNode findSuccessor(BigInteger requestId, BigInteger id) {
		try {
			SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
			SSLSocket socket = (SSLSocket) factory.createSocket(ip, port);

			DataOutputStream out = new DataOutputStream(socket.getOutputStream());
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			String message = "FINDSUCCESSOR " + requestId + " " + id + " \n";
			System.out.println("[Node " + requestId + "] " + message);
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

	/**
	 * 
	 * @param requestId
	 * @return
	 */
	public ExternalNode getPredecessor(BigInteger requestId) {
		try {
			SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
			String response;
			SSLSocket socket = (SSLSocket) factory.createSocket(ip, port);

			DataOutputStream out = new DataOutputStream(socket.getOutputStream());
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			String message = "GETPREDECESSOR " + requestId + " \n";
			System.out.println("[Node " + requestId + "] " + message);
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

	/**
	 * 
	 * @param requestId
	 * @param other
	 */
	public void notify(BigInteger requestId, ExternalNode other) {
		try {
			SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
			SSLSocket socket = (SSLSocket) factory.createSocket(ip, port);

			DataOutputStream out = new DataOutputStream(socket.getOutputStream());
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			String message = "NOTIFY " + requestId + " " + other.ip + " " + other.port + " \n";
			System.out.println("[Node " + requestId + "] " + message);
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

	/**
	 * 
	 * @param requestId
	 * @return
	 */
	public boolean failed(BigInteger requestId) {
		try {
			SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
			SSLSocket socket = (SSLSocket) factory.createSocket(ip, port);
			socket.setSoTimeout(1000);

			DataOutputStream out = new DataOutputStream(socket.getOutputStream());
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			String message = "HI " + requestId + " \n";
			System.out.println("[Node " + requestId + "] " + message);
			out.writeBytes(message);
			System.out.println("[Node " + requestId + "] " + in.readLine());
			socket.close();

			return false;
		} catch (SocketTimeoutException e) {
			return true;
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return true;
	}

	/**
	 * 
	 * @param node
	 * @param keys
	 */
	public void giveKeys(Node node, HashMap<BigInteger, String> keys) {
		if (keys.size() == 0)
			return;

		Iterator<BigInteger> it = keys.keySet().iterator();

		while (it.hasNext()) {
			BigInteger i = it.next();

			if(keys.get(i).contains("-")) {
				byte[] content = node.storage.readChunk(i);
				node.executor.execute(new ChunkSenderThread(node, this, i, keys.get(i), content, true));
			} else {
				class SendKey implements Runnable {
					Node sender;
					ExternalNode receiver;
					BigInteger key;
					String value;

					SendKey(Node sender, ExternalNode receiver, BigInteger key, String value) {
						this.sender = sender;
						this.receiver = receiver;
						this.key = key;
						this.value = value;
					}

					public void run(){
						receiver.storeKey(sender.id, key, value);
						sender.deleteKey(sender.id, key);
					}
					
				}
				node.executor.execute(new SendKey(node,this,i,keys.get(i)));
			}
		}
	}

	/**
	 * 
	 * @param requestId
	 * @param encrypted
	 * @param value
	 */
	public void storeKey(BigInteger requestId, BigInteger encrypted, String value) {
		try {
			SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
			SSLSocket socket = (SSLSocket) factory.createSocket(ip, port);

			DataOutputStream out = new DataOutputStream(socket.getOutputStream());
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			String message = "STOREKEY " + requestId + " " + encrypted + " " + value + " \n";
			System.out.println("[Node " + requestId + "] " + message);
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
	
	/**
	 * 
	 * @param requestId
	 * @param id
	 * @return
	 */
	public String getKey(BigInteger requestId, BigInteger id) {
		try {
			SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
			SSLSocket socket = (SSLSocket) factory.createSocket(ip, port);

			DataOutputStream out = new DataOutputStream(socket.getOutputStream());
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			String message = "GETKEY " + requestId + " " + id + " \n";
			System.out.println("[Node " + requestId + "] " + message);
			out.writeBytes(message);

			String response = in.readLine().trim();

			socket.close();

			System.out.println("[Node " + requestId + "] " + response);

			String[] args = response.split(" ");

			return args[2];
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * 
	 * @param requestId
	 * @param key
	 */
	public void deleteKey(BigInteger requestId, BigInteger key) {
		try {
			SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
			SSLSocket socket = (SSLSocket) factory.createSocket(ip, port);

			DataOutputStream out = new DataOutputStream(socket.getOutputStream());
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			String message = "DELETEKEY " + requestId + " " + key + " \n";
			System.out.println("[Node " + requestId + "] " + message);
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

	/**
	 * 
	 * @param requestId
	 * @param key
	 */
	public void deleteChunk(BigInteger requestId, BigInteger key) {
		try {
			SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
			SSLSocket socket = (SSLSocket) factory.createSocket(ip, port);

			DataOutputStream out = new DataOutputStream(socket.getOutputStream());
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			String message = "DELETECHUNK " + requestId + " " + key + " \n";
			System.out.println("[Node " + requestId + "] " + message);
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
}