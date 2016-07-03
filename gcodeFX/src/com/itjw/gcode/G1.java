package com.itjw.gcode;

public class G1 extends AxisMove {

	public G1(String raw, String[] tokens, int lineNr) {
		super(raw, tokens, lineNr);
	}

	@Override
	public Boolean process(IMachineProcessor machine) {
		machine.lineTo(getX(), getY(), getZ(), getE());
		machine.finishCommand(this);
		return true;
	}

}
