package com.CPWsoft.ServerCore.Handlers;

import java.util.List;

import com.CPWsoft.ServerCore.ServerThread;
import com.JedisManagement.JedisUtils;
import com.utils.ParsingUtils;

import lombok.Getter;
import redis.clients.jedis.Jedis;

public class GameHandler implements Handler {
	@Getter(lazy = true)
	private static final GameHandler instance = initializeInstance();
	
	@Override
	public String handle(int start, int end, String requests, String username, Jedis jedis) {
		String room_id = jedis.hget(username, "room_id");
		String game_id = jedis.hget(room_id, "game_id");
		
		if(requests.charAt(start + 1) == 'g') {
			return  gameEnded(game_id)? "p" : "f";
		}
		
		if(gameEnded(game_id)) return "f";
		
		if(requests.charAt(start + 1) == 'n') {
			return "p" + getPlayers(game_id, jedis);
		}
		
		if(requests.charAt(start + 1) == 'b') {
			String broadcast = getBroadcast(game_id, username, jedis);
			
			if(broadcast == null) return "f";
			
			return "p" + broadcast;
		}
		
		return "f";
	}

	private static GameHandler initializeInstance() {
		return new GameHandler();
	}
	
	private String getPlayers(String game_id, Jedis jedis) {
		List<String> players = jedis.lrange(game_id + ":players", 0, -1);
		
		StringBuilder result = new StringBuilder();
		
		for(String username : players) {
			ParsingUtils.appendData(username, result);
		}
				
		return result.toString();
	}
	
	private String getBroadcast(String game_id, String username, Jedis jedis) {
		// TODO Auto-generated method stub
		return jedis.lpop(game_id + ":b:" + username);
	}
	
	private boolean gameEnded(String game_id) {
		return game_id.equals(JedisUtils.None);
	}
	
}
