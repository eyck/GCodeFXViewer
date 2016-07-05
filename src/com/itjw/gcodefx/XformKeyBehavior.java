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