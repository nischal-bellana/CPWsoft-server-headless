package com.Connection;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Queue;

public class RoomConnection {
	private int inputindex = 0;
	private Queue<String> inputs;
	private Queue<String>[] broadcasts;
	private Queue<Integer> removed;
	private String[] names; 
	private int size = 0;
	private boolean gameended = false;
	
	@SuppressWarnings("unchecked")
	public RoomConnection(String[] names, int size) {
		inputs = new Queue<>();
		broadcasts = new Queue[size];
		removed = new Queue<>();
		for(int i = 0; i < size; i++) {
			broadcasts[i] = new Queue<>();
		}
		this.names = names;
		this.size = size;
	}
	
	public synchronized void setSize(int size) {
		this.size = size;
	}
	
	public synchronized int size() {
		return size;
	}
	
	public synchronized String getName(int i) {
		return names[i];
	}
	
	public synchronized void addRemovedIndex(int i) {
		removed.addLast(i);
	}
	
	public synchronized int pollRemovedIndex() {
		if(removed.isEmpty()) return -1;
		
		return removed.removeFirst();
	}
	
	public synchronized void addInput(String input) {
		if(inputs.size >= 10) return;
		
		inputs.addLast(input);
	}
	
	public synchronized int getInputsSize() {
		return inputs.size;
	}
	
	public synchronized String pollInput() {
		if(inputs.size == 0) return "";
		
		return inputs.removeFirst();
	}
	
	public synchronized void addBroadcast(String broadcast) {
		for(int i = 0; i < size; i++) {
			if(broadcasts[i].size >= 10) continue;
			
			broadcasts[i].addLast(broadcast);
		} 
	}
	
	public synchronized int getInputIndex() {
		return inputindex;
	}
	
	public synchronized void setInputIndex(int index) {
		inputindex = index;
	}
	
	public synchronized void incrementInputIndex(int increment) {
		inputindex += increment;
		inputindex %= size;
	}
	
	public synchronized void setGameEnded() {
		gameended = true;
	}
	
	public synchronized boolean isGameEnded() {
		return gameended;
	}
	
	public synchronized String getBroadcast(int requestindex) {
		if(broadcasts[requestindex].size == 0) {
			return "";
		}
		
		return broadcasts[requestindex].removeFirst();
	}
	
}
