package com.CPWsoft.Threads;

import java.io.*;
import java.net.*;
import java.util.Random;

import com.Connection.RoomConnection;

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

				String[] requestssplitted = requests.split(";");
				StringBuilder responses = new StringBuilder();
				
				for(String request: requestssplitted) {

					String response = request.substring(0, 2) + requestHandler(request);
					
					responses.append(response);
					responses.append(';');
				}
				
				responses.deleteCharAt(responses.length() - 1);
				out.println(responses.toString());
			}
			closeThread();
		}
			catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
	}
	
	public String requestHandler(String request) throws IOException, InterruptedException {
		if(request.length() < 2) return "f";
		
		switch(request.charAt(0)) {
		case 'c':
			return commonHandler(request);
		case 'h':
			return HomeHandler(request);
		case 'l':
			return LobbyHandler(request);
		case 'r':
			return RoomHandler(request);
		case 'g':
			return GameHandler(request);
		}
		
		return "f";
	}
	
	public String commonHandler(String request) {
		
		if(request.charAt(1) == 'o') {
			if(!clientcountchanged) return "f";
			clientcountchanged = false;
			return "p" + serverthread.getClientCount();
		}
		
		return "f";
	}
	
	public String HomeHandler(String request) {
		if(request.charAt(1) == 'b') {
			return "p";
		}
		
		if(request.charAt(1) == 'o') {
			markClientCountChanged();
			return "p";
		}
		
		return "f";
	}
	
	public String LobbyHandler(String request) throws SocketException, InterruptedException {
		if(request.charAt(1) == 'r') {
			if(lobbystatecontainer.roomschanged) {
				lobbystatecontainer.roomschanged = false;
				return "p" + serverthread.getRoomsData();
			}
			return "f";
		}
		
		if(request.charAt(1) == 'f') {
			
			String filter = request.substring(2);
			serverthread.setRoomFilter(filter);
			
			return "p" + serverthread.getRoomsData();
		}
		
		if(request.charAt(1) == 'j' && request.length() >= 3) {
			String roomname = request.substring(2);
			roomstatecontainer.room = serverthread.joinRoom(this, roomname);
			if(roomstatecontainer.room == null) return "f";
			roomstatecontainer.userslistchanged = true;
			return "p" + roomname;
		}
		if(request.charAt(1) == 'c') {
			String roomname = generateID();
			if(!serverthread.createRoom(roomname)) return "f";
			roomstatecontainer.room = serverthread.joinRoom(this, roomname);
			roomstatecontainer.userslistchanged = true;
			return "p" + roomname;
		}
		
		return "f";
	}
	
	public String RoomHandler(String request) {
		if(roomstatecontainer.room == null) return "f";
		
		if(request.charAt(1) == 'i') {
			roomstatecontainer.userslistchanged = true;
			roomstatecontainer.ready = false;
			return "p";
		}
		
		if(request.charAt(1) == 'b') {
			roomstatecontainer.room.removeClient(this);
			roomstatecontainer.room = null;
			return "p";
		}
		if(request.charAt(1) == 'n') {
			return "p" + roomstatecontainer.room.size();
		}
		if(request.charAt(1) == 'h' && request.length() >= 3) {
			int chatIndex = 0;
			try {
				chatIndex = Integer.parseInt(request.substring(2));
			} catch (Exception e) {
				// TODO: handle exception
				return "f";
			}
			
			if(chatIndex >= roomstatecontainer.room.getChatSize()) return "f";
			
			return "p" + roomstatecontainer.room.getChatEntries(chatIndex);
		}
		if(request.charAt(1) == 'u') {
			if(!roomstatecontainer.userslistchanged) return "f";
			String list = roomstatecontainer.room.getUsersList();
			if(list.length() == 0) return "f";
			return "p" + list;
		} 
		if(request.charAt(1) == 'm' && request.length() >= 3) {
			String message = request.substring(2);
			addChat(message);
			return "p";
		}
		if(request.charAt(1) == 'r') {
			toggleReady();
			roomstatecontainer.room.markUsersListChanged();
			roomstatecontainer.room.checkAllReady();
			return "p";
		}
		if(request.charAt(1) == 'a') {
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
	
	private String GameHandler(String request) {
		
		if(request.charAt(1) == 'n') {
			return "p" + roomstatecontainer.room.getIndex(this) + "&" + roomstatecontainer.room.getNamesAsString();
		}
		
		if(request.charAt(1) == 'b') {
			if(!roomstatecontainer.room.isGameStarted()) return "f";
			
			String broadcast = roomstatecontainer.room.getBroadcast(this);
			if(broadcast.equals("")) return "f";
			
			return "p" + broadcast;
		}
		
		if(request.charAt(1) == 'i' && request.length() >= 3) {
			if(!roomstatecontainer.room.isGameStarted() || !roomstatecontainer.room.isInput(this)) return "f";
			
			roomstatecontainer.room.addInput(request.substring(2));
			
			return "p";
		}
		
		if(request.charAt(1) == 'g') {
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
			out.println("f");
			closeThread();
			return false;
		}
		out.println("p");
		serverthread.addClient(name, this);
		prefix = "Client " + name + ": ";
		System.out.println(prefix + "new Client connected and running");
		return true;
	}
	
}
