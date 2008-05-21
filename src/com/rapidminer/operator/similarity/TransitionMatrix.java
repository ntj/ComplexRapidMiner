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

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeString;
import com.rapidminer.tools.Ontology;
import com.rapidminer.tools.math.matrix.Matrix;
import com.rapidminer.tools.math.matrix.SimpleSparseMatrix;

/**
 * Creates a transition matrix for a given nominal attribute. An entry v(x,y) in the matrix
 * denotes the conditional probability that value y occurs, after value x occurred.
 * 
 * @author Michael Wurst
 * @version $Id: TransitionMatrix.java,v 1.3 2008/05/09 19:22:52 ingomierswa Exp $
 *
 */
public class TransitionMatrix extends Operator {

	public TransitionMatrix(OperatorDescription description) {
		super(description);
	}

	@Override
	public IOObject[] apply() throws OperatorException {
		
		ExampleSet exampleSet = getInput(ExampleSet.class);
		
		Iterator<Example> it = exampleSet.iterator();
		
		String attName = getParameterAsString("attribute");
		
		Attribute attribute = exampleSet.getAttributes().get(attName);
		
		Attribute groupAttribute = null;
		
		if(isParameterSet("group_attribute"))
			groupAttribute = exampleSet.getAttributes().get(getParameterAsString("group_attribute"));
		
		if(!Ontology.ATTRIBUTE_VALUE_TYPE.isA(attribute.getValueType(), Ontology.NOMINAL))
			throw new UserError(null, 103, "TransitionMatrix" , attribute);
		
		SimpleSparseMatrix<String, String> matrix = new SimpleSparseMatrix<String, String>();

		Set<String> items = new HashSet<String>();
		String oldValue = null;
		double oldGroup = Double.NaN;
		
		while(it.hasNext()) {
		
			Example e = it.next();
			String value = e.getNominalValue(attribute);
			
			boolean consider = true;
			if(groupAttribute != null)
				if(e.getValue(groupAttribute) != oldGroup)
					consider = false;
				
			if((oldValue != null)&&consider)
				matrix.incEntry(oldValue, value, 1.0);
			items.add(value);
			oldValue = value;
			if(groupAttribute != null)
				oldGroup = e.getValue(groupAttribute);
			
		}
		
		for(String item: items) {
			
			double count = 0.0;
			for(String item2: items)	
				count = count + matrix.getEntry(item, item2);
				
			for(String item2: items)
				if(count > 0)
					matrix.setEntry(item, item2, matrix.getEntry(item, item2)/count);
				else
					matrix.setEntry(item, item2, 0);

		}
		
		return new IOObject[]{matrix};
	}

	@Override
	public Class[] getInputClasses() {
		return new Class[]{ExampleSet.class};
	}

	@Override
	public Class[] getOutputClasses() {
		return new Class[]{Matrix.class};
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();

		
		types.add(new ParameterTypeString("attribute", "the name of a nominal attribute for which the transition matrix should be created.", false));
		types.add(new ParameterTypeString("group_attribute", "provides a groups for examples, transitions between groups are not considered.", true));

		
		return types;
	}
	
}
