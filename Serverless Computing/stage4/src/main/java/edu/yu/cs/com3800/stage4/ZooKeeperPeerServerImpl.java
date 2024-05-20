package edu.yu.cs.com3800.stage4;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

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
	
	private final Logger logger;
    private final LinkedBlockingQueue<Message> outgoingMessages, incomingMessages;
    private final Map<Long,InetSocketAddress> peerIDtoAddress;
    private final List<InetSocketAddress> addrs;
    private final UDPMessageSender senderWorker;
    private final UDPMessageReceiver receiverWorker;
    private final InetSocketAddress myAddress;
    private final int udpPort, tcpPort;
    private final Long id;
    
    private volatile boolean shutdown;
    private volatile Vote currentLeader;
    private ServerState state;
    private Thread workerThread;
    private long peerEpoch;
    
    
    public ZooKeeperPeerServerImpl(int myPort, long peerEpoch, Long serverID, Map<Long,InetSocketAddress> peerIDtoAddress, Long gatewayID) {
    	this.udpPort= myPort;
    	this.tcpPort= myPort + 2;
    	this.peerEpoch= peerEpoch;
    	this.id= serverID;
    	this.peerIDtoAddress= peerIDtoAddress;
    	this.addrs= peerIDtoAddress.entrySet().stream().filter(e -> e.getKey() != gatewayID).map(Map.Entry::getValue).toList();
    	this.outgoingMessages= new LinkedBlockingQueue<>();
    	this.incomingMessages= new LinkedBlockingQueue<>();
		this.myAddress= new InetSocketAddress(getLocalHost(), myPort);
		this.senderWorker= new UDPMessageSender(outgoingMessages, myPort);
		this.state= ServerState.LOOKING;

    	try {
			this.receiverWorker= new UDPMessageReceiver(incomingMessages, new InetSocketAddress(myPort), myPort, this);
			this.logger= initializeLogging(this.getClass().getCanonicalName() + "-on-serverID-" + serverID + "-with-udpPort-" + udpPort);
			
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
    }

    @Override
    public void shutdown(){
    	logger.info("Server shutting down");
        this.shutdown = true;
        this.senderWorker.shutdown();
        this.receiverWorker.shutdown();
        LoggingServer.closeHandlers(this.logger);
    }

    @Override
    public void run(){
    	logger.fine("Starting server");
    	this.receiverWorker.start();
    	this.senderWorker.start();

        try {
            while(!this.shutdown){
                switch (getPeerState()){
                    case LOOKING:
                    	ZooKeeperLeaderElection election = new ZooKeeperLeaderElection(this, incomingMessages, logger);
                    	setCurrentLeader(election.lookForLeader());
                        break;
                    case LEADING:
                    	this.workerThread= Util.startAsDaemon(new RoundRobinLeader(this, addrs), "WorkDistributer");
                    	this.workerThread.join();
                    	break;
                    case FOLLOWING:
                    	this.workerThread= Util.startAsDaemon(new JavaRunnerFollower(this), "Worker");
                    	this.workerThread.join();;
                    	break;
                    case OBSERVER:
                    	this.observe();
                    	break;
                    default:
                    	throw new IllegalStateException();
                }
            }
        } catch(Exception e) {
        	this.logger.log(Level.SEVERE, "Exception thrown", e);
        	this.shutdown();
        }
    }
    
	private void observe() {
		logger.fine("Sending election messages");
		this.sendBroadcast(MessageType.ELECTION, ZooKeeperLeaderElection.buildMsgContent(new ElectionNotification(this.id, state,this.id , 0)));
		
    	while(!this.shutdown) {
    		Message msg;
    		try {
    			msg= this.incomingMessages.take();
    		} catch(InterruptedException e) {
    			shutdown();
    			return;
    		}
    		
			if(MessageType.ELECTION.equals(msg.getMessageType())) {
				ElectionNotification notification = ZooKeeperLeaderElection.getNotificationFromMessage(msg);
				
				if(!Objects.equals(this.currentLeader, notification)
						&& (ServerState.LEADING.equals(notification.getState()) || ServerState.FOLLOWING.equals(notification.getState()))) {
					try {
						this.setCurrentLeader(notification);
						logger.info("Found new leader: " + this.currentLeader);
					} catch (IOException e) {
						throw new UncheckedIOException(e);
					}
				}
			}
    	}
	}

	@Override
	public void setCurrentLeader(Vote v) throws IOException {
		this.currentLeader= new Vote(v.getProposedLeaderID(), v.getPeerEpoch());
	}

	@Override
	public Vote getCurrentLeader() {
		return this.currentLeader;
	}

	@Override
	public void sendMessage(MessageType type, byte[] messageContents, InetSocketAddress target)
			throws IllegalArgumentException {
		Message message = new Message(type, messageContents, this.myAddress.getHostName(), this.udpPort, target.getHostName(), target.getPort());
		putMsgOnQueue(this.outgoingMessages, message);
	}

	@Override
	public void sendBroadcast(MessageType type, byte[] messageContents) {
		for(InetSocketAddress addr: this.peerIDtoAddress.values()) {
			sendMessage(type, messageContents, addr);
		}
	}

	@Override
	public ServerState getPeerState() {
		return this.state;
	}

	@Override
	public void setPeerState(ServerState newState) {
		this.logger.info("Setting server state to " + newState.name());
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
	
	public int getTcpPort() {
		return this.tcpPort;
	}

	@Override
	public InetSocketAddress getPeerByID(long peerId) {
		return this.peerIDtoAddress.get(peerId);
	}

	@Override
	public int getQuorumSize() {
		int numberOfServers= this.addrs.size() + 1;
		return ceilDiv(numberOfServers+1, 2);
	}
	
	// return x divided by y rounded up
	private static int ceilDiv(int x, int y) {
		return (x+y-1) / y;
	}
	
	private void putMsgOnQueue(LinkedBlockingQueue<Message> queue, Message msg) {
		try {
			queue.put(msg);
		} catch (InterruptedException e) {
			this.shutdown();
		}
	}
	
    private InetAddress getLocalHost() {
    	try {
			return InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			e.printStackTrace();
			this.shutdown();
			return null;
		}
    }
}
