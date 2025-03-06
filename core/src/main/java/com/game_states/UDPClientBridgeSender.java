package com.game_states;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.badlogic.gdx.utils.Array;

public class UDPClientBridgeSender implements Runnable{
	
	private DatagramSocket socket;
	private DatagramPacket packet;
	private byte[] content;
	private Array<InetAddress> clientips;
	private Array<Integer> ports;
	
	private ConcurrentLinkedQueue<String> messages;
	private ConcurrentLinkedQueue<String> return_messages;
	private volatile boolean running = true;
	
	public UDPClientBridgeSender() throws SocketException {
		
		socket = new DatagramSocket(1433);
		socket.setSoTimeout(300);
		
		messages = new ConcurrentLinkedQueue<>();
		
		clientips = new Array<>();
		ports = new Array<>();
		
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
		while(running) {
			if(!messages.isEmpty()) {
				try {
					sendMessage(messages.poll());
				} catch (IOException e) {
					// TODO Auto-generated catch block
				}
			}
		}
		
	}
	
	public void addMessage(String message) {
		
		messages.offer(message);
	}
	
	public String pollReturnMessage() {
		if(return_messages.isEmpty()) return "";
		
		return return_messages.poll();
	}
	
	public synchronized void closeSocket() {
		running = false;
		socket.close();
	}
	
	public boolean isClosed() {
		return socket.isClosed();
	}
	
	private synchronized void sendMessage(String message) throws IOException {
		content = message.getBytes();
		
		for(int i = 0; i < clientips.size; i++) {
			
			packet = new DatagramPacket(content, content.length, clientips.get(i), ports.get(i));
			
			socket.send(packet);
		}
		
	}
	
	private synchronized String receiveMessage() throws IOException {
		content = new byte[65535];
		
		packet = new DatagramPacket(content, content.length);
		
		socket.receive(packet);
		
		return data(content);
		
	}
	
	private String data(byte[] a) 
    { 
        if (a == null) 
            return null; 
        StringBuilder ret = new StringBuilder(); 
        int i = 0; 
        while (a[i] != 0) 
        { 
            ret.append((char) a[i]); 
            i++; 
        } 
        return ret.toString(); 
    }
	
	public synchronized void addClient(InetAddress address, int port) {
		if(clientips.contains(address, true)) return;
		
		clientips.add(address);
		ports.add(port);
	}
	
	public int getClientIPSize() {
		return clientips.size;
	}
	
}
