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
package com.rapidminer.operator.preprocessing.discretization;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.tools.Ontology;
import com.rapidminer.tools.Tools;


/**
 * The superclass of all discretization filters.
 * 
 * @author Ingo Mierswa
 * @version $Id: Discretization.java,v 1.3 2007/07/13 22:52:14 ingomierswa Exp $
 */
public abstract class Discretization extends Operator {

	public Discretization(OperatorDescription description) {
		super(description);
	}

	/**
	 * Delivers the maximum range thresholds for all attributes, i.e. the value
	 * getRanges()[a][b] is the b-th threshold for the a-th attribute.
	 */
	public abstract double[][] getRanges(ExampleSet exampleSet) throws OperatorException;

	public IOObject[] apply() throws OperatorException {
		ExampleSet exampleSet = (ExampleSet)getInput(ExampleSet.class).clone();
		exampleSet.recalculateAllAttributeStatistics();

		double[][] ranges = getRanges(exampleSet);

		// change data
		for (Example example : exampleSet) {
			int a = 0;
			for (Attribute attribute : exampleSet.getAttributes()) {
				if (!attribute.isNominal() && (ranges[a] != null)) {
					double value = example.getValue(attribute);
					for (int b = 0; b < ranges[a].length; b++) {
						if (Tools.isLessEqual(value, ranges[a][b])) {
                            example.setValue(attribute, b);
							break;
						}
					}
				}
				a++;
			}
		}
        
        // change value type
        int a = 0;
        for (Attribute attribute : exampleSet.getAttributes()) {
            attribute = exampleSet.getAttributes().replace(attribute, AttributeFactory.changeValueType(attribute, Ontology.NOMINAL));
            for (int b = 0; b < ranges[a].length; b++) {
                attribute.getMapping().mapString("range" + (b + 1));
            }
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
