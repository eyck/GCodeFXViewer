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

import javafx.geometry.Point3D;

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
		Point3D current= new Point3D(0,0,0);
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
