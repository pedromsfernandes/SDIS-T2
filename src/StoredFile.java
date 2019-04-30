import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
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

		int chunkSize = 64 * 1000;// 64KByte
		byte[] buffer = new byte[chunkSize];
		ArrayList<Chunk> chunks = new ArrayList<Chunk>();
		File f = new File(this.fileName);
		int chunkNo = 0, bytesAmount = 0;

		// try-with-resources to ensure closing stream
		try (FileInputStream fis = new FileInputStream(f); BufferedInputStream bis = new BufferedInputStream(fis)) {

			for (; (bytesAmount = bis.read(buffer)) > 0; chunkNo++) {
				byte[] smaller = new byte[bytesAmount];
				System.arraycopy(buffer, 0, smaller, 0, bytesAmount);
				chunks.add(new Chunk(fileId, chunkNo, smaller, bytesAmount, replicationDegree));
				this.chunks.put(chunkNo, new ArrayList<Integer>());
			}
		}

		if (f.length() % chunkSize == 0) {
			chunks.add(new Chunk(fileId, chunkNo, buffer, 0, replicationDegree));
			this.chunks.put(chunkNo, new ArrayList<Integer>());
		}

		return chunks;
	}

	public byte[] getFileId() {
		return fileId;
	}
}