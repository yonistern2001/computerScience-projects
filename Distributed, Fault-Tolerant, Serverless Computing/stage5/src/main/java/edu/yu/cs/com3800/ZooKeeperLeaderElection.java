package edu.yu.cs.com3800;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import edu.yu.cs.com3800.Message.MessageType;
import edu.yu.cs.com3800.ZooKeeperPeerServer.ServerState;

public class ZooKeeperLeaderElection implements LoggingServer {
	/**
	 * time to wait once we believe we've reached the end of leader election.
	 */
	private final static int FINALIZE_WAIT = 200;

	/**
	 * Upper bound on the amount of time between two consecutive notification
	 * checks. This impacts the amount of time to get the system up again after long
	 * partitions. Currently 60 seconds.
	 */
	private final static int MAX_NOTIFICATION_INTERVAL = 60000;

	private final Logger logger;
	private final LinkedBlockingQueue<Message> incomingMessages;
	private final ZooKeeperPeerServer myPeerServer;

	private long proposedLeader;
	private long proposedEpoch;
	

	public ZooKeeperLeaderElection(ZooKeeperPeerServer server, LinkedBlockingQueue<Message> incomingMessages, Logger logger) {
		this.logger= logger;
		this.incomingMessages= incomingMessages;
		this.myPeerServer= server;
		this.proposedLeader= server.getServerId();
		this.proposedEpoch= server.getPeerEpoch();
	}

	public synchronized Vote lookForLeader() throws InterruptedException {
		this.logger.info("Starting leader election");
		Map<Long, Vote> votes= new HashMap<>();
        
		sendNotifications();
        while(!Thread.currentThread().isInterrupted()) {
            Message received = getNextMessage();
        	ElectionNotification notification= getNotificationFromMessage(received);
			boolean isUpdated= updateVote(notification, votes);
			if(isUpdated && haveEnoughVotes(votes, notification)) {
				ServerState state= notification.getState();
				if(ServerState.LEADING.equals(state) || ServerState.FOLLOWING.equals(state) || !hasHigherVote()) {
					return acceptElectionWinner(notification);
				}
			}
        }
        throw new InterruptedException();
	}
	
	private boolean hasHigherVote() {
		return this.incomingMessages.stream().map(ZooKeeperLeaderElection::getNotificationFromMessage)
				.anyMatch(n -> supersedesCurrentVote(n.getProposedLeaderID(), n.getPeerEpoch()));
	}

	private boolean updateVote(ElectionNotification notification, Map<Long, Vote> votes) {
		if(!isValidVote(notification)) {
			return false;
		}
		
		votes.put(notification.getSenderID(), notification);
		if(supersedesCurrentVote(notification.getProposedLeaderID(), notification.getPeerEpoch())) {
			this.logger.info("Higher vote received, updating vote: " + notification.getProposedLeaderID() + " > " + proposedLeader);
			this.proposedLeader= notification.getProposedLeaderID();
			this.proposedEpoch= notification.getPeerEpoch();
			sendNotifications();
		}
		return true;
	}

	private boolean isValidVote(ElectionNotification notification) {
		if(ServerState.OBSERVER == notification.getState()) {
			return false;
		}
		if(notification.getPeerEpoch() == this.proposedEpoch) {
			return notification.getProposedLeaderID() >= this.proposedLeader;
		}
		return false;
	}

	private Message getNextMessage() throws InterruptedException {
		int waitTime = FINALIZE_WAIT;
		Message received = null;

		while (received == null) {
			received = this.incomingMessages.poll(waitTime, TimeUnit.MILLISECONDS);
			sendNotifications();
			
			if (waitTime < MAX_NOTIFICATION_INTERVAL) {
				waitTime *= 2;
			}
		}

		return received;
	}

	private void sendNotifications() {
		ElectionNotification notification = new ElectionNotification(this.proposedLeader,
				this.myPeerServer.getPeerState(), this.myPeerServer.getServerId(), this.proposedEpoch);
		this.myPeerServer.sendBroadcast(MessageType.ELECTION, buildMsgContent(notification));
	}

	private Vote acceptElectionWinner(ElectionNotification n) {
		logger.info("Accepting leader: "+n.getProposedLeaderID());
		ServerState state= this.myPeerServer.getServerId() == n.getProposedLeaderID() ? ServerState.LEADING : ServerState.FOLLOWING;
		this.myPeerServer.setPeerState(state);
		
		this.incomingMessages.clear();
		this.sendNotifications();
		return n;
	}

	/*
	 * We return true if one of the following three cases hold: 1- New epoch is
	 * higher 2- New epoch is the same as current epoch, but server id is higher.
	 */
	protected boolean supersedesCurrentVote(long newId, long newEpoch) {
		return (newEpoch > this.proposedEpoch) || ((newEpoch == this.proposedEpoch) && (newId > this.proposedLeader));
	}

	/**
	 * Termination predicate. Given a set of votes, determines if have sufficient
	 * support for the proposal to declare the end of the election round. Who voted
	 * for who isn't relevant, we only care that each server has one current vote
	 */
	protected boolean haveEnoughVotes(Map<Long, Vote> votes, Vote proposal) {
		long proposedLeaderID = proposal.getProposedLeaderID();
		long numOfVotes= votes.values().stream().map(Vote::getProposedLeaderID).filter((id) -> (id == proposedLeaderID)).count()+1;
		int quorumSize= this.myPeerServer.getQuorumSize();
		if(quorumSize <= numOfVotes) {
			return true;
		}
		return false;
	}

	public static ElectionNotification getNotificationFromMessage(Message received) {
		ByteBuffer buffer= ByteBuffer.wrap(received.getMessageContents());
		long proposedID= buffer.getLong();
		ServerState state = ServerState.getServerState(buffer.getChar());
		long senderID= buffer.getLong();
		long epoch= buffer.getLong();
		ElectionNotification notification= new ElectionNotification(proposedID, state, senderID, epoch);
		return notification;
	}

	public static byte[] buildMsgContent(ElectionNotification notification) {
		long proposedID= notification.getProposedLeaderID();
		char state = notification.getState().getChar();
		long senderID= notification.getSenderID();
		long epoch= notification.getPeerEpoch();
		ByteBuffer buffer= ByteBuffer.allocate(26);
		buffer.putLong(proposedID);
		buffer.putChar(state);
		buffer.putLong(senderID);
		buffer.putLong(epoch);
		return buffer.array();
	}
}