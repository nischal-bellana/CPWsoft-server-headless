package com.JedisManagement;

import java.util.List;
import java.util.Set;

import com.utils.ParsingUtils;

import redis.clients.jedis.Jedis;

public class JedisUtils {
	
	public static final String None = "_None_";
	
	public static boolean containsInSet(String setKey, String value, Jedis jedis) {
		Set<String> users_set = jedis.smembers(setKey);
		
		if(users_set.size() == 0) return false;
		
		return users_set.contains(value);
	}
	
	public static boolean createRoom(String room_id, Jedis jedis) {
		if(containsInSet(room_id, "rooms_set", jedis)) return false;
		
		jedis.sadd("rooms_set", room_id);
		jedis.hset(room_id, "target_time", "10");
		jedis.hset(room_id, "elapsed_time", "0");
		jedis.hset(room_id, "timer_interrupt", "t");
		jedis.hset(room_id, "game_id", JedisUtils.None);
		
		return true;
	}
	
	public static void joinRoom(String room_id, String username, Jedis jedis) {
		jedis.sadd(room_id + ":players", username);
		jedis.hset(username, "room_id", room_id);
		
		roomsDataChanged(jedis);
		inRoomDataChanged(room_id, jedis);
	}
	
	public static void leaveRoom(String username, String room_id, Jedis jedis) {
		jedis.srem(room_id + ":players", username);
		jedis.hset(username, "room_id", "_None_");
		
		if(jedis.scard(room_id + ":players") == 0) {
			jedis.srem("rooms_set", room_id);
			jedis.del(room_id);
		}
		else {
			inRoomDataChanged(room_id, jedis);
		}
		
		roomsDataChanged(jedis);
	}
	
	public static void inRoomDataChanged(String room_id, Jedis jedis) {
		Set<String> players = jedis.smembers(room_id + ":players");
		
		for(String username: players) {
			jedis.hset(username, "inroom_data_changed", "t");
		}
	}
	
	public static String getRoomsData(Jedis jedis) {
		Set<String> rooms_set = jedis.smembers("rooms_set");
		
		if(rooms_set.size() == 0) return "";
		
		StringBuilder result = new StringBuilder();
		
		for(String room_id : rooms_set) {
			long no_players = jedis.scard(room_id + ":players");
			ParsingUtils.appendData(room_id + "&" + no_players, result);
		}
		
		return result.toString();
	}
	
	public static void roomsDataChanged(Jedis jedis) {
		Set<String> users_set = jedis.smembers("users_set");
		
		for(String username: users_set) {
			jedis.hset(username, "rooms_data_changed", "t");
		}
		
	}
	
	public static String getChatEntries(String room_id, long chatIndex, Jedis jedis) {
		List<String> chat_entries = jedis.lrange(room_id + ":chat", chatIndex, -1);
		
		StringBuilder result = new StringBuilder();
		
		for(String entry : chat_entries) {
			result.append(entry);
		}
		
		return result.toString();
	}
	
	public static void addChat(String room_id, String username, String message, Jedis jedis) {
		StringBuilder entry = new StringBuilder();
		
		ParsingUtils.appendData(username, entry);
		ParsingUtils.appendData(message, entry);
		
		jedis.rpush(room_id + ":chat", entry.toString());
	}
	
	public static String getPlayersList(String room_id, Jedis jedis) {
		Set<String> players = jedis.smembers(room_id + ":players");
		
		StringBuilder result = new StringBuilder();
		
		for(String username : players) {
			ParsingUtils.appendData(username + "&" + jedis.hget(username, "ready_for_game"), result);
		}
		
		return result.toString();
	}
	
	public static boolean areAllReady(String room_id, Jedis jedis) {
		if(jedis.scard(room_id + ":players") < 2) return false;
		
		Set<String> players = jedis.smembers(room_id + ":players");
		
		for(String username : players) {
			if(jedis.hget(username, "ready_for_game").equals("f")) {
				return false;
			}
		}
		
		return true;
	}
	
}
