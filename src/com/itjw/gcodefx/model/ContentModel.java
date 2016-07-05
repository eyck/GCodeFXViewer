/*
 * Copyright (c) 2010, 2016 Oracle and/or its affiliates.
 * All rights reserved. Use is subject to license terms.
 *
 * This file is available and licensed under the following license:
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  - Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the distribution.
 *  - Neither the name of Oracle Corporation nor the names of its
 *    contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.itjw.gcodefx.model;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import com.itjw.gcodefx.Xform;

import javafx.animation.Timeline;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.SubScene;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.ZoomEvent;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.paint.Material;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Shape3D;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

/**
 * 3D Content Model for Viewer App. Contains the 3D scene and everything related to it: light, cameras etc.
 */
public class ContentModel {
	private final int HIGHLITE_DENOM=3;
	private final int HIGHLITE_NUMER=2;
	private final SimpleObjectProperty<SubScene> subScene = new SimpleObjectProperty<>();
	private final Group root3D = new Group();
	private final PerspectiveCamera camera = new PerspectiveCamera(true);
	private final Rotate cameraXRotate = new Rotate(-20,0,0,0,Rotate.X_AXIS);
	private final Rotate cameraYRotate = new Rotate(-20,0,0,0,Rotate.Y_AXIS);
	private final Rotate cameraLookXRotate = new Rotate(0,0,0,0,Rotate.X_AXIS);
	private final Rotate cameraLookZRotate = new Rotate(0,0,0,0,Rotate.Z_AXIS);
	private final Translate cameraPosition = new Translate(0,0,0);
	private final Translate contentPosition = new Translate(0,0,0);
	private final Xform cameraXform1 = new Xform();
	private final Xform cameraXform2 = new Xform();
	private final Xform cameraXform3 = new Xform();
	private final double cameraDistance = 200;
	private final PhongMaterial redMaterial = createMaterial(Color.DARKRED, Color.RED);
	private final PhongMaterial greenMaterial = createMaterial(Color.DARKGREEN, Color.GREEN);
	private final PhongMaterial blueMaterial = createMaterial(Color.DARKBLUE, Color.BLUE);
	private ObjectProperty<Node> content = new SimpleObjectProperty<>();
	private ObjectProperty<Node> highlite = new SimpleObjectProperty<>();
	private Map<Shape3D, Material> materialMap=new HashMap<>();
	private AutoScalingGroup autoScalingGroup = new AutoScalingGroup(2);
	private Box xAxis, yAxis, zAxis;
	private Box xViewCrossAxis, yViewCrossAxis, zViewCrossAxis;
	private Sphere xSphere, ySphere, zSphere;
	private AmbientLight ambientLight = new AmbientLight(Color.DARKGREY);
	private PointLight light1 = new PointLight(Color.WHITE);
	private PointLight light2 = new PointLight(Color.ANTIQUEWHITE);
	private PointLight light3 = new PointLight(Color.ALICEBLUE);
	private final SimpleObjectProperty<Timeline> timeline = new SimpleObjectProperty<>();
	public Timeline getTimeline() { return timeline.get(); }
	public SimpleObjectProperty<Timeline> timelineProperty() { return timeline; }
	public void setTimeline(Timeline timeline) { this.timeline.set(timeline); }
	private SimpleBooleanProperty ambientLightEnabled = new SimpleBooleanProperty(false){
		@Override protected void invalidated() {
			if (get()) {
				root3D.getChildren().add(ambientLight);
			} else {
				root3D.getChildren().remove(ambientLight);
			}
		}
	};
	private SimpleBooleanProperty light1Enabled = new SimpleBooleanProperty(false){
		@Override protected void invalidated() {
			if (get()) {
				root3D.getChildren().add(light1);
			} else {
				root3D.getChildren().remove(light1);
			}
		}
	};
	private SimpleBooleanProperty light2Enabled = new SimpleBooleanProperty(false){
		@Override protected void invalidated() {
			if (get()) {
				root3D.getChildren().add(light2);
			} else {
				root3D.getChildren().remove(light2);
			}
		}
	};
	private SimpleBooleanProperty light3Enabled = new SimpleBooleanProperty(false){
		@Override protected void invalidated() {
			if (get()) {
				root3D.getChildren().add(light3);
			} else {
				root3D.getChildren().remove(light3);
			}
		}
	};
	private SimpleBooleanProperty showAxis = new SimpleBooleanProperty(true){
		@Override protected void invalidated() {
			if (get()) {
				if (xAxis == null) createAxes();
				autoScalingGroup.getChildren().addAll(xAxis, yAxis, zAxis);
				autoScalingGroup.getChildren().addAll(xSphere, ySphere, zSphere);
			} else if (xAxis != null) {
				autoScalingGroup.getChildren().removeAll(xAxis, yAxis, zAxis);
				autoScalingGroup.getChildren().removeAll(xSphere, ySphere, zSphere);
			}
		}
	};
	private SimpleBooleanProperty showViewCross = new SimpleBooleanProperty(false){
		@Override protected void invalidated() {
			if (get()) {
				if (xViewCrossAxis == null) createViewCross();
				autoScalingGroup.getChildren().addAll(xViewCrossAxis, yViewCrossAxis, zViewCrossAxis);
			} else if (xAxis != null) {
				autoScalingGroup.getChildren().removeAll(xViewCrossAxis, yViewCrossAxis, zViewCrossAxis);
			}
		}
	};
	private Rotate yUpRotate = new Rotate(0,0,0,0,Rotate.X_AXIS);
	private SimpleBooleanProperty yUp = new SimpleBooleanProperty(true){
		@Override protected void invalidated() {
			if (get()) {
				yUpRotate.setAngle(180);
			} else {
				yUpRotate.setAngle(0);
			}
		}
	};
	private SimpleBooleanProperty msaa = new SimpleBooleanProperty(){
		@Override protected void invalidated() {
			rebuildSubScene();
		}
	};

