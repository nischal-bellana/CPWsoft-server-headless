 package com.utils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;

import nu.pattern.OpenCV;

public class polygonGen {
	public static void imageToFixtures(String imagepath, String outputXMLpath) {
		List<List<Point>> polygonVertices = generateContours(imagepath);
		System.out.println("Contours generated: " + polygonVertices.size());
		Array<PointNode> polygons = convertToConvexPolygons(polygonVertices);
		System.out.println("Convex Polygons: " + polygons.size);
		try {
        	XMLHelper.xmlWrite(polygons, outputXMLpath);
        }
        catch(Exception e) {
        	e.printStackTrace();
        }
	}
	
	public static List<List<Point>> generateContours(String imagepath) {
		OpenCV.loadLocally();
		Mat image = Imgcodecs.imread(imagepath);
        // Convert image to grayscale
        Mat grayImage = new Mat();
        Imgproc.cvtColor(image, grayImage, Imgproc.COLOR_BGR2GRAY);
        // Threshold the grayscale image
        Mat thresholded = new Mat();
        Imgproc.threshold(grayImage, thresholded, 10, 255, Imgproc.THRESH_BINARY);

        // Find contours
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(thresholded, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        // Initialize list to store polygon vertices
        List<List<Point>> polygonVertices = new ArrayList<>();
        int height = image.rows();
        // Iterate through each contour
        for (MatOfPoint contour : contours) {
            // Approximate the contour to a polygon
            MatOfPoint2f approxCurve = new MatOfPoint2f();
            double epsilon = 0.001 * Imgproc.arcLength(new MatOfPoint2f(contour.toArray()), true);
            Imgproc.approxPolyDP(new MatOfPoint2f(contour.toArray()), approxCurve, epsilon, true);

            // Filter out small polygons
            if (Imgproc.contourArea(approxCurve) > 100) {
                // Convert polygon points to a list of Point objects
                List<Point> polygon = new ArrayList<>();
                for (Point point : approxCurve.toList()) {
                	point.y = height - point.y;
                    polygon.add(point);
                }
                System.out.println("Vertex count = " + polygon.size());
                polygonVertices.add(polygon);
            }
        }
        // Display the vertices of each polygon
//        Array<PointNode> polys = trcHelp(polygonVertices,image.rows());
        System.out.println("polynodes created!");
//        try {
//        	XMLHelper.xmlWrite(polys, output);
//        }
//        catch(Exception e) {
//        	e.printStackTrace();
//        }
        
        return polygonVertices;
	}
	
	public static Array<PointNode> convertToConvexPolygons(List<List<Point>> polygonVertices){
		Array<PointNode> polys = new Array<>();
		for(int i=0;i<polygonVertices.size();i++) {
			PointNode head = PointNode.listoNode(polygonVertices.get(i));
			System.out.println("decomp started!");
			Decomp.decomp(head, polys);
			System.out.println("decomp ended!");
		}
		return polys;
	}
	
	public static Body createCustomBody(World world, String path, Body body, FixtureDef fdef, float scale) {
		List<float[]> polys = new ArrayList<>();
		
		try {
	        polys = XMLHelper.xmlRead(path);
	    }
	    catch(Exception e) {
	        	e.printStackTrace();
	    }
		
		for(float[] poly: polys) {
			for(int i = 0; i < poly.length; i++) {
				poly[i] *= scale;
			}
			
			PolygonShape pshp = new PolygonShape();
			pshp.set(poly);
			fdef.shape = pshp;
			body.createFixture(fdef).setUserData(body.getUserData());
			pshp.dispose();
		}
		return body;
	}
}
