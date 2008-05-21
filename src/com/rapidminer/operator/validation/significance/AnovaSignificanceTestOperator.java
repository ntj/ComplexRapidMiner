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
package com.rapidminer.operator.validation.significance;

import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.performance.PerformanceCriterion;
import com.rapidminer.operator.performance.PerformanceVector;
import com.rapidminer.tools.math.AnovaCalculator;
import com.rapidminer.tools.math.SignificanceCalculationException;
import com.rapidminer.tools.math.SignificanceTestResult;


/**
 * Determines if the null hypothesis (all actual mean values are the same) holds
 * for the input performance vectors. This operator uses an ANalysis Of
 * VAriances approach to determine probability that the null hypothesis is
 * wrong.
 * 
 * @author Ingo Mierswa
 * @version $Id: AnovaSignificanceTestOperator.java,v 1.5 2006/03/21 15:35:52
 *          ingomierswa Exp $
 */
public class AnovaSignificanceTestOperator extends SignificanceTestOperator {

	public AnovaSignificanceTestOperator(OperatorDescription description) {
		super(description);
	}

	public SignificanceTestResult performSignificanceTest(PerformanceVector[] allVectors, double alpha) throws OperatorException {
		AnovaCalculator calculator = new AnovaCalculator();
		calculator.setAlpha(alpha);

		for (int i = 0; i < allVectors.length; i++) {
			PerformanceCriterion pc = allVectors[i].getMainCriterion();
			calculator.addGroup(pc.getAverageCount(), pc.getAverage(), pc.getVariance());
		}

		try {
			return calculator.performSignificanceTest();
		} catch (SignificanceCalculationException e) {
			throw new UserError(this, 920, e.getMessage());
		}
	}

	public int getMinSize() {
		return 2;
	}

	public int getMaxSize() {
		return Integer.MAX_VALUE;
	}
}
