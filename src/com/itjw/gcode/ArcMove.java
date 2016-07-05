package com.itjw.gcode;

import java.math.BigDecimal;

abstract public class ArcMove extends AxisMove {

	BigDecimal i, j, k;

	public ArcMove(String raw, String[] tokens, int lineNr) {
		super(raw, tokens, lineNr);
	}

	protected void parseTokens(String[] tokens, int lineNr) {
		if(tokens!=null){
			for(String token: tokens){
				try {
					switch(token.toUpperCase().charAt(0)){
					case 'I':
						i=new BigDecimal(token.substring(1));
						break;
					case 'J':
						j=new BigDecimal(token.substring(1));
						break;
					case 'K':
						k=new BigDecimal(token.substring(1));
						break;
					default:
						super.parseTokens(tokens, lineNr);
						break;
					}
				} catch(java.lang.NumberFormatException exeption){
					System.out.println("Could not parse number '"+token.substring(1)+"' in line "+lineNr);
				}
			}
		}
	}

	public Double getI() {
		return i!=null?i.doubleValue():null;
	}

	public Double getJ() {
		return j!=null?j.doubleValue():null;
	}

}
