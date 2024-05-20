package edu.yu.cs.com3800.stage5;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.Test;

import edu.yu.cs.com3800.Message;
import edu.yu.cs.com3800.Message.MessageType;
import edu.yu.cs.com3800.Vote;
import edu.yu.cs.com3800.ZooKeeperPeerServer;

public class Stage5Test {
	
	@Test
	public void testFailureDetector() throws InterruptedException {
		Map<Long, InetSocketAddress> idToaddrs= Map.of(10L, new InetSocketAddress("a", 1), 20L, new InetSocketAddress("b", 2), 30L, new InetSocketAddress("c", 3));
		Map<InetSocketAddress, BlockingQueue<Message>> messages= idToaddrs.values().stream().collect(Collectors.toMap(Function.identity(), a -> new LinkedBlockingQueue<Message>()));
		
		FakeServer server1= new FakeServer(idToaddrs, messages, 10000);
		FakeServer server2= new FakeServer(idToaddrs, messages, 10010);
		FakeServer server3= new FakeServer(idToaddrs, messages, 10020);
		
		Set<Long> server1IDs = new HashSet<>(idToaddrs.keySet());
		server1IDs.remove(10L);
		
		Set<Long> server2IDs =  new HashSet<>(idToaddrs.keySet());
		server2IDs.remove(20L);
		
		Set<Long> server3IDs = new HashSet<>(idToaddrs.keySet());
		server3IDs.remove(30L);
		
		FailureDetector detector1= new FailureDetector(server1, server1IDs, messages.get(idToaddrs.get(10L)), 10L);
		FailureDetector detector2= new FailureDetector(server2, server2IDs, messages.get(idToaddrs.get(20L)), 20L);
		FailureDetector detector3= new FailureDetector(server3, server3IDs, messages.get(idToaddrs.get(30L)), 30L);
		
		detector1.start();
		detector2.start();
		detector3.start();
		
		Thread.sleep(2000);
		
		detector3.shutdown();
		
		Thread.sleep(50000);
		
		HashSet<Long> expected= new HashSet<>();
		expected.add(30L);
		
		assertEquals(expected, server1.getDeadNodes());
		assertEquals(expected, server2.getDeadNodes());

		detector1.shutdown();
		detector2.shutdown();
	}
	
	private static class FakeServer implements ZooKeeperPeerServer {

		private final Set<Long> deadNodes= Collections.synchronizedSet(new HashSet<>());
		private final Map<Long, InetSocketAddress> idToAddress;
		private final Map<InetSocketAddress, BlockingQueue<Message>> messages;
		private final int port;
		
		public FakeServer(Map<Long, InetSocketAddress> idToAddress, Map<InetSocketAddress, BlockingQueue<Message>> messages, int port) {
			this.idToAddress= idToAddress;
			this.messages= messages;
			this.port= port;
		}

		@Override
		public void shutdown() {}

		@Override
		public void setCurrentLeader(Vote v) throws IOException {}

		@Override
		public Vote getCurrentLeader() {return null;}

