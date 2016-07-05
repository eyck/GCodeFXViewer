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
