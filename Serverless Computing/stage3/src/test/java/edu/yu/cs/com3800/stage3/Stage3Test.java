package edu.yu.cs.com3800.stage3;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import org.junit.Test;

import edu.yu.cs.com3800.Message;
import edu.yu.cs.com3800.UDPMessageReceiver;
import edu.yu.cs.com3800.UDPMessageSender;
import edu.yu.cs.com3800.Util;
import edu.yu.cs.com3800.Vote;
import edu.yu.cs.com3800.ZooKeeperPeerServer;

public class Stage3Test {
	@Test
	public void testRunCode() throws Exception {
		new Stage3PeerServerRunnerTest();
	}
	
	public static class Stage3PeerServerRunnerTest {
	    private String validClass = "package edu.yu.cs.fall2019.com3800.stage1;\n\npublic class HelloWorld\n{\n    public String run()\n    {\n        return \"Hello world!\";\n    }\n}\n";

	    private LinkedBlockingQueue<Message> outgoingMessages;
	    private LinkedBlockingQueue<Message> incomingMessages;
	    private int[] ports = {8010, 8020, 8030, 8040, 8050, 8060, 8070, 8080};
	    //private int[] ports = {8010, 8020};
	    private int leaderPort = this.ports[this.ports.length - 1];
	    private int myPort = 9999;
	    private InetSocketAddress myAddress = new InetSocketAddress("localhost", this.myPort);
	    private ArrayList<ZooKeeperPeerServer> servers;

	    public Stage3PeerServerRunnerTest() throws Exception {
	        //step 1: create sender & sending queue
	        this.outgoingMessages = new LinkedBlockingQueue<>();
	        UDPMessageSender sender = new UDPMessageSender(this.outgoingMessages,this.myPort);
	        //step 2: create servers
	        createServers();
	        //step2.1: wait for servers to get started
	        try {
	            Thread.sleep(5000);
	        }
	        catch (InterruptedException e) {
	        }
	        printLeaders();
	        //step 3: since we know who will win the election, send requests to the leader, this.leaderPort
	        for (int i = 0; i < this.ports.length; i++) {
	            String code = this.validClass.replace("world!", "world! from code version " + i);
	            sendMessage(code);
	        }
	        Util.startAsDaemon(sender, "Sender thread");
	        this.incomingMessages = new LinkedBlockingQueue<>();
	        UDPMessageReceiver receiver = new UDPMessageReceiver(this.incomingMessages, this.myAddress, this.myPort,null);
	        Util.startAsDaemon(receiver, "Receiver thread");
	        //step 4: validate responses from leader

	        checkResponses();

	        //step 5: stop servers
	        stopServers();
	    }

	    private void printLeaders() {
	        for (ZooKeeperPeerServer server : this.servers) {
	            Vote leader = server.getCurrentLeader();
	            assertEquals(this.ports.length-1, leader.getProposedLeaderID());
	        }
	    }

	    private void stopServers() {
	    	this.servers.forEach(ZooKeeperPeerServer::shutdown);
	    }

	    private void checkResponses() throws Exception {
	        for (int i = 0; i < this.ports.length; i++) {
	            Message msg = this.incomingMessages.take();
	            long requestId= msg.getRequestID();
	            String response = new String(msg.getMessageContents());
	            assertEquals("Hello world! from code version " + requestId, response);
	        }
	    }

	    private void sendMessage(String code) throws InterruptedException {
	        Message msg = new Message(Message.MessageType.WORK, code.getBytes(), this.myAddress.getHostString(), this.myPort, "localhost", this.leaderPort);
	        this.outgoingMessages.put(msg);
	    }

	    @SuppressWarnings("unchecked")
		private void createServers() throws IOException {
	        //create IDs and addresses
	        HashMap<Long, InetSocketAddress> peerIDtoAddress = new HashMap<>(8);
	        for (int i = 0; i < this.ports.length; i++) {
	            peerIDtoAddress.put(Integer.valueOf(i).longValue(), new InetSocketAddress("localhost", this.ports[i]));
	        }
	        //create servers
	        this.servers = new ArrayList<>(3);
	        for (Map.Entry<Long, InetSocketAddress> entry : peerIDtoAddress.entrySet()) {
	            HashMap<Long, InetSocketAddress> map = (HashMap<Long, InetSocketAddress>) peerIDtoAddress.clone();
	            map.remove(entry.getKey());
	            ZooKeeperPeerServerImpl server = new ZooKeeperPeerServerImpl(entry.getValue().getPort(), 0, entry.getKey(), map);
	            this.servers.add(server);
	            server.start();
	        }
	    }
	}
}