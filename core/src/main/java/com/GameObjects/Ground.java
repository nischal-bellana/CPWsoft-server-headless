package com.GameObjects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Queue;
import com.utils.PointNode;
import com.utils.WorldUtils;

public class Ground{
	private Sprite sprite;
	private Array<float[]> fixtures;
	
	public Ground() {
		fixtures = new Array<>();
		sprite = new Sprite();
		sprite.setBounds(-2, 3, 36.54f, 25.83f);
	}

	public void addFixture(float[] arr) {
		fixtures.add(arr);
	}
	
	public void removeFixture(int index) {
		fixtures.removeIndex(index);
	}
	
}
