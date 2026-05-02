package ir.ac.ut.ece.ie.staticcontentserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.StringTokenizer;

public class StaticContentServer {
	ServerSocket serverSocket;
	
	public void start() throws IOException {
		serverSocket = new ServerSocket(9091);
		Socket socket;
		while ((socket = serverSocket.accept()) != null) {
			BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			String readLine = reader.readLine();
			if (readLine == null)
				continue;
			String fileName = getFileName(readLine);
			try {
				File file = new File("./src/main/resources/" + fileName);
				String header = "HTTP1.1 200 OK \r\nContent-Type: text/html\r\nContent.Length: " 
				+ file.length()
				+ "\r\n\r\n";

				RandomAccessFile raf = new RandomAccessFile(file, "r");
				byte[] data = new byte[1024];
				int size = 0;
				socket.getOutputStream().write(header.getBytes());
				try {
					while((size = raf.read(data)) != -1) {
						socket.getOutputStream().write(data, 0 , size);
					}
				} catch(IOException e) {
					raf.close();
				}
			} catch (FileNotFoundException e) {
				String header = "HTTP1.1 404 Page Not Found\r\n\r\n";
				socket.getOutputStream().write(header.getBytes());
			}
			socket.getOutputStream().flush();
			socket.close();
		}
		serverSocket.close();
	}

	private String getFileName(String readLine) {
		StringTokenizer tokenizer = new StringTokenizer(readLine, " ");
		tokenizer.nextToken();
		String fileName = tokenizer.nextToken().substring(1);
		return fileName;
	}

	public void stop() throws IOException {
		serverSocket.close();
	}
	
	public static void main(String[] args) throws IOException {
		StaticContentServer server = new StaticContentServer();
		server.start();
	}
}
