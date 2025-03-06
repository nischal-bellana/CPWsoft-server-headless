package com.GameObjects;

import java.util.HashSet;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.utils.PointNode;
import com.utils.WorldUtils;

public class Clip {
	private Vector2 position;
	private Body body;
	private boolean requestedDestroyClip = false;
	private float time = 0;
	
	private World world;
	private Player player;
	private HashSet<Fixture> processed;
	private HashSet<Body> processedBody;
	
	public Clip(PlayerWorld playerworld, World world, float x, float y) {
		this.player = playerworld.getPlayer();
		this.world = world;
		position = new Vector2(x, y);
	}
	
	public void setPosition(float x, float y) {
		position.set(x, y);
	}
	
	public Vector2 getPosition() {
		return position;
	}
	
	public Player getPlayer() {
		return player;
	}
	
	private void create() {
		BodyDef bdef = new BodyDef();
		bdef.position.set(position);
		bdef.fixedRotation = true;
		bdef.type = BodyType.DynamicBody;
		bdef.linearDamping = 10;
		
		body = world.createBody(bdef);
		body.setUserData(this);
		
		FixtureDef fdef = WorldUtils.createFixDef(0.001f, 0.1f, 0.1f);
		CircleShape shape = new CircleShape();
		shape.setRadius(1f);
		fdef.shape = shape;
		fdef.isSensor = true;
		
		body.createFixture(fdef).setUserData(this);
		shape.dispose();
		
		processed = new HashSet<>();
		processedBody = new HashSet<>();
	}
	
	public boolean processedFixture(Fixture f) {
		return processed.contains(f);
	}
	
	public void addToProcessed(Fixture f) {
		processed.add(f);
	}
	
	public boolean processedBody(Body body) {
		return processedBody.contains(body);
	}
	
	public void addToProcessedBody(Body body) {
		processedBody.add(body);
	}
	
	public boolean updateClip(float delta) {
		if(body == null) {
			create();
			return false;
		}
		
		if(time > 1 || requestedDestroyClip) {
			destroyClip();
			return true;
		}
		
		time += delta;
		return false;
	}
	
	public void destroyClip() {
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
		
		System.out.println("Clip is destroyed in " + (this.time+time) + "secs");
		body.getWorld().destroyBody(body);
		body = null;
		time = 0;
		requestedDestroyClip = false;
	}
	
	public void requestDestroyClip() {
		requestedDestroyClip = true;
	}
	
}
