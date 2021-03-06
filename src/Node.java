import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Node extends ExternalNode {
  ScheduledExecutorService executor;
  ExternalNode predecessor;
  ExternalNode successor;
  ExternalNode[] fingerTable;
  ConcurrentHashMap<BigInteger, String> keys;
  Storage storage;

  /**
   * 
   * @param ip
   * @param port
   * @throws UnknownHostException
   */
  Node(String ip, int port) throws UnknownHostException {
    super(ip, port);

    loadKeys();

    keys = new ConcurrentHashMap<>();
    storage = new Storage(id.toString());

    this.predecessor = null;
    this.successor = this;

    fingerTable = new ExternalNode[160];
    for (int i = 0; i < fingerTable.length; i++)
      fingerTable[i] = this;

    executor = Executors.newScheduledThreadPool(100);
    executor.execute(new NodeThread(this));
    executor.scheduleAtFixedRate(new StabilizeThread(this), 15, 15, TimeUnit.SECONDS);
  }

  public void loadKeys() {
    File file = new File(this.id + ".ser");

    if (!file.exists())
      return;

    try {
      FileInputStream f = new FileInputStream(file);
      ObjectInputStream o = new ObjectInputStream(f);

      this.keys = (ConcurrentHashMap<BigInteger, String>) o.readObject();

      o.close();
      f.close();
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public void saveKeys() {
    try {
      FileOutputStream f = new FileOutputStream(new File(this.id + ".ser"));
      ObjectOutputStream o = new ObjectOutputStream(f);

      o.writeObject(keys);

      o.close();
      f.close();
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  /**
   * 
   * @param ringNode
   * @throws UnknownHostException
   * @throws IOException
   */
  public void join(ExternalNode ringNode) throws UnknownHostException, IOException {
    this.successor = ringNode.findSuccessor(this.id, this.id);
    this.predecessor = this.successor;

    for (int i = 0; i < fingerTable.length; i++) {
      if (idBetween(calculateFinger(i), this.id, this.successor.id))
        fingerTable[i] = this.successor;

      else
        fingerTable[i] = this;
    }
  }

  /**
   * 
   * @param id
   * @param lhs
   * @param rhs
   * @return
   */
  public static boolean idBetween(BigInteger id, BigInteger lhs, BigInteger rhs) {
    if (lhs.compareTo(rhs) > 0)
      return (id.compareTo(lhs) >= 0) || (id.compareTo(rhs) <= 0);

    return (id.compareTo(lhs) >= 0) && (id.compareTo(rhs) <= 0);
  }

  public ExternalNode findSuccessor(BigInteger requestId, BigInteger id) {
    if (id.equals(this.id))
      return this;

    if (idBetween(id, this.id, this.successor.id))
      return this.successor;

    ExternalNode n0 = closestPrecedingNode(id);

    if (n0.id.equals(this.id))
      return this;

    return n0.findSuccessor(this.id, id);
  }

  /**
   * 
   * @param id
   * @return
   */
  public ExternalNode closestPrecedingNode(BigInteger id) {
    for (int i = fingerTable.length - 1; i >= 0; i--)
      if (fingerTable[i] != null && idBetween(fingerTable[i].id, this.id, id))
        return fingerTable[i];

    return this;
  }

  
  public ExternalNode getPredecessor(BigInteger requestId) {
    return this.predecessor;
  }

  /**
   * 
   * @param otherId
   * @return
   */
  public HashMap<BigInteger, String> computeKeys(BigInteger otherId) {
    HashMap<BigInteger, String> keysToGive = new HashMap<>();

    Enumeration<BigInteger> mapKeys = keys.keys();

    while (mapKeys.hasMoreElements()) {
      BigInteger i = mapKeys.nextElement();

      if (idBetween(i, this.id, otherId)) {
        keysToGive.put(i, keys.get(i));
        keys.remove(i);
        saveKeys();
      }
    }

    return keysToGive;
  }

  
  public void notify(BigInteger requestId, ExternalNode other) {
    if (this.predecessor == null || !this.predecessor.id.equals(this.id)
        || idBetween(other.id, this.predecessor.id, this.id)) {
      if (other.id.equals(this.id))
        return;

      if (this.predecessor == null || !other.id.equals(this.predecessor.id)) {
        this.predecessor = other;

        this.predecessor.giveKeys(this, computeKeys(other.id));

        if (this.successor.id.equals(this.id)) {
          this.successor = other;
          fingerTable[0] = other;
        }
      }
    }
  }

  
  public void stabilize() {
    ExternalNode x = this.successor.getPredecessor(this.id);

    if (x != null && !this.id.equals(x.id)
        && (this.id.equals(this.successor.id) || idBetween(x.id, this.id, successor.id))) {
      this.successor = x;
      fingerTable[0] = x;
    }

    this.successor.notify(this.id, this);
  }

  /**
   * 
   * @param i
   * @return
   */
  private BigInteger calculateFinger(int i) {
    return ((this.id.add(new BigInteger("2").pow(i))).mod(new BigInteger("2").pow(fingerTable.length)));
  }

  public void fixFingers() {
    for (int i = 1; i < fingerTable.length; i++) {
      BigInteger fingerID = calculateFinger(i);
      fingerTable[i] = findSuccessor(this.id, fingerID);
    }
  }

  public boolean failed(BigInteger requestId) {
    return false;
  }


  public void checkPredecessor() {
    if (this.predecessor != null && this.predecessor.failed(this.id))
      this.predecessor = null;
  }

  public void storeKey(BigInteger requestId, BigInteger encrypted, String value) {
    keys.put(encrypted, value);
    saveKeys();
  }

  /** */
  public String getKey(BigInteger requestId, BigInteger encrypted) {
    return keys.get(encrypted);
  }

  /** */
  public void deleteKey(BigInteger requestId, BigInteger encrypted) {
    keys.remove(encrypted);
    saveKeys();
  }

  /** */
  public void deleteChunk(BigInteger requestId, BigInteger encrypted) {
    keys.remove(encrypted);
    saveKeys();
    storage.delete(encrypted);
  }

  /**
   * 
   * @param out
   * @param in
   * @return
   * @throws IOException
   */
  public String backup(DataOutputStream out, DataInputStream in) throws IOException {
    int chunks = in.readInt();

    for (int i = 0; i < chunks; i++) {
      int length = in.readInt();
      byte[] message = new byte[length];
      in.readFully(message, 0, message.length); // read the message

      String[] header = Utils.getHeader(message);
      byte[] content = Utils.getChunkContent(message, length);

      if (i == 0) {
        String key = header[1];
        BigInteger encrypted = Utils.getSHA1(key);

        ExternalNode successor = this.findSuccessor(this.id, encrypted);

        if (successor.id.equals(this.id)) {
          this.storeKey(this.id, encrypted, chunks + ":" + header[3]);
        } else {
          successor.storeKey(this.id, encrypted, chunks + ":" + header[3]);
        }
      }

      for (int j = 0; j < Integer.parseInt(header[3]); j++) {
        String key = header[1] + "-" + header[2] + "-" + j;
        BigInteger encrypted = Utils.getSHA1(key);

        ExternalNode successor = this.findSuccessor(this.id, encrypted);

        if (successor.id.equals(this.id)) {
          storeChunk(encrypted, key, content);
        } else {
          executor.execute(new ChunkSenderThread(this, successor, encrypted, key, content, false));
        }
      }
    }

    return "BACKED UP";
  }

  /**
   * 
   * @param out
   * @param in
   * @return
   * @throws IOException
   */
  public String restore(DataOutputStream out, DataInputStream in) throws IOException {
    String fileName = in.readUTF();
    BigInteger encrypted = Utils.getSHA1(fileName);
    ExternalNode successor = this.findSuccessor(this.id, encrypted);

    String value;

    if (successor.id.equals(this.id)) {
      value = this.getKey(this.id, encrypted);
    } else {
      value = successor.getKey(this.id, encrypted);
    }

    String[] args = value.split(":", 2);
    int numChunks = Integer.parseInt(args[0]);

    ArrayList<String> keys = new ArrayList<String>();
    storage.addFileToRestore(fileName, numChunks);

    for (int i = 0; i < numChunks; i++) {
      String key = fileName + "-" + i + "-0";
      BigInteger chunkID = Utils.getSHA1(key);
      keys.add(chunkID.toString());
      successor = this.findSuccessor(this.id, chunkID);

      if (successor.id.equals(this.id)) {
        storage.addRestoredChunk(chunkID.toString(), fileName, storage.readChunk(chunkID));
      } else {
        executor.execute(new ChunkRequestThread(this, successor, chunkID, fileName));
        executor.schedule(new CheckChunkReceiveThread(this, successor, key, keys, fileName, Integer.parseInt(args[1])),
            5, TimeUnit.SECONDS);
      }
    }

    while (storage.getFileCount(fileName) != 0) {
      try {
        Thread.sleep(500);
      } catch (InterruptedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }

    ArrayList<byte[]> chunks = storage.getChunks(keys);
    storage.freeRestoredChunks(keys, fileName);
    out.writeInt(numChunks);
    for (byte[] chunk : chunks) {
      out.writeInt(chunk.length);
      out.write(chunk);
    }

    return "RESTORED";
  }

  /**
   * 
   * @param out
   * @param in
   * @return
   * @throws IOException
   */
  public String delete(DataOutputStream out, DataInputStream in) throws IOException {
    String fileName = in.readUTF();
    BigInteger encrypted = Utils.getSHA1(fileName);

    ExternalNode successor = this.findSuccessor(this.id, encrypted);

    String value;

    if (successor.id.equals(this.id)) {
      value = this.getKey(this.id, encrypted);
    } else {
      value = successor.getKey(this.id, encrypted);
    }

    String[] args = value.split(":", 2);
    int chunks = Integer.parseInt(args[0]);
    int repDegree = Integer.parseInt(args[1]);

    for (int i = 0; i < chunks; i++) {
      for (int j = 0; j < repDegree; j++) {
        String key = fileName + "-" + i + "-" + j;
        BigInteger chunkID = Utils.getSHA1(key);

        ExternalNode chunkSuccessor = this.findSuccessor(this.id, chunkID);

        if (chunkSuccessor.id.equals(this.id)) {
          deleteChunk(this.id, chunkID);
        } else {
          chunkSuccessor.deleteChunk(this.id, chunkID);
        }
      }
    }

    if (successor.id.equals(this.id)) {
      this.deleteKey(this.id, encrypted);
    } else {
      successor.deleteKey(this.id, encrypted);
    }

    return "DELETED";
  }

  /** */
  public static void usage() {
    System.err.println("USAGE: java Node <ip> <port> [ <ring_node_ip> <ring_node_port> ]");
    System.err.println("\nExamples\nRing starter node:\njava Node 10.227.161.150 8000");
    System.err.println("\nConnect to ring:\njava Node 10.227.161.149 8000 10.227.161.150 8000");
  }

  public static void main(String[] args) throws IOException {

    if (args.length != 2 && args.length != 4) {
      usage();
      return;
    }

    Node node = new Node(args[0], Integer.parseInt(args[1]));
    node.executor.execute(new ClientRequestListenerThread(node));

    if (args.length == 4)
      node.join(new ExternalNode(args[2], Integer.parseInt(args[3])));
  }

  /**
   * 
   * @return
   */
  public Storage getStorage() {
    return storage;
  }

  /**
   * 
   * @param storage
   */
  public void setStorage(Storage storage) {
    this.storage = storage;
  }

  /**
   * 
   * @param key
   * @param value
   * @param chunk
   */
  public void storeChunk(BigInteger key, String value, byte[] chunk) {
    this.keys.put(key, value);
    saveKeys();
    this.storage.storeChunk(key, chunk);
  }

  /**
   * 
   * @param out
   * @param in
   * @return
   * @throws IOException
   */
  public String reclaim(DataOutputStream out, DataInputStream in) throws IOException {
    int spaceToReclaim = (1000 * getUsedSpace()) - in.readInt();

    File chunks = new File("chunks-" + id);

    for (File file : chunks.listFiles()) {
      String fileName = file.getName();
      BigInteger key = new BigInteger(fileName.split("\\.")[0]);
      long fileSize = file.length();

      if (spaceToReclaim <= 0)
        break;

      // Falta ver a cena do desiredRepDegree vs actualRepDegree

      deleteChunk(this.id, key);
      spaceToReclaim -= fileSize;
    }

    return "RECLAIMED";
  }

  /**
   * 
   * @return
   */
  int getUsedSpace() {
    File chunks = new File("chunks-" + id);
    int size = 0;

    for (File file : chunks.listFiles()) {
      size += file.length();
    }

    return size;
  }
}