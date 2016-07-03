package com.itjw.gcode;

import java.math.BigDecimal;

import javafx.geometry.Point3D;

public abstract class AbstractGCode{
	String raw;
	String[] tokens;
	int lineNr;
	BigDecimal x, y, z, e, f, i, j;
	Point3D start;

	public AbstractGCode(String raw, String[] tokens, int lineNr) {
		super();
		this.raw = raw;
		this.tokens = tokens;
		this.lineNr = lineNr;
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
					case 'I':
						i=new BigDecimal(token.substring(1));
						break;
					case 'J':
						j=new BigDecimal(token.substring(1));
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

	public String getCode() {
		return tokens[0];
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

	public Double getI() {
		return i!=null?i.doubleValue():null;
	}

	public Double getJ() {
		return j!=null?j.doubleValue():null;
	}

	public Double getE() {
		return e!=null?e.doubleValue():null;
	}

	public Double getF() {
		return f!=null?f.doubleValue():null;
	}

	public Double getS() {
		return null;
	}

	public Double getP() {
		return null;
	}

	public void setStart(Point3D start) {
		this.start = start;
	}

	public Point3D getStart() {
		return start;
	}

	public Point3D getEnd() {
		return new Point3D(x!=null?x.doubleValue():start!=null?start.getX():0.0, y!=null?y.doubleValue():start!=null?start.getY():0.0, z!=null?z.doubleValue():start!=null?start.getZ():0.0);
	}

	public Integer getLineNo(){
		return lineNr;
	}

	abstract public Boolean process(IMachineProcessor machine);
}
