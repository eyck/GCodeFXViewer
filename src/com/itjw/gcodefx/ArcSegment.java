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


import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

public class ArcSegment extends Group {

	private static final Logger logger = Logger.getLogger(ArcSegment.class.getName());

	final double INCREMENT = 5d;
	public ArcSegment(Point3D start, Point3D end, Point3D center, double width, double height, PhongMaterial material) {
		Double rStart = center.distance(start);
		Double rEnd = center.distance(end);
		Sphere s = new Sphere(width/2);
		s.getTransforms().add(new Translate(start.getX(), start.getY(), start.getZ()));
		s.setMaterial(material);
		getChildren().add(s);
		if(Math.abs(rStart-rEnd)>0.01){
			logger.log(Level.WARNING, "Radius difference of "+Math.abs(rStart-rEnd)+" detected");
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
