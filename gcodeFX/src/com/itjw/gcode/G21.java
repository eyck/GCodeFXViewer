package com.itjw.gcode;

public class G21 extends AbstractGCode {

	public G21(String raw, String[] tokens, int lineNr) {
		super(raw, tokens, lineNr);
	}

	@Override
	public Boolean process(IMachineProcessor machine) {
		machine.setMeasureMultiplier(1d);
		machine.finishCommand(this);
		return false;
	}

}
