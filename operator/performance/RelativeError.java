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
 * The average relative error: <i>Sum(|label-predicted|/label)/#examples</i>.
 * The relative error of label 0 and prediction 0 is defined as 0, the relative
 * error of label 0 and prediction != 0 is infinite.
 * 
 * @author Stefan Rueping
 * @version $Id: RelativeError.java,v 1.3 2008/05/09 19:22:43 ingomierswa Exp $
 */
public class RelativeError extends SimpleCriterion {

	private static final long serialVersionUID = 203943264201733699L;

	public RelativeError() {
	}

	public RelativeError(RelativeError sc) {
		super(sc);
	}

	public double countExample(double label, double predictedLabel) {
		double dif = Math.abs(label - predictedLabel);
		if (dif != 0.0d) {
			dif /= label;
		}
		return dif;
	}

	public String getName() {
		return "relative_error";
	}

	public String getDescription() {
		return "Average relative error (average of absolute deviation of the prediction from the actual value divided by actual value)";
	}
}
