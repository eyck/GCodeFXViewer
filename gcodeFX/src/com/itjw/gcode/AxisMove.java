package com.itjw.gcode;

abstract public class AxisMove extends AbstractMove {

	public AxisMove(String raw, String[] tokens, int lineNr) {
		super(raw, tokens, lineNr);
	}

}
