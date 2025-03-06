package com.GameObjects;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;

public class Bomb {
	private Sprite sprite;
	private boolean alive = true;
	
	public Bomb() {
		sprite = new Sprite();
		sprite.setBounds(0, 0, 0.4f, 0.4f);
		sprite.setOrigin(0.2f , 0.2f);
	}
	
	public Sprite getSprite() {
		return sprite;
	}
	
	public void setCenter(float x, float y) {
		sprite.setCenter(x, y);
	}
	
	public void setRotation(float angle) {
		sprite.setRotation(angle);
	}
	
	public void setAlive(boolean value) {
		alive = value;
	}
	
	public boolean isAlive() {
		return alive;
	}
	
}
