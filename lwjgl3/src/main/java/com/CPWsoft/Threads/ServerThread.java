package com.CPWsoft.Threads;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

import com.utils.ParsingUtils;

public class ServerThread {
	private int clientcount = 0;
	private HashMap<String, Room> roommap;
	private HashMap<String, nonGameThread> clientmap;
	private String roomfilter = "";
	
	public ServerThread() {
		roommap = new HashMap<>();
		clientmap = new HashMap<>();
	}
	
	public void run() {
		try(ServerSocket serverSocket = new ServerSocket(1323);){
			System.out.println("Version 1.5.0");
    		while(true) {
    			
    			System.out.println("Server Waiting for new Connections...");
    			Socket clientSocket = serverSocket.accept();
    			Thread newClient = new Thread(new nonGameThread(clientSocket, this));
    			newClient.start();
    		}
    	} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public synchronized void addClient(String clientName, nonGameThread clientThread) {
		clientmap.put(clientName, clientThread);
		markClientCountChanged();
		clientcount++;
	}
	
	public synchronized void removeClient(nonGameThread clientThread) {
		clientmap.remove(clientThread.getName());
		markClientCountChanged();
		clientcount--;
	}
	
	public synchronized int getClientCount() {
		return clientcount;
	}
	
	public synchronized String getRoomsData() {
		StringBuilder str = new StringBuilder();
		for(String roomname: roommap.keySet()) {
			
			if(!compareByFilter(roomname) ||roommap.get(roomname).isGameStarted()) continue;
			
			String roomdata = roomname + '&' + roommap.get(roomname).size();
			ParsingUtils.appendData(roomdata, str);
		}
		
		return str.toString();
	}
	
	private boolean compareByFilter(String roomname) {
		if(roomfilter.equals("")) return true;
		
		return roomname.equals(roomfilter);
	}
	
	public synchronized void setRoomFilter(String filter) {
		roomfilter = filter;
	}
	
	public synchronized Room joinRoom(nonGameThread client, String roomname) {
		if(!roommap.containsKey(roomname)) return null;
		Room room = roommap.get(roomname);
		room.addClient(client);
		return room;
	}
	
	public synchronized boolean createRoom(String roomname) {
		if(roommap.containsKey(roomname)) return false;
		Room newroom = new Room(this, roomname);
		roommap.put(roomname, newroom);
		markRoomsChanged();
		return true;
	}
	
	public synchronized void removeRoom(String roomname) {
		roommap.remove(roomname);
		markRoomsChanged();
	}
	
	public synchronized boolean containsClient(String name) {
		return clientmap.containsKey(name);
	}
	
	public synchronized void markRoomsChanged() {
		for(nonGameThread client: clientmap.values()) {
			client.markRoomsChanged();
		}
	}
	
	private void markClientCountChanged() {
		for(nonGameThread client : clientmap.values()) {
			client.markClientCountChanged();
		}
	}
	
}
