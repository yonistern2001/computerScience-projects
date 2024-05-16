package edu.yu.cs.com3800.stage3;

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
import edu.yu.cs.com3800.Util;
import edu.yu.cs.com3800.Vote;
import edu.yu.cs.com3800.ZooKeeperLeaderElection;
import edu.yu.cs.com3800.ZooKeeperPeerServer;


public class ZooKeeperPeerServerImpl extends Thread implements ZooKeeperPeerServer {
	
    private final LinkedBlockingQueue<Message> outgoingMessages, incomingMessages;
    private final Map<Long,InetSocketAddress> peerIDtoAddress;
    private final UDPMessageSender senderWorker;
    private final UDPMessageReceiver receiverWorker;
    private final InetSocketAddress myAddress;
    private final int myPort;
    private final Long id;
    
    private volatile boolean shutdown;
    private volatile Vote currentLeader;
    private ServerState state;
    private Thread workerThread;
    private long peerEpoch;
    
    
    public ZooKeeperPeerServerImpl(int myPort, long peerEpoch, Long id, Map<Long,InetSocketAddress> peerIDtoAddress){
    	this.myPort= myPort;
    	this.peerEpoch= peerEpoch;
    	this.id= id;
    	this.peerIDtoAddress= peerIDtoAddress;
    	this.outgoingMessages= new LinkedBlockingQueue<>();
    	this.incomingMessages= new LinkedBlockingQueue<>();
		this.myAddress= new InetSocketAddress(getLocalHost(), myPort);
		this.senderWorker= new UDPMessageSender(outgoingMessages, myPort);
		
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
        if(this.workerThread != null) {
        	this.workerThread.interrupt();
        }
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
                    case LEADING:
                    	this.workerThread= Util.startAsDaemon(new RoundRobinLeader(this, peerIDtoAddress, incomingMessages), "WorkDistributer");
                    	checkState();
                    	break;
                    case FOLLOWING:
                    	this.workerThread= Util.startAsDaemon(new JavaRunnerFollower(this, incomingMessages), "Worker");
                    	checkState();
                    	break;
                    default:
                    	throw new IllegalStateException();
                }
            }
        }
        catch (Exception e) {
        	e.printStackTrace();
        	this.shutdown();
        }
    }
    
    private void checkState() {
    	while(!this.shutdown) {
    		try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				this.shutdown();;
				return;
			}
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
		this.sendMessage(type, messageContents, target, -1);
	}
	
	public void sendMessage(MessageType type, byte[] messageContents, InetSocketAddress target, long id)
			throws IllegalArgumentException {
		Message message = new Message(type, messageContents, this.myAddress.getHostName(), this.myPort, target.getHostName(), target.getPort(), id);
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
		}
		return null;
    }
}
