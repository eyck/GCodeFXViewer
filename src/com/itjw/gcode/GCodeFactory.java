package com.itjw.gcode;

import java.util.HashMap;
import java.util.function.Function;

public class GCodeFactory {
	static class CreationArgs {
		String raw;
		String[] tokens;
		int lineNr;
		public CreationArgs(String raw, String[] tokens, int lineNr){
			this.raw=raw;
			this.tokens=tokens;
			this.lineNr=lineNr;
		}

	}

	private static final HashMap<String, Function<CreationArgs, AbstractGCode>> gcodeLookup = new HashMap<String, Function<CreationArgs, AbstractGCode>>() {
		private static final long serialVersionUID = 6069046436968594627L;
	{
		put("G0", (args)  -> { return new G0(args.raw, args.tokens, args.lineNr); });
		put("G1", (args)  -> { return new G1(args.raw, args.tokens, args.lineNr); });
		put("G2", (args)  -> { return new G2(args.raw, args.tokens, args.lineNr); });
		put("G3", (args)  -> { return new G3(args.raw, args.tokens, args.lineNr); });
		put("G20", (args) -> { return new G20(args.raw, args.tokens, args.lineNr); });
		put("G21", (args) -> { return new G21(args.raw, args.tokens, args.lineNr); });
		put("G28", (args) -> { return new G28(args.raw, args.tokens, args.lineNr); });
		put("G90", (args) -> { return new G90(args.raw, args.tokens, args.lineNr); });
		put("G91", (args) -> { return new G91(args.raw, args.tokens, args.lineNr); });
	}};

	static public AbstractGCode createGcode(String raw, String[] tokens, int lineNr) {
		String cmd = tokens[0].toUpperCase();
		if(gcodeLookup.containsKey(cmd))
			return gcodeLookup.get(cmd).apply(new CreationArgs(raw, tokens, lineNr));
		if(cmd.charAt(0)=='G')
			return new UnsupportedGCode(raw, tokens, lineNr);
		if(cmd.charAt(0)=='M')
			return new MachineCode(raw, tokens, lineNr);
		return new UnsupportedGCode(raw, tokens, lineNr);
	}

}
