package com.GameObjects;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar.ProgressBarStyle;
import com.badlogic.gdx.utils.Queue;
import com.badlogic.gdx.utils.viewport.Viewport;

public class Player {
	private Sprite sprite;
	private Sprite lastsprite;
	private String name;
	private int health = 100;
	private int score = 0;
	private Queue<Integer> damages;
	private Queue<Integer> scorepoints;
	private float respawntime = 0;
	
	private Sprite powersprite;
	private float lastangle = 0;
	private int lastpowerindicatorLevel = -1;
	private int powerindicatorLevel = -1;
	
	public Player(String name) {
		this.name = name;
		
		
		damages = new Queue<>();
		scorepoints = new Queue<>();
		
		sprite = new Sprite();
		sprite.setBounds(0, 0, 0.4f, 0.4f);
		lastsprite = new Sprite();
		
		powersprite = new Sprite();
		powersprite.setBounds(0, 0, 1, 1);
		powersprite.setOrigin(-0.25f, powersprite.getHeight()/2);
		
	}
	
	public int getHealth() {
		return health;
	}
	
	public void addDamage(int amount) {
		int min = Math.min(amount, health);
		damages.addLast(min);
	}
	
	public int pollDamage() {
		if(damages.isEmpty()) return -1;
		
		return damages.removeFirst();
		
	}
	
	public void addScorePoints(int amount) {
		scorepoints.addLast(amount);
	}
	
	public int pollScorePoint() {
		if(scorepoints.isEmpty()) return -1;
		
		return scorepoints.removeFirst();
		
	}
	
	public void damageBy(int amount) {
		health -= amount;
	}
	
	public void scoreBy(int amount) {
		score += amount;
	}
	
	public int getScore() {
		return score;
	}
	
	public void respawn() {
		damages.clear();
		scorepoints.clear();
		
	}
	
	public void passrespawntime(float delta) {
		respawntime += delta;
	}
	
	public float getRespawnTime() {
		return respawntime;
	}
	
	public void resetRespawnTime() {
		respawntime = 0;
	}
	
	public String getName() {
		return name;
	}
	
	public Sprite getSprite() {
		return sprite;
	}
	
	public Sprite getPowerSprite() {
		return powersprite;
	}
	
	public void centerSpriteToHere(float x, float y) {
		sprite.setCenter(x, y);
	}
	
	public boolean changedSignificantly() {
		float dx = sprite.getX() - lastsprite.getX();
		float dy = sprite.getY() - lastsprite.getY();
		
		float len = (dx*dx) + (dy*dy);
		
		if(Math.abs(len) < 0.0001) {
			lastsprite.setPosition(sprite.getX(), sprite.getY());
			return true;
		}
		
		float da = powersprite.getRotation() - lastangle;
		if(Math.abs(da) < 0.1) {
			lastangle = powersprite.getRotation();
			return true;
		}
		
		if(lastpowerindicatorLevel != powerindicatorLevel) {
			powerindicatorLevel = lastpowerindicatorLevel;
			return true;
		}
		
		return false;
		
	}
	
	public void updatePowerIndicator() {
		powersprite.setPosition(sprite.getX() + (sprite.getWidth()/2) - powersprite.getOriginX(), sprite.getY() + (sprite.getWidth()/2) - powersprite.getOriginY());
	}
	
	public void setPowerLevel(int x) {
		powerindicatorLevel = x % 100;
	}
	
	public void incrementPowerLevel() {
		if(powerindicatorLevel < 99) {
			powerindicatorLevel++;
		}
	}
	
	public int getPowerLevel() {
		return powerindicatorLevel;
	}
	
}
