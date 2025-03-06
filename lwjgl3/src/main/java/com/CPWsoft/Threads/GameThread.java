package com.CPWsoft.Threads;

import com.CPWsoft.CPWsoft;
import com.CPWsoft.lwjgl3.Lwjgl3Launcher;
import com.CPWsoft.lwjgl3.StartupHelper;
import com.Connection.RoomConnection;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.game_states.GameState;

public class GameThread implements Runnable {
	private Room room;
	public RoomConnection roomconnection;
	
	public GameThread(Room room) {
		this.room = room;
		roomconnection = new RoomConnection(room.getNames(), room.size());
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
      
      if (StartupHelper.startNewJvmIfRequired()) return; // This handles macOS support and helps on Windows.
      Lwjgl3Launcher.createHeadlessApplication(roomconnection);
      
      while(!roomconnection.isGameEnded()) {
    	  try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
      }
      
      room.endGame();
      
	}
	
}
