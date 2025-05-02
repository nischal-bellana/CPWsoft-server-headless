package com.GameObjects;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;

public class Bomb {
	private Sprite sprite;
	private Sprite lastsprite;
	private boolean alive = true;
	
	public Bomb() {
		sprite = new Sprite();
		lastsprite = new Sprite();
		sprite.setBounds(0, 0, 0.4f, 0.4f);
		sprite.setOrigin(0.2f , 0.2f);
	}
	
	public Sprite getSprite() {
		return sprite;
	}
	
	public boolean changedSignificantly() {
		float dx = sprite.getX() - lastsprite.getX();
		float dy = sprite.getY() - lastsprite.getY();
		
		float len = (dx*dx) + (dy*dy);
		
		if(Math.abs(len) > 0.0001) {
			lastsprite.setPosition(sprite.getX(), sprite.getY());
			return true;
		}
		
		float da = sprite.getRotation() - lastsprite.getRotation();
		if(Math.abs(da) > 0.1) {
			lastsprite.setRotation(sprite.getRotation());
			return true;
		}
		return false;
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
