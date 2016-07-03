package com.itjw.gcodefx;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.itjw.gcode.AbstractGCode;
import com.itjw.gcode.IMachineProcessor;

import javafx.geometry.Point3D;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.Material;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

public class JavaFXMachineProcessor implements IMachineProcessor {

	Double measureMultiplier = 1d;
	Point3D posCur = new Point3D(0d, 0d, -10000d);
	Point3D posNew;
	Double extrusion=0d;
	CoordinateType type=CoordinateType.ABSOLUTE;
	
	final Double EPSILON = 0.000001;

	Double plateDimensions[]={245.0, 245.0, 200.0};
	Double extrusionWidth=0.3;
	Double nonExtrusionWidth=0.1;

	Map<Integer, Node> line2nodeMap = new TreeMap<>();
	Map<Integer, Material> highlightedElem = new TreeMap<>();

	List<Xform> gcodes = new ArrayList<>();
	Xform curLayerXform=null;
	Integer curLayerNo = 0;
	Node curGcodeXform=null;
	
	final PhongMaterial whiteMaterial  = createMaterial(Color.WHITE, Color.LIGHTBLUE);
	final PhongMaterial greyMaterial   = createMaterial(Color.GREY, Color.LIGHTGREY);
	final PhongMaterial yellowMaterial = createMaterial(Color.YELLOW, Color.LIGHTYELLOW);
	final PhongMaterial blueMaterial   = createMaterial(Color.BLUE, Color.LIGHTBLUE);

	private PhongMaterial createMaterial(Color diffuse, Color specular) {
		final PhongMaterial blueMaterial = new PhongMaterial();
		blueMaterial.setDiffuseColor(diffuse);
		blueMaterial.setSpecularColor(specular);
		return blueMaterial;
	}
	
	public List<Xform> getGcodeGroup() {
		return gcodes;
	}

	public Node getXform4Line(Integer lineNo){
		return line2nodeMap.get(lineNo);
	}
	
	@Override
	public void lineTo(Double x, Double y, Double z, Double extrusion) {
		calcNewPos(x, y, z);
		curGcodeXform=null;
		if(posNew.equals(posCur)) return;
		if(extrusion!=null && Math.abs(extrusion)>EPSILON){
			curGcodeXform = createSegment(posCur, posNew, extrusionWidth, blueMaterial);
			this.extrusion=type==CoordinateType.ABSOLUTE?extrusion:this.extrusion+extrusion;
		} else {
			curGcodeXform = createSegment(posCur, posNew, nonExtrusionWidth, greyMaterial);
		}
	}

	private void calcNewPos(Double x, Double y, Double z) {
		posNew=new Point3D(
				x!=null?x*measureMultiplier:posCur.getX(),
						y!=null?y*measureMultiplier:posCur.getY(),
								z!=null?z*measureMultiplier:posCur.getZ());
		if(posNew.getZ()!=posCur.getZ()){
			if(curLayerXform!=null) gcodes.add(curLayerXform);
			curLayerXform=new Xform();
			curLayerXform.setId("L"+curLayerNo+ " ("+posNew.getZ()+"mm)");
			curLayerNo++;
		}
	}

	@Override
	public void arcTo(Double x, Double y, Double z, Double cx, Double cy, Double cz, Boolean ccw, Double extrusion) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setMeasureMultiplier(Double mult) {
		measureMultiplier=mult;	
	}

	@Override
	public void setCoordinates(Double x, Double y, Double z, Double extrusion) {
		CoordinateType tmp=type;
		type=CoordinateType.ABSOLUTE;
		calcNewPos(x, y, z);
		if(extrusion!=null) this.extrusion=extrusion;
		type=tmp;
	}

	@Override
	public void setCoordinateType(CoordinateType type) {
		this.type=type;
	}

	@Override
	public void finishCommand(AbstractGCode gcode) {
		if(curGcodeXform != null){
			curGcodeXform.setId("Line "+gcode.getLineNo());
			curLayerXform.getChildren().add(curGcodeXform);
			curGcodeXform.setUserData(gcode);
			line2nodeMap.put(gcode.getLineNo(), curGcodeXform);
			curGcodeXform=null;
		}
		if(posNew!=null) posCur=posNew;
	}

	private Node createSegment(Point3D start, Point3D end, double width, PhongMaterial material) {
		Double dx = end.getX()-start.getX();
		Double dy = end.getY()-start.getY();
		Double length = Math.sqrt(dx*dx+dy*dy);
		if(length<EPSILON) return null;
		Double angle = 180 * Math.atan2(dy, dx)/Math.PI;
		Cylinder extrude = new Cylinder(width, length); // cylinder is along the y-axis
		extrude.setMaterial(material);
		extrude.getTransforms().addAll(new Translate(start.getX()+dx/2, start.getY()+dy/2, start.getZ()), new Rotate(angle-90, Rotate.Z_AXIS));
		return extrude;
	}

}
