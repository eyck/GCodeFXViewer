/*******************************************************************************
 * Copyright (c) 2016, Eyck Jentzsch and others
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * * Neither the name of GCodeFXViewer nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
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
