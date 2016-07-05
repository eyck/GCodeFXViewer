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
import javafx.scene.input.KeyEvent;

class XformKeyBehavior implements EventHandler<KeyEvent> {

	/**
	 * 
	 */
	private Xform node;

	public XformKeyBehavior(Xform node) {
		this.node = node;
	}

	@Override
	public void handle(KeyEvent event) {
		if( KeyEvent.KEY_PRESSED.equals(event.getEventType())){
			double linOffset = event.isShiftDown()?50:10;
			double angleOffset = event.isShiftDown()?15:5;
			switch (event.getCode()) {
			case W:
				if(event.isAltDown())
					node.setPy(node.p.getY() + linOffset);	        		
				else
					node.setTy(node.t.getY() + linOffset);	        		
				break;
			case S:
				if(event.isAltDown()){
					node.setPy(node.p.getY() - linOffset);	        		
				} else
					node.setTy(node.t.getY() - linOffset);	        		
				break;
			case A:
				if(event.isAltDown())
					node.setPx(node.p.getX() + linOffset);	        		
				else
					node.setTx(node.t.getX() + linOffset);	        		
				break;
			case D:
				if(event.isAltDown())
					node.setPx(node.p.getX() - linOffset);	        		
				else
					node.setTx(node.t.getX() - linOffset);	        		
				break;
			case Z: // us key codes
				if(event.isAltDown())
					node.setPz(node.p.getZ() - linOffset);	        		
				else
					node.setTz(node.t.getZ() - linOffset);	        		
				break;
			case Q:
				if(event.isAltDown())
					node.setPz(node.p.getZ() + linOffset);	        		
				else
					node.setTz(node.t.getZ() + linOffset);	        		
				break;
			case UP:
				node.setRx(node.rx.getAngle() + angleOffset);
				break;
			case DOWN:
				node.setRx(node.rx.getAngle() - angleOffset);
				break;
			case RIGHT:
				node.setRy(node.ry.getAngle() + angleOffset);
				break;
			case LEFT:
				node.setRy(node.ry.getAngle() - angleOffset);
				break;
			default:
				break;
			}
			event.consume();
		}
	}
}