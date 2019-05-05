import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class StoredFile implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8932985840046828L;
	private String fileName;
	private byte[] fileId;
	private int replicationDegree;
	private ConcurrentHashMap<Integer, ArrayList<Integer>> chunks;

	public StoredFile(String fileName, int replicationDegree) {
		this.fileName = fileName;
		this.replicationDegree = replicationDegree;
		this.chunks = new ConcurrentHashMap<Integer, ArrayList<Integer>>();
		this.fileId = encryptFileId(fileName);
	}

	public int getPerceivedReplicationDegree(int chunkNo) {
		return chunks.get(chunkNo).size();
	}

	public static byte[] encryptFileId(String fileName) {

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

	public ArrayList<Chunk> splitFile() throws IOException {

		int chunkSize = 64 * 1000; // 64KByte
		ArrayList<Chunk> chunks = new ArrayList<Chunk>();

		FileInputStream fin = new FileInputStream(this.fileName);
		FileChannel fc = fin.getChannel();
		ByteBuffer byteBuffer = ByteBuffer.allocate(chunkSize);
		int chunkNo = 0, bytesAmount = 0;

		for (; (bytesAmount = fc.read(byteBuffer)) > 0; chunkNo++) {
			byte[] smaller = new byte[bytesAmount];

			byteBuffer.flip();
			byteBuffer.get(smaller);
			byteBuffer.clear();
			
			chunks.add(new Chunk(fileId, chunkNo, smaller, bytesAmount, replicationDegree));
			this.chunks.put(chunkNo, new ArrayList<Integer>());
		}

		fin.close();

		return chunks;
	}

	public byte[] getFileId() {
		return fileId;
	}
}