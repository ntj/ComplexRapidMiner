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

import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.tools.Ontology;


/**
 * Transforms all regular attributes of a given example set into a value series.
 * All attributes must have the same value type. Attributes with block type
 * value series can be used by special feature extraction operators or by the
 * operators from the value series plugin.
 * 
 * @author Ingo Mierswa
 * @version $Id: SingleAttributes2ValueSeries.java,v 1.9 2006/04/05 08:57:27
 *          ingomierswa Exp $
 */
public class SingleAttributes2ValueSeries extends Operator {

	public SingleAttributes2ValueSeries(OperatorDescription description) {
		super(description);
	}

	public IOObject[] apply() throws OperatorException {
		ExampleSet exampleSet = getInput(ExampleSet.class);

		// change attribute type
		int valueType = Ontology.ATTRIBUTE_VALUE;
		int a = 0;
		for (Attribute attribute : exampleSet.getAttributes()) {
			if (a == 0) {
				valueType = attribute.getValueType();
			}
			if (!Ontology.ATTRIBUTE_BLOCK_TYPE.isA(attribute.getBlockType(), Ontology.SINGLE_VALUE)) {
				throw new UserError(this, 121, new Object[] { attribute, Ontology.BLOCK_TYPE_NAMES[attribute.getBlockType()], Ontology.BLOCK_TYPE_NAMES[Ontology.SINGLE_VALUE] });
			}
			if (!Ontology.ATTRIBUTE_VALUE_TYPE.isA(attribute.getValueType(), valueType)) {
				throw new UserError(this, 120, new Object[] { attribute, Ontology.VALUE_TYPE_NAMES[attribute.getValueType()], Ontology.VALUE_TYPE_NAMES[valueType] });
			}

			if (a == 0) {
				attribute.setBlockType(Ontology.VALUE_SERIES_START);
			} else if (a == (exampleSet.getAttributes().size() - 1)) {
				attribute.setBlockType(Ontology.VALUE_SERIES_END);
			} else {
				attribute.setBlockType(Ontology.VALUE_SERIES);
			}
			checkForStop();
			a++;
		}

		return new IOObject[] { exampleSet };
	}

	public Class[] getOutputClasses() {
		return new Class[] { ExampleSet.class };
	}

	public Class[] getInputClasses() {
		return new Class[] { ExampleSet.class };
	}
}
