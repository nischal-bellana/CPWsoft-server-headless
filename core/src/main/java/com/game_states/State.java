package com.game_states;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import redis.clients.jedis.Jedis;

public class State {
	//fields
	public GameStateManager gsm;
	
	public void create(String game_id, Jedis jedis) {
		
	}
	
	public void render() {
		
	}
	
	public void dispose() {
		
	}
	
	public void resize(int width,int height) {
		
	}
    
}
