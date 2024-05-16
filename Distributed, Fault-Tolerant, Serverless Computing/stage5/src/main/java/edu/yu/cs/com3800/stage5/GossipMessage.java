package edu.yu.cs.com3800.stage5;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

class GossipMessage {
	private final Map<Long, Long> gossip;
	private final long srcId, destId;
	
	public GossipMessage(Map<Long, Long> gossip, long srcId, long destId) {
		this.gossip= gossip;
		this.srcId= srcId;
		this.destId= destId;
	}
	
	public GossipMessage(byte[] content) {		
		ByteBuffer buffer= ByteBuffer.wrap(content);
		int numberOfEntries= buffer.getInt();
		this.srcId= buffer.getLong();
		this.destId= buffer.getLong();
				
		this.gossip= new HashMap<>();
		
		for(int i= 0; i < numberOfEntries; i++) {
			long key= buffer.getLong();
			long val= buffer.getLong();
			gossip.put(key, val);
		}
	}
	
	public byte[] createMsgContent() {
		ByteBuffer buffer= ByteBuffer.allocate(gossip.size() * 2 * 8 + 20);
		buffer.putInt(gossip.size());
		buffer.putLong(srcId);
		buffer.putLong(destId);
		for(Map.Entry<Long, Long> entry: gossip.entrySet()) {
			buffer.putLong(entry.getKey());
			buffer.putLong(entry.getValue());
		}
		return buffer.array();
	}
	
	public long getHeartbeat(long id) {
		return this.gossip.getOrDefault(id, -1L);
	}
	
	public long getSrcId() {
		return this.srcId;
	}
	
	public long getDestId() {
		return this.destId;
	}
	
	@Override
	public String toString() {
		StringBuilder builder= new StringBuilder();
		builder.append("Sender ID: ");
		builder.append(srcId);
		builder.append("\nReceiver ID: ");
		builder.append(destId);
		
		for(Map.Entry<Long, Long> entry: gossip.entrySet()) {
			builder.append("\nID: ");
			builder.append(entry.getKey());
			builder.append("  Heartbeat: ");
			builder.append(entry.getValue());
		}
		
		return builder.toString();
	}
}