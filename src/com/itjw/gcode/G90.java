package com.itjw.gcode;

import com.itjw.gcode.IMachineProcessor.CoordinateType;

public class G90 extends AbstractGCode {

	public G90(String raw, String[] tokens, int lineNr) {
		super(raw, tokens, lineNr);
	}

	@Override
	public Boolean process(IMachineProcessor machine) {
		machine.setCoordinateType(CoordinateType.ABSOLUTE);
		machine.finishCommand(this);
		return false;
	}

	protected void parseTokens(String[] tokens, int lineNr) {
	}
}
