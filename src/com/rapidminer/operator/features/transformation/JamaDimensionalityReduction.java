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
package com.rapidminer.operator.features.transformation;

import java.util.Iterator;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.InputDescription;
import com.rapidminer.operator.OperatorDescription;

import Jama.Matrix;

/**
 * This class represents an abstract framework for performing dimensionality reduction using the JAMA package.
 * 
 * @author Michael Wurst, Ingo Mierswa
 * @version $Id: JamaDimensionalityReduction.java,v 1.5 2008/05/09 19:22:51 ingomierswa Exp $
 * 
 */
public abstract class JamaDimensionalityReduction extends DimensionalityReducer {

	public JamaDimensionalityReduction(OperatorDescription description) {
		super(description);
	}

	protected abstract Matrix callMatrixMethod(ExampleSet es, int dimension, Matrix in);

	public InputDescription getInputDescription(Class cls) {
		if (ExampleSet.class.isAssignableFrom(cls)) {
			return new InputDescription(cls, false, true);
		}
		return super.getInputDescription(cls);
	}

	protected double[][] dimensionalityReduction(ExampleSet es, int dimensions) {
		// encode matrix
		Matrix in = new Matrix(es.size(), es.getAttributes().size());
		Iterator<Example> er = es.iterator();
		int count = 0;
		while (er.hasNext()) {
			Example e = er.next();
			int i = 0;
			for (Attribute attribute : e.getAttributes()) {
				in.set(count, i++, e.getValue(attribute));
			}

			count++;
		}
		Matrix result = callMatrixMethod(es, dimensions, in);

		// decode matrix
		return result.getArray();
	}
}
