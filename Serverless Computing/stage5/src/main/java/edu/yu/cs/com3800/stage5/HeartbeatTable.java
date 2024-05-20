package edu.yu.cs.com3800.stage5;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;


class HeartbeatTable {
		
		static final int GOSSIP = 3000; 
		static final int FAIL = GOSSIP * 10; 
		static final int CLEANUP = FAIL * 2; 
		
		private final List<Long> ids;
		private final Random rand= new Random();
		private final Logger verboseLogger, summaryLogger;
		private final long myId;
		
		private Set<Entry> entries;
		private long heartbeat= 0;
		
		public HeartbeatTable(Set<Long> ids, long myId, Logger verboseLogger, Logger summaryLogger) {
			this.ids= new ArrayList<>(ids);
			this.verboseLogger = verboseLogger;
			this.summaryLogger= summaryLogger;
			this.myId= myId;
		}

		public synchronized void initializeTable() {
			if(entries != null) {
				throw new IllegalStateException();
			}
			entries= ids.stream().map(Entry::new).collect(Collectors.toCollection(HashSet::new));
		}
		
		public synchronized void incrementHeartbeat() {
			this.heartbeat++;
			verboseLogger.info("Incrementing heartbeat, heartbeat= " + heartbeat);
		}

		public synchronized void merge(GossipMessage gossip) {
			for(Entry entry: entries) {
				long heartbeat= gossip.getHeartbeat(entry.getId());
				entry.updateHeartbeat(heartbeat, gossip.getSrcId());
			}
		}
		
		public synchronized Set<Long> updateStateAndCleanup() {
			verboseLogger.info("Updating heartbeat table");
			
			Set<Long> failedNodeIDs= new HashSet<>();
			Set<Entry> deadNodes= new HashSet<>();
			
			for(Entry entry: this.entries) {
				HeartbeatState newState= entry.updateState();
				if(newState == HeartbeatState.CLEANUP) {
					failedNodeIDs.add(entry.getId());
				}
				
				if(newState == HeartbeatState.TERMINATED) {
					deadNodes.add(entry);
				}
			}
						
			entries.removeAll(deadNodes);
			return failedNodeIDs;
		}
		
		public synchronized GossipMessage createGossipMessage() {
			Map<Long, Long> gossip = new HashMap<>(
					this.entries.stream().filter(e -> e.getState() == HeartbeatState.RUNNING)
							.collect(Collectors.toMap(Entry::getId, Entry::getHeartbeat)));
			gossip.put(this.myId, this.heartbeat);
			return new GossipMessage(gossip, myId, getRandomID());
		}
		
		private synchronized long getRandomID() {
			List<Long> aliveIds = entries.stream().filter(e -> e.getState() == HeartbeatState.RUNNING).map(Entry::getId)
					.toList();
			if(aliveIds.size() == 0) {
				throw new IllegalStateException("All servers have failed");
			}
			int index= rand.nextInt(aliveIds.size());
			return aliveIds.get(index);
		}
		
		private enum HeartbeatState {
			RUNNING, CLEANUP, TERMINATED;
		}
		
		private class Entry {
			
			private final long id;
			
			private HeartbeatState state= HeartbeatState.RUNNING;
			private long heartbeat= 0;
			private long lastModified;
			
			private Entry(long id) {
				this.id= id;
				this.lastModified= System.currentTimeMillis();
			}
			
			private void updateHeartbeat(long newHeartbeat, long senderId) {
				if((HeartbeatState.RUNNING != state)) {
					return;
				}
				
				if(newHeartbeat > heartbeat) {					
					this.heartbeat= newHeartbeat;
					this.lastModified= System.currentTimeMillis();
					
					String info= myId + ": updated " + this.id + "'s heartbeat sequence to " + this.heartbeat + " based on message from " + senderId + " at node time " + lastModified;
					System.out.println(info);
					summaryLogger.info(info);
				}
			}
			
			private HeartbeatState updateState() {
				long currTime= System.currentTimeMillis();
				long elapsedTime= currTime - lastModified;
								
				if(HeartbeatState.RUNNING == state && elapsedTime > FAIL) {
					this.state= HeartbeatState.CLEANUP;
					
					String info= myId + ": no heartbeat from server " + this.id + "-SERVER FAILED";
					System.out.println(info);
					summaryLogger.info(info);
					return HeartbeatState.CLEANUP;
				}
				
				if(state == HeartbeatState.CLEANUP && elapsedTime > FAIL + CLEANUP) {
					this.state= HeartbeatState.TERMINATED;
					return HeartbeatState.TERMINATED;
				}
				
				return null;
			}
			
			private long getId() {
				return this.id;
			}
			
			private HeartbeatState getState() {
				return this.state;
			}
			
			private long getHeartbeat() {
				return this.heartbeat;
			}
		}
	}