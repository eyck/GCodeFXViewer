package com.itjw.gcode;

import javafx.geometry.Point3D;

public interface Gcode {
	
	public String getCode();
	public Double getX();
	public Double getY();
	public Double getZ();
	public Double getI();
	public Double getJ();
	public Double getE();
	public Double getF();
	public Double getS();
	public Double getP();
	public Point3D getStart();
	public Point3D getEnd();
	public Integer getLineNo();
}
