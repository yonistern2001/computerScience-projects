package edu.yu.cs.com3800.stage5;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import edu.yu.cs.com3800.Blocker;
import edu.yu.cs.com3800.ElectionNotification;
import edu.yu.cs.com3800.LoggingServer;
import edu.yu.cs.com3800.Message;
import edu.yu.cs.com3800.Message.MessageType;
import edu.yu.cs.com3800.UDPMessageReceiver;
import edu.yu.cs.com3800.UDPMessageSender;
import edu.yu.cs.com3800.Util;
import edu.yu.cs.com3800.Vote;
import edu.yu.cs.com3800.ZooKeeperLeaderElection;
import edu.yu.cs.com3800.ZooKeeperPeerServer;


public class ZooKeeperPeerServerImpl extends Thread implements ZooKeeperPeerServer, LoggingServer {
	
	public static void main(String[] args) throws IOException, URISyntaxException {
		long serverID= Long.parseLong(args[0]);
		long gatewayID= Long.parseLong(args[1]);
		Path addrsFilePath= Paths.get(args[2]);
		
		Map<Long, InetSocketAddress> peerIdToAddr = new HashMap<>(
				Files.readAllLines(addrsFilePath).stream().skip(1).map(l -> l.split(",")).collect(Collectors
						.toMap(a -> Long.valueOf(a[0]), a -> new InetSocketAddress(a[1], Integer.parseInt(a[2])))));
		
		InetSocketAddress serverAddr= peerIdToAddr.remove(serverID);
		
		ZooKeeperPeerServerImpl server= new ZooKeeperPeerServerImpl(serverAddr.getPort(), 0, serverID, peerIdToAddr, gatewayID);
		server.start();
	}
	
	protected final TCPSenderReceiver tcpService;
	private final Set<Long> deadPeerIDs= new HashSet<>();
	private final ConcurrentHashMap<Long, Message> results= new ConcurrentHashMap<Long, Message>();
	private final LinkedBlockingQueue<Message> outgoingMessages = new LinkedBlockingQueue<>(),
			incomingElectionMessages = new LinkedBlockingQueue<>(),
			incomingGossipMessages = new LinkedBlockingQueue<>();
    private final Map<Long,InetSocketAddress> peerIDtoAddress;
    private final Object fLock= new Object(), lLock= new Object();
    private final List<InetSocketAddress> addrs;
    private final UDPMessageSender senderWorker;
    private final UDPMessageReceiver receiverWorker;
    private final FailureDetector failureDetector;
    private final InetSocketAddress myAddress;
    private final Blocker blocker= new Blocker();
    private final Logger logger;
    private final int udpPort;
    private final Long id;
    
