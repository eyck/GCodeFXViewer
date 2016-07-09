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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

import com.itjw.gcode.AbstractGCode;
import com.itjw.gcode.IMachineProcessor;

import javafx.geometry.Point3D;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.Material;
import javafx.scene.paint.PhongMaterial;

public class JavaFXMachineProcessor implements IMachineProcessor {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(JavaFXMachineProcessor.class.getName());
	
	private Double measureMultiplier = 1d;
	private Point3D posCur = new Point3D(0d, 0d, 0d);
	private Point3D posNew;
	private Double extrusion=0d;
	private CoordinateType type=CoordinateType.ABSOLUTE;
	
	private Point3D plateDimensions=null;
	
	private Double extrusionWidth=0.6;
	private Double extrusionHeight=0.3;
	private Double nonExtrusionWidth=0.1;

	private Map<Integer, Node> line2nodeMap = new TreeMap<>();

	private List<Layer> gcodes = new ArrayList<>();
	private Layer curLayer=null;
	private Integer curLayerNo = 0;
	private Integer curLineNo=0;
	private Node curGcodeXform=null;
	
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
	
	public void initialize(){
		gcodes.clear();
	}
	
	public List<Layer> getGcodeGroup() {
		return gcodes;
	}

	public Node getNode4Line(Integer lineNo){
		return line2nodeMap.get(lineNo);
	}
	
	public Integer getCurLayerNo() {
		return curLayerNo;
	}

	public Integer getCurLineNo() {
		return curLineNo;
	}

	public Point3D getPlateDimensions() {
		return plateDimensions;
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
		curLineNo=gcode.getLineNo();
	}

}