	private double mousePosX;
	private double mousePosY;
	private double mouseOldX;
	private double mouseOldY;
	private double mouseDeltaX;
	private double mouseDeltaY;

	private final EventHandler<MouseEvent> mouseEventHandler = event -> {
		double yFlip = 1.0;
		if (getYUp()) {
			yFlip = 1.0;
		} else {
			yFlip = -1.0;
		}
		if (event.getEventType() == MouseEvent.MOUSE_PRESSED) {
			mousePosX = event.getSceneX();
			mousePosY = event.getSceneY();
			mouseOldX = event.getSceneX();
			mouseOldY = event.getSceneY();
			root3D.requestFocus();
		} else if (event.getEventType() == MouseEvent.MOUSE_DRAGGED) {
			double modifier = 1.0;
			double modifierFactor = 0.3;

			if (event.isAltDown()) {
				modifier = 10.0;
			}

			mouseOldX = mousePosX;
			mouseOldY = mousePosY;
			mousePosX = event.getSceneX();
			mousePosY = event.getSceneY();
			mouseDeltaX = (mousePosX - mouseOldX); //*DELTA_MULTIPLIER;
			mouseDeltaY = (mousePosY - mouseOldY); //*DELTA_MULTIPLIER;

			double flip = -1.0;

			if ( event.isPrimaryButtonDown()) {
				if(event.isShiftDown() && event.isControlDown()){
					cameraXform1.ry.setAngle(cameraXform1.ry.getAngle() - yFlip*mouseDeltaX*modifierFactor*modifier*2.0);
					contentPosition.setZ(contentPosition.getZ() - yFlip*mouseDeltaY*modifierFactor*modifier*0.3);  // -					
				} else if(event.isShiftDown()){
					contentPosition.setX(contentPosition.getX() - flip*mouseDeltaX*modifierFactor*modifier*0.3);  // -
					contentPosition.setY(contentPosition.getY() - yFlip*mouseDeltaY*modifierFactor*modifier*0.3);  // -					
				} else if(event.isControlDown() ) {
					cameraXform2.t.setX(cameraXform2.t.getX() + flip*mouseDeltaX*modifierFactor*modifier*0.3);  // -
					cameraXform2.t.setY(cameraXform2.t.getY() + yFlip*mouseDeltaY*modifierFactor*modifier*0.3);  // -
				} else {
					cameraXform1.rz.setAngle(cameraXform1.rz.getAngle() + flip*mouseDeltaX*modifierFactor*modifier*2.0);  // -
					cameraXform1.rx.setAngle(cameraXform1.rx.getAngle() + flip*mouseDeltaY*modifierFactor*modifier*2.0);  // -
				}
			}
		}
	};
	
