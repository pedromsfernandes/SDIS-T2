import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

class Storage {

    private ConcurrentHashMap<String, byte[]> restoredChunks;
    private ConcurrentHashMap<String, AtomicInteger> restoredChunksCount;

    private String chunksPath;

    /**
     * 
     * @param nodeId
     */
    public Storage(String nodeId) {
        chunksPath = "chunks-" + nodeId;
        new File(chunksPath).mkdirs();
        restoredChunks = new ConcurrentHashMap<String, byte[]>();
        restoredChunksCount = new ConcurrentHashMap<String, AtomicInteger>();
    }

    /**
     * 
     * @param file
     * @return
     */
    public int getFileCount(String file){
        return restoredChunksCount.get(file).get();
    }

    /**
     * 
     * @param keys
     * @return
     */
    public ArrayList<byte[]> getChunks(ArrayList<String> keys){
        ArrayList<byte[]> chunks = new ArrayList<byte[]>();

        for(String key : keys){
            chunks.add(restoredChunks.get(key));
        }

        return chunks;
    }

    /**
     * 
     * @param key
     * @param file
     * @param content
     */
    public void addRestoredChunk(String key, String file, byte[] content){
        restoredChunks.put(key, content);
        restoredChunksCount.replace(file, new AtomicInteger(restoredChunksCount.get(file).decrementAndGet()));
    }

    /**
     * 
     * @param file
     * @param numChunks
     */
    public void addFileToRestore(String file, int numChunks){
        restoredChunksCount.put(file, new AtomicInteger(numChunks));
    }

    /**
     * 
     * @param keys
     * @param file
     */
    public void freeRestoredChunks(ArrayList<String> keys, String file){
        restoredChunksCount.remove(file);

        for(String key : keys){
            restoredChunks.remove(key);
        }
    }

    /**
     * 
     * @param key
     * @param chunk
     */
    public void storeChunk(BigInteger key, byte[] chunk) {
        String path = chunksPath + Utils.getCharSeparator() + key.toString() + ".chunk";

        try {

            FileOutputStream fout = new FileOutputStream(path);
            FileChannel fcout = fout.getChannel();

            ByteBuffer buffer = ByteBuffer.allocate(64 * 1000);

            buffer.clear();
            buffer.put(chunk);
            buffer.flip();
            fcout.write(buffer);

            fout.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 
     * @param key
     * @return
     */
    public byte[] readChunk(BigInteger key) {
        int chunkSize = 64 * 1000;
        byte[] chunk = new byte[chunkSize];
        String path = chunksPath + Utils.getCharSeparator() + key.toString() + ".chunk";

        FileInputStream fin = null;
        try {
            fin = new FileInputStream(path);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        FileChannel fc = fin.getChannel();
        ByteBuffer byteBuffer = ByteBuffer.allocate(chunkSize);
        int bytesAmount = 0;

        try {
            bytesAmount = fc.read(byteBuffer);
            chunk = new byte[bytesAmount];

            byteBuffer.flip();
            byteBuffer.get(chunk);
            byteBuffer.clear();

            fin.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return chunk;

    }

    /**
     * 
     * @param key
     */
    public void delete(BigInteger key) {
        String path = chunksPath + Utils.getCharSeparator() + key.toString() + ".chunk";
        new File(path).delete();
    }

    /**
     * 
     * @param key
     * @return
     */
    public byte[] getChunkContent(BigInteger key) {
        return Utils.splitFile(chunksPath + Utils.getCharSeparator() + key.toString() + ".chunk").get(0);
    }
}