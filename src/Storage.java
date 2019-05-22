import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class Storage {

    private ConcurrentHashMap<String, byte[]> restoredChunks;
    private String chunksPath;

    public Storage(String nodeId) {
        chunksPath = "chunks-" + nodeId;
        new File(chunksPath).mkdirs();
    }

    public void restoreFile(String fileId, String fileName) throws IOException {
        String path = fileName;
        ConcurrentHashMap<Integer, byte[]> chunks = getChunks(fileId);

        FileOutputStream fout = new FileOutputStream(path);
        FileChannel fcout = fout.getChannel();

        ByteBuffer buffer = ByteBuffer.allocate(64 * 1000);

        for (Map.Entry<Integer, byte[]> chunk : chunks.entrySet()) {
            buffer.clear();
            buffer.put(chunk.getValue());
            buffer.flip();
            fcout.write(buffer);
        }

        fout.close();
    }

    public ConcurrentHashMap<Integer, byte[]> getChunks(String fileId) {

        ConcurrentHashMap<Integer, byte[]> chunks = new ConcurrentHashMap<Integer, byte[]>();

        for (Map.Entry<String, byte[]> entry : restoredChunks.entrySet()) {
            String key = entry.getKey();
            if (key.contains(fileId)) {
                chunks.put(Integer.parseInt(key.substring(key.indexOf("-") + 1)), entry.getValue());
            }
        }

        return chunks;
    }

    public void storeChunk(BigInteger key, byte[] chunk) {
        String path = chunksPath + "/" + key.toString() + ".chunk";
        
        try {

            FileOutputStream fout = new FileOutputStream(path);
            FileChannel fcout = fout.getChannel();
    
            ByteBuffer buffer = ByteBuffer.allocate(64 * 1000);
    
            buffer.clear();
            buffer.put(chunk);
            buffer.flip();
            fcout.write(buffer);
    
            fout.close();
        }
        catch(IOException e){
            e.printStackTrace();
        }

	}
	
	public void delete(BigInteger key) {
		String path = chunksPath + "/" + key.toString() + ".chunk";
		new File(path).delete();
	}
}