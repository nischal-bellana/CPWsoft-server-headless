package com.utils;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.PolygonShape;

public class phelp {
	public static float inang(PointNode head) {
		float th = 0;
		float dth = 0;
		PointNode cur = head.next;
		Vector2 prev = new Vector2();
		prev.set((float)(cur.x-head.x),(float)(cur.y-head.y));
		Vector2 vec = new Vector2(prev);
		cur = cur.next;
		while(cur!=head) {
			vec.set((float)(cur.x-head.x),(float)(cur.y-head.y));
			dth = vec.angleDeg()-prev.angleDeg();
			if(dth>180) {
				dth-=360;
			}
			else if(dth<-180) {
				dth+=360;
			}
			th+=dth;
			prev.set(vec);
			cur = cur.next;
		}
		return th; //+ve in ACW
	}
	public static boolean isclw(PointNode head) {
		if(inang(head)<0) return true;
		return false;
	}
	public static void reverse(PointNode head) {
		boolean first = true;
		PointNode cur = head;
		while(cur!=head||first) {
			if(first) first = false;
			PointNode temp = cur.next;
			cur.next = cur.prev;
			cur = cur.prev = temp;
		}
	}
	public static boolean is_in(PointNode p, PointNode head) {
		Vector2 vec  = new Vector2();
		PointNode cur = head;
		Vector2 ref = new Vector2();
		double th = 0;
		double dth = 0; 
		ref.set((float)(head.x-p.x),(float)(head.y-p.y));
		cur = (PointNode)cur.next;
		vec.set((float)(cur.x-p.x),(float)(cur.y-p.y));
		dth = vec.angleDeg(ref);
		th += dth<180? dth:360-dth;
		while(cur!=head) {
			cur = (PointNode)cur.next;
			ref.set(vec);
			vec.set((float)(cur.x-p.x),(float)(cur.y-p.y));
			dth = vec.angleDeg(ref);
			th += dth<180? dth:360-dth;
		}
		int thf = (int)(Math.round(th)/360);
		if(thf%2!=0) {
			return true;
		}
		return false;
	}
	public static PointNode shptoPN(PolygonShape shp,double x,double y) {
		Vector2 vec = new Vector2();
		shp.getVertex(0, vec);
		PointNode head = new PointNode(x+vec.x,y+vec.y);
		PointNode cur = head;
		for(int i=1;i<shp.getVertexCount();i++) {
			shp.getVertex(i, vec);
			cur.next = new PointNode(x+vec.x,y+vec.y);
			cur.next.prev = cur;
			cur = (PointNode)cur.next;
		}
		cur.next = head;
		head.prev = cur;
		return head;
	}
	public static float sign (PointNode p1, PointNode p2, PointNode p3)
	{
	    return (float)((p1.x - p3.x) * (p2.y - p3.y) - (p2.x - p3.x) * (p1.y - p3.y));
	}

	static boolean Pit (PointNode pt, PointNode v1, PointNode v2, PointNode v3)
	{
	    float d1, d2, d3;
	    boolean has_neg, has_pos;

	    d1 = sign(pt, v1, v2);
	    d2 = sign(pt, v2, v3);
	    d3 = sign(pt, v3, v1);

	    has_neg = (d1 < 0) || (d2 < 0) || (d3 < 0);
	    has_pos = (d1 > 0) || (d2 > 0) || (d3 > 0);

	    return !(has_neg && has_pos);
	}
	public static boolean isref(PointNode head) {
		return Math.abs(inang(head))>=180;
	}
	public static boolean isref2(PointNode head) {
		//polygon is clockwise
		Vector2 ref = new Vector2();
		ref.set((float)(head.next.x-head.x),(float)(head.next.y-head.y));
		Vector2 vec = new Vector2();
		vec.set((float)(head.prev.x-head.x),(float)(head.prev.y-head.y));
		float ang = vec.angleDeg(ref); //external angle
		return ang<180;
	}
	public static boolean chincLt(PointNode cur_prev,PointNode cur,PointNode cur_next) {
		PointNode cur2 = cur_prev.prev;
		while(cur2!=cur_next) {
			if(Pit(cur2,cur_prev,cur,cur_next)) {
				return false;
			}
			cur2 = cur2.prev;
		}
		return true;
	}
	public static boolean chincRt(PointNode cur_prev,PointNode cur,PointNode cur_next) {
		PointNode cur2 = cur_next.next;
		while(cur2!=cur_prev) {
			if(Pit(cur2,cur_prev,cur,cur_next)) {
				return false;
			}
			cur2 = cur2.next;
		}
		return true;
	}
	public static PointNode copypoly(PointNode poly) {
		PointNode newpoly = new PointNode(poly);
		PointNode newcur = newpoly;
		PointNode cur = poly;
		boolean first = true;
		while(cur!=poly||first) {
			if(first) first = false;
			newcur.next = new PointNode(cur.next);
			newcur.next.prev = newcur;
			cur = (PointNode)cur.next;
			newcur = (PointNode)newcur.next;
		}
		newcur = (PointNode)newcur.prev;
		newcur.next = newpoly;
		newpoly.prev = newcur;
		return newpoly;
	}
}
