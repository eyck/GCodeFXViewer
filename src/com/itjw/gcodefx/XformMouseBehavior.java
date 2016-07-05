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