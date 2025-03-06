package com.CPWsoft.Threads;

public class AllReadyTimerThread implements Runnable {
	private float time = 10;
	private Room room;
	
	public AllReadyTimerThread(Room room) {
		this.room = room;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		System.out.println("Timer started");
		while(time >= 0) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(!room.isAllReady() || room.getTimer() != this) {
				System.out.println("Timer thread closed");
				return;
			}
			decrementTime();
		}
		
		room.startGame();
		System.out.println("started game");
	}
	
	public synchronized float getTime() {
		return time;
	}
	
	private synchronized void decrementTime() {
		time -= 0.1;
	}
	
}
