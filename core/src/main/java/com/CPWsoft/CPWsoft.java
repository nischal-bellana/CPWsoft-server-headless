package com.CPWsoft;

import com.Connection.RoomConnection;
import com.badlogic.gdx.ApplicationAdapter;
import com.game_states.GameStateManager;
import com.game_states.State;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class CPWsoft extends ApplicationAdapter {
	private GameStateManager gsm;
	private RoomConnection roomconnection;
	
	public CPWsoft(RoomConnection roomconnection) {
		this.roomconnection = roomconnection;
	}
	
	@Override
	public void create () {
		gsm = new GameStateManager(roomconnection);
	}
	@Override
	public void render () {
		gsm.render();
	}
	@Override
	public void dispose () {
		gsm.dispose();
	}
	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub
		gsm.resize(width, height);
	}
}
