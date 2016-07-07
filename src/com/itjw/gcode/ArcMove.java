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
import java.util.logging.Level;
import java.util.logging.Logger;

import com.itjw.gcodefx.MainViewController;

abstract public class ArcMove extends AxisMove {

	private static final Logger logger = Logger.getLogger(ArcMove.class.getName());

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
					logger.log(Level.SEVERE, "Could not parse number '"+token.substring(1)+"' in line "+lineNr);
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
