package com.CPWsoft;

import com.badlogic.gdx.ApplicationAdapter;
import com.game_states.GameStateManager;
import com.game_states.State;

import redis.clients.jedis.Jedis;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class CPWsoft extends ApplicationAdapter {
	private GameStateManager gsm;
	private String game_id;
	private Jedis jedis;
	
	
	public CPWsoft(String game_id, Jedis jedis) {
		this.game_id = game_id;
		this.jedis = jedis;
	}
	
	@Override
	public void create () {
		gsm = new GameStateManager(game_id, jedis);
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
