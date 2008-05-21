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
package com.rapidminer.generator;

import java.util.ArrayList;
import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.ConstructionDescription;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.table.DataRow;
import com.rapidminer.example.table.ExampleTable;
import com.rapidminer.tools.LogService;
import com.rapidminer.tools.Ontology;


/**
 * Objects of this generator class have two numerical input attributes and one
 * output attribute.
 * 
 * @author Ingo Mierswa
 * @version $Id: BinaryNumericalGenerator.java,v 2.11 2006/03/27 13:21:58
 *          ingomierswa Exp $
 */
public abstract class BinaryNumericalGenerator extends FeatureGenerator {

	private static final Attribute[] INPUT_ATTR = { AttributeFactory.createAttribute(Ontology.NUMERICAL), AttributeFactory.createAttribute(Ontology.NUMERICAL) };

	public abstract double calculateValue(double value1, double value2);

	/** Must return true if this generator is commutative. */
	public abstract boolean isCommutative();

	/** Must return true if this generator is self applicable. */
	public abstract boolean isSelfApplicable();

	public Attribute[] getInputAttributes() {
		return INPUT_ATTR;
	}

	public Attribute[] getOutputAttributes(ExampleTable input) {
		Attribute a1 = getArgument(0);
		Attribute a2 = getArgument(1);
		Attribute ao = 
			AttributeFactory.createAttribute(Ontology.NUMERICAL, 
											 Ontology.SINGLE_VALUE, 
											 getFunction(), 
											 new ConstructionDescription[] { a1.getConstruction(), a2.getConstruction() });
		return new Attribute[] { ao };
	}

	/**
	 * Returns all compatible input attribute arrays for this generator from the
	 * given example set as list.
	 */
	public List<Attribute[]> getInputCandidates(ExampleSet exampleSet, int maxDepth, String[] functions) {
		List<Attribute[]> result = new ArrayList<Attribute[]>();
		if (getSelectionMode() == SELECTION_MODE_ALL) {
			for (Attribute first : exampleSet.getAttributes()) {
				if (!checkCompatibility(first, INPUT_ATTR[0], maxDepth, functions))
					continue;
				for (Attribute second : exampleSet.getAttributes()) {
					if (checkCompatibility(second, INPUT_ATTR[1], maxDepth, functions))
						result.add(new Attribute[] { first, second });
				}
			}
		} else {
			if (isCommutative() && isSelfApplicable()) {
				int firstCounter = 0;
				for (Attribute first : exampleSet.getAttributes()) {
					if (!checkCompatibility(first, INPUT_ATTR[0], maxDepth, functions))
						continue;
					int secondCounter = 0;
					for (Attribute second : exampleSet.getAttributes()) {
						if (secondCounter >= firstCounter) {
							if (checkCompatibility(second, INPUT_ATTR[1], maxDepth, functions))
								result.add(new Attribute[] { first, second });
						}
						secondCounter++;
					}
					firstCounter++;
				}
			} else if (isCommutative() && !isSelfApplicable()) {
				int firstCounter = 0;
				for (Attribute first : exampleSet.getAttributes()) {
					if (!checkCompatibility(first, INPUT_ATTR[0], maxDepth, functions))
						continue;
					int secondCounter = 0;
					for (Attribute second : exampleSet.getAttributes()) {
						if (secondCounter > firstCounter) {
							if (checkCompatibility(second, INPUT_ATTR[1], maxDepth, functions))
								result.add(new Attribute[] { first, second });
						}
						secondCounter++;
					}
					firstCounter++;
				}
			} else if (!isCommutative() && isSelfApplicable()) {
				for (Attribute first : exampleSet.getAttributes()) {
					if (!checkCompatibility(first, INPUT_ATTR[0], maxDepth, functions))
						continue;
					for (Attribute second : exampleSet.getAttributes()) {
						if (checkCompatibility(second, INPUT_ATTR[1], maxDepth, functions))
							result.add(new Attribute[] { first, second });
					}
				}
			} else if (!isCommutative() && !isSelfApplicable()) {
				int firstCounter = 0;
				for (Attribute first : exampleSet.getAttributes()) {
					if (!checkCompatibility(first, INPUT_ATTR[0], maxDepth, functions))
						continue;
					int secondCounter = 0;
					for (Attribute second : exampleSet.getAttributes()) {
						if (firstCounter != secondCounter) {
							if (checkCompatibility(second, INPUT_ATTR[1], maxDepth, functions))
								result.add(new Attribute[] { first, second });
						}
						secondCounter++;
					}
					firstCounter++;
				}
			}
		}
		return result;
	}

	public void generate(DataRow data) throws GenerationException {
		try {
			Attribute a0 = getArgument(0);
			Attribute a1 = getArgument(1);
			double o1 = data.get(a0);
			double o2 = data.get(a1);
			double r = calculateValue(o1, o2);

			if (Double.isInfinite(r)) {
				LogService.getGlobal().log(getFunction() + ": Infinite value generated.", LogService.WARNING);
			}
			if (Double.isNaN(r)) {
				LogService.getGlobal().log(getFunction() + ": NaN generated.", LogService.WARNING);
			}
			
			if (resultAttributes[0] != null)
				data.set(resultAttributes[0], r);
		} catch (ArrayIndexOutOfBoundsException ex) {
			throw new GenerationException("a1:" + getArgument(0) + " a2: " + getArgument(1), ex);
		}
	}

	public String toString() {
		String s = "binary function (";
		if ((resultAttributes != null) && (resultAttributes.length > 0) && (resultAttributes[0] != null))
			s += resultAttributes[0].getName() + ":=";
		if (argumentsSet())
			s += getArgument(0).getName() + " ";
		s += getFunction();
		if (argumentsSet())
			s += " " + getArgument(1).getName();
		s += ")";
		return s;
	}
}
