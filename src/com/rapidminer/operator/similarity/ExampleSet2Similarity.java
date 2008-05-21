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
package com.rapidminer.operator.similarity;

import java.util.List;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.InputDescription;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.parameter.ParameterType;


/**
 * This class represents an operator that creates a similarity measure based on an ExampleSet.
 * 
 * @author Michael Wurst, Ingo Mierswa
 * @version $Id: ExampleSet2Similarity.java,v 1.4 2008/05/09 19:22:52 ingomierswa Exp $
 */
public class ExampleSet2Similarity extends Operator {

	public ExampleSet2Similarity(OperatorDescription description) {
		super(description);
	}

	public IOObject[] apply() throws OperatorException {
		ExampleSet es = (ExampleSet) getInput(ExampleSet.class).clone();		
		return new IOObject[] {
			SimilarityUtil.resolveSimilarityMeasure(getParameters(), null, es)
		};
	}

	public InputDescription getInputDescription(Class cls) {
		if (cls.equals(ExampleSet.class)) {
			return new InputDescription(cls, true, true);
		} else {
			return super.getInputDescription(cls);
		}
	}

	public Class[] getInputClasses() {
		return new Class[] {
			ExampleSet.class
		};
	}

	public Class[] getOutputClasses() {
		return new Class[] {
			SimilarityMeasure.class
		};
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		types.add(SimilarityUtil.generateSimilarityParameter());
		return types;
	}
}
