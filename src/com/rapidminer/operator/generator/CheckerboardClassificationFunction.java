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
package com.rapidminer.operator.generator;

/**
 * Returns a checkerboard classification function. Each tile has size 5.
 * Attribute values should be positive.
 * 
 * @author Ingo Mierswa
 * @version $Id: CheckerboardClassificationFunction.java,v 1.1 2007/05/27 21:58:46 ingomierswa Exp $
 */
public class CheckerboardClassificationFunction extends ClassificationFunction {

	private static final double NUMBER = 5.0d;

	public double calculate(double[] att) throws FunctionException {
		if (att.length != 2)
			throw new FunctionException("Checkerboard classification function", "needs 2 attributes!");
		if ((int) (att[0] / NUMBER) % 2 == 0) {
			if ((int) (att[1] / NUMBER) % 2 == 0)
				return getLabel().getMapping().mapString("positive");
			else
				return getLabel().getMapping().mapString("negative");
		} else {
			if ((int) (att[1] / NUMBER) % 2 == 0)
				return getLabel().getMapping().mapString("negative");
			else
				return getLabel().getMapping().mapString("positive");
		}
	}
}