	private final EventHandler<ScrollEvent> scrollEventHandler = event -> {
		if (event.getTouchCount() > 0) { // touch pad scroll
			cameraXform2.t.setX(cameraXform2.t.getX() - (0.01*event.getDeltaX()));  // -
			cameraXform2.t.setY(cameraXform2.t.getY() + (0.01*event.getDeltaY()));  // -
		} else {
			double z = cameraPosition.getZ()+(event.getDeltaY()*0.2);
			z = Math.max(z,-1000);
			z = Math.min(z,0);
			cameraPosition.setZ(z);
		}
	};
	private final EventHandler<ZoomEvent> zoomEventHandler = event -> {
		if (!Double.isNaN(event.getZoomFactor()) && event.getZoomFactor() > 0.8 && event.getZoomFactor() < 1.2) {
			double z = cameraPosition.getZ()/event.getZoomFactor();
			z = Math.max(z,-1000);
			z = Math.min(z,0);
			cameraPosition.setZ(z);
		}
	};
	private final EventHandler<KeyEvent> keyEventHandler = event -> {
/*		Timeline timeline = getTimeline();
		Duration currentTime; */
		double CONTROL_MULTIPLIER = 0.1;
		double SHIFT_MULTIPLIER = 0.1;
		double ALT_MULTIPLIER = 0.5;
		
		switch (event.getCode()) {
		case Z:
			resetCamera(event.isShiftDown());
			break;
		case UP:
			if (event.isControlDown() && event.isShiftDown()) {
				cameraXform2.t.setY(cameraXform2.t.getY() - 10.0*CONTROL_MULTIPLIER);  
			}  
			else if (event.isAltDown() && event.isShiftDown()) {
				cameraXform1.rx.setAngle(cameraXform1.rx.getAngle() - 10.0*ALT_MULTIPLIER);  
			}
			else if (event.isControlDown()) {
				cameraXform2.t.setY(cameraXform2.t.getY() - 1.0*CONTROL_MULTIPLIER);  
			}
			else if (event.isAltDown()) {
				cameraXform1.rx.setAngle(cameraXform1.rx.getAngle() - 2.0*ALT_MULTIPLIER);  
			}
			else if (event.isShiftDown()) {
				double z = camera.getTranslateZ();
				double newZ = z + 5.0*SHIFT_MULTIPLIER;
				camera.setTranslateZ(newZ);
			}
			break;
		case DOWN:
			if (event.isControlDown() && event.isShiftDown()) {
				cameraXform2.t.setY(cameraXform2.t.getY() + 10.0*CONTROL_MULTIPLIER);  
			}  
			else if (event.isAltDown() && event.isShiftDown()) {
				cameraXform1.rx.setAngle(cameraXform1.rx.getAngle() + 10.0*ALT_MULTIPLIER);  
			}
			else if (event.isControlDown()) {
				cameraXform2.t.setY(cameraXform2.t.getY() + 1.0*CONTROL_MULTIPLIER);  
			}
			else if (event.isAltDown()) {
				cameraXform1.rx.setAngle(cameraXform1.rx.getAngle() + 2.0*ALT_MULTIPLIER);  
			}
			else if (event.isShiftDown()) {
				double z = camera.getTranslateZ();
				double newZ = z - 5.0*SHIFT_MULTIPLIER;
				camera.setTranslateZ(newZ);
			}
			break;
		case RIGHT:
			if (event.isControlDown() && event.isShiftDown()) {
				cameraXform2.t.setX(cameraXform2.t.getX() + 10.0*CONTROL_MULTIPLIER);  
			}  
			else if (event.isAltDown() && event.isShiftDown()) {
				cameraXform1.ry.setAngle(cameraXform1.ry.getAngle() - 10.0*ALT_MULTIPLIER);  
			}
			else if (event.isControlDown()) {
				cameraXform2.t.setX(cameraXform2.t.getX() + 1.0*CONTROL_MULTIPLIER);  
			}
/*			else if (event.isShiftDown()) {
				currentTime = timeline.getCurrentTime();
				timeline.jumpTo(Frame.frame(Math.round(Frame.toFrame(currentTime)/10.0)*10 + 10));
				// timeline.jumpTo(Duration.seconds(currentTime.toSeconds() + ONE_FRAME));
			}
*/			else if (event.isAltDown()) {
				cameraXform1.ry.setAngle(cameraXform1.ry.getAngle() - 2.0*ALT_MULTIPLIER);  
			}
/*			else {
				currentTime = timeline.getCurrentTime();
				timeline.jumpTo(Frame.frame(Frame.toFrame(currentTime) + 1));
				// timeline.jumpTo(Duration.seconds(currentTime.toSeconds() + ONE_FRAME));
			}
*/			break;
		case LEFT:
			if (event.isControlDown() && event.isShiftDown()) {
				cameraXform2.t.setX(cameraXform2.t.getX() - 10.0*CONTROL_MULTIPLIER);  
			}  
			else if (event.isAltDown() && event.isShiftDown()) {
				cameraXform1.ry.setAngle(cameraXform1.ry.getAngle() + 10.0*ALT_MULTIPLIER);  // -
			}
			else if (event.isControlDown()) {
				cameraXform2.t.setX(cameraXform2.t.getX() - 1.0*CONTROL_MULTIPLIER);  
			}
/*			else if (event.isShiftDown()) {
				currentTime = timeline.getCurrentTime();
				timeline.jumpTo(Frame.frame(Math.round(Frame.toFrame(currentTime)/10.0)*10 - 10));
				// timeline.jumpTo(Duration.seconds(currentTime.toSeconds() - ONE_FRAME));
			}
*/			else if (event.isAltDown()) {
				cameraXform1.ry.setAngle(cameraXform1.ry.getAngle() + 2.0*ALT_MULTIPLIER);  // -
			}
/*			else {
				currentTime = timeline.getCurrentTime();
				timeline.jumpTo(Frame.frame(Frame.toFrame(currentTime) - 1));
				// timeline.jumpTo(Duration.seconds(currentTime.toSeconds() - ONE_FRAME));
			}
*/			break;
		default:
			break;
		}
	};
	
