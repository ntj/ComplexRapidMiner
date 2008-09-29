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
package com.rapidminer.operator.learner.meta;

import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.operator.Model;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.tools.Ontology;

/**
 * This meta regression learner transforms the label on-the-fly relative
 * to the value of the specified attribute. This is done right before 
 * the inner regression learner is applied. This can be useful in order
 * to allow time series predictions on data sets with large trends.
 *  
 * @author Ingo Mierswa
 * @version $Id: RelativeRegression.java,v 1.3 2008/07/24 15:23:20 ingomierswa Exp $
 */
public class RelativeRegression extends AbstractMetaLearner {

	public static final String PARAMETER_RELATIVE_ATTRIBUTE = "relative_attribute";
	
	public RelativeRegression(OperatorDescription description) {
		super(description);
	}

	public Model learn(ExampleSet exampleSet) throws OperatorException {		
		// attribute retrieval
		int relativeAttributeIndex = getParameterAsInt(PARAMETER_RELATIVE_ATTRIBUTE);

		// checks 1
		if (Math.abs(relativeAttributeIndex) > exampleSet.getAttributes().size()) {
			throw new UserError(this, 207, new Object[] { relativeAttributeIndex, PARAMETER_RELATIVE_ATTRIBUTE, "must be between 1 and the number of attributes or between -1 and the negative number of attributes" } );
		}
		
		if (relativeAttributeIndex == 0) {
			throw new UserError(this, 207, new Object[] { relativeAttributeIndex, PARAMETER_RELATIVE_ATTRIBUTE, "must be between 1 and the number of attributes or between -1 and the negative number of attributes" } );
		}
		
		int headIndex = relativeAttributeIndex;
		if (relativeAttributeIndex < 0) {
			headIndex = exampleSet.getAttributes().size() + relativeAttributeIndex;
		}
		
		Attribute relativeAttribute = null;
		if (headIndex > 0) {
			int counter = 0;
			for (Attribute a : exampleSet.getAttributes()) {
				if (counter == headIndex) {
					relativeAttribute = a;
					break;
				}
				counter++;
			}
		}
		
		// checks 2
		if (relativeAttribute == null) {
			throw new UserError(this, 111, "counter: " + relativeAttributeIndex);
		}
		
		if (!relativeAttribute.isNumerical()) {
			throw new UserError(this, 120, new Object[] { relativeAttribute.getName(), Ontology.VALUE_TYPE_NAMES[relativeAttribute.getValueType()], Ontology.VALUE_TYPE_NAMES[Ontology.NUMERICAL]} );
		}
		
		String relativeAttributeName = relativeAttribute.getName();
		
		// create transformed label
		Attribute originalLabel = exampleSet.getAttributes().getLabel();
		Attribute transformedLabel = AttributeFactory.createAttribute(originalLabel, "Relative");
		exampleSet.getExampleTable().addAttribute(transformedLabel);
		exampleSet.getAttributes().addRegular(transformedLabel);

		for (Example e : exampleSet) {
			double originalLabelValue = e.getValue(originalLabel);
			double relativeValue = e.getValue(relativeAttribute);
			e.setValue(transformedLabel, originalLabelValue - relativeValue);
		}
		
		exampleSet.getAttributes().remove(originalLabel);
		exampleSet.getAttributes().setLabel(transformedLabel);
		
		// base model learning
		Model baseModel = applyInnerLearner(exampleSet);
			
		// clean up
		exampleSet.getAttributes().remove(transformedLabel);
		exampleSet.getExampleTable().removeAttribute(transformedLabel);
		exampleSet.getAttributes().setLabel(originalLabel);
		
		return new RelativeRegressionModel(exampleSet, baseModel, relativeAttributeName);
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		ParameterType type = new ParameterTypeInt(PARAMETER_RELATIVE_ATTRIBUTE, "Indicates which attribute should be used as a base for the relative comparison (counting starts with 1 or -1; negative: counting starts with the last; positive: counting starts with the first).", -Integer.MAX_VALUE, Integer.MAX_VALUE, -1);
		type.setExpert(false);
		types.add(type);
		return types;
	}
}
