package com.CPWsoft.ServerCore.Handlers;

import redis.clients.jedis.Jedis;

public interface Handler {
	public String handle(int start, int end, String requests, String username, Jedis jedis);
}
