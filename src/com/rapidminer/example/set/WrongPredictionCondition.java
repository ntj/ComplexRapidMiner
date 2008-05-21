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
package com.rapidminer.example.set;

import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;

/**
 * This subclass of {@link Condition} serves to accept all examples which are
 * wrongly predicted.
 * 
 * @author Ingo Mierswa
 * @version $Id: FalsePredictionCondition.java,v 2.2 2006/03/21 15:35:39
 *          ingomierswa Exp $
 */
public class WrongPredictionCondition implements Condition {

	private static final long serialVersionUID = -3254098600455281034L;

	/** Creates a new condition. */
	public WrongPredictionCondition() {}

	/**
	 * Throws an exception since this condition does not support parameter
	 * string.
	 */
	public WrongPredictionCondition(ExampleSet exampleSet, String parameterString) {
		if ((parameterString != null) && (parameterString.trim().length() != 0))
			throw new IllegalArgumentException("FalsePredictionCondition does not need any parameters!");
		if (exampleSet.getAttributes().getLabel() == null)
			throw new IllegalArgumentException("FalsePredictionCondition needs an example set with label attribute!");
		if (exampleSet.getAttributes().getPredictedLabel() == null)
			throw new IllegalArgumentException("FalsePredictionCondition needs an example set with predicted label attribute!");
	}

	/**
	 * Since the condition cannot be altered after creation we can just return
	 * the condition object itself.
	 * 
	 * @deprecated Conditions should not be able to be changed dynamically and hence there is no need for a copy
	 */
	@Deprecated
	public Condition duplicate() {
		return this;
	}

	/** Returns true if the example wrongly classified. */
	public boolean conditionOk(Example example) {
        return !example.equalValue(example.getAttributes().getLabel(), example.getAttributes().getPredictedLabel());
	}
}
