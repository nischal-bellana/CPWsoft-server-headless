package com.GameObjects;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;

public class BombContext {
	private PlayerWorld playerworld;
	private float angle;
	private Vector2 position;
	private Vector2 velocity;
	private World world;
	
	public PlayerWorld getPlayerworld() {
		return playerworld;
	}
	
	public void setPlayerworld(PlayerWorld playerworld) {
		this.playerworld = playerworld;
	}
	
	public float getAngle() {
		return angle;
	}
	
	public void setAngle(float angle) {
		this.angle = angle;
	}
	
	public Vector2 getPosition() {
		return position;
	}
	
	public void setPosition(Vector2 position) {
		this.position = position;
	}
	
	public Vector2 getVelocity() {
		return velocity;
	}
	
	public void setVelocity(Vector2 velocity) {
		this.velocity = velocity;
	}
	
	public World getWorld() {
		return world;
	}
	
	public void setWorld(World world) {
		this.world = world;
	}
	
}
