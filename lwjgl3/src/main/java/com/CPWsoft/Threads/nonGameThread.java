package com.CPWsoft.Threads;

import java.io.*;
import java.net.*;
import java.util.Random;

import com.Connection.RoomConnection;
import com.utils.ParsingUtils;

public class nonGameThread implements Runnable {
	public static final char[] IDletters = {'0', '1', '2', '3', '4'
			, '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
			, 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q'
			, 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};
	public static final long roommax = 2176782336l;
	public static Random random = new Random();
	
	private ServerThread serverthread;
	private Socket clientSocket;
	private BufferedReader in;
	private PrintWriter out;
	private boolean clientcountchanged = true;
	private LobbyStateContainer lobbystatecontainer;
	private RoomStateContainer roomstatecontainer;
	private String name;
	private String prefix;
	
	public nonGameThread(Socket clientSocket, ServerThread serverthread) throws IOException {
		this.clientSocket = clientSocket;
		this.serverthread = serverthread;
		in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream())); 
		out = new PrintWriter(clientSocket.getOutputStream(), true);
		lobbystatecontainer = new LobbyStateContainer();
		roomstatecontainer = new RoomStateContainer();
		
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
			return commonHandler(start, end, requests);
		case 'h':
			return HomeHandler(start, end, requests);
		case 'l':
			return LobbyHandler(start, end, requests);
		case 'r':
			return RoomHandler(start, end, requests);
		case 'g':
			return GameHandler(start, end, requests);
		}
		
		return "f";
	}
	
	public String commonHandler(int start, int end, String requests) {
		
		if(requests.charAt(start + 1) == 'o') {
			if(!clientcountchanged) return "f";
			clientcountchanged = false;
			return "p" + serverthread.getClientCount();
		}
		
		return "f";
	}
	
	public String HomeHandler(int start, int end, String requests) {
		if(requests.charAt(start + 1) == 'b') {
			return "p";
		}
		
		if(requests.charAt(start + 1) == 'o') {
			markClientCountChanged();
			return "p";
		}
		
		return "f";
	}
	
	public String LobbyHandler(int start, int end, String requests) throws SocketException, InterruptedException {
		if(requests.charAt(start + 1) == 'r') {
			if(lobbystatecontainer.roomschanged) {
				lobbystatecontainer.roomschanged = false;
				return "p" + serverthread.getRoomsData();
			}
			return "f";
		}
		
		if(requests.charAt(start + 1) == 'f') {
			
			String filter = requests.substring(start + 2, end);
			serverthread.setRoomFilter(filter);
			
			return "p" + serverthread.getRoomsData();
		}
		
		if(requests.charAt(start + 1) == 'j' && end - start >= 3) {
			String roomname = requests.substring(start + 2, end);
			roomstatecontainer.room = serverthread.joinRoom(this, roomname);
			if(roomstatecontainer.room == null) return "f";
			roomstatecontainer.userslistchanged = true;
			return "p" + roomname;
		}
		if(requests.charAt(start + 1) == 'c') {
			String roomname = generateID();
			if(!serverthread.createRoom(roomname)) return "f";
			roomstatecontainer.room = serverthread.joinRoom(this, roomname);
			roomstatecontainer.userslistchanged = true;
			return "p" + roomname;
		}
		
		return "f";
	}
	
	public String RoomHandler(int start, int end, String requests) {
		if(roomstatecontainer.room == null) return "f";
		
		if(requests.charAt(start + 1) == 'i') {
			roomstatecontainer.userslistchanged = true;
			roomstatecontainer.ready = false;
			return "p";
		}
		
		if(requests.charAt(start + 1) == 'b') {
			roomstatecontainer.room.removeClient(this);
			roomstatecontainer.room = null;
			return "p";
		}
		if(requests.charAt(start + 1) == 'n') {
			return "p" + roomstatecontainer.room.size();
		}
		if(requests.charAt(start + 1) == 'h' && end - start >= 3) {
			int chatIndex = 0;
			try {
				chatIndex = ParsingUtils.parseInt(start + 2, end, requests);
			} catch (Exception e) {
				// TODO: handle exception
				return "f";
			}
			
			if(chatIndex >= roomstatecontainer.room.getChatSize()) return "f";
			
			return "p" + roomstatecontainer.room.getChatEntries(chatIndex);
		}
		if(requests.charAt(start + 1) == 'u') {
			if(!roomstatecontainer.userslistchanged) return "f";
			String list = roomstatecontainer.room.getUsersList();
			if(list.length() == 0) return "f";
			return "p" + list;
		} 
		if(requests.charAt(start + 1) == 'm' && end - start >= 3) {
			String message = requests.substring(start + 2, end);
			addChat(message);
			return "p";
		}
		if(requests.charAt(start + 1) == 'r') {
			toggleReady();
			roomstatecontainer.room.markUsersListChanged();
			roomstatecontainer.room.checkAllReady();
			return "p";
		}
		if(requests.charAt(start + 1) == 'a') {
			if(!isReady()) return "f1";
			
			if(!roomstatecontainer.room.isAllReady()) return "f2";
			
			int time = (int)roomstatecontainer.room.getTime();
			
			if(time <= 0 && roomstatecontainer.room.isGameStarted()) {
				time = -10;
			}
			
			return "p"+time;
		}
		
		return "f";
	}
	
	private String GameHandler(int start, int end, String requests) {
		
		if(requests.charAt(start + 1) == 'n') {
			return "p" + roomstatecontainer.room.getIndex(this) + "&" + roomstatecontainer.room.getNamesAsString();
		}
		
		if(requests.charAt(start + 1) == 'b') {
			if(!roomstatecontainer.room.isGameStarted()) return "f";
			
			String broadcast = roomstatecontainer.room.getBroadcast(this);
			if(broadcast.equals("")) return "f";
			
			return "p" + broadcast;
		}
		
		if(requests.charAt(start + 1) == 'i' && end - start >= 3) {
			if(!roomstatecontainer.room.isGameStarted() || !roomstatecontainer.room.isInput(this)) return "f";
			
			roomstatecontainer.room.addInput(requests.substring(start + 2, end));
			
			return "p";
		}
		
		if(requests.charAt(start + 1) == 'g') {
			return roomstatecontainer.room.isGameStarted() ? "p" : "f";
		}
		
		return "f";
	}
	
	public void markClientCountChanged() {
		clientcountchanged = true;
	}
	
	private void closeThread() throws IOException {
		System.out.println(prefix + "closed");
		clientSocket.close();
		in.close();
		out.close();
		if(roomstatecontainer.room != null) {
			roomstatecontainer.room.removeClient(this);
		}
		serverthread.removeClient(this);
	}
	
	private void addChat(String message) {
		roomstatecontainer.room.addChatMessage(name, message);
	}
	
	private String generateID() {
		StringBuilder ID = new StringBuilder();
		long idNo = Math.abs(random.nextLong()) % roommax;;
		while(idNo!=0) {
			char c = IDletters[(int) (idNo%36)];
			ID.append(c);
			idNo/=36;
		}
		return ID.toString();
	}
	
	public String getName() {
		return name;
	}
	
	public Room getRoom() {
		return roomstatecontainer.room;
	}
	
	public boolean isReady() {
		return roomstatecontainer.ready;
	}
	
	private boolean toggleReady() {
		roomstatecontainer.ready = !roomstatecontainer.ready;
		return roomstatecontainer.ready;
	}
	
	public void markUsersListChanged() {
		roomstatecontainer.userslistchanged = true;
	}
	
	public void markRoomsChanged() {
		lobbystatecontainer.roomschanged = true;
	}
	
	private boolean registerClient() throws IOException {
		name = in.readLine();
		if(serverthread.containsClient(name)) {
			out.println("1&f");
			closeThread();
			return false;
		}
		out.println("1&p");
		serverthread.addClient(name, this);
		prefix = "Client " + name + ": ";
		System.out.println(prefix + "new Client connected and running");
		return true;
	}
	
}
