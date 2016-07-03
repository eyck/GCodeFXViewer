package com.itjw.gcode;

public class G3 extends ArcMove {

	public G3(String raw, String[] tokens, int lineNr) {
		super(raw, tokens, lineNr);
	}

	@Override
	public Boolean process(IMachineProcessor machine) {
		machine.finishCommand(this);
		return true;
	}

}
