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
package com.rapidminer.gui.plotter;

/**
 * Transforms the given value, e.g. by just returning it (id) or applying a 
 * log function.
 * 
 * @author Ingo Mierswa
 * @version $Id: AxisTransformation.java,v 1.1 2007/05/27 21:59:05 ingomierswa Exp $
 */
public interface AxisTransformation {

	/** Transforms the given value, e.g. by just returning it (id) or applying a 
	 *  log function. Please note that this method might throw an IllegalArgumentException 
	 *  if the number is not supported. */
	public double transform(double value);

	/** Returns the inverse transformation of the given value, e.g. just returning it (id) or applying a 
	 *  exponential function (for log transformation). Please note that this method might throw an 
	 *  IllegalArgumentException if the number is not supported. */
	public double inverseTransform(double value);
	
	/** Returns the formatted value. Might return null. The format number indicates the number of 
	 *  format calls done before. */
	public String format(double value, int formatNumber);
	
	/** Adapts the minimum corresponding to the given tic size. */
	public double adaptTicsMin(double min, double ticSize);
	
	/** Adapts the maximum corresponding to the given tic size. */
	public double adaptTicsMax(double max, double ticSize);
	
}
