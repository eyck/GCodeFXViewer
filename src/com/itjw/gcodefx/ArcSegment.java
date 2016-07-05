package com.itjw.gcodefx;


import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

public class ArcSegment extends Group {
	final double INCREMENT = 5d;
	public ArcSegment(Point3D start, Point3D end, Point3D center, double width, double height, PhongMaterial material) {
		Double rStart = center.distance(start);
		Double rEnd = center.distance(end);
		Sphere s = new Sphere(width/2);
		s.getTransforms().add(new Translate(start.getX(), start.getY(), start.getZ()));
		s.setMaterial(material);
		getChildren().add(s);
		if(Math.abs(rStart-rEnd)>0.01){
			System.out.println("ERROR: radius difference of "+Math.abs(rStart-rEnd)+" detected");
			Node n = Util.createSegment(start,  end,  width,  material);
			if(n!=null){
				Sphere e = new Sphere(width/2);
				e.getTransforms().add(new Translate(end.getX(), end.getY(), end.getZ()));
				e.setMaterial(material);
				getChildren().addAll(n,e);
			}
		} else {
			Point3D dStart=start.subtract(center);
			Point3D dEnd=end.subtract(center);
			Point3D axis = dStart.crossProduct(dEnd);
			if(axis.magnitude()<Util.EPSILON) // by default around Z-Axis upright
				axis=new Point3D(0d, 0d, 1d);
	        double diff = Math.atan2(dEnd.getY(), dEnd.getX())-Math.atan2(dStart.getY(), dStart.getX());
	        Double angle =  Math.toDegrees(diff<0?(diff+2*Math.PI):diff);
			Point3D p1=dStart;
			for(double i=INCREMENT; i<angle; i+=INCREMENT){
				Rotate rotate=new Rotate(i, axis);
				Point3D p2=rotate.transform(dStart);
				Point3D p2t=p2.add(center);
				Node n = Util.createSegment(p1.add(center), p2t,  width,  material);
				if(n!=null){
					Sphere m = new Sphere(width/2);
					m.getTransforms().add(new Translate(p2t.getX(), p2t.getY(), p2t.getZ()));
					m.setMaterial(material);
					getChildren().addAll(n,m);
				}
				p1=p2;
			}
			if(p1.distance(dEnd)>Util.EPSILON){
				Node n = Util.createSegment(p1.add(center), end,  width,  material);
				if(n!=null){
					Sphere m = new Sphere(width/2);
					m.getTransforms().add(new Translate(end.getX(), end.getY(), end.getZ()));
					m.setMaterial(material);
					getChildren().addAll(n,m);
				}
			}
		}
		setScaleZ(height/width);
	}

}
