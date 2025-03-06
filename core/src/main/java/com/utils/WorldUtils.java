package com.utils;

import com.badlogic.gdx.physics.box2d.FixtureDef;

public class WorldUtils {
	public static FixtureDef createFixDef(float den,float fric,float rest) {
		FixtureDef fdef = new FixtureDef();
		fdef.density = den;
		fdef.friction = fric;
		fdef.restitution = rest;
		return fdef;
	}
}
