package edu.yu.cs.com3800.stage2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import edu.yu.cs.com3800.Vote;
import edu.yu.cs.com3800.ZooKeeperPeerServer;
import edu.yu.cs.com3800.ZooKeeperPeerServer.ServerState;

public class Stage2Test {
	
	@Test
	public void test() {
		tradeMessages();
	}
	

    public static void tradeMessages() {
        //create IDs and addresses
        HashMap<Long, InetSocketAddress> peerIDtoAddress = new HashMap<>(3);
        String host= "localhost";
        peerIDtoAddress.put(1L, new InetSocketAddress(host, 8010));
        peerIDtoAddress.put(2L, new InetSocketAddress(host, 8020));
        peerIDtoAddress.put(3L, new InetSocketAddress(host, 8030));
        peerIDtoAddress.put(4L, new InetSocketAddress(host, 8040));
        peerIDtoAddress.put(5L, new InetSocketAddress(host, 8050));
        peerIDtoAddress.put(6L, new InetSocketAddress(host, 8060));
        peerIDtoAddress.put(7L, new InetSocketAddress(host, 8070));
        peerIDtoAddress.put(8L, new InetSocketAddress(host, 8080));
        peerIDtoAddress.put(9L, new InetSocketAddress(host, 8090));
        peerIDtoAddress.put(10L, new InetSocketAddress(host, 9000));



        //create servers
        List<ZooKeeperPeerServer> servers = new ArrayList<>(peerIDtoAddress.size());
        for (Map.Entry<Long, InetSocketAddress> entry : peerIDtoAddress.entrySet()) {
            HashMap<Long, InetSocketAddress> map = (HashMap<Long, InetSocketAddress>) peerIDtoAddress.clone();
            map.remove(entry.getKey());
            ZooKeeperPeerServerImpl server = new ZooKeeperPeerServerImpl(entry.getValue().getPort(), 0, entry.getKey(), map);
            servers.add(server);
            new Thread(server, "Server on port " + server.getAddress().getPort()).start();
            assertEquals(6, server.getQuorumSize());
        }
        //wait for threads to start
        try {
            Thread.sleep(2000);
        }
        catch (Exception e) {
        	e.printStackTrace();
        	fail();
        }
        
        
        int leaderID = servers.size();
        //print out the leaders and shutdown
        for (ZooKeeperPeerServer server : servers) {
            Vote leader = server.getCurrentLeader();
            if (leader != null) {
                System.out.println("Server on port " + server.getAddress().getPort() + " whose ID is " + server.getServerId() + " has the following ID as its leader: " + leader.getProposedLeaderID() + " and its state is " + server.getPeerState().name());
            }
            
            assertEquals(leaderID, leader.getProposedLeaderID());
            if(server.getServerId() == leaderID) {
                assertEquals(ServerState.LEADING, server.getPeerState());
            } else {
                assertEquals(ServerState.FOLLOWING, server.getPeerState());
            }
            server.shutdown();
        }
    }
}
