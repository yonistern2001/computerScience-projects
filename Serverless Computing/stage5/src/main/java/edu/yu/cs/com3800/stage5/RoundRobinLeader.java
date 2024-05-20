package edu.yu.cs.com3800.stage5;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.yu.cs.com3800.LoggingServer;
import edu.yu.cs.com3800.Message;
import edu.yu.cs.com3800.Message.MessageType;
import edu.yu.cs.com3800.Util;
import edu.yu.cs.com3800.ZooKeeperPeerServer;
import edu.yu.cs.com3800.stage5.TCPSenderReceiver.DeadNodeException;

public class RoundRobinLeader implements Runnable, LoggingServer {
	
	private final ConcurrentHashMap<Long, Message> results;
	private final List<InetSocketAddress> addrs;
	private final ServerSocket serverSocket;
	private final ExecutorService executor;
	private final TCPSenderReceiver tcpService;
	private final Logger logger;
	
	private int curr= 0;
	
	public RoundRobinLeader(ZooKeeperPeerServer server, List<InetSocketAddress> addrs, TCPSenderReceiver tcpService, ConcurrentHashMap<Long, Message> results) {
		this.results= results;
		this.tcpService= tcpService;
		this.executor= Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 3);
		this.addrs= addrs;
		
		try {
			this.logger= initializeLogging(this.getClass().getCanonicalName() + "-on-serverID-" + server.getServerId() + "-with-tcpPort-" + tcpService.getTcpPort());
			this.serverSocket= new ServerSocket(tcpService.getTcpPort());
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
	
	@Override
	public void run() {
		this.logger.info("Starting thread");
		try {
			while (!Thread.currentThread().isInterrupted()) {
				try {
					Socket socket = serverSocket.accept();
					this.executor.submit(() -> processTask(socket));
				} catch (IOException e) {
					Thread.currentThread().interrupt();
				}
			}
		} finally {
			terminate();
		}
	}

	private void processTask(Socket socket) {
		try {
			byte[] input = Util.readAllBytesFromNetwork(socket.getInputStream());
			Message msg = new Message(input);

			if (msg.getMessageType() == MessageType.WORK) {
				computeAndSendResponse(socket, msg);
			} else if (msg.getMessageType() == MessageType.COMPLETED_WORK) {
				addResult(msg);
			}
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Could not connect to gateway", e);
		} catch (InterruptedException o) {
			logger.info("Task interrupted");
		}
	}

	private void addResult(Message msg) {
		if(!tcpService.isNodeDead(new InetSocketAddress(msg.getSenderHost(), msg.getSenderPort()))) {
			this.logger.fine("Adding result for request ID: " + msg.getRequestID() + "\n" + msg);
			this.results.put(msg.getRequestID(), msg);
		}
	}

	private void computeAndSendResponse(Socket socket, Message msg) throws IOException, InterruptedException {
		Message responseMsg;
		if (this.results.containsKey(msg.getRequestID())) {
			this.logger.fine("Already has result for id: " + msg.getRequestID());
			responseMsg = this.results.remove(msg.getRequestID());
		} else {
			responseMsg = fowardMsgToWorker(msg);
		}

		this.tcpService.sendResponse(MessageType.COMPLETED_WORK, responseMsg.getMessageContents(), socket,
				msg.getRequestID(), responseMsg.getErrorOccurred(),
				new InetSocketAddress(msg.getSenderHost(), msg.getSenderPort()), logger);
	}

	private Message fowardMsgToWorker(Message msg) throws InterruptedException {
		InetSocketAddress targetAddress = TCPSenderReceiver.udpAddrToTcpAddr(getNextAddress());
		while(!Thread.currentThread().isInterrupted()) {
			try {
				logger.fine("Sending work to: " + targetAddress.getHostName() + ":" + targetAddress.getPort());
				return this.tcpService.sendAndReceive(MessageType.WORK, msg.getMessageContents(), targetAddress, msg.getRequestID(), logger);
			} catch(DeadNodeException e) {
				logger.info("Node at "+ targetAddress + " is down");
				targetAddress= TCPSenderReceiver.udpAddrToTcpAddr(getNextAddress());
			} catch (IOException e) {
				logger.info("IOException occurred, message not sent");
				Thread.sleep(5000);
			}
		}
		throw new InterruptedException();
	}
	
	private synchronized InetSocketAddress getNextAddress() {
		InetSocketAddress currAddr= this.addrs.get(curr);
		curr= (curr+1) % this.addrs.size();
		return currAddr;
	}
	
	public void shutdown() {
		Util.closeServerSocket(serverSocket);
	}
	
	private void terminate() {
		this.logger.info("Shutting down thread");
		this.executor.shutdown();
		Util.closeServerSocket(serverSocket);
		LoggingServer.closeHandlers(this.logger);
	}
}