	public void resetCamera(Boolean fullReset) {
		if (fullReset) {
			cameraXform1.ry.setAngle(0.0);
			cameraXform1.rx.setAngle(0.0);
			cameraXform1.rz.setAngle(0.0);
			if (yUp.get()) {
				yUpRotate.setAngle(180);
			} else {
				yUpRotate.setAngle(0);
			}
			cameraPosition.setZ(-cameraDistance);
		}   
		cameraXform2.t.setX(0d);
		cameraXform2.t.setY(0d);
		contentPosition.setX(0d);  // -
		contentPosition.setY(0d);  // -					
		contentPosition.setZ(0d);  // -					
	}

	public ContentModel() {
		// CAMERA
		camera.setNearClip(1.0); // TODO: Workaround as per RT-31255
		camera.setFarClip(10000.0); // TODO: Workaround as per RT-31255

		camera.getTransforms().addAll(
				yUpRotate,
				cameraPosition,
				cameraLookXRotate,
				cameraLookZRotate);
		if (yUp.get()) {
			yUpRotate.setAngle(180);
		} else {
			yUpRotate.setAngle(0);
		}

		root3D.getChildren().add(cameraXform1);
		cameraXform1.getChildren().add(cameraXform2);
		cameraXform2.getChildren().add(cameraXform3);
		cameraXform3.getChildren().add(camera);
		cameraPosition.setZ(-cameraDistance);
		root3D.getChildren().add(autoScalingGroup);
		// Build SubScene
		rebuildSubScene();
	}

