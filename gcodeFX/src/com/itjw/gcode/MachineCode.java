package com.itjw.gcode;

public class MachineCode extends AbstractGCode {

	public MachineCode(String raw, String[] tokens, int lineNr) {
		super(raw, tokens, lineNr);
	}

	@Override
	public Boolean process(IMachineProcessor machine) {
		machine.finishCommand(this);
		return false;
	}

}
