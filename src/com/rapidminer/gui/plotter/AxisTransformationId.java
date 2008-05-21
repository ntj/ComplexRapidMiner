/*
 *  RapidMiner
 *
 *  Copyright (C) 2001-2008 by Rapid-I and the contributors
 *
 *  Complete list of developers available at our web site:
 *
 *       http://rapid-i.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package com.rapidminer.gui.plotter;

import java.text.DecimalFormat;

/**
 * Transforms the given value by just returning it.
 * 
 * @author Ingo Mierswa
 * @version $Id: AxisTransformationId.java,v 1.3 2008/05/09 19:22:51 ingomierswa Exp $
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
