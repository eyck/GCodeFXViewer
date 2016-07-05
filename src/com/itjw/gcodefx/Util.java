/*******************************************************************************
 * Copyright (c) 2016, Eyck Jentzsch and others
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * * Neither the name of GCodeFXViewer nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package com.itjw.gcodefx;

import javafx.geometry.Point3D;
import javafx.scene.Node;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

public class Util {
	
	public static final Double EPSILON = 0.000001;

	public static void alignXYZ(Xform node, Point3D start, Point3D end){
		Double dx = end.getX()-start.getX();
		Double dy = end.getY()-start.getY();
		Double angle = Math.atan2(dx, dy);
		node.setRotateZ(180*angle/Math.PI);
		node.setTx(start.getX()+dx/2);
		node.setTy(start.getY()+dy/2);
	}
	
	public static Node createSegment(Point3D start, Point3D end, double width, PhongMaterial material) {
		Double length = start.distance(end);
		if(length<EPSILON) return null;
		Point3D center = start.midpoint(end); // center point of the segment
		Point3D tEnd = end.subtract(center); // non-transalted endpoint of segment
		Point3D cEnd = new Point3D(0d, length/2, 0d); // endpoint of the cylinder
		Point3D axis = cEnd.crossProduct(tEnd); // rotation axis
		Double angle = Point3D.ZERO.angle(cEnd, tEnd); // angle to rotate around
		Cylinder cylinder = new Cylinder(width/2, length); // cylinder is along the y-axis
		cylinder.setMaterial(material);
		cylinder.getTransforms().addAll(new Translate(center.getX(), center.getY(), center.getZ()), new Rotate(angle, axis));
		return cylinder;
	}
}
