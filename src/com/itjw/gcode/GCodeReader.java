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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GCodeReader {

	public static List<AbstractGCode> readFile(String inputfile){
		try {
			FileReader fileReader = new FileReader(inputfile);
			List<AbstractGCode> gcodes=parseLines(fileReader);
			fileReader.close();
			return gcodes;
		} catch (Exception e) {
			Logger.getLogger(GCodeReader.class.getName()).log(Level.SEVERE, "File read error", e);
		}
		return null;
	}

	public static List<AbstractGCode> parseStrings(String inputdata){
		try {
			StringReader stringReader = new StringReader(inputdata);
			List<AbstractGCode> gcodes=parseLines(stringReader);
			return gcodes;
		} catch (Exception e) {
			Logger.getLogger(GCodeReader.class.getName()).log(Level.SEVERE, "String read error", e);
		}
		return null;
	}

	private static List<AbstractGCode> parseLines(Reader reader) throws IOException {
		String readString;
		BufferedReader bf = new BufferedReader(reader);
		List<AbstractGCode> gcodes=new ArrayList<>();
		int lineNr=1;
		while ((readString = bf.readLine()) != null) {
			String lineTokens[] = readString.split(";");
			if(lineTokens[0]==null || lineTokens[0].length()==0){
				gcodes.add(new Comment(lineTokens[0], lineNr));
			} else {
				String tokens[]=lineTokens[0].split("\\s+");
				gcodes.add(GCodeFactory.createGcode(readString, tokens, lineNr));
			}
			lineNr++;
		}
		return gcodes;
	}
}