    protected ServerState state;
    private volatile Vote currentLeader;
    private volatile int quorumSize;
    private Thread follower, leader;
    private long peerEpoch;
	private JavaRunnerFollower worker;
	private RoundRobinLeader workDistributer;
    
    
    public ZooKeeperPeerServerImpl(int myPort, long peerEpoch, Long serverID, Map<Long,InetSocketAddress> peerIDtoAddress, Long gatewayID) {
    	this.setName("PeerServer-" + serverID);
    	this.udpPort= myPort;
    	this.peerEpoch= peerEpoch;
    	this.id= serverID;
    	this.peerIDtoAddress= peerIDtoAddress;
    	this.addrs= peerIDtoAddress.entrySet().stream().filter(e -> e.getKey() != gatewayID).map(Map.Entry::getValue).toList();
		this.myAddress= new InetSocketAddress(getLocalHost(), myPort);
		this.senderWorker= new UDPMessageSender(outgoingMessages, myPort);
		this.failureDetector= new FailureDetector(this, peerIDtoAddress.keySet(), this.incomingGossipMessages, serverID);
		this.tcpService= new TCPSenderReceiver(myAddress, myPort + 2);
    	this.state= ServerState.LOOKING;

    	try {
			this.receiverWorker= new UDPMessageReceiver(this.incomingElectionMessages, this.incomingGossipMessages, new InetSocketAddress(myPort), myPort, this);
			this.logger= initializeLogging(this.getClass().getCanonicalName() + "-on-serverID-" + serverID + "-with-udpPort-" + udpPort);
			
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
    	
    	this.updateQuorumSize();
    }

    @Override
    public void shutdown(){
    	interrupt();
    }
    
    private void terminate() {
    	logger.info("Server shutting down");
        this.senderWorker.shutdown();
        this.receiverWorker.shutdown();
        this.failureDetector.shutdown();
        try {
        	terminateRoundRobinLeader();
        	terminateRunnerFollower();
        } catch(InterruptedException e) {}
        LoggingServer.closeHandlers(this.logger);
    }

    @Override
    public void run(){
    	logger.fine("Starting server");
    	this.receiverWorker.start();
    	this.senderWorker.start();
    	this.failureDetector.start();
    	blocker.block();

		try {
			while (!isInterrupted()) {
				switch (getPeerState()) {
				case LOOKING:
					this.peerEpoch++;
					ZooKeeperLeaderElection election = new ZooKeeperLeaderElection(this, incomingElectionMessages, logger);
					setCurrentLeader(election.lookForLeader());
					break;
				case LEADING:
					terminateRunnerFollower();
					synchronized (lLock) {
						workDistributer = new RoundRobinLeader(this, addrs, tcpService, results);
						this.leader = Util.startAsDaemon(workDistributer, "WorkDistributer");
					}
					waitForStateChange(ServerState.LEADING);
					break;
				case FOLLOWING:
					synchronized (fLock) {
						if (this.follower == null || this.follower.getState() == State.TERMINATED) {
							this.worker = new JavaRunnerFollower(this, tcpService, results);
							this.follower = Util.startAsDaemon(worker, "Worker");
						}
					}
					waitForStateChange(ServerState.FOLLOWING);
					break;
				case OBSERVER:
					this.observe();
					break;
				default:
					throw new IllegalStateException();
				}
			}
		} catch (InterruptedException o) {
			this.logger.info("Thread interrupted");
		} catch (Exception e) {
			this.logger.log(Level.SEVERE, "Exception thrown", e);
		} finally {
			this.terminate();
		}
    }

	private void terminateRunnerFollower() throws InterruptedException {
		synchronized (fLock) {
			if (this.follower != null) {
				this.worker.shutdown();
				this.follower.join();
				this.follower = null;
				this.worker = null;
			}
		}
	}
	
	private void terminateRoundRobinLeader() throws InterruptedException {
		synchronized (lLock) {
			if (this.leader != null) {
				this.workDistributer.shutdown();
				this.leader.join();
				this.leader = null;
				this.workDistributer = null;
			}
		}
	}

	private synchronized void waitForStateChange(ServerState currentState) throws InterruptedException {
		while(getPeerState() == currentState) {
			wait();
		}
	}
    
	private void observe() throws InterruptedException, IOException {
    	while(!isInterrupted()) {
    		blocker.awaitIfUnblocked();
    		
    		this.listenForNewLeader();
    	}
	}

	private void listenForNewLeader() throws InterruptedException, IOException {
		this.logger.info("Listening for new leader");
		ScheduledExecutorService executor= new ScheduledThreadPoolExecutor(1);
		executor.scheduleAtFixedRate(this::sendNotifications, 0, 2000, TimeUnit.MILLISECONDS);

		try {
			while (!interrupted()) {
				Message msg = this.incomingElectionMessages.take();

				ElectionNotification notification = ZooKeeperLeaderElection.getNotificationFromMessage(msg);

				if (this.peerEpoch < notification.getPeerEpoch() && (ServerState.LEADING == notification.getState()
						|| ServerState.FOLLOWING == notification.getState())) {
					this.peerEpoch = notification.getPeerEpoch();
					this.setCurrentLeader(notification);
					logger.info("Found new leader: " + this.currentLeader);
					return;
				}
			}
		} finally {
			executor.shutdown();
		}
	}

	private void sendNotifications() {
		logger.fine("Sending election messages");
		ElectionNotification notification = new ElectionNotification(this.id, state, this.id , this.peerEpoch);
		this.sendBroadcast(MessageType.ELECTION, ZooKeeperLeaderElection.buildMsgContent(notification));
	}

	@Override
	public void setCurrentLeader(Vote v) throws IOException {
		this.currentLeader= new Vote(v.getProposedLeaderID(), v.getPeerEpoch());
		blocker.unblock();
	}

	@Override
	public Vote getCurrentLeader() {
		return this.currentLeader;
	}
	
	public Vote getCurrentLeaderBlocking() {
		blocker.awaitIfBlocked();
		Vote leader= this.currentLeader;
		if(Objects.isNull(leader)) {
			leader= getCurrentLeaderBlocking();
		}
		return leader;
	}

	@Override
	public void sendMessage(MessageType type, byte[] messageContents, InetSocketAddress target)
			throws IllegalArgumentException {
		Message message = new Message(type, messageContents, this.myAddress.getHostName(), this.udpPort, target.getHostName(), target.getPort());
		try {
			this.outgoingMessages.put(message);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	@Override
	public void sendBroadcast(MessageType type, byte[] messageContents) {
		for(InetSocketAddress addr: this.peerIDtoAddress.values()) {
			sendMessage(type, messageContents, addr);
		}
	}

	@Override
	public synchronized ServerState getPeerState() {
		return this.state;
	}

	@Override
	public synchronized void setPeerState(ServerState newState) {
		String info = this.id + ": switching from " + this.state + " to " + newState;
		System.out.println(info);
		this.failureDetector.summaryLogger.info(info);
		
		this.logger.info("Changing state from " + state + " to " + newState);
		
		notifyAll();
		
		this.state= newState;
	}

	@Override
	public Long getServerId() {
		return this.id;
	}

	@Override
	public long getPeerEpoch() {
		return this.peerEpoch;
	}

	@Override
	public InetSocketAddress getAddress() {
		return this.myAddress;
	}

	@Override
	public int getUdpPort() {
		return this.udpPort;
	}

	@Override
	public InetSocketAddress getPeerByID(long peerId) {
		return this.peerIDtoAddress.get(peerId);
	}

	@Override
	public int getQuorumSize() {
		return this.quorumSize;
	}

	private synchronized void updateQuorumSize() {
		int numberOfServers= this.addrs.size() + 1 - this.deadPeerIDs.size();
		this.quorumSize= ceilDiv(numberOfServers + 1, 2);
		logger.fine("Updated quorum size to: " + this.quorumSize);
	}
	
	@Override
	public synchronized void reportFailedPeer(long peerID) {
		this.logger.info("Server with ID " + peerID + " is down");
		
		this.deadPeerIDs.add(peerID);
		this.updateQuorumSize();
		
		this.tcpService.reportFailedNode(getPeerByID(peerID));
		
		if(currentLeader != null && peerID == currentLeader.getProposedLeaderID()) {
			blocker.block();
			this.currentLeader= null;
			if(this.state != ServerState.OBSERVER) {
				this.setPeerState(ServerState.LOOKING);				
			}
		}
	}
	
	@Override
	public synchronized boolean isPeerDead(long peerID) {
		return this.deadPeerIDs.contains(peerID);
	}
	
	@Override
	public synchronized boolean isPeerDead(InetSocketAddress address) {
		for(long id: this.deadPeerIDs) {
			InetSocketAddress deadNodeAddr= this.peerIDtoAddress.get(id);
			if(deadNodeAddr.equals(address)) {
				return true;
			}
		}
		return false;
	}
	
	// return x divided by y rounded up
	private static int ceilDiv(int x, int y) {
		return (x+y-1) / y;
	}
	
    private InetAddress getLocalHost() {
    	try {
			return InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return null;
		}
    }
}
