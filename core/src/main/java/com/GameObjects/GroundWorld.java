package com.GameObjects;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Queue;
import com.utils.PointNode;
import com.utils.WorldUtils;

public class GroundWorld {
	private Body body;
	private Ground ground;
	private Queue<Fixture> tobedestroyed;
	private Queue<PointNode> tobecreated;
	private Queue<float[]> broadcastcreate;
	private Queue<Integer> broadcastdestroy;
	
	public GroundWorld(Ground ground, Body body) {
		this.ground = ground;
		this.body = body;
		tobedestroyed = new Queue<>();
		tobecreated = new Queue<>();
		broadcastcreate = new Queue<float[]>();
		broadcastdestroy = new Queue<Integer>();
	}
	
	public Vector2 getPosition() {
		return body.getPosition();
	}
	
	public Body getBody() {
		return body;
	}
	
	public void queueDestroy(Fixture fixture) {
		tobedestroyed.addLast(fixture);
	}
	
	public void queueCreate(Array<PointNode> finalfixtures) {
		for(PointNode f : finalfixtures) {
			tobecreated.addLast(f);
		}
	}
	
	public void createFixtures() {
		if(tobecreated.isEmpty()) return;

		while(!tobecreated.isEmpty()) {
			PointNode poly = tobecreated.removeFirst();
			uniqueVertices(poly);
			if(poly.vert_count < 3) continue;
			
			float time = 0;
			while(getBody().getWorld().isLocked()) {
				System.out.print("\r" + "is Locked for " + time + " secs");
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				time += 0.01;
			}
			
			FixtureDef fdef = WorldUtils.createFixDef(1, 1f, 0.3f);
			PolygonShape shape = new PolygonShape();
			float[] arr = toArray(poly);
			shape.set(arr);
			ground.addFixture(arr);
			broadcastcreate.addLast(arr);
			fdef.shape = shape;
			getBody().createFixture(fdef).setUserData(this);
			shape.dispose();
		}
		
	}
	
	public void destroyFixtures() {
		if(tobedestroyed.isEmpty()) return;
		
		while(!tobedestroyed.isEmpty()) {
			Fixture f = tobedestroyed.removeFirst();
			int index = getBody().getFixtureList().indexOf(f, true);
			ground.removeFixture(index);
			broadcastdestroy.addLast(index);
			float time = 0;
			while(getBody().getWorld().isLocked()) {
				System.out.print("\r" + "is Locked for " + time + " secs");
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				time += 0.01;
			}
			
			getBody().destroyFixture(f);
		}
	}
	
	private float[] toArray(PointNode poly) {
		float[] arr = new float[2*poly.vert_count];
		PointNode cur = poly;
		for(int i = 0; i < arr.length; i++) {
			arr[i] = i%2==0? (float)cur.x : (float)cur.y;
			if(i%2 == 1) cur = cur.next;
		}
		
		return arr;
	}
	
	private void uniqueVertices(PointNode head) {
		int vert_count = 1;
		PointNode tail = head;
		PointNode cur = head.next;
		tail.next = null;
		cur.prev = null;
		while(cur != head) {
			PointNode cur2 = head;
			while(cur2 != null) {
				if(coincide(cur, cur2)) {
					break;
				}
				cur2 = cur2.next;
			}
			if(cur2 == null) {
				tail.next = cur;
				cur.prev = tail;
				tail = cur;
				vert_count ++;
				cur = cur.next;
				tail.next = null;
				cur.prev = null;
				continue;
			}
			cur = cur.next;
			cur.prev.next = null;
			cur.prev = null;
		}
		
		tail.next = head;
		head.prev = tail;
		head.vert_count = vert_count;
		
	}
	
	private boolean coincide(PointNode p1, PointNode p2) {
		PointNode diff = p2.sub(p1);
		Vector2 vec = new Vector2((float) diff.x, (float) diff.y);
		
		return vec.len2() < (0.00000625);
	}
	
	public boolean shouldBroadcastCreate() {
		return !broadcastcreate.isEmpty();
	}
	
	public boolean shouldBroadcastDestroy() {
		return !broadcastdestroy.isEmpty();
	}
	
	public void addToBroadcastCreate(float[] arr) {
		broadcastcreate.addLast(arr);
	}
	
	public String getBroadcastCreate(StringBuilder str) {
		
		while(!broadcastcreate.isEmpty()) {
			float[] fixture = broadcastcreate.removeFirst();
			for(int i = 0; i < fixture.length; i++) {
				str.append(fixture[i]);
				str.append(',');
			}
			str.deleteCharAt(str.length()-1);
			str.append('c');
		}
		str.deleteCharAt(str.length()-1);
		
		return str.toString();
		
	}
	
	public String getBroadcastDestroy(StringBuilder str) {
		
		while(!broadcastdestroy.isEmpty()) {
			int index = broadcastdestroy.removeFirst();
			str.append(index);
			str.append('d');
		}
		str.deleteCharAt(str.length()-1);
		
		return str.toString();
		
	}
	
	
}
