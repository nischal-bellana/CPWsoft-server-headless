package com.GameObjects;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.utils.Queue;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.utils.WorldUtils;

public class BombWorldFactory {
	
	private Queue<Clip> clipsqueue;
	
	public BombWorldFactory(Queue<Clip> clipsqueue) {
		this.clipsqueue = clipsqueue;
	}
	
	public BombWorld LaunchBomb(BombContext context) {
		BombWorld bombworld = new BombWorld(clipsqueue);
		bombworld.setPlayer(context.getPlayerworld());
		
		BodyDef bdef = new BodyDef();
		float angle = context.getAngle();
		bdef.position.set(context.getPosition());
		bdef.angle = angle;
		bdef.type = BodyType.DynamicBody;
		bdef.fixedRotation = false;
		bdef.angularDamping = 10;
		bdef.linearVelocity.set(context.getVelocity());
		
		Body body = context.getWorld().createBody(bdef);
		body.setUserData(bombworld);
		bombworld.setBody(body);
		
		FixtureDef fdef = WorldUtils.createFixDef(1f, 0.7f, 0.2f);
		PolygonShape shape = new PolygonShape();
		shape.setAsBox(0.2f, 0.1f);
		fdef.shape = shape;
		body.createFixture(fdef).setUserData(body.getUserData());
		shape.dispose();
		
		return bombworld;
	}
}
