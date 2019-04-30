import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class Chunk implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8102200653806457589L;
	private byte[] fileId;
	private int chunkNo;
	private byte[] buffer;
	private int bufferSize;
	private int desiredReplicationDegree;
	private int perceivedReplicationDegree;

	public Chunk(byte[] fileId, int chunkNo, byte[] buffer, int bufferSize, int desiredReplicationDegree) {
		this.setFileId(fileId);
		this.setChunkNo(chunkNo);
		this.setBuffer(buffer);
		this.setBufferSize(bufferSize);
		this.setDesiredReplicationDegree(desiredReplicationDegree);
		this.perceivedReplicationDegree = 1;
	}

	/**
	 * @return the desiredReplicationDegree
	 */
	public int getDesiredReplicationDegree() {
		return desiredReplicationDegree;
	}

	/**
	 * @param desiredReplicationDegree the desiredReplicationDegree to set
	 */
	public void setDesiredReplicationDegree(int desiredReplicationDegree) {
		this.desiredReplicationDegree = desiredReplicationDegree;
	}

	public int getBufferSize() {
		return bufferSize;
	}

	public void setBufferSize(int bufferSize) {
		this.bufferSize = bufferSize;
	}

	public byte[] getBuffer() {
		return buffer;
	}

	public void setBuffer(byte[] buffer) {
		this.buffer = buffer;
	}

	public int getChunkNo() {
		return chunkNo;
	}

	public void setChunkNo(int chunkNo) {
		this.chunkNo = chunkNo;
	}

	public byte[] getFileId() {
		return fileId;
	}

	public void setFileId(byte[] fileId) {
		this.fileId = fileId;
	}

	public String toString() {
		return "Id: " + chunkNo + "\nSize (KBytes): " + bufferSize/1000;
	}

	public void serialize(String path) {

		String fullPath = path + Utils.getCharSeparator() + "chk" + chunkNo + ".ser";
		try {
			FileOutputStream fileOut = new FileOutputStream(fullPath);
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(this);
			out.close();
			fileOut.close();
			System.out.println("Serialized data is saved in " + fullPath);
		} catch (IOException i) {
			i.printStackTrace();
		}
	}

	public static Chunk deserialize(String path) {
		Chunk chunk = null;
		try {
			FileInputStream fileIn = new FileInputStream(path);
			ObjectInputStream in = new ObjectInputStream(fileIn);
			chunk = (Chunk) in.readObject();
			in.close();
			fileIn.close();
		} catch (IOException i) {
			i.printStackTrace();
			return null;
		} catch (ClassNotFoundException c) {
			System.out.println("Chunk class not found");
			c.printStackTrace();
			return null;
		}

		return chunk;
	}

	public int getPerceivedReplicationDegree() {
		// TODO Auto-generated method stub
		return perceivedReplicationDegree;
	}

	public void updatePerceivedReplicationDegree(int value) {
		perceivedReplicationDegree+=value;
	}

}
