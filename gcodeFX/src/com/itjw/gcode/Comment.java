package com.itjw.gcode;

public class Comment extends AbstractGCode {

	public Comment(String raw, int lineNr) {
		super(raw, null, lineNr);
	}

	@Override
	public Boolean process(IMachineProcessor machine) {
		return false;
	}

}
