package com.utils;

import java.util.Arrays;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.PolygonShape;

public class Clipping {
	private static PointNode clipnodebase;
	
	public static PointNode clip(PointNode subject, PointNode clip) {
		int intersectspresent = phase1(subject, clip);
		if(intersectspresent % 2 == 1) {
			return subject;
		}
		if(intersectspresent == 0) {
			if(is_in(subject,clip)) {
				return null;
			}
			else {
				return subject;
			}
		}
		phase2(subject, clip);
		PointNode res = phase3(subject, clip);
		return res;
	}
	
	private static int phase1(PointNode subject, PointNode clip) {
		int intersectspresent = 0;
		double[] alphas = new double[2];
		PointNode sub_cur = subject;
		PointNode sub_cur2 = subject.next;
		boolean first = true;
		while(first || sub_cur != subject) {
			first = false;
			PointNode clip_cur = clip;
			PointNode clip_cur2 = clip.next;
			while(clip_cur2.intersect) {
				clip_cur2 = clip_cur2.next;
			}
			boolean first2 = true;
			while(first2 || clip_cur != clip) {
				first2 = false;
				if(intersect(sub_cur, sub_cur2, clip_cur, clip_cur2, alphas)) {
					PointNode I1 = createIntersec(sub_cur, sub_cur2, alphas[0]);
					PointNode I2 = createIntersec(clip_cur, clip_cur2, alphas[1]);
					I1.neighbor = I2;
					I2.neighbor = I1;
					intersectspresent++;
				}
				
				clip_cur = clip_cur2;
				clip_cur2 = clip_cur2.next;
				while(clip_cur2.intersect) {
					clip_cur2 = clip_cur2.next;
				}
			}
			sub_cur = sub_cur2;
			sub_cur2 = sub_cur2.next;
		}
		
		return intersectspresent;
	}
	
	private static void phase2(PointNode subject, PointNode clip) {
		fillEntryExits(subject, clip, false);
		fillEntryExits(clip, subject, true);
	}
	
	private static PointNode phase3(PointNode subject, PointNode clip) {
		PointNode cur = subject.next;
		PointNode headPoly = null;
		PointNode tailPoly = new PointNode();
		PointNode polycur = null;
		while(cur != subject) {
			if(cur.intersect) {
				tailPoly = newPolygon(tailPoly, cur);
				cur.intersect = false;
				polycur = tailPoly;
				if(headPoly == null) {
					headPoly = tailPoly;
					headPoly.prevPoly = null;
				}
				
				PointNode cur2 = cur;
				boolean first = true;
				
				while(first || cur2 != cur) {
					first = false;
					if(cur2.en_ex) {
						while(!cur2.intersect) {
							cur2 = cur2.next;
							polycur = newVertex(polycur, cur2, tailPoly);
						}
					}
					else {
						while(!cur2.intersect) {
							cur2 = cur2.prev;
							polycur = newVertex(polycur, cur2, tailPoly);
						}
					}
					cur2.intersect = false;
					cur2 = cur2.neighbor;
					cur2.intersect = false;
				}
				closePolygon(tailPoly, polycur);
				
			}
			cur = cur.next;
		}
		return headPoly;
	}
	
	private static PointNode newPolygon(PointNode tailPoly, PointNode cur) {
		tailPoly.nextPoly = new PointNode(cur);
		tailPoly.nextPoly.prevPoly = tailPoly;
		tailPoly = tailPoly.nextPoly;
		tailPoly.ishead = true;
		tailPoly.vert_count = 1;
		return tailPoly;
	}
	
	private static void closePolygon(PointNode tailPoly, PointNode polycur) {
		polycur = polycur.prev;
		polycur.next = tailPoly;
		tailPoly.prev = polycur;
		tailPoly.vert_count--;
	}
	
	private static PointNode newVertex(PointNode polycur,PointNode b, PointNode tailPoly) {
		polycur.next = new PointNode(b);
		polycur.next.prev = polycur;
		tailPoly.vert_count++;
		return polycur.next;
	}
	
	private static void fillEntryExits(PointNode p1, PointNode p2, boolean inside) {
		boolean en_ex = !is_in(p1, p2);
		if(!inside) en_ex = !en_ex;
		
		PointNode cur = p1;
		boolean first = true;
		
		while(first || cur != p1) {
			first = false;
			if(cur.intersect) {
				cur.en_ex = en_ex;
				en_ex = !en_ex;
			}
			cur = cur.next;
		}
		
	}
	
