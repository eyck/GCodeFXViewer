package com.itjw.gcode;

public class G20 extends AbstractGCode {

	public G20(String raw, String[] tokens, int lineNr) {
		super(raw, tokens, lineNr);
	}

	@Override
	public Boolean process(IMachineProcessor machine) {
		machine.setMeasureMultiplier(2.54);
		machine.finishCommand(this);
		return false;
	}

}
