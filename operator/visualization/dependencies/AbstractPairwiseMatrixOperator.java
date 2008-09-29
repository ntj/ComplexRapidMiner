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
package com.rapidminer.operator.visualization.dependencies;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;


/**
 * <p>This operator calculates a dependency matrix between all attributes of the
 * input example set. This operator simply produces a dependency matrix like, for 
 * example, a correlation matrix. Such matrixed up to now cannot be used by other 
 * operators but can be displayed to the user in the result tab.</p> 
 * 
 * <p>Please note that this simple implementation
 * performs a data scan for each attribute combination and might therefore take
 * some time for non-memory example tables.</p>
 * 
 * @author Ingo Mierswa
 * @version $Id: AbstractPairwiseMatrixOperator.java,v 1.1 2008/08/25 08:10:33 ingomierswa Exp $
 */
public abstract class AbstractPairwiseMatrixOperator extends Operator {
	
	public AbstractPairwiseMatrixOperator(OperatorDescription description) {
		super(description);
	}

	public abstract String getMatrixName();
	
	public abstract double getMatrixValue(ExampleSet exampleSet, Attribute firstAttribute, Attribute secondAttribute);
	
	/** This default implementation does nothing. Subclasses might calculate for example a discretization
	 *  but should either deliver a new view or a fresh example set in order to not change the underlying
	 *  data. */
	protected ExampleSet performPreprocessing(ExampleSet exampleSet) throws OperatorException {
		return exampleSet;
	}
	
	public IOObject[] apply() throws OperatorException {
		ExampleSet eSet = getInput(ExampleSet.class);
		
		// discretize values (view!)
		ExampleSet exampleSet = performPreprocessing(eSet);
		
		// calculate mutual information
		SymmetricalMatrix matrix = new SymmetricalMatrix(getMatrixName(), exampleSet);
		int k = 0;
		for (Attribute firstAttribute : exampleSet.getAttributes()) {
			int l = 0;
			for (Attribute secondAttribute : exampleSet.getAttributes()) {
				matrix.setValue(k, l, getMatrixValue(exampleSet, firstAttribute, secondAttribute));
				checkForStop();
				l++;
			}
			k++;
		}
		
		return new IOObject[] { eSet, matrix };
	}

	public Class<?>[] getInputClasses() {
		return new Class[] { ExampleSet.class };
	}

	public Class<?>[] getOutputClasses() {
		return new Class[] { ExampleSet.class, SymmetricalMatrix.class };
	}
}
