package com.CPWsoft.ServerCore;

import java.io.*;
import java.net.*;
import java.util.Random;

import com.CPWsoft.ServerCore.Handlers.CommonHandler;
import com.CPWsoft.ServerCore.Handlers.GameHandler;
import com.CPWsoft.ServerCore.Handlers.LobbyHandler;
import com.CPWsoft.ServerCore.Handlers.RoomHandler;
import com.JedisManagement.JedisUtils;
import com.utils.ParsingUtils;

import redis.clients.jedis.Jedis;

public class nonGameThread implements Runnable {
	private Socket clientSocket;
	private Jedis jedis;
	private BufferedReader in;
	private PrintWriter out;
	private String username;
	private String prefix;
	
	public nonGameThread(Socket clientSocket, ServerThread serverthread, Jedis jedis) throws IOException {
		this.clientSocket = clientSocket;
		this.jedis = jedis;
		in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream())); 
		out = new PrintWriter(clientSocket.getOutputStream(), true);
	}
	
	@Override
	public void run(){
		// TODO Auto-generated method stub
		try{
			if(!registerClient()) return;
			
			while(true) {
				String requests = in.readLine();
				
				if(requests == null) break;

				StringBuilder responses = new StringBuilder();
				
				for(int i = 0; i < requests.length();) {
					
					int start = ParsingUtils.getBeginIndex(i, requests, '&');
					int end = start + ParsingUtils.parseInt(i, start - 1, requests);
					
					String response = requests.substring(start, start + 2) + requestHandler(start, end, requests);
					ParsingUtils.appendData(response, responses);
					
					i = end;
				}
				
				out.println(responses.toString());
			}
			closeThread();
		}
			catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
	}
	
	public String requestHandler(int start, int end, String requests) throws IOException, InterruptedException {
		if(end - start < 2) return "f";
		
		switch(requests.charAt(start)) {
		case 'c':
			return CommonHandler.getInstance().handle(start, end, requests, username, jedis);
		case 'l':
			return LobbyHandler.getInstance().handle(start, end, requests, username, jedis);
		case 'r':
			return RoomHandler.getInstance().handle(start, end, requests, username, jedis);
		case 'g':
			return GameHandler.getInstance().handle(start, end, requests, username, jedis);
		}
		
		return "f";
	}
	
	private void closeThread() throws IOException {
		System.out.println(prefix + "closed");
		clientSocket.close();
		in.close();
		out.close();
		
		removeUser();
		
		jedis.close();
	}
	
	private boolean registerClient() throws IOException {
		//Get username
		username = in.readLine();
		
		//Check if username already exists, close thread if it is
		if(userExists()) {
			
			out.println("1&f");
			
			closeThread();
			
			return false;
		}
		
		//Send pass response
		out.println("1&p");
		
		//Create prefix for logging purpose
		prefix = "Client " + username + ": ";
		System.out.println(prefix + "new Client connected and running");
		
		//Register username on jedis
		addUser();
		
		return true;
	}
	
	private boolean userExists() {
		return JedisUtils.containsInSet(username, "users_set", jedis);
	}
	
	private void addUser() {
		jedis.sadd("users_set", username);
		jedis.hset(username, "rooms_data_changed", "t");
		jedis.hset(username, "inroom_data_changed", "t");
		jedis.hset(username, "room_id", JedisUtils.None);
		jedis.hset(username, "ready_for_game", "f");
	}
	
	private void removeUser() {
		if(!jedis.hget(username, "room_id").equals(JedisUtils.None)) {
			String room_id = jedis.hget(username, "room_id");
			
			if(!jedis.hget(room_id, "game_id").equals(JedisUtils.None)) {
				String game_id = jedis.hget(room_id, "game_id");
				
				long index = jedis.lpos(game_id + ":players", username);
				jedis.lrem(game_id + ":players", 1, username);
				jedis.rpush(game_id + ":removed_indices", String.valueOf(index));
				
				jedis.del(game_id + ":b:" + username);
			}
			
			JedisUtils.leaveRoom(username, jedis.hget(username, "room_id"), jedis);
		}
		
		jedis.srem("users_set", username);
		jedis.del(username);
	}
	
}
