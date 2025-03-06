package com.GameObjects;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.utils.Queue;

public class BombWorld {
	private PlayerWorld playerworld;
	private Body body;
	private Bomb bomb;
	private boolean requestedDestroyBomb = false;
	private Queue<Clip> clipsqueue;
	private float time = 0;
	private float vel = 0;
	
	public BombWorld(Queue<Clip> clipsqueue) {
		this.clipsqueue = clipsqueue;
	}
	
	public void setBody(Body body) {
		this.body = body;
	}
	
	public Body getBody() {
		return body;
	}
	
	public void setBomb(Bomb bomb) {
		this.bomb = bomb;
	}
	
	public Bomb getBomb() {
		return bomb;
	}
	
	public void setPlayer(PlayerWorld playerworld) {
		this.playerworld = playerworld;
	}
	
	public PlayerWorld getPlayerWorld() {
		return playerworld;
	}
	
	public void update(float delta) {
		if(body == null) return;
		
		if(time > 7 || requestedDestroyBomb) {
			destroyBomb();
			return;
		}
		
		angleCorrection();
		
		time += delta;
	}
	
	private void destroyBomb() {
		int time = 0;
		System.out.println();
		
		while(body.getWorld().isLocked()) {
			System.out.print("\r" + "World is locked: " + time);
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			time += 0.01;
		}
		
		Vector2 vec = new Vector2(bomb.getSprite().getWidth()/2, 0);
		vec.setAngleRad(body.getAngle());
		float vel = this.vel/17f;
		System.out.println("vel: " + vel);
		vec.scl(vel - 1);
		
		System.out.println("Bomb is destroyed in " + (this.time+time) + "secs");
		clipsqueue.addLast(new Clip(playerworld, body.getWorld(), body.getPosition().x + vec.x, body.getPosition().y + vec.y));
		body.getWorld().destroyBody(body);
		body = null;
		time = 0;
		vel = 0;
		requestedDestroyBomb = false;
		bomb.setAlive(false);
	}
	
	private void angleCorrection() {
		Vector2 vec = body.getLinearVelocity().cpy();
		vec.scl(1/vec.len());
		Vector2 vec2 = new Vector2(1,0);
		vec2.setAngleRad(body.getAngle());
		body.applyTorque(vec2.crs(vec), true);
	}
	
	public void requestDestroyBomb(float vel) {
		requestedDestroyBomb = true;
		this.vel = vel;
	}
	
}
