package com.game_states;

import redis.clients.jedis.Jedis;

public class GameStateManager {
	public State st;
	public State next_st;
	
	public GameStateManager(String game_id, Jedis jedis) {
		st = new GameState();
		st.gsm = this;
		
		st.create(game_id, jedis);
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
