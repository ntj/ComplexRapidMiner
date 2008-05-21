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
package com.rapidminer.operator.meta;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.AttributeRole;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.OperatorChain;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.condition.InnerOperatorCondition;
import com.rapidminer.operator.condition.SimpleChainInnerOperatorCondition;


/**
 * Performs the inner operator for all label attributes, i.e. special attributes
 * whose name starts with &quot;label&quot;. In each iteration one of the
 * multiple labels is used as label. The results of the inner operators are
 * collected and returned. The example set will be consumed during the
 * iteration.
 * 
 * @author Ingo Mierswa
 * @version $Id: MultipleLabelIterator.java,v 1.9 2006/04/05 08:57:26
 *          ingomierswa Exp $
 */
public class MultipleLabelIterator extends OperatorChain {

	public MultipleLabelIterator(OperatorDescription description) {
		super(description);
	}

	public IOObject[] apply() throws OperatorException {
		ExampleSet exampleSet = getInput(ExampleSet.class);

		Attribute[] labels = getLabels(exampleSet);
		if (labels.length == 0)
			throw new UserError(this, 105);

		List<IOObject> outputs = new LinkedList<IOObject>();
		for (int i = 0; i < labels.length; i++) {
			ExampleSet cloneSet = (ExampleSet) exampleSet.clone();
			cloneSet.getAttributes().setLabel(labels[i]);
			IOContainer input = new IOContainer(new IOObject[] { cloneSet });
			for (int o = 0; o < getNumberOfOperators(); o++) {
				input = getOperator(o).apply(input);
			}
			// add the result ioobjects to the result list
			IOObject[] innerResult = input.getIOObjects();
			for (int j = 0; j < innerResult.length; j++)
				outputs.add(innerResult[j]);
            
            inApplyLoop();
		}

		IOObject[] result = new IOObject[outputs.size()];
		outputs.toArray(result);
		return result;
	}

	public Class[] getInputClasses() {
		return new Class[] { ExampleSet.class };
	}

	public Class[] getOutputClasses() {
		if (getNumberOfOperators() == 0)
			return new Class[0];
		else
			return getOperator(getNumberOfOperators() - 1).getOutputClasses();
	}

	public int getMinNumberOfInnerOperators() {
		return 1;
	}

	public int getMaxNumberOfInnerOperators() {
		return Integer.MAX_VALUE;
	}
	
	/**
	 * Returns true since this operator chain should just return the output of
	 * the last inner operator.
	 */
	public boolean shouldReturnInnerOutput() {
		return true;
	}

	/** Returns a simple chain condition. */
	public InnerOperatorCondition getInnerOperatorCondition() {
		return new SimpleChainInnerOperatorCondition();
	}

	private Attribute[] getLabels(ExampleSet exampleSet) {
		List<Attribute> attributes = new LinkedList<Attribute>();
		Iterator<AttributeRole> i = exampleSet.getAttributes().specialAttributes();
		while (i.hasNext()) {
			AttributeRole role = i.next();
			String name = role.getSpecialName();
			if (name.startsWith(Attributes.LABEL_NAME))
				attributes.add(role.getAttribute());
		}
		Attribute[] result = new Attribute[attributes.size()];
		attributes.toArray(result);
		return result;
	}
}
