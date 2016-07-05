package com.itjw.gcode;

import com.itjw.gcode.IMachineProcessor.CoordinateType;

public class G91 extends AbstractGCode {

	public G91(String raw, String[] tokens, int lineNr) {
		super(raw, tokens, lineNr);
	}

	@Override
	public Boolean process(IMachineProcessor machine) {
		machine.setCoordinateType(CoordinateType.RELATIVE);
		machine.finishCommand(this);
		return false;
	}

	protected void parseTokens(String[] tokens, int lineNr) {
	}
}
