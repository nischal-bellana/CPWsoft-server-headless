package com.CPWsoft.ServerCore.Handlers;

import com.CPWsoft.ServerCore.ServerThread;

import lombok.Getter;
import redis.clients.jedis.Jedis;

public class CommonHandler implements Handler {
	
	@Getter(lazy = true)
	private static final CommonHandler instance = initializeInstance();
	
	private ServerThread serverthread;
	
	@Override
	public String handle(int start, int end, String requests, String username, Jedis jedis) {
		if(requests.charAt(start + 1) == 'o') {
			return "p" + getUsersCount(jedis);
		}
		
		return "f";
	}
	
	private static CommonHandler initializeInstance() {
		return new CommonHandler();
	}
	
	private long getUsersCount(Jedis jedis){
		return jedis.scard("users_set");
	}
	
}
