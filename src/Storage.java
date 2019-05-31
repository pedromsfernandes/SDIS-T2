import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

class Storage {

    private ConcurrentHashMap<String, byte[]> restoredChunks;
    private ConcurrentHashMap<String, AtomicInteger> restoredChunksCount;

    private String chunksPath;

    public Storage(String nodeId) {
        chunksPath = "chunks-" + nodeId;
        new File(chunksPath).mkdirs();
        restoredChunks = new ConcurrentHashMap<String, byte[]>();
        restoredChunksCount = new ConcurrentHashMap<String, AtomicInteger>();
    }

    public int getFileCount(String file){
        return restoredChunksCount.get(file).get();
    }

    public ArrayList<byte[]> getChunks(ArrayList<String> keys){
        ArrayList<byte[]> chunks = new ArrayList<byte[]>();

        for(String key : keys){
            chunks.add(restoredChunks.get(key));
        }

        return chunks;
    }

    public void addRestoredChunk(String key, String file, byte[] content){
        restoredChunks.put(key, content);
        restoredChunksCount.replace(file, new AtomicInteger(restoredChunksCount.get(file).decrementAndGet()));
    }

    public void addFileToRestore(String file, int numChunks){
        restoredChunksCount.put(file, new AtomicInteger(numChunks));
    }

    public void freeRestoredChunks(ArrayList<String> keys, String file){
        restoredChunksCount.remove(file);

        for(String key : keys){
            restoredChunks.remove(key);
        }
    }

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

    public void delete(BigInteger key) {
        String path = chunksPath + Utils.getCharSeparator() + key.toString() + ".chunk";
        new File(path).delete();
    }

    public byte[] getChunkContent(BigInteger key) {
        return Utils.splitFile(chunksPath + Utils.getCharSeparator() + key.toString() + ".chunk").get(0);
    }
}