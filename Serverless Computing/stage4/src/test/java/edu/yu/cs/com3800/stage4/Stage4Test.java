package edu.yu.cs.com3800.stage4;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


import org.junit.Test;

import edu.yu.cs.com3800.ZooKeeperPeerServer;

public class Stage4Test {
	@Test
	public void testRunCode() throws Exception {
		new Stage4PeerServerRunnerTest();
	}
	
	public static class Stage4PeerServerRunnerTest {
	    private String validClass = "package edu.yu.cs.fall2019.com3800.stage1;\n\npublic class HelloWorld\n{\n    public String run()\n    {\n        return \"Hello world!\";\n    }\n}\n";

	    private int[] ports = {8010, 8020, 8030, 8040, 8050, 8060, 8070, 8080, 8090};

	    private int myPort = 9999;
	    private ArrayList<ZooKeeperPeerServer> servers;

		private GatewayServer gateway;

	    public Stage4PeerServerRunnerTest() throws Exception {

	        createServers();
	        
	        try {
	            Thread.sleep(5000);
	        }
	        catch (InterruptedException e) {
	        }
	        
	    	sendCompileAndRunRequest(validClass, 200 , "Hello world!");
	    	String validClass2= validClass.replaceAll(" world", " World");
	    	sendCompileAndRunRequest(validClass2, 200, "Hello World!");
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
	        final long gatewayId= 20;
	        for (int i = 0; i < this.ports.length; i++) {
	            peerIDtoAddress.put(Integer.valueOf(i).longValue(), new InetSocketAddress("localhost", this.ports[i]));
	        }
	        	        
	        GatewayPeerServerImpl gatewayServer= null;
	        
	        this.servers = new ArrayList<>();
			for (Map.Entry<Long, InetSocketAddress> entry : peerIDtoAddress.entrySet()) {
	            ConcurrentHashMap<Long, InetSocketAddress> map = new ConcurrentHashMap<>(peerIDtoAddress);
	            map.remove(entry.getKey());
	            ZooKeeperPeerServerImpl server;
	            if(entry.getKey() == this.ports.length-1) {
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