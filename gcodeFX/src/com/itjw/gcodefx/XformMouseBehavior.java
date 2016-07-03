package com.itjw.gcodefx;

import javafx.event.EventHandler;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

class XformMouseBehavior implements EventHandler<MouseEvent> {

	private double anchorAngleX, anchorAngleY;
	private double anchorTranslateX, anchorTranslateY, anchorTranslateZ;
	private double anchorPivotX, anchorPivotY, anchorPivotZ;
	private double anchorX;
	private double anchorY;
	private MouseButton btn;
	private Xform node;

	public XformMouseBehavior(Xform node, MouseButton btn) {
		this.node = node;
		this.btn = btn;
		if (btn == null) this.btn = MouseButton.MIDDLE;
	}

	@Override
	public void handle(MouseEvent t) {
		if (!btn.equals(t.getButton())) return;
		if (MouseEvent.MOUSE_PRESSED.equals(t.getEventType())) {
			anchorX = t.getSceneX();
			anchorY = t.getSceneY();
			anchorAngleX = node.rx.getAngle();
			anchorAngleY = node.ry.getAngle();
			anchorTranslateX = node.t.getX();
			anchorTranslateY = node.t.getY();
			anchorTranslateZ=node.t.getZ();
			anchorPivotX = node.p.getX();
			anchorPivotY = node.p.getY();
			anchorPivotZ = node.p.getZ();
			t.consume();
		} else if (MouseEvent.MOUSE_DRAGGED.equals(t.getEventType())) {
			if(t.isShiftDown() && t.isAltDown()){
				node.setPz(anchorPivotZ + (anchorX - t.getSceneX()) * 0.7);
				node.setTz(anchorTranslateZ + (anchorY - t.getSceneY()) * 0.7);	        		
			} else if(t.isShiftDown()){
				node.setTx(anchorTranslateX + (anchorX - t.getSceneX()) * 0.7);
				node.setTy(anchorTranslateY - (anchorY - t.getSceneY()) * 0.7);	        		
			} else if(t.isAltDown()){
				node.setPx(anchorPivotX + (anchorX - t.getSceneX()) * 0.7);
				node.setPy(anchorPivotY + (anchorY - t.getSceneY()) * 0.7);
			} else {
				node.setRx(anchorAngleX + (anchorY - t.getSceneY()) * 0.3);
				node.setRy(anchorAngleY + (anchorX - t.getSceneX()) * 0.3);
			}
		} else if(MouseEvent.MOUSE_CLICKED.equals(t.getEventType())){
			node.requestFocus();
		}
		t.consume();
	}
}