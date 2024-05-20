package edu.yu.cs.com3800.stage4;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import edu.yu.cs.com3800.JavaRunner;
import edu.yu.cs.com3800.LoggingServer;
import edu.yu.cs.com3800.Message;
import edu.yu.cs.com3800.Message.MessageType;
import edu.yu.cs.com3800.Util;

public class JavaRunnerFollower implements Runnable, LoggingServer {
	
	private final JavaRunner runner;
	private final Logger logger;
	private final ServerSocket serverSocket;
	private final ExecutorService executor;
	private final TCPSenderReceiver tcpService;
	
	
	public JavaRunnerFollower(ZooKeeperPeerServerImpl server) {
		try {
			this.runner= new JavaRunner();
			this.serverSocket= new ServerSocket(server.getTcpPort());
			this.logger= initializeLogging(this.getClass().getCanonicalName() + "-on-serverID-" + server.getServerId() + "-with-tcpPort-" + server.getTcpPort());
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		
		this.executor= Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1);
		this.tcpService= new TCPSenderReceiver(server.getAddress(), server.getTcpPort(), logger);
	}

	@Override
	public void run() {
		logger.fine("Starting thread");
		while(!Thread.currentThread().isInterrupted()) {
			Socket socket= TCPSenderReceiver.acceptSocket(serverSocket);
			executor.submit(createHandler(socket));
		}
		shutdown();
	}

	private Runnable createHandler(Socket socket) {
		return () -> {
			try {
				Message msg = new Message(Util.readAllBytesFromNetwork(socket.getInputStream()));
				Result response = executeCode(msg.getMessageContents());
				logger.fine("Executed code, result: " + response.output);
				this.tcpService.sendResponse(MessageType.COMPLETED_WORK, response.output.getBytes(), socket, msg.getRequestID(), response.errorOccurred, 
						new InetSocketAddress(msg.getSenderHost(), msg.getSenderPort()));
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		};
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
	
	private void shutdown() {
		this.logger.info("Shutting down thread");
		this.executor.shutdown();
		LoggingServer.closeHandlers(this.logger);
	}
}