	private static boolean intersect(PointNode p1, PointNode p2, PointNode q1, PointNode q2, double[] alphas) {
		double wec_p1 = wedgeProd(p1.sub(q1), q2.sub(q1));
		double wec_p2 = wedgeProd(p2.sub(q1), q2.sub(q1));
		if(wec_p1*wec_p2 <= 0) {
			double wec_q1 = wedgeProd(q1.sub(p1), p2.sub(p1));
			double wec_q2 = wedgeProd(q2.sub(p1), p2.sub(p1));
			if(wec_q1*wec_q2 <= 0) {
				alphas[0] = wec_p1/(wec_p1- wec_p2);
				alphas[1] = wec_q1/(wec_q1- wec_q2);
				return true;
			}
		}
		return false;
	}
	
	private static double wedgeProd(PointNode p1, PointNode p2) {
		return (p1.x*p2.y - p1.y*p2.x);
	}
	
	private static PointNode createIntersec(PointNode p1, PointNode p2, double alpha) {
		PointNode I = new PointNode(p1.x + (p2.x - p1.x)*alpha, p1.y + (p2.y - p1.y)*alpha);
		I.intersect = true;
		I.alpha = alpha;
		PointNode cur = p1.next;
		if(cur == p2) {
			p1.next = I;
			I.prev = p1;
			I.next = p2;
			p2.prev = I;
			return I;
		}
		
		while(cur != p2 && cur.alpha < alpha) {
			cur = cur.next;
		}
		
		I.prev = cur.prev;
		cur.prev.next = I;
		I.next = cur;
		cur.prev = I;
		return I;
	}
	
	private static boolean is_in(PointNode p, PointNode head) {
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
	
	public static PointNode getClipPointNode(Vector2 pos) {
		if(clipnodebase == null) setClipBase();
		
		int vertex_count = clipnodebase.vert_count;
		
		PointNode cur2 = clipnodebase;
		PointNode head = new PointNode(pos.x + cur2.x, pos.y + cur2.y);
		head.ishead = true;
		head.vert_count = vertex_count;
		
		PointNode cur = head;
		cur2 = cur2.next;
		
		for(int i = 1; i < vertex_count; i++) {
			cur.next = new PointNode(pos.x + cur2.x, pos.y + cur2.y);
			cur.next.prev = cur;
			cur = cur.next;
			cur2 = cur2.next;
		}
		
		cur.next = head;
		head.prev = cur;
		
		return head;
		
	}
	
	private static void setClipBase() {
		int vertex_count = 8;
		int radius = 1;
		double dth = (2*Math.PI)/vertex_count;
		
		clipnodebase = new PointNode(radius, 0);
		clipnodebase.ishead = true;
		clipnodebase.vert_count = vertex_count;
		
		PointNode cur = clipnodebase;
		
		for(int i = 1; i < vertex_count; i++) {
			cur.next = new PointNode((radius*Math.cos(i*dth)), (radius*Math.sin(i*dth)));
			cur.next.prev = cur;
			cur = cur.next;
		}
		
		cur.next = clipnodebase;
		clipnodebase.prev = cur;
	}
	
	public static PointNode getGroundPointNode(Fixture fixture) {
		PolygonShape pshape = (PolygonShape)fixture.getShape();
		
		Vector2 vec = new Vector2();
		pshape.getVertex(0, vec);
		PointNode head = new PointNode(vec.x, vec.y);
		head.ishead = true;
		head.vert_count = pshape.getVertexCount();
		
		PointNode cur = head;
		
		for(int i = 1; i < head.vert_count; i++) {
			pshape.getVertex(i, vec);
			cur.next  = new PointNode(vec.x, vec.y);
			cur.next.prev = cur;
			cur = cur.next;
		}
		
		cur.next = head;
		head.prev = cur;
		
		return head;
	}
	
	public static PointNode toPointNode(float[] arr) {
		PointNode head = new PointNode(arr[0], arr[1]);
		head.vert_count = arr.length/2;
		head.ishead = true;
		PointNode cur = head;
		for(int i = 1; i < arr.length/2; i++) {
			cur.next = new 	PointNode(arr[2*i], arr[2*i+1]);
			cur.next.prev = cur;
			cur = cur.next;
		}
		
		cur.next = head;
		head.prev = cur;
		
		return head;
	}
	
	public static float[] toArray(PointNode poly) {
		float[] arr = new float[2*poly.vert_count];
		PointNode cur = poly;
		for(int i = 0; i < arr.length; i++) {
			arr[i] = i%2==0? (float)cur.x : (float)cur.y;
			if(i%2 == 1) cur = cur.next;
		}
		
		return arr;
	}
	
	public static void resetClip(PointNode clip) {
		PointNode cur = clip.next;
		PointNode tail = clip;
		
		while(cur != clip) {
			if(cur.intersect) {
				while(cur.intersect) {
					cur = cur.next;
				}
				tail.next = cur;
				cur.prev = tail;
			}
			tail = cur;
			cur = cur.next;
		}
	}
	
}
