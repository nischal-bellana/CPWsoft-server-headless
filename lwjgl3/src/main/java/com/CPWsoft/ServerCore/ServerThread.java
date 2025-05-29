package com.CPWsoft.ServerCore;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

import com.utils.ParsingUtils;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class ServerThread {
	private static JedisPool jedispool = new JedisPool("localhost", 6379);
	
	public void run() {
		try(ServerSocket serverSocket = new ServerSocket(1323);){
			System.out.println("Version 2.0");
			
			flushAllJedis();
			
    		while(true) {
    			System.out.println("Server Waiting for new Connections...");
    			Socket clientSocket = serverSocket.accept();
    			Thread newClient = new Thread(new nonGameThread(clientSocket, this, getResource()));
    			newClient.start();
    		}
    	} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			jedispool.close();
		}
	}
	
	public static Jedis getResource() {
		return jedispool.getResource();
	}
	
	public static void flushAllJedis() {
		try(Jedis jedis = getResource()){
			jedis.flushAll();
		}
	}
	
}
