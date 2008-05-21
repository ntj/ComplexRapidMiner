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
package com.rapidminer.operator.preprocessing.filter;

import java.util.Iterator;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.tools.Ontology;


/**
 * <p>
 * This operator iterates over an example set with numeric label and converts the 
 * label values to either the class 'up' or the class 'down' based on whether the 
 * change from the previous label is positive or negative. Please note that this 
 * does not make sense on example sets where the examples are not ordered in some 
 * sense (like, e.g. ordered according to time). This operator might become useful
 * in the context of a {@link Series2WindowExamples} operator. 
 * </p>
 * 
 * @author Ingo Mierswa
 * @version $Id: LabelTrend2Classification.java,v 1.3 2008/05/09 19:22:58 ingomierswa Exp $
 */
public class LabelTrend2Classification extends Operator {

	public LabelTrend2Classification(OperatorDescription description) {
		super(description);
	}

	public IOObject[] apply() throws OperatorException {
		ExampleSet exampleSet = getInput(ExampleSet.class);
		
		Attribute label = exampleSet.getAttributes().getLabel();
		// some checks
		if (label == null) {
			throw new UserError(this, 105);
		}
		if (label.isNominal()) {
			throw new UserError(this, 102, getName(), label.getName());
		}
		
		Attribute newLabel = AttributeFactory.createAttribute(Attributes.LABEL_NAME, Ontology.BINOMINAL);
		newLabel.getMapping().mapString("up");
		newLabel.getMapping().mapString("down");
		
		exampleSet.getExampleTable().addAttribute(newLabel);
		exampleSet.getAttributes().addRegular(newLabel);
		
		Iterator<Example> i = exampleSet.iterator();
		double lastValue = 0.0d;
		while (i.hasNext()) {
			Example example = i.next();
			double currentValue = example.getLabel();
			if (currentValue > lastValue) {
				example.setValue(newLabel, "up");
			} else {
				example.setValue(newLabel, "down");
			}
			lastValue = currentValue;
		}
		
		exampleSet.getAttributes().remove(newLabel);
		exampleSet.getAttributes().setLabel(newLabel);
		
		return new IOObject[] { exampleSet };
	}

	public Class[] getInputClasses() {
		return new Class[] { ExampleSet.class };
	}

	public Class[] getOutputClasses() {
		return new Class[] { ExampleSet.class };
	}
}
