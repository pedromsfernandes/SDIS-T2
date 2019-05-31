import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;


public class Utils {
	/**
	 * 
	 * @param message
	 * @param length
	 * @return
	 */
	public static byte[] getChunkContent(byte[] message, int length) {

		byte[] chunkContent = new byte[64 * 1000];

		for (int i = 0; i < message.length; i++) {
			if ((int) message[i] == 13 && (int) message[i + 1] == 10 && (int) message[i + 2] == 13
					&& (int) message[i + 3] == 10) {
				chunkContent = new byte[length - i - 4];
				System.arraycopy(message, i + 4, chunkContent, 0, length - i - 4);
				break;
			}
		}

		return chunkContent;
	}

	/**
	 * 
	 * @param message
	 * @return
	 */
	public static String[] getHeader(byte[] message) {

		byte[] header = new byte[1000];

		for (int i = 0; i < message.length; i++) {
			if ((int) message[i] == 13 && (int) message[i + 1] == 10 && (int) message[i + 2] == 13
					&& (int) message[i + 3] == 10) {
				header = new byte[i];
				System.arraycopy(message, 0, header, 0, i);
				break;
			}
		}

		return new String(header, StandardCharsets.US_ASCII).split("\\s+");
	}

	/**
	 * 
	 * @return
	 */
	public static char getCharSeparator() {
		String os = System.getProperty("os.name");

		if(os.toLowerCase().contains("win"))
			return '\\';
		
		return '/';
	}	

	/**
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static byte[] concatenateArrays(byte[] a, byte[] b) {
		byte[] c = new byte[a.length + b.length];
		System.arraycopy(a, 0, c, 0, a.length);
		System.arraycopy(b, 0, c, a.length, b.length);

		return c;
	}

	/**
	 * 
	 * @param input
	 * @return
	 */
	public static BigInteger getSHA1(String input){
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-1");
			byte[] encoded = digest.digest(input.getBytes());
			return new BigInteger(1,encoded);
		} catch (Exception e) {
			e.printStackTrace();
			return new BigInteger(1,"0".getBytes());
		}
	}
	
	/**
	 * 
	 * @param fileName
	 * @return
	 */
	public static ArrayList<byte[]> splitFile(String fileName) {

        int chunkSize = 64 * 1000; // 64KByte
        ArrayList<byte[]> chunks = new ArrayList<byte[]>();

        FileInputStream fin = null;
        try {
            fin = new FileInputStream(fileName);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        FileChannel fc = fin.getChannel();
        ByteBuffer byteBuffer = ByteBuffer.allocate(chunkSize);
        int bytesAmount = 0;

        try {
            while ((bytesAmount = fc.read(byteBuffer)) > 0) {
                byte[] smaller = new byte[bytesAmount];

                byteBuffer.flip();
                byteBuffer.get(smaller);
                byteBuffer.clear();

                chunks.add(smaller);
            }

            fin.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return chunks;
    }
}