		@Override
		public void sendMessage(MessageType type, byte[] messageContents, InetSocketAddress target)
				throws IllegalArgumentException {
			Message msg= new Message(type, messageContents, "", 0, target.getHostName(), target.getPort());
			BlockingQueue<Message> sender= this.messages.get(target);
			try {
				sender.put(msg);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void sendBroadcast(MessageType type, byte[] messageContents) {}

		@Override
		public ServerState getPeerState() {return null;}

		@Override
		public void setPeerState(ServerState newState) {}

		@Override
		public Long getServerId() {return null;}

		@Override
		public long getPeerEpoch() {return 0;}

		@Override
		public InetSocketAddress getAddress() {return null;}

		@Override
		public int getUdpPort() {
			return this.port;
		}

		@Override
		public InetSocketAddress getPeerByID(long peerId) {
			return this.idToAddress.get(peerId);
		}

		@Override
		public int getQuorumSize() {return 0;}
		
		@Override
		public void reportFailedPeer(long peerID) {
			this.deadNodes.add(peerID);
		}
		
		public Set<Long> getDeadNodes() {
			return this.deadNodes;
		}
	}
	
	
	
	@Test
	public void testRunCode() throws Exception {
		new Stage5PeerServerRunnerTest();
	}
	
	public static class Stage5PeerServerRunnerTest {
	    private String validClass = "package edu.yu.cs.fall2019.com3800.stage1;\n\npublic class HelloWorld\n{\n    public String run()\n    {\n        return \"Hello world!\";\n    }\n}\n";

	    private int[] ports = {8010, 8020, 8030, 8040, 8050, 8060, 8070, 8080, 8090};

	    private int myPort = 9999;
	    private ArrayList<ZooKeeperPeerServer> servers;

		private GatewayServer gateway;

	    public Stage5PeerServerRunnerTest() throws Exception {

	        createServers();
	        
	        Thread.sleep(5000);
	        
	        ZooKeeperPeerServer server= servers.remove(0);
	        server.shutdown();
	        
	        Thread.sleep(2000);
	        
	        ZooKeeperPeerServer leader= servers.remove(servers.size() - 2);
	        leader.shutdown();
	        
	        for(int i= 0; i < 8; i++) {	        	
	        	sendCompileAndRunRequest(validClass.replaceFirst("Hello world!", "Hello world-" + i), 200 , "Hello world-" + i);
	        }

	    	String invalidClass= validClass + ":";
	    	sendCompileAndRunRequest(invalidClass, 400, "Code did not compile:");
	        stopServers();
	    }

	    private void stopServers() {
	    	this.servers.forEach(ZooKeeperPeerServer::shutdown);
	    	this.gateway.shutdown();
	    }

		private void createServers() throws IOException {
	        HashMap<Long, InetSocketAddress> peerIDtoAddress = new HashMap<>(9);
	        final long gatewayId= this.ports.length-1;
	        for (int i = 0; i < this.ports.length; i++) {
	            peerIDtoAddress.put(Integer.valueOf(i).longValue(), new InetSocketAddress("localhost", this.ports[i]));
	        }
	        	        
	        GatewayPeerServerImpl gatewayServer= null;
	        
	        this.servers = new ArrayList<>();
			for (Map.Entry<Long, InetSocketAddress> entry : peerIDtoAddress.entrySet()) {
	            ConcurrentHashMap<Long, InetSocketAddress> map = new ConcurrentHashMap<>(peerIDtoAddress);
	            map.remove(entry.getKey());
	            
	            ZooKeeperPeerServerImpl server;
	            if(entry.getKey() == gatewayId) {
	            	gatewayServer= new GatewayPeerServerImpl(entry.getValue().getPort(), 0, entry.getKey(), map);
	            	server= gatewayServer;
	            } else {
	            	server= new ZooKeeperPeerServerImpl(entry.getValue().getPort(), 0, entry.getKey(), map, gatewayId);
	            }
	            this.servers.add(server);
	            server.start();
	        }
	        
	        this.gateway= new GatewayServer(myPort, gatewayServer);
	        this.gateway.start();
	    }
	    
	    public void sendCompileAndRunRequest(String src, int rCode, String expected) throws IOException, URISyntaxException {
	    	HttpClient client= HttpClient.newHttpClient();
	    	HttpRequest request= HttpRequest.newBuilder().header("Content-Type", "text/x-java-source").uri(new URL("http", "localhost", this.myPort, "/compileandrun").toURI()).POST(BodyPublishers.ofString(src)).build();
	    	HttpResponse<String> response= null;
	    	
	    	try {
	    		response= client.send(request, BodyHandlers.ofString());
	    	} catch(InterruptedException e) {
	    		System.out.println(e.getMessage());
	    	}
	    	System.out.println(response.body());
	    	
	    	assertEquals(rCode, response.statusCode());
	    	assertTrue(response.body().startsWith(expected));
	    }
	}
	
}