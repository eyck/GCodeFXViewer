package com.itjw.gcode;


public abstract class AbstractGCode{
	String raw;
	String[] tokens;
	int lineNr;

	public AbstractGCode(String raw, String[] tokens, int lineNr) {
		super();
		this.raw = raw;
		this.tokens = tokens;
		this.lineNr = lineNr;
		parseTokens(tokens, lineNr);
	}

	abstract protected void parseTokens(String[] tokens, int lineNr);

	public String getCode() {
		return tokens[0];
	}

	public Integer getLineNo(){
		return lineNr;
	}

	abstract public Boolean process(IMachineProcessor machine);
}
