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
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.tools.Ontology;

/**
 * Converts all string attributes to nominal attributes. Each string value is simply
 * used as nominal value of the new attribute. If the value is missing, the new value 
 * will be missing.
 * 
 * @author Ingo Mierswa
 * @version $Id: String2Nominal.java,v 1.1 2008/08/05 20:08:29 ingomierswa Exp $
 */
public class String2Nominal extends Operator {

	public String2Nominal(OperatorDescription description) {
		super(description);
	}

	public IOObject[] apply() throws OperatorException {
		ExampleSet exampleSet = getInput(ExampleSet.class);

		for (Attribute attribute : exampleSet.getAttributes()) {
			if (attribute.isNominal()) {
				Attribute newAttribute = AttributeFactory.changeValueType(attribute, Ontology.NOMINAL);
				exampleSet.getAttributes().replace(attribute, newAttribute);
			}
		}
		
		return new IOObject[] { exampleSet };
	}

	public Class<?>[] getInputClasses() {
		return new Class[] { ExampleSet.class };
	}

	public Class<?>[] getOutputClasses() {
		return new Class[] { ExampleSet.class };
	}
}
