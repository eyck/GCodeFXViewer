package com.itjw.gcode;

public abstract class AbstractMove extends AbstractGCode {

	public AbstractMove(String raw, String[] tokens, int lineNr) {
		super(raw, tokens, lineNr);
	}

}
