package edu.yu.cs.com3800.stage5;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.yu.cs.com3800.JavaRunner;
import edu.yu.cs.com3800.LoggingServer;
import edu.yu.cs.com3800.Message;
import edu.yu.cs.com3800.Message.MessageType;
import edu.yu.cs.com3800.Util;
import edu.yu.cs.com3800.stage5.TCPSenderReceiver.DeadNodeException;

public class JavaRunnerFollower implements Runnable, LoggingServer {
	
	private final Map<Long, Message> results;
	private final ServerSocket serverSocket;
	private final ExecutorService executor;
	private final TCPSenderReceiver tcpService;
	private final ZooKeeperPeerServerImpl server;
	private final JavaRunner runner;
	private final Logger logger;
	
	
	public JavaRunnerFollower(ZooKeeperPeerServerImpl server, TCPSenderReceiver tcpService, ConcurrentHashMap<Long, Message> results) {
		this.results= results;
		this.server= server;
		this.executor= Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1);
		this.tcpService= tcpService;
		
		try {
			this.runner= new JavaRunner();
			this.serverSocket= new ServerSocket(tcpService.getTcpPort());
			this.logger= initializeLogging(this.getClass().getCanonicalName() + "-on-serverID-" + server.getServerId() + "-with-tcpPort-" + tcpService.getTcpPort());
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	@Override
	public void run() {
		logger.fine("Starting thread");
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
			Message msg = new Message(Util.readAllBytesFromNetwork(socket.getInputStream()));
			Result response = executeCode(msg.getMessageContents());
			logger.fine("Executed code, result: " + response.output);
			sendResponseToLeader(socket, new InetSocketAddress(msg.getSenderHost(), msg.getSenderPort()), response, msg.getRequestID());
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Error communicating with leader", e);
		}
	}

	private void sendResponseToLeader(Socket socket, InetSocketAddress destAddr, Result response, long requestId)
			throws IOException {
		while(!Thread.currentThread().isInterrupted()) {
			try {
				this.tcpService.sendResponse(MessageType.COMPLETED_WORK, response.output.getBytes(), socket, requestId,
						response.errorOccurred, destAddr, logger);
				return;
			} catch (DeadNodeException e) {
				socket.close();
				this.logger.info("Send unsuccessful, leader is down");
				long leaderId = server.getCurrentLeaderBlocking().getProposedLeaderID();
				if(leaderId == server.getServerId()) {
					addResult(destAddr, response, requestId);
					return;
				}
				destAddr= TCPSenderReceiver.udpAddrToTcpAddr(this.server.getPeerByID(leaderId));
				socket= new Socket(destAddr.getHostName(), destAddr.getPort());
			}
		}
	}

	private void addResult(InetSocketAddress addr, Result response, long requestId) {
		Message msg = new Message(MessageType.COMPLETED_WORK, response.output.getBytes(), addr.getHostName(),
				addr.getPort(), addr.getHostName(), addr.getPort(), requestId, response.errorOccurred);
		logger.info("Sending result straight to leader: " + msg);
		results.put(requestId, msg);
	}

	private Result executeCode(byte[] code) {
		try {
			String response= runner.compileAndRun(new ByteArrayInputStream(code));
			return new Result(response, false);
			
		} catch (IllegalArgumentException | IOException | ReflectiveOperationException e) {
			String errorMsg= e.getMessage() + "\n" + Util.getStackTrace(e);
			return new Result(errorMsg, true);
		}
	}
	
	private static class Result {
		private final String output;
		private final boolean errorOccurred;
		
		private Result(String output, boolean errorOccurred) {
			this.output= output;
			this.errorOccurred= errorOccurred;
		}
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
