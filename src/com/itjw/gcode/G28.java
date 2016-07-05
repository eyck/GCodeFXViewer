package com.itjw.gcode;

public class G28 extends AxisMove {

	public G28(String raw, String[] tokens, int lineNr) {
		super(raw, tokens, lineNr);
	}

	@Override
	public Boolean process(IMachineProcessor machine) {
		if(getX()==null && getY()==null && getZ()==null)
			machine.setCoordinates(0.0, 0.0, 0.0, null);
		else
			machine.setCoordinates(getX(), getY(), getZ(), null);
		machine.finishCommand(this);
		return true;
	}

}
