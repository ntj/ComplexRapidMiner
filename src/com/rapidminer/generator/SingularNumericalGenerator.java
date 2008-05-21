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
 * Generators of this class will have one numerical input attribute and one
 * output attribute.
 * 
 * @author Ingo Mierswa
 * @version $Id: SingularNumericalGenerator.java,v 2.13 2006/03/27 13:21:58
 *          ingomierswa Exp $
 */
public abstract class SingularNumericalGenerator extends FeatureGenerator {

	private static final Attribute[] INPUT_ATTR = { AttributeFactory.createAttribute(Ontology.NUMERICAL) };

	public SingularNumericalGenerator() {}

	/**
	 * Subclasses have to implement this method to calculate the function
	 * result.
	 */
	public abstract double calculateValue(double value);

	public Attribute[] getInputAttributes() {
		return INPUT_ATTR;
	}

	public Attribute[] getOutputAttributes(ExampleTable input) {
		Attribute a1 = getArgument(0);
		Attribute ao = 
			AttributeFactory.createAttribute(Ontology.NUMERICAL, 
											 Ontology.SINGLE_VALUE, 
											 getFunction(), 
											 new ConstructionDescription[] { a1.getConstruction() });
		return new Attribute[] { ao };
	}

	/**
	 * Returns all compatible input attribute arrays for this generator from the
	 * given example set as list.
	 */
	public List<Attribute[]> getInputCandidates(ExampleSet exampleSet, int maxDepth, String[] functions) {
		List<Attribute[]> result = new ArrayList<Attribute[]>();
		for (Attribute attribute : exampleSet.getAttributes()) {
			if (checkCompatibility(attribute, INPUT_ATTR[0], maxDepth, functions))
				result.add(new Attribute[] { attribute });
		}
		return result;
	}

	public void generate(DataRow data) throws GenerationException {
		try {
			Attribute a = getArgument(0);
			double value = data.get(a);
			double r = calculateValue(value);

			if (Double.isInfinite(r)) {
				LogService.getGlobal().log(getFunction() + ": Infinite value generated, replaced by NaN.", LogService.WARNING);
				r = Double.NaN;
			}
			if (Double.isNaN(r)) {
				LogService.getGlobal().log(getFunction() + ": NaN generated.", LogService.WARNING);
			}

			if (resultAttributes[0] != null)
				data.set(resultAttributes[0], r);
		} catch (ArrayIndexOutOfBoundsException ex) {
			throw new GenerationException("a:" + getArgument(0), ex);
		}
	}

	public String toString() {
		String s = "singular function ";
		if ((resultAttributes != null) && (resultAttributes.length > 0) && (resultAttributes[0] != null))
			s += resultAttributes[0].getName() + ":=";
		s += getFunction() + "(";
		if (argumentsSet())
			s += getArgument(0).getName();
		s += ")";
		return s;
	}
}
