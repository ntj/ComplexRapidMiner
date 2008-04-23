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

import java.util.List;

import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeDouble;


/**
 * Wraps a {@link MinMaxCriterion} around each performance criterion of type
 * MeasuredPerformance. This criterion uses the minimum fitness achieved instead
 * of the average fitness or arbitrary weightings of both. Please note that the
 * average values stay the same and only the fitness values change.
 * 
 * @author Ingo Mierswa
 * @version $Id: MinMaxWrapper.java,v 1.2 2007/06/15 16:58:38 ingomierswa Exp $
 */
public class MinMaxWrapper extends Operator {


	/** The parameter name for &quot;Defines the weight for the minimum fitness agains the average fitness&quot; */
	public static final String PARAMETER_MINIMUM_WEIGHT = "minimum_weight";
	public MinMaxWrapper(OperatorDescription description) {
		super(description);
	}

	public IOObject[] apply() throws OperatorException {
		PerformanceVector performanceVector = getInput(PerformanceVector.class);
		PerformanceVector result = new PerformanceVector();
		for (int i = 0; i < performanceVector.size(); i++) {
			PerformanceCriterion crit = performanceVector.getCriterion(i);
			if (crit instanceof MeasuredPerformance)
				result.addCriterion(new MinMaxCriterion((MeasuredPerformance) crit, getParameterAsDouble(PARAMETER_MINIMUM_WEIGHT)));
		}
		result.setMainCriterionName(performanceVector.getMainCriterion().getName());
		return new IOObject[] { result };
	}

	public Class[] getOutputClasses() {
		return new Class[] { PerformanceVector.class };
	}

	public Class[] getInputClasses() {
		return new Class[] { PerformanceVector.class };
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		ParameterType type = new ParameterTypeDouble(PARAMETER_MINIMUM_WEIGHT, "Defines the weight for the minimum fitness agains the average fitness", 0.0d, 1.0d, 1.0d);
		type.setExpert(false);
		types.add(type);
		return types;
	}
}
