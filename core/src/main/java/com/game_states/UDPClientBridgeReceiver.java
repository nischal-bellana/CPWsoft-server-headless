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

public class UDPClientBridgeReceiver implements Runnable{
	
	UDPClientBridgeSender udpbridgesender;
	private DatagramSocket socket;
	private DatagramPacket packet;
	private byte[] content;
	
	private ConcurrentLinkedQueue<String> return_messages;
	private volatile boolean running = true;
	
	public UDPClientBridgeReceiver(UDPClientBridgeSender udpbridgesender) throws SocketException {
		
		socket = new DatagramSocket(1344);
		socket.setSoTimeout(300);
		
	    return_messages = new ConcurrentLinkedQueue<>();
	    this.udpbridgesender = udpbridgesender;
		
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
		while(running) {
			
			try {
				String return_message = receiveMessage();
				
				if(return_message.charAt(0) == 'h') {
					udpbridgesender.addClient(packet.getAddress(), packet.getPort());
					sendMessage("pass", packet.getAddress(), packet.getPort());
					
					continue;
				}
				
				return_messages.offer(return_message);
			} catch (IOException e) {
				// TODO Auto-generated catch block
			}
		}
		
	}
	
	public String pollReturnMessage() {
		if(return_messages.isEmpty()) return "";
		
		return return_messages.poll();
	}
	
	public void closeSocket() {
		running = false;
		socket.close();
	}
	
	public boolean isClosed() {
		return socket.isClosed();
	}
	
	private synchronized void sendMessage(String message, InetAddress address, int port) throws IOException {
		content = message.getBytes();
			
		packet = new DatagramPacket(content, content.length, address, port);
			
		socket.send(packet);
		
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
	
}
