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
package com.rapidminer.operator.performance;

/**
 * The absolute error: <i>Sum(|label-predicted|)/#examples</i>. Mean absolue
 * error is the average of the difference between predicted and actual value in
 * all test cases; it is the average prediction error.
 * 
 * @author Ingo Mierswa
 * @version $Id: AbsoluteError.java,v 1.3 2008/05/09 19:22:43 ingomierswa Exp $
 */
public class AbsoluteError extends SimpleCriterion {

	private static final long serialVersionUID = 1113614384637128963L;

	public AbsoluteError() {
	}

	public AbsoluteError(AbsoluteError ae) {
		super(ae);
	}

	public double countExample(double label, double predictedLabel) {
		double dif = Math.abs(label - predictedLabel);
		return dif;
	}

	public String getName() {
		return "absolute_error";
	}

	public String getDescription() {
		return "Average absolute deviation of the prediction from the actual value";
	}
}
