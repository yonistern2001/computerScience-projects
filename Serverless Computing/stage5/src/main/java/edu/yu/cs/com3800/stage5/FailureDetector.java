package edu.yu.cs.com3800.stage5;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.yu.cs.com3800.LoggingServer;
import edu.yu.cs.com3800.Message;
import edu.yu.cs.com3800.Message.MessageType;
import edu.yu.cs.com3800.ZooKeeperPeerServer;

public class FailureDetector extends Thread implements Runnable, LoggingServer {
	
	private final ZooKeeperPeerServer server;
	private final ScheduledExecutorService scheduledExecutor;
	private final BlockingQueue<Message> incomingMessages;
	private final HeartbeatTable table;
	private final LoggerEndpoints loggerEndpoints;
	protected final Logger verboseLogger, summaryLogger;

	public FailureDetector(ZooKeeperPeerServer server, Set<Long> ids, BlockingQueue<Message> incomingMessages, long id) {
		try {
			this.verboseLogger= initializeLogging(this.getClass().getCanonicalName() + "-verbose-on-serverID-" + id);
			this.summaryLogger= initializeLogging(this.getClass().getCanonicalName() + "-summary-on-serverID-" + id);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		
		super.setDaemon(true);
		super.setName("Failure-Detector-ID-" + id);
		this.server= server;
		this.incomingMessages= incomingMessages;
		this.table= new HeartbeatTable(ids, id, verboseLogger, summaryLogger);
		this.scheduledExecutor= new ScheduledThreadPoolExecutor(1);	
		this.loggerEndpoints= new LoggerEndpoints(this.verboseLogger.getName()+".log", this.summaryLogger.getName()+".log", server.getUdpPort() + 4);
	}
	
	@Override
	public void run() {
		verboseLogger.info("Starting thread");
		this.loggerEndpoints.start();
		
		table.initializeTable();
		this.scheduledExecutor.scheduleAtFixedRate(this::updateAndSendGossip, 0, HeartbeatTable.GOSSIP, TimeUnit.MILLISECONDS);
		try {
			while (!Thread.currentThread().isInterrupted()) {
				Message msg;
				try {
					msg = incomingMessages.take();
				} catch (InterruptedException e) {
					break;
				}
				GossipMessage gossip = new GossipMessage(msg.getMessageContents());
				verboseLogger.fine("Received gossip message:\n" + gossip + "\nTime: " + System.currentTimeMillis());
				table.merge(gossip);
			}
		} catch (Exception e) {
			this.verboseLogger.log(Level.SEVERE, "Exception thrown", e);
		} finally {
			terminate();
		}
	}
	
	private void updateAndSendGossip() {
		try {
			table.incrementHeartbeat();
			Set<Long> failedNodes = table.updateStateAndCleanup();
			failedNodes.forEach(server::reportFailedPeer);
			GossipMessage gossip = this.table.createGossipMessage();
			this.verboseLogger.fine("Sending gossip message:\n" + gossip);

			server.sendMessage(MessageType.GOSSIP, gossip.createMsgContent(), server.getPeerByID(gossip.getDestId()));
		} catch (Exception e) {
			this.verboseLogger.log(Level.SEVERE, "Exception thrown", e);
		}
	}
	
	public void shutdown() {
		interrupt();
	}
	
	private void terminate() {
		this.verboseLogger.info("Shutting down");
		this.loggerEndpoints.shutdown();
		this.scheduledExecutor.shutdown();
		LoggingServer.closeHandlers(verboseLogger);
		LoggingServer.closeHandlers(summaryLogger);
	}
}
