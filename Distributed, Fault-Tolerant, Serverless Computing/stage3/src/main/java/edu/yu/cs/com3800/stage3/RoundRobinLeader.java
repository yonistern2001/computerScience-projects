package edu.yu.cs.com3800.stage3;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.yu.cs.com3800.LoggingServer;
import edu.yu.cs.com3800.Message;
import edu.yu.cs.com3800.Message.MessageType;

public class RoundRobinLeader implements Runnable, LoggingServer {
	
	private final ZooKeeperPeerServerImpl server;
	private final LinkedBlockingQueue<Message> incoming;
	private final List<InetSocketAddress> addrs;
	private final Map<Long, InetSocketAddress> requests;
	private final Logger logger;
	
	private int curr;
	private long requestID;
	
	public RoundRobinLeader(ZooKeeperPeerServerImpl server, Map<Long,InetSocketAddress> peerIDtoAddress, LinkedBlockingQueue<Message> incoming) {
		try {
			this.logger= initializeLogging(this.getClass().getCanonicalName() + "-on-port-" + server.getUdpPort());
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		
		this.server= server;
		this.addrs= new ArrayList<>(peerIDtoAddress.values());
		this.requests= new HashMap<>();
		this.incoming= incoming;
		this.curr= 0;
		this.requestID= 0;
	}
	
	@Override
	public void run() {
		while(!Thread.currentThread().isInterrupted()) {
			Message message;
			try {
				message= this.incoming.take();
			} catch (InterruptedException e) {
				logger.log(Level.WARNING, "Thread interrupted", e);
				return;
			}
			
			switch (message.getMessageType()) {
				case WORK:
					sendWork(message);
					break;
				case COMPLETED_WORK:
					receiveWork(message);
					break;
				default:
					throw new IllegalArgumentException("Unexpected value: " + message.getMessageType());
			}
		}
	}
	
	private void sendWork(Message message) {
		this.requests.put(requestID, new InetSocketAddress(message.getSenderHost(), message.getSenderPort()));
		InetSocketAddress targetAddress = getNextAddress();
		this.server.sendMessage(MessageType.WORK, message.getMessageContents(), targetAddress, requestID++);
		logger.fine("Sending work to: " + targetAddress);
	}

	private void receiveWork(Message message) {
		long currRequestId= message.getRequestID();
		InetSocketAddress dest= this.requests.remove(currRequestId);
		this.server.sendMessage(MessageType.COMPLETED_WORK, message.getMessageContents(), dest, currRequestId);
	}
	
	private InetSocketAddress getNextAddress() {
		InetSocketAddress currAddr= this.addrs.get(curr);
		curr= (curr+1) % this.addrs.size();
		return currAddr;
	}
}
