/*
 *  RapidMiner
 *
 *  Copyright (C) 2001-2007 by Rapid-I and the contributors
 *
 *  Complete list of developers available at our web site:
 *
 *       http://rapid-i.com
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License as 
 *  published by the Free Software Foundation; either version 2 of the
 *  License, or (at your option) any later version. 
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 *  USA.
 */
package com.rapidminer.parameter;


/**
 * A parameter type for parameter matrices. Operators ask for the matrix of the
 * specified values with
 * {@link com.rapidminer.operator.Operator#getParameterAsMatrix(String)}.
 * 
 * @author Helge Homburg, Ingo Mierswa
 * @version $Id: ParameterTypeMatrix.java,v 1.2 2007/05/29 00:13:05 ingomierswa Exp $
 */
public class ParameterTypeMatrix extends ParameterTypeString {

	private static final long serialVersionUID = 0L;
	
	private boolean isSquare = false;
	
	public ParameterTypeMatrix(String key, String description, boolean isSquare) {
		this(key, description, isSquare, true);
	}
	
	public ParameterTypeMatrix(String key, String description, boolean isSquare, boolean isOptional) {
		super(key, description, isOptional);
		this.isSquare = isSquare;
	}
	
	public void setStatus(boolean isSquare) {
		this.isSquare = isSquare;
	}
	
	public boolean getStatus() {
		return this.isSquare;
	}
}
