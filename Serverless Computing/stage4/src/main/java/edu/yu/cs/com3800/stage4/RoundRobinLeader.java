package edu.yu.cs.com3800.stage4;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import edu.yu.cs.com3800.LoggingServer;
import edu.yu.cs.com3800.Message;
import edu.yu.cs.com3800.Message.MessageType;
import edu.yu.cs.com3800.Util;

public class RoundRobinLeader implements Runnable, LoggingServer {
	
	private final List<InetSocketAddress> addrs;
	private final ServerSocket serverSocket;
	private final ExecutorService executor;
	private final Logger logger;
	private final TCPSenderReceiver tcpService;
	
	private int curr;
	private long requestID;
	
	public RoundRobinLeader(ZooKeeperPeerServerImpl server, List<InetSocketAddress> addrs) {
		try {
			this.logger= initializeLogging(this.getClass().getCanonicalName() + "-on-serverID-" + server.getServerId() + "-with-tcpPort-" + server.getTcpPort());
			this.serverSocket= new ServerSocket(server.getTcpPort());
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		
		this.tcpService= new TCPSenderReceiver(server.getAddress(), server.getTcpPort(), logger);
		this.executor= Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 3);
		this.addrs= addrs;
		this.requestID= 0;
		this.curr= 0;
	}
	
	@Override
	public void run() {
		this.logger.info("Starting thread");
		while(!Thread.currentThread().isInterrupted()) {
			Socket socket= TCPSenderReceiver.acceptSocket(serverSocket);
			this.executor.submit(createHandler(socket));
		}
		shutdown();
	}
	
	private Runnable createHandler(final Socket socket) {
		return () -> {
			InetSocketAddress targetAddress = getNextAddress();
			try {
				byte[] input= Util.readAllBytesFromNetwork(socket.getInputStream());
				Message msg= new Message(input);
				logger.fine("Sending work to: " + targetAddress.getHostName() + ":" + targetAddress.getPort() + 2);
				Message responseMsg= this.tcpService.sendAndReceive(MessageType.WORK, msg.getMessageContents(), targetAddress, requestID++);
				this.tcpService.sendResponse(MessageType.COMPLETED_WORK, responseMsg.getMessageContents(), socket, -1, responseMsg.getErrorOccurred(),
						new InetSocketAddress(msg.getSenderHost(), msg.getSenderPort()));
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		};
	}
	
	private synchronized InetSocketAddress getNextAddress() {
		InetSocketAddress currAddr= this.addrs.get(curr);
		curr= (curr+1) % this.addrs.size();
		return currAddr;
	}
	
	private void shutdown() {
		this.logger.info("Shutting down thread");
		this.executor.shutdown();
		LoggingServer.closeHandlers(this.logger);
	}
}