	private void rebuildSubScene() {
		SubScene oldSubScene = this.subScene.get();
		if (oldSubScene != null) {
			oldSubScene.setRoot(new Region());
			oldSubScene.setCamera(null);
			oldSubScene.removeEventHandler(MouseEvent.ANY, mouseEventHandler);
			oldSubScene.removeEventHandler(KeyEvent.ANY, keyEventHandler);
			oldSubScene.removeEventHandler(ScrollEvent.ANY, scrollEventHandler);
		}

		javafx.scene.SceneAntialiasing aaVal = msaa.get() ?	javafx.scene.SceneAntialiasing.BALANCED : javafx.scene.SceneAntialiasing.DISABLED;
		SubScene subScene = new SubScene(root3D,400,400,true,aaVal);
		this.subScene.set(subScene);
		subScene.setFill(Color.TRANSPARENT);
		subScene.setCamera(camera);
		// SCENE EVENT HANDLING FOR CAMERA NAV
		subScene.addEventHandler(MouseEvent.ANY, mouseEventHandler);
		subScene.addEventHandler(KeyEvent.ANY, keyEventHandler);
		subScene.addEventHandler(ZoomEvent.ANY, zoomEventHandler);
		subScene.addEventHandler(ScrollEvent.ANY, scrollEventHandler);
	}

	public boolean getAmbientLightEnabled() {
		return ambientLightEnabled.get();
	}

	public SimpleBooleanProperty ambientLightEnabledProperty() {
		return ambientLightEnabled;
	}

	public void setAmbientLightEnabled(boolean ambientLightEnabled) {
		this.ambientLightEnabled.set(ambientLightEnabled);
	}

	public boolean getLight1Enabled() {
		return light1Enabled.get();
	}

	public SimpleBooleanProperty light1EnabledProperty() {
		return light1Enabled;
	}

	public void setLight1Enabled(boolean light1Enabled) {
		this.light1Enabled.set(light1Enabled);
	}

	public boolean getLight2Enabled() {
		return light2Enabled.get();
	}

	public SimpleBooleanProperty light2EnabledProperty() {
		return light2Enabled;
	}

	public void setLight2Enabled(boolean light2Enabled) {
		this.light2Enabled.set(light2Enabled);
	}

	public boolean getLight3Enabled() {
		return light3Enabled.get();
	}

	public SimpleBooleanProperty light3EnabledProperty() {
		return light3Enabled;
	}

	public void setLight3Enabled(boolean light3Enabled) {
		this.light3Enabled.set(light3Enabled);
	}

	public AmbientLight getAmbientLight() {
		return ambientLight;
	}

	public PointLight getLight1() {
		return light1;
	}

	public PointLight getLight2() {
		return light2;
	}

	public PointLight getLight3() {
		return light3;
	}

	public boolean getYUp() {
		return yUp.get();
	}

	public SimpleBooleanProperty yUpProperty() {
		return yUp;
	}

	public void setYUp(boolean yUp) {
		this.yUp.set(yUp);
	}

	public boolean getShowAxis() {
		return showAxis.get();
	}

	public SimpleBooleanProperty showAxisProperty() {
		return showAxis;
	}

	public void setShowAxis(boolean showAxis) {
		this.showAxis.set(showAxis);
	}

	public boolean getShowViewCross() {
		return showViewCross.get();
	}

	public SimpleBooleanProperty showViewCrossProperty() {
		return showViewCross;
	}

	public void setShowViewCross(boolean showViewCross) {
		this.showViewCross.set(showViewCross);
	}

	public AutoScalingGroup getAutoScalingGroup() {
		return autoScalingGroup;
	}

	public ObjectProperty<Node> contentProperty() { return content; }
	public Node getContent() { return content.get(); }
	public void setContent(Node content) { this.content.set(content); }

	{
		contentProperty().addListener((ObservableValue<? extends Node> ov, Node oldContent, Node newContent) -> {
			autoScalingGroup.getChildren().remove(oldContent);
			autoScalingGroup.getChildren().add(newContent);
			newContent.getTransforms().add(contentPosition);
		});
	}

