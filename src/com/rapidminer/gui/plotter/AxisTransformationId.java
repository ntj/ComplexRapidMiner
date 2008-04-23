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

import java.text.DecimalFormat;

/**
 * Transforms the given value by just returning it.
 * 
 * @author Ingo Mierswa
 * @version $Id: AxisTransformationId.java,v 1.1 2007/05/27 21:59:04 ingomierswa Exp $
 */
public class AxisTransformationId implements AxisTransformation {

	private DecimalFormat format = new DecimalFormat("0.00E0");
	
	/** Transforms the given value by just returning it. */
	public double transform(double value) {
		return value;
	}

	/** Transforms the given value by just returning it. */
	public double inverseTransform(double value) {
		return value;
	}
	
	/** Returns the formatted value. */
	public String format(double value, int formatNumber) {
		return format.format(value);
	}
	
	/** Adapts the minimum corresponding to the given tic size. */
	public double adaptTicsMin(double min, double ticSize) {
		return Math.floor(min / ticSize) * ticSize;
	}
	
	/** Adapts the maximum corresponding to the given tic size. */
	public double adaptTicsMax(double max, double ticSize) {
		return Math.ceil(max / ticSize) * ticSize;
	}
}
