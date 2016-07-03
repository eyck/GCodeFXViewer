package com.itjw.gcode;

public class UnsupportedGCode extends AbstractGCode {

	public UnsupportedGCode(String raw, String[] tokens, int lineNr) {
		super(raw, tokens, lineNr);
	}

	@Override
	public Boolean process(IMachineProcessor machine) {
		machine.finishCommand(this);
		return false;
	}
}
