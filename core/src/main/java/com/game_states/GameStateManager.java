package com.game_states;

import com.Connection.RoomConnection;

public class GameStateManager {
	public State st;
	public State next_st;
	public GameStateManager() {
		st = new GameState(null);
		st.gsm = this;
	}
	public GameStateManager(RoomConnection roomconnection) {
		this.st = new GameState(roomconnection);
		st.gsm = this;
	}
	
	public void render() {
		if(next_st==null) {
			st.render();
		}
		else {
			st.dispose();
			st = next_st;
			next_st = null;
		}
	}
	public void dispose() {
		st.dispose();
		if(next_st!=null) next_st.dispose();
	}
	
	public void resize(int width,int height) {
		st.resize(width, height);
	}
}
