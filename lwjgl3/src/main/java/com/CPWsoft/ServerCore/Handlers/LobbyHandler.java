package com.CPWsoft.ServerCore.Handlers;

import java.util.Random;

import com.CPWsoft.ServerCore.ServerThread;
import com.JedisManagement.JedisUtils;
import com.utils.OtherUtils;

import lombok.Getter;
import redis.clients.jedis.Jedis;

public class LobbyHandler implements Handler {
	@Getter(lazy = true)
	private static final LobbyHandler instance = initializeInstance();
	
	private ServerThread serverthread;
	
	@Override
	public String handle(int start, int end, String requests, String username, Jedis jedis) {
		if(requests.charAt(start + 1) == 'r') {
			if(hasRoomsDataChanged(username, jedis)) {
				return "p" + getRoomsData(jedis);
			}
			return "f";
		}
		
		if(requests.charAt(start + 1) == 'j' && end - start >= 3) {
			String room_id = requests.substring(start + 2, end);
			if(!roomExists(room_id, jedis)) return "f";
			
			joinRoom(room_id, username, jedis);
			
			return "p" + room_id;
		}
		
		if(requests.charAt(start + 1) == 'c') {
			String room_id = generateID();
			
			if(!createRoom(room_id, jedis)) return "f";
			
			joinRoom(room_id, username, jedis);
			
			return "p" + room_id;
		}
		
		return "f";
	}
	
	private static LobbyHandler initializeInstance() {
		return new LobbyHandler();
	
	}
	
	
	private String generateID() {
		return OtherUtils.generateID();
	}
	
	private boolean hasRoomsDataChanged(String username, Jedis jedis) {
		boolean result = jedis.hget(username, "rooms_data_changed").equals("t");
		jedis.hset(username, "rooms_data_changed", "f");
		return result;
	}
	
	private String getRoomsData(Jedis jedis) {
		return JedisUtils.getRoomsData(jedis);
	}
	
	private boolean roomExists(String room_id, Jedis jedis) {
		return JedisUtils.containsInSet("rooms_set", room_id, jedis);
	}
	
	private void joinRoom(String room_id, String username, Jedis jedis) {
		JedisUtils.joinRoom(room_id, username, jedis);
	}
	
	private boolean createRoom(String room_id, Jedis jedis) {
		return JedisUtils.createRoom(room_id, jedis);
	}
	
}
