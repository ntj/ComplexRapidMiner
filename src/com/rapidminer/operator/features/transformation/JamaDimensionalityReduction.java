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
 * TODO: see super class
 * 
 * @author Michael Wurst, Ingo Mierswa
 * @version $Id: JamaDimensionalityReduction.java,v 1.2 2007/06/07 21:40:28 ingomierswa Exp $
 * 
 */
public abstract class JamaDimensionalityReduction extends DimensionalityReducer {

	public JamaDimensionalityReduction(OperatorDescription description) {
		super(description);
	}

	protected abstract Matrix callMatrixMethod(Matrix in);

	public InputDescription getInputDescription(Class cls) {
		if (ExampleSet.class.isAssignableFrom(cls)) {
			return new InputDescription(cls, false, true);
		}
		return super.getInputDescription(cls);
	}

	protected void dimensionalityReduction() {
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
		Matrix result = callMatrixMethod(in);

		// decode matrix
		p = result.getArray();
	}
}
