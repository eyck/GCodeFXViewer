package com.itjw.gcodefx;

import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Translate;

public class LinSegment extends Group {
	
	public LinSegment(Point3D start, Point3D end, double width, double height, PhongMaterial material) {
		Sphere s = new Sphere(width/2);
		s.getTransforms().add(new Translate(start.getX(), start.getY(), start.getZ()));
		s.setMaterial(material);
		Node n = Util.createSegment(start,  end,  width,  material);
		if(n!=null){
			Sphere e = new Sphere(width/2);
			e.getTransforms().add(new Translate(end.getX(), end.getY(), end.getZ()));
			e.setMaterial(material);
			getChildren().addAll(s, n, e);
		} else {
			getChildren().add(s);
		}
		setScaleZ(height/width);
	}
}
