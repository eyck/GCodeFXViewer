package com.itjw.gcodefx;

import javafx.geometry.Point3D;

public class Util {
	public static void alignXYZ(Xform node, Point3D start, Point3D end){
		Double dx = end.getX()-start.getX();
		Double dy = end.getY()-start.getY();
		// Double length = Math.sqrt(dx*dx+dy*dy);
		Double angle = Math.atan2(dx, dy);
		node.setRotateZ(180*angle/Math.PI);
		node.setTx(start.getX()+dx/2);
		node.setTy(start.getY()+dy/2);
	}

}
