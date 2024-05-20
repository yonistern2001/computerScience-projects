package edu.yu.cs.com3800.stage5;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.BufferUnderflowException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import edu.yu.cs.com3800.Message;
import edu.yu.cs.com3800.Message.MessageType;
import edu.yu.cs.com3800.Util;

public class TCPSenderReceiver {
	
	private final Set<InetSocketAddress> deadNodes= Collections.synchronizedSet(new HashSet<>());
	private final InetSocketAddress address;
	private final int tcpPort;
	
	public TCPSenderReceiver(InetSocketAddress address, int tcpPort) {
		this.address= address;
		this.tcpPort= tcpPort;
	}
	
	public int getTcpPort() {
		return this.tcpPort;
	}
	
	public Message sendAndReceive(MessageType type, byte[] content, InetSocketAddress destAddr, long requestID, Logger logger)
			throws IOException, DeadNodeException, InterruptedException {
		assertNodeIsAlive(destAddr);
		
		Socket socket= null;
		Message responseMsg= null;

		try {
			Message msg = new Message(type, content, this.address.getHostName(), tcpPort, destAddr.getHostName(),
					destAddr.getPort(), requestID);
			socket = new Socket(destAddr.getAddress(), destAddr.getPort());
			logger.fine("Message sent:\n" + msg);
			socket.getOutputStream().write(msg.getNetworkPayload());
			responseMsg = new Message(this.readAllBytesFromNetwork(socket.getInputStream(), destAddr));
			logger.fine("Message received:\n" + responseMsg);
		} catch(IOException | BufferUnderflowException e) {}
		
		if (socket != null) {
			socket.close();
		}
		
		assertNodeIsAlive(destAddr);
		
		if(responseMsg == null) {
			throw new IOException();
		}
		return responseMsg;
	}
	
	public void sendResponse(MessageType type, byte[] content, Socket socket, long requestID, boolean errorOccurred,
			InetSocketAddress dest, Logger logger) throws IOException, DeadNodeException {
		assertNodeIsAlive(dest);
		boolean error= false;

		try {
			Message msg = new Message(type, content, this.address.getHostName(), tcpPort, dest.getHostName(),
					dest.getPort(), requestID, errorOccurred);
			socket.getOutputStream().write(msg.getNetworkPayload());
			logger.fine("Message response sent:\n" + msg);
		} catch(IOException e) {
			error= true;
		}
		
		assertNodeIsAlive(dest);
		
		if(error) {
			throw new IOException();
		}
	}
	
	public void reportFailedNode(InetSocketAddress deadNodeAddrs) {
		InetSocketAddress deadNodeAddrTCP = udpAddrToTcpAddr(deadNodeAddrs);
		this.deadNodes.add(deadNodeAddrTCP);
	}
	
	private void assertNodeIsAlive(InetSocketAddress address) throws DeadNodeException {
		if(isNodeDead(address)) {
			throw new DeadNodeException();
		}
	}

	public boolean isNodeDead(InetSocketAddress address) {
		return deadNodes.contains(address);
	}
	
	private byte[] readAllBytesFromNetwork(InputStream in, InetSocketAddress addr) throws IOException, InterruptedException, DeadNodeException {
		while (in.available() == 0) {
			for(int i= 0; i < 10 && in.available() == 0; i++) {
				Thread.sleep(500);
			}
			assertNodeIsAlive(addr);
		}
		return Util.readAllBytes(in);
	}
	
	public static InetSocketAddress udpAddrToTcpAddr(InetSocketAddress addr) {
		return new InetSocketAddress(addr.getHostName(), addr.getPort() + 2);
	}
	
	public static Socket acceptSocket(ServerSocket serverSocket) {
		try {
			return serverSocket.accept();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
	
	public static class DeadNodeException extends RuntimeException {
		
		public DeadNodeException() {
			super();
		}
	}
}
