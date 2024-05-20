package edu.yu.cs.com3800;

public class Blocker {
	
	private boolean blocked= false;
	
	public synchronized void block() {
		this.blocked= true;
		this.notifyAll();
	}
	
	public synchronized void unblock() {
		this.blocked= false;
		this.notifyAll();
	}
	
	public synchronized void awaitIfBlocked() {
		while(this.blocked) {
			try {
				wait();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}
	
	public synchronized void awaitIfUnblocked() throws InterruptedException {
		while(!this.blocked) {
			wait();
		}
	}
}
