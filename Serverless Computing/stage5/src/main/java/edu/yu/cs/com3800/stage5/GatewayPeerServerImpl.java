package edu.yu.cs.com3800.stage5;

import java.net.InetSocketAddress;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class GatewayPeerServerImpl extends ZooKeeperPeerServerImpl {

	private final Set<Long> ids;

	public GatewayPeerServerImpl(int myPort, long peerEpoch, Long serverID, ConcurrentHashMap<Long, InetSocketAddress> peerIDtoAddress) {
		super(myPort, peerEpoch, serverID, peerIDtoAddress, serverID);
		super.state= ServerState.OBSERVER;
		this.ids= peerIDtoAddress.keySet();
	}
	
	public Set<Long> getIds() {
		return this.ids;
	}
}
