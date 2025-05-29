package com.CPWsoft.ServerCore;

import java.util.Set;

import com.CPWsoft.lwjgl3.Lwjgl3Launcher;
import com.utils.OtherUtils;

import redis.clients.jedis.Jedis;

public class AllReadyTimerThread implements Runnable {
	
	private String room_id;
	private Jedis jedis;
	
	public AllReadyTimerThread(String room_id, Jedis jedis){
		this.room_id = room_id;
		this.jedis = jedis;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		float elapsed_time = 0;
		float target_time = Float.parseFloat(jedis.hget(room_id, "target_time"));
		
		while(elapsed_time < target_time) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(interrupted()) {
				System.out.println("Timer thread closed");
				closeAnyResources();
				return;
			}
			elapsed_time = incrementTime();
		}
		
		startGame();
		System.out.println("Timer thread closed");
		closeAnyResources();
		
	}
	
	private float incrementTime() {
		float elapsed_time = Float.parseFloat(jedis.hget(room_id, "elapsed_time"));
		elapsed_time += 0.1f;
		jedis.hset(room_id, "elapsed_time", String.valueOf(elapsed_time));
		
		return elapsed_time;
	}
	
	private boolean interrupted() {
		return jedis.hget(room_id, "timer_interrupt").equals("t");
	}
	
	private void startGame() {
		String game_id = OtherUtils.generateID();
		
		jedis.hset(room_id, "game_id", game_id);
		
		jedis.hset(game_id, "room_id", room_id);
		jedis.hset(game_id, "input_index", "0");
		jedis.sadd("games_set", game_id);
		
		Set<String> players = jedis.smembers(room_id + ":players");
		
		for(String username: players) {
			jedis.rpush(game_id + ":players", username);
		}
		
		Lwjgl3Launcher.createHeadlessApplication(game_id, ServerThread.getResource());
	}
	
	private void closeAnyResources() {
		jedis.close();
	}
	
}
