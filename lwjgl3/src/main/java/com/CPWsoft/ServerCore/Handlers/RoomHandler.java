package com.CPWsoft.ServerCore.Handlers;

import com.CPWsoft.ServerCore.AllReadyTimerThread;
import com.CPWsoft.ServerCore.ServerThread;
import com.JedisManagement.JedisUtils;
import com.utils.ParsingUtils;

import lombok.Getter;
import redis.clients.jedis.Jedis;

public class RoomHandler implements Handler {
	@Getter(lazy = true)
	private static final RoomHandler instance = initializeInstance();
	
	@Override
	public String handle(int start, int end, String requests, String username, Jedis jedis) {
		String room_id = jedis.hget(username, "room_id");
		if(room_id.equals("_None_")) return "f";
		
		if(requests.charAt(start + 1) == 'i') {
			initializeRoomState(username, jedis);
			return "p";
		}
		
		if(requests.charAt(start + 1) == 'b') {
			leaveRoom(username, room_id, jedis);
			return "p";
		}
		if(requests.charAt(start + 1) == 'n') {
			return "p" + getRoomSize(username, room_id, jedis);
		}
		if(requests.charAt(start + 1) == 'h' && end - start >= 3) {
			int chatIndex = 0;
			try {
				chatIndex = ParsingUtils.parseInt(start + 2, end, requests);
			} catch (Exception e) {
				// TODO: handle exception
				return "f";
			}
			
			if(chatIndex >= getChatSize(room_id, jedis)) return "f";
			
			return "p" + getChatEntries(room_id, chatIndex, jedis);
		}
		if(requests.charAt(start + 1) == 'u') {
			if(!hasInroomDataChanged(username, jedis)) return "f";
			
			return "p" + getPlayersList(room_id, jedis);
		} 
		if(requests.charAt(start + 1) == 'm' && end - start >= 3) {
			String message = requests.substring(start + 2, end);
			addChat(room_id, username, message, jedis);
			
			return "p";
		}
		if(requests.charAt(start + 1) == 'r') {
			boolean current_value = toggleReady(username, jedis);
			
			if(current_value && areAllReadyAfterToggle(room_id, jedis)) {
				startTimer(room_id, jedis);
				
				return "pt";
			}
			
			interruptTimer(room_id, jedis);
			
			return "pf";
		}
		if(requests.charAt(start + 1) == 'g') {
			if(!areAllReady(room_id, jedis)) return "f1";
			
			if(!hasGameStarted(room_id, jedis)) return "f2" + getRemainingTime(room_id, jedis);
			
			return "p";
		}
		
		return "f";
	}
	
	private static RoomHandler initializeInstance() {
		return new RoomHandler();
	}
	
	
	
	private void initializeRoomState(String username, Jedis jedis) {
		jedis.hset(username, "inroom_data_changed", "t");
		jedis.hset(username, "ready_for_game", "f");
	}
	
	private void leaveRoom(String username, String room_id, Jedis jedis) {
		JedisUtils.leaveRoom(username, room_id, jedis);
	}
	
	private long getRoomSize(String username, String room_id, Jedis jedis) {
		return jedis.scard(room_id + ":players");
	}
	
	private long getChatSize(String room_id, Jedis jedis) {
		return jedis.llen(room_id+ ":chat");
	}
	
	private String getChatEntries(String room_id, long chatIndex, Jedis jedis) {
		return JedisUtils.getChatEntries(room_id, chatIndex, jedis);
	}
	
	private void addChat(String room_id, String username, String message, Jedis jedis) {
		JedisUtils.addChat(room_id, username, message, jedis);
	}
	
	private boolean hasInroomDataChanged(String username, Jedis jedis) {
		return jedis.hget(username, "inroom_data_changed").equals("t");
	}
	
	private String getPlayersList(String room_id, Jedis jedis) {
		return JedisUtils.getPlayersList(room_id, jedis);
	}
	
	private boolean toggleReady(String username, Jedis jedis) {
		boolean old_value = jedis.hget(username, "ready_for_game").equals("t");
		
		jedis.hset(username, "ready_for_game", !old_value ? "t" : "f");
		
		return !old_value;
	}
	
	private boolean areAllReadyAfterToggle(String room_id, Jedis jedis) {
		return JedisUtils.areAllReady(room_id, jedis);
	}
	
	private void startTimer(String room_id, Jedis jedis) {
		jedis.hset(room_id, "timer_interrupt", "f");
		jedis.hset(room_id, "elapsed_time", "0");
		
		Jedis timer_jedis = ServerThread.getResource();
		
		AllReadyTimerThread timer_runnable = new AllReadyTimerThread(room_id, timer_jedis);
		Thread new_thread = new Thread(timer_runnable);
		new_thread.start();
	}
	
	private void interruptTimer(String room_id, Jedis jedis) {
		jedis.hset(room_id, "timer_interrupt", "t");
	}
	
	private boolean areAllReady(String room_id, Jedis jedis) {
		return jedis.hget(room_id, "timer_interrupt").equals("f");
	}
	
	private boolean hasGameStarted(String room_id, Jedis jedis) {
		return !jedis.hget(room_id, "game_id").equals(JedisUtils.None);
	}
	
	private int getRemainingTime(String room_id, Jedis jedis) {
		float elapsed_time = Float.parseFloat(jedis.hget(room_id, "elapsed_time"));
		float target_time = Float.parseFloat(jedis.hget(room_id, "target_time"));
		
		return (int)(target_time - elapsed_time);
		
	}
	
}
	