	public ObjectProperty<Node> highlightProperty() { return highlite; }
	public Node getHighlight() { return highlite.get(); }
	public void setHighlight(Node content) { this.highlite.set(content); }
	final PhongMaterial highliteMaterial = createMaterial(Color.YELLOW, Color.LIGHTYELLOW);
	Consumer<Node> processNode = n -> {
		if(n instanceof Shape3D){
			Shape3D shape = (Shape3D) n;
			materialMap.put(shape, shape.getMaterial());
			shape.setMaterial(highliteMaterial);
			modifySize(shape, HIGHLITE_DENOM, HIGHLITE_NUMER);
		} else if(n instanceof Parent){
			for(Node nn:((Parent) n).getChildrenUnmodifiable()){
				ContentModel.this.processNode.accept(nn);
			}
		}
	};
	{
		highlightProperty().addListener((ObservableValue<? extends Node> ov, Node oldContent, Node newContent) -> {
			if(oldContent!=null){
				for(Map.Entry<Shape3D, Material> entry: materialMap.entrySet()){
					entry.getKey().setMaterial(entry.getValue());
					modifySize(entry.getKey(), HIGHLITE_NUMER, HIGHLITE_DENOM);
				}
			}
			materialMap.clear();
			if(newContent!=null) processNode.accept(newContent);
		});
	}

	private void modifySize(Shape3D shape, int denominator, int numerator) {
		if(shape instanceof Cylinder)
			((Cylinder)shape).setRadius( denominator * ((Cylinder)shape).getRadius() / numerator);
		else if(shape instanceof Sphere)
			((Sphere)shape).setRadius(denominator * ((Sphere)shape).getRadius() / numerator);
	}

	public boolean getMsaa() {
		return msaa.get();
	}

	public SimpleBooleanProperty msaaProperty() {
		return msaa;
	}

	public void setMsaa(boolean msaa) {
		this.msaa.set(msaa);
	}

	public SubScene getSubScene() {
		return subScene.get();
	}

	public SimpleObjectProperty<SubScene> subSceneProperty() {
		return subScene;
	}

	public Group getRoot3D() {
		return root3D;
	}

	public PerspectiveCamera getCamera() {
		return camera;
	}

	public Rotate getCameraXRotate() {
		return cameraXRotate;
	}

	public Rotate getCameraYRotate() {
		return cameraYRotate;
	}

	public Translate getCameraPosition() {
		return cameraPosition;
	}

	public Rotate getCameraLookXRotate() {
		return cameraLookXRotate;
	}

	public Rotate getCameraLookZRotate() {
		return cameraLookZRotate;
	}

	private PhongMaterial createMaterial(Color diffuse, Color specular) {
		final PhongMaterial blueMaterial = new PhongMaterial();
		blueMaterial.setDiffuseColor(diffuse);
		blueMaterial.setSpecularColor(specular);
		return blueMaterial;
	}

	private void createViewCross(){
		double length = 5.0;
		double width = 0.1;
		double radius = 2.0;
		xViewCrossAxis = new Box(length, width, width);
		yViewCrossAxis = new Box(width, length, width);
		zViewCrossAxis = new Box(width, width, length);
		xViewCrossAxis.setMaterial(redMaterial);
		yViewCrossAxis.setMaterial(greenMaterial);
		zViewCrossAxis.setMaterial(blueMaterial);

	}
	private void createAxes() {
		double length = 240.0;
		double width = 0.2;
		double radius = 2.0;

		xSphere = new Sphere(radius);
		ySphere = new Sphere(radius);
		zSphere = new Sphere(radius);
		xSphere.setMaterial(redMaterial);
		ySphere.setMaterial(greenMaterial);
		zSphere.setMaterial(blueMaterial);

		xSphere.setTranslateX(length);
		ySphere.setTranslateY(length);
		zSphere.setTranslateZ(length);

		xAxis = new Box(length, width, width);
		xAxis.setTranslateX(length/2);
		yAxis = new Box(width, length, width);
		yAxis.setTranslateY(length/2);
		zAxis = new Box(width, width, length);
		zAxis.setTranslateZ(length/2);
		xAxis.setMaterial(redMaterial);
		yAxis.setMaterial(greenMaterial);
		zAxis.setMaterial(blueMaterial);
		
		xSphere.getTransforms().add(contentPosition);
		ySphere.getTransforms().add(contentPosition);
		zSphere.getTransforms().add(contentPosition);
		xAxis.getTransforms().add(contentPosition);
		yAxis.getTransforms().add(contentPosition);
		zAxis.getTransforms().add(contentPosition);
	}

}
