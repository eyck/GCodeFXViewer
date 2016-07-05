package com.itjw.gcode;

import java.math.BigDecimal;

abstract public class AxisMove extends AbstractGCode {

	BigDecimal x, y, z, e, f;

	public AxisMove(String raw, String[] tokens, int lineNr) {
		super(raw, tokens, lineNr);
	}

	protected void parseTokens(String[] tokens, int lineNr) {
		if(tokens!=null){
			for(String token: tokens){
				try {
					switch(token.toUpperCase().charAt(0)){
					case 'X':
						x=new BigDecimal(token.substring(1));
						break;
					case 'Y':
						y=new BigDecimal(token.substring(1));
						break;
					case 'Z':
						z=new BigDecimal(token.substring(1));
						break;
					case 'E':
						e=new BigDecimal(token.substring(1));
						break;
					case 'F':
						f=new BigDecimal(token.substring(1));
						break;
					default:
						break;
					}
				} catch(java.lang.NumberFormatException exeption){
					System.out.println("Could not parse number '"+token.substring(1)+"' in line "+lineNr);
				}
			}
		}
	}

	public Double getX() {
		return x!=null?x.doubleValue():null;
	}

	public Double getY() {
		return y!=null?y.doubleValue():null;
	}

	public Double getZ() {
		return z!=null?z.doubleValue():null;
	}

	public Double getE() {
		return e!=null?e.doubleValue():null;
	}

	public Double getF() {
		return f!=null?f.doubleValue():null;
	}

}
