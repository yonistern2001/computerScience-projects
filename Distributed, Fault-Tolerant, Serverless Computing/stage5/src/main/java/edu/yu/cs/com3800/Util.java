package edu.yu.cs.com3800;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.net.ServerSocket;

public class Util {

    public static byte[] readAllBytesFromNetwork(InputStream in) throws IOException  {
		int tries = 0;
		while (in.available() == 0 && tries < 10) {
			try {
				tries++;
				Thread.currentThread().sleep(500);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
		return readAllBytes(in);
    }

	public static byte[] readAllBytes(InputStream in) throws IOException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		int numberRead;
		byte[] data = new byte[40960];
		while (in.available() > 0 && (numberRead = in.read(data, 0, data.length)) != -1) {
			buffer.write(data, 0, numberRead);
		}
		return buffer.toByteArray();
    }

    public static Thread startAsDaemon(Runnable run, String name) {
        Thread thread = new Thread(run, name);
        thread.setDaemon(true);
        thread.start();
        return thread;
    }

    public static String getStackTrace(Exception e){
        ByteArrayOutputStream bas = new ByteArrayOutputStream();
        PrintStream myErr = new PrintStream(bas,true);
        e.printStackTrace(myErr);
        myErr.flush();
        myErr.close();
        return bas.toString();
    }

	public static void closeServerSocket(ServerSocket serverSocket) {
		try {
			if (!serverSocket.isClosed()) {
				serverSocket.close();
			}
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
}
