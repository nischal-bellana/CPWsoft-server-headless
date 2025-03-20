package com.CPWsoft.Threads;

import java.util.Deque;
import java.util.HashMap;

import com.badlogic.gdx.utils.Array;
import com.utils.ParsingUtils;

public class Room {
	private ServerThread serverthread;
	private Array<nonGameThread> clients;
	private boolean allready = false;
	private Array<String> chat;
	private String name;
	private String prefix;
	private int size;
	private AllReadyTimerThread timer;
	private GameThread gamethread;
	private StringBuilder namesasstring;
	
	public Room(ServerThread serverthread, String name) {
		this.serverthread = serverthread;
		this.name = name;
		prefix = "Room " + name + ": ";
		clients = new Array<>();
		chat = new Array<>();
	}
	
	public synchronized String getName() {
		return name;
	}
	
	public synchronized int size() {
		return size;
	}
	
	public synchronized void addClient(nonGameThread client) {
		clients.add(client);
		markUsersListChanged();
		serverthread.markRoomsChanged();
		checkAllReady();
		size++;
	}
	
	public synchronized void removeClient(nonGameThread client) {
		
		int index = getIndex(client);
		
		if(!clients.removeValue(client, true)) return;
		size--;
		serverthread.markRoomsChanged();
		
		removeUpdate(index);
		
		if(size == 0) {
			serverthread.removeRoom(name);
			resetAllReady();
		}
		
	}
	
	private void removeUpdate(int index) {
		if(isGameStarted()) {
			gamethread.roomconnection.setSize(size);
			gamethread.roomconnection.addRemovedIndex(index);
			return;
		}
		
		if(size != 0) {
			markUsersListChanged();
			checkAllReady();
		}
	}
	
	public synchronized int getChatSize() {
		return chat.size;
	}
	
	public synchronized String getChatEntries(int chatIndex) {
		StringBuilder entries = new StringBuilder();
		
		for(int i = chatIndex; i < chat.size; i++) {
			entries.append(chat.get(i));
		}
		
		return entries.toString();
	}
	
	public void addChatMessage(String clientname, String messageentry) {
		chat.add(clientname.length() + "&" + clientname + messageentry.length() + "&" + messageentry);
	}
	
	public synchronized String getUsersList() {
		StringBuilder str = new StringBuilder();
		for(nonGameThread client: clients) {
			String clientdata = client.getName() + '&' + (client.isReady()? "p" : "f");
			
			ParsingUtils.appendData(clientdata, str);
		}
		return str.toString();
	}
	
	public synchronized void markUsersListChanged() {
		for(nonGameThread client: clients) {
			client.markUsersListChanged();
		}
	}
	
	public synchronized void checkAllReady() {
		resetAllReady();
		
		if(clients.size < 2) return;
		
		for(nonGameThread client: clients) {
			if(!client.isReady()) {
				return;
			}
		}
		
		setAllReady();
	}
	
	private void setAllReady() {
		allready = true;
		timer = new AllReadyTimerThread(this);
		Thread timerThread = new Thread(timer);
		log("new Timer created");
		timerThread.start();
	}
	
	private void resetAllReady() {
		allready = false;
		if(timer != null) log("Timer Destroyed");
		timer = null;
	}
	
	public synchronized float getTime() {
		return timer.getTime();
	}
	
	public synchronized AllReadyTimerThread getTimer() {
		return timer;
	}
	
	public synchronized boolean isAllReady() {
		return allready;
	}
	
	public synchronized boolean isInput(nonGameThread client) {
		return gamethread.roomconnection.getInputIndex() == getIndex(client);
	}
	
	public synchronized int getIndex(nonGameThread client) {
		return clients.indexOf(client, true);
	}
	
	public synchronized void addInput(String input) {
		gamethread.roomconnection.addInput(input);
	}
	
	public synchronized void startGame() {
		gamethread = new GameThread(this);
		gamethread.roomconnection.setSize(size);
		Thread newthread = new Thread(gamethread);
		newthread.start();
	}
	
	public void endGame() {
		gamethread = null;
		resetAllReady();
		markUsersListChanged();
	}
	
	public synchronized boolean isGameStarted() {
		return gamethread != null;
	}
	
	public synchronized String[] getNames(){
		String[] names = new String[size];
		
		for(int i = 0; i < size; i++) {
			nonGameThread client = clients.get(i);
			names[i] = client.getName();
		}
		
		return names;
	}
	
	public synchronized String getNamesAsString() {
		if(namesasstring != null) return namesasstring.toString();
		
		namesasstring = new StringBuilder();
		
		for(nonGameThread client : clients) {
			namesasstring.append(client.getName());
			namesasstring.append(',');
		}
		
		namesasstring.deleteCharAt(namesasstring.length() - 1);
		
		return namesasstring.toString();
		
	}
	
	public synchronized void resetNamesAsString() {
		namesasstring = null;
	}
	
	public synchronized String getBroadcast(nonGameThread client) {
		int index = getIndex(client);
		if(index == -1) return "";
		return gamethread.roomconnection.getBroadcast(index);
	}
	
	public nonGameThread getPlayer(int i) {
		return clients.get(i % size);
	}
	
	private void log(String message) {
		System.out.println(prefix + message);
	}
	
}
