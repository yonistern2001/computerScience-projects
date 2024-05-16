package edu.yu.cs.com3800.stage3;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.yu.cs.com3800.JavaRunner;
import edu.yu.cs.com3800.LoggingServer;
import edu.yu.cs.com3800.Message;
import edu.yu.cs.com3800.Message.MessageType;
import edu.yu.cs.com3800.Util;

public class JavaRunnerFollower implements Runnable, LoggingServer {
	
	private final LinkedBlockingQueue<Message> incoming;
	private final JavaRunner runner;
	private final ZooKeeperPeerServerImpl server;
	private final Logger logger;
	
	
	public JavaRunnerFollower(ZooKeeperPeerServerImpl server, LinkedBlockingQueue<Message> incoming) {
		this.server= server;
		this.incoming= incoming;
		
		try {
			this.runner= new JavaRunner();
			this.logger= initializeLogging(this.getClass().getCanonicalName() + "-on-port-" + server.getUdpPort());
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	@Override
	public void run() {
		while(!Thread.currentThread().isInterrupted()) {
			Message message;
			try {
				message= incoming.take();
			} catch (InterruptedException e) {
				logger.log(Level.WARNING, "Thread interrupted", e);
				return;
			}
			
			if(MessageType.WORK.equals(message.getMessageType())) {
				processMsg(message);
			}
		}
	}

	private void processMsg(Message message) {
		byte[] code= message.getMessageContents();
		String response = executeCode(code);
		logger.fine("Executed code, result: " + response);
		InetSocketAddress targetAddr = new InetSocketAddress(message.getSenderHost(), message.getSenderPort());		
		this.server.sendMessage(MessageType.COMPLETED_WORK, response.getBytes(), targetAddr, message.getRequestID());
	}

	private String executeCode(byte[] code) {
		String response;
		try {
			response= runner.compileAndRun(new ByteArrayInputStream(code));
		} catch (IllegalArgumentException | IOException | ReflectiveOperationException e) {
			response= e.getMessage() + "\n" + Util.getStackTrace(e);
		}
		return response;
	}
}
