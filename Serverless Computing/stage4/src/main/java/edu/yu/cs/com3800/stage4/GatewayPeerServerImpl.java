package edu.yu.cs.com3800.stage4;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;

public class GatewayPeerServerImpl extends ZooKeeperPeerServerImpl {

	public GatewayPeerServerImpl(int myPort, long peerEpoch, Long serverID, ConcurrentHashMap<Long, InetSocketAddress> peerIDtoAddress) {
		super(myPort, peerEpoch, serverID, peerIDtoAddress, serverID);
		super.setPeerState(ServerState.OBSERVER);
	}
}
