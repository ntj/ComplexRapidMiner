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
package com.rapidminer.operator.visualization;

import Jama.Matrix;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.tools.math.matrix.CovarianceMatrix;

/** This operator calculates the covariances between all attributes
 *  of the input example set and returns a covariance matrix object
 *  which can be visualized. 
 *  
 *  @author Ingo Mierswa
 *  @version $Id: CovarianceMatrixOperator.java,v 1.2 2008/05/09 19:23:14 ingomierswa Exp $
 */
public class CovarianceMatrixOperator extends Operator {

	public CovarianceMatrixOperator(OperatorDescription description) {
		super(description);
	}

	@Override
	public IOObject[] apply() throws OperatorException {
		ExampleSet exampleSet = getInput(ExampleSet.class);
		String[] columnNames = new String[exampleSet.getAttributes().size()];
		int counter = 0;
		for (Attribute attribute : exampleSet.getAttributes()) {
			columnNames[counter++] = attribute.getName();
		}
		Matrix covarianceMatrix = CovarianceMatrix.getCovarianceMatrix(exampleSet);
		return new IOObject[] { exampleSet, new SymmetricalMatrix("Covariance", columnNames, covarianceMatrix) };
	}

	@Override
	public Class[] getInputClasses() {
		return new Class[] { ExampleSet.class };
	}

	@Override
	public Class[] getOutputClasses() {
		return new Class[] { ExampleSet.class, SymmetricalMatrix.class };
	}
}
