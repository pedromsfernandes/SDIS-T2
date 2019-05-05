import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class Storage {

    private ConcurrentHashMap<String, byte[]> restoredChunks;

    public Storage() {

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
}