package edu.yu.cs.com3800.stage2;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import edu.yu.cs.com3800.Message;
import edu.yu.cs.com3800.Message.MessageType;
import edu.yu.cs.com3800.UDPMessageReceiver;
import edu.yu.cs.com3800.UDPMessageSender;
import edu.yu.cs.com3800.Vote;
import edu.yu.cs.com3800.ZooKeeperLeaderElection;
import edu.yu.cs.com3800.ZooKeeperPeerServer;


public class ZooKeeperPeerServerImpl extends Thread implements ZooKeeperPeerServer{
    private final InetSocketAddress myAddress;
    private final int myPort;
    private ServerState state;
    private volatile boolean shutdown;
    private final LinkedBlockingQueue<Message> outgoingMessages;
    private final LinkedBlockingQueue<Message> incomingMessages;
    private final Long id;
    private long peerEpoch;
    private volatile Vote currentLeader;
    private final Map<Long,InetSocketAddress> peerIDtoAddress;

    private final UDPMessageSender senderWorker;
    private final UDPMessageReceiver receiverWorker;

    public ZooKeeperPeerServerImpl(int myPort, long peerEpoch, Long id, Map<Long,InetSocketAddress> peerIDtoAddress){
    	this.myPort= myPort;
    	this.peerEpoch= peerEpoch;
    	this.id= id;
    	this.peerIDtoAddress= peerIDtoAddress;
    	this.outgoingMessages= new LinkedBlockingQueue<>();
    	this.incomingMessages= new LinkedBlockingQueue<>();
    	this.senderWorker= new UDPMessageSender(outgoingMessages, myPort);
		this.myAddress= new InetSocketAddress(getLocalHost(), myPort);
    	try {
			this.receiverWorker= new UDPMessageReceiver(incomingMessages, new InetSocketAddress(myPort), myPort, this);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
    }

    @Override
    public void shutdown(){
        this.shutdown = true;
        this.senderWorker.shutdown();
        this.receiverWorker.shutdown();
    }

    @Override
    public void run(){
    	this.receiverWorker.start();
    	this.senderWorker.start();

    	this.setPeerState(ServerState.LOOKING);
        try{
            while (!this.shutdown){
                switch (getPeerState()){
                    case LOOKING:
                    	ZooKeeperLeaderElection election = new ZooKeeperLeaderElection(this, incomingMessages);
                    	setCurrentLeader(election.lookForLeader());
                        break;
                    default:
                    	break;
                }
            }
        }
        catch (Exception e) {
        	e.printStackTrace();
        	this.shutdown();
        }
    }

	@Override
	public void setCurrentLeader(Vote v) throws IOException {
		this.currentLeader= v;
	}

	@Override
	public Vote getCurrentLeader() {
		return this.currentLeader;
	}

	@Override
	public void sendMessage(MessageType type, byte[] messageContents, InetSocketAddress target)
			throws IllegalArgumentException {
		Message message = new Message(type, messageContents, this.myAddress.getHostName(), this.myPort, target.getHostName(), target.getPort());
		this.outgoingMessages.add(message);
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
		return this.myPort;
	}

	@Override
	public InetSocketAddress getPeerByID(long peerId) {
		return this.getPeerByID(peerId);
	}

	@Override
	public int getQuorumSize() {
		int numberOfServers= this.peerIDtoAddress.size()+1;
		return ceilDiv(numberOfServers+1, 2);
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
			this.shutdown();
		}
		return null;
    }
}
