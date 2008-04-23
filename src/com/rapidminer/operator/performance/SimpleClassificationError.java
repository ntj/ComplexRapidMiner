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
package com.rapidminer.operator.performance;

/**
 * This class calculates the classification error without determining the complete
 * contingency table. This criterion should be used if several different
 * accuracy measurements are averaged. The usual classification error criterion cannot be
 * used in cases where the number of classes differ.
 * 
 * @author Michael Wurst, Ingo Mierswa
 * @version $Id: SimpleClassificationError.java,v 1.1 2007/06/06 11:21:33 ingomierswa Exp $
 */
public class SimpleClassificationError extends SimpleCriterion {

	private static final long serialVersionUID = -2679855049315180163L;

	public SimpleClassificationError() {
	}

	public SimpleClassificationError(SimpleClassificationError sc) {
		super(sc);
	}

	public double countExample(double label, double predictedLabel) {
		if (label == predictedLabel)
			return 0;
		else
			return 1;
	}

	public String getName() {
		return "simple_error";
	}

	public String getDescription() {
		return "Simple Classification Error";
	}
}
