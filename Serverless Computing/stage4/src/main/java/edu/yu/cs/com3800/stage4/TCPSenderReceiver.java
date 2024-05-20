package edu.yu.cs.com3800.stage4;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;

import edu.yu.cs.com3800.Message;
import edu.yu.cs.com3800.Util;
import edu.yu.cs.com3800.Message.MessageType;

public class TCPSenderReceiver {
	
	private final InetSocketAddress address;
	private final int tcpPort;
	private final Logger logger;
	
	public TCPSenderReceiver(InetSocketAddress address, int tcpPort, Logger logger) {
		this.address= address;
		this.tcpPort= tcpPort;
		this.logger= logger;
	}
	
	
	public Message sendAndReceive(MessageType type, byte[] content, InetSocketAddress destAddr, long requestID) throws IOException {
		Message msg = new Message(type, content, this.address.getHostName(), tcpPort,
				destAddr.getHostName(), destAddr.getPort() + 2, requestID);
		Socket socket= new Socket(destAddr.getAddress(), destAddr.getPort() + 2);
		logger.fine("Message sent:\n" + msg);
		socket.getOutputStream().write(msg.getNetworkPayload());
		Message responseMsg= new Message(Util.readAllBytesFromNetwork(socket.getInputStream()));
		logger.fine("Message received:\n" + responseMsg);
		socket.close();
		return responseMsg;
	}
	
	public void sendResponse(MessageType type, byte[] content, Socket socket, long requestID, boolean errorOccurred, InetSocketAddress dest) throws IOException {
		
		Message msg= new Message(type, content, this.address.getHostName(), tcpPort, dest.getHostName(),
				dest.getPort(), requestID, errorOccurred);
		socket.getOutputStream().write(msg.getNetworkPayload());
		logger.fine("Message response sent:\n" + msg);
	}
	
	public static Socket acceptSocket(ServerSocket serverSocket) {
		try {
			return serverSocket.accept();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
}
