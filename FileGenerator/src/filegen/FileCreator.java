package filegen;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class FileCreator {

	private String fileName;
	private int fileSize;
	private char[] charSet;

	public FileCreator(final String fileName, final String size) {
		this.fileName = fileName;
		fileSize = Integer.parseInt(size) ;//in bytes
		String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvwxyz";
		charSet = alphabet.toCharArray();

		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < fileSize; ++i){
			Random r =  new Random();
			int index = r.nextInt(charSet.length);
			builder.append(charSet[index]);
		}

		FileWriter fstream;
		try {
			fstream = new FileWriter(fileName);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write(builder.toString());
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {		
		FileCreator creator = new FileCreator(args[0], args[1]);

	}

}
