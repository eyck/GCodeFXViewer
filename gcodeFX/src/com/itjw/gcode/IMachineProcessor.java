package com.itjw.gcode;

public interface IMachineProcessor {
	
	enum CoordinateType { ABSOLUTE, RELATIVE}
	
	public void lineTo(Double x, Double y, Double z, Double extrusion);
	
	public void arcTo(Double x, Double y, Double z, Double cx, Double cy, Double cz, Boolean ccw, Double extrusion);

	public void setMeasureMultiplier(Double mult);
	
	public void setCoordinates(Double x, Double y, Double z, Double e);

	public void setCoordinateType(CoordinateType type);
	
	public void finishCommand(AbstractGCode gcode);
}
