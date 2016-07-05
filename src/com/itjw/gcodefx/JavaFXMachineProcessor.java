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

public class JavaFXMachineProcessor implements IMachineProcessor {

	Double measureMultiplier = 1d;
	Point3D posCur = new Point3D(0d, 0d, 0d);
	Point3D posNew;
	Double extrusion=0d;
	CoordinateType type=CoordinateType.ABSOLUTE;
	
	Double plateDimensions[]={245.0, 245.0, 200.0};
	Double extrusionWidth=0.6;
	Double extrusionHeight=0.3;
	Double nonExtrusionWidth=0.1;

	Map<Integer, Node> line2nodeMap = new TreeMap<>();
	Map<Integer, Material> highlightedElem = new TreeMap<>();

	List<Layer> gcodes = new ArrayList<>();
	Layer curLayer=null;
	Integer curLayerNo = 0;
	Node curGcodeXform=null;
	
	final PhongMaterial whiteMaterial  = createMaterial(Color.WHITE, Color.LIGHTBLUE);
	final PhongMaterial greyMaterial   = createMaterial(Color.GREY, Color.LIGHTGREY);
	final PhongMaterial yellowMaterial = createMaterial(Color.YELLOW, Color.LIGHTYELLOW);
	final PhongMaterial blueMaterial   = createMaterial(Color.BLUE, Color.LIGHTBLUE);

	private PhongMaterial createMaterial(Color diffuse, Color specular) {
		final PhongMaterial material = new PhongMaterial();
		material.setDiffuseColor(diffuse);
		material.setSpecularColor(specular);
		return material;
	}
	
	public List<Layer> getGcodeGroup() {
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
		if(extrusion!=null && Math.abs(extrusion)>Util.EPSILON){
			curGcodeXform = new LinSegment(posCur, posNew, extrusionWidth, extrusionHeight, blueMaterial);
			this.extrusion=type==CoordinateType.ABSOLUTE?extrusion:this.extrusion+extrusion;
		} else {
			curGcodeXform = new LinSegment(posCur, posNew, nonExtrusionWidth, nonExtrusionWidth, greyMaterial);
		}
	}

	private void calcNewPos(Double x, Double y, Double z) {
		posNew=new Point3D(
				x!=null?x*measureMultiplier:posCur.getX(),
						y!=null?y*measureMultiplier:posCur.getY(),
								z!=null?z*measureMultiplier:posCur.getZ());
		if(posNew.getZ()!=posCur.getZ()){
			curLayer=new Layer();
			curLayer.setId("L"+curLayerNo+ " ("+posNew.getZ()+"mm)");
			gcodes.add(curLayer);
			curLayerNo++;
		}
	}

	@Override
	public void arcTo(Double x, Double y, Double z, Double cx, Double cy, Double cz, Boolean cw, Double extrusion) {
		calcNewPos(x, y, z);
		curGcodeXform=null;
		Point3D center = new Point3D(posCur.getX()+(cx!=null?cx:posCur.getX()),
				posCur.getY()+(cy!=null?cy:posCur.getY()),
						posCur.getZ()+(cz!=null?cz:posCur.getZ()));
		if(extrusion!=null && Math.abs(extrusion)>Util.EPSILON){
			curGcodeXform = new ArcSegment(cw?posNew:posCur, cw?posCur:posNew, center, extrusionWidth, extrusionHeight, blueMaterial);
			this.extrusion=type==CoordinateType.ABSOLUTE?extrusion:this.extrusion+extrusion;
		} else {
			curGcodeXform = new ArcSegment(cw?posNew:posCur, cw?posCur:posNew, center, nonExtrusionWidth, nonExtrusionWidth, greyMaterial);
		}
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
			curLayer.getChildren().add(curGcodeXform);
			curGcodeXform.setUserData(gcode);
			line2nodeMap.put(gcode.getLineNo(), curGcodeXform);
			curGcodeXform=null;
		}
		if(posNew!=null) posCur=posNew;
	}

}
