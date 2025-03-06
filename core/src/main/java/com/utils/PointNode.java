package com.utils;

import java.util.List;

import org.opencv.core.Point;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class PointNode{
	public double x;
	public double y;
	public PointNode next;
	public PointNode prev;
	public PointNode nextPoly;
	public PointNode prevPoly;
	public boolean ishead = false;
	public int vert_count = 0;
	public boolean intersect = false;
	public boolean en_ex = false;
	public PointNode neighbor;	
	public double alpha;

	public PointNode(double x,double y){
		this.x = x;
		this.y = y;
	}
	public PointNode() {
		super();
		// TODO Auto-generated constructor stub
	}
	public PointNode(PointNode b) {
		this.x = b.x;
		this.y = b.y;
		// TODO Auto-generated constructor stub
	}
	
	public PointNode sub(PointNode b) {
		return new PointNode(x-b.x, y-b.y);
	}
	
	public static PointNode listoNode(List<Point> polygonVertices) {
		PointNode head;
		PointNode cur = head = new PointNode();
		for(int i = 0; i < polygonVertices.size(); i++) {
			cur.x = polygonVertices.get(i).x;
			cur.y = polygonVertices.get(i).y;
			cur.next = new PointNode();
			cur.next.prev = cur;
			cur = cur.next;
		}
		cur = cur.prev;
		cur.next = head;
		head.prev = cur;
		head.ishead = true;
		head.vert_count = polygonVertices.size();
		return head;
	}
}
