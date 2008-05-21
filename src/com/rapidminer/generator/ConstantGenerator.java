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
import com.rapidminer.tools.Ontology;


/**
 * Generates a constant attribute. The format is &quot;const[value]()&quot; for
 * the {@link com.rapidminer.operator.features.construction.FeatureGenerationOperator}
 * operator.
 * 
 * @author Ingo Mierswa
 * @version $Id: ConstantGenerator.java,v 2.11 2006/03/27 13:21:58 ingomierswa
 *          Exp $
 */
public class ConstantGenerator extends FeatureGenerator {

	public static final String FUNCTION_NAME = "const";

	private double constant = 1.0d;

	private String constantString = "1";

	public ConstantGenerator() {}

	public ConstantGenerator(double constant) {
		this.constant = constant;
		this.constantString = constant + "";
	}

	public void setArguments(Attribute[] args) {

	}

	public FeatureGenerator newInstance() {
		return new ConstantGenerator();
	}

	public String getFunction() {
		return FUNCTION_NAME + "[" + constantString + "]";
	}

	public void setFunction(String functionName) {
		int leftIndex = functionName.indexOf("[");
		int rightIndex = functionName.indexOf("]");
		if ((leftIndex != -1) && (rightIndex != -1)) {
			this.constantString = functionName.substring(leftIndex + 1, rightIndex);
			this.constant = Double.parseDouble(constantString);
		}
	}

	public Attribute[] getInputAttributes() {
		return new Attribute[0];
	}

	public Attribute[] getOutputAttributes(ExampleTable input) {
		Attribute ao = 
			AttributeFactory.createAttribute(Ontology.NUMERICAL, 
											 Ontology.SINGLE_VALUE, 
											 getFunction(), 
											 new ConstructionDescription[0]);
		return new Attribute[] { ao };
	}

	/**
	 * Returns all compatible input attribute arrays for this generator from the
	 * given example set as list.
	 */
	public List<Attribute[]> getInputCandidates(ExampleSet exampleSet, int maxDepth, String[] functions) {
		return new ArrayList<Attribute[]>();
	}

	public void generate(DataRow data) throws GenerationException {
		try {
			if (resultAttributes[0] != null)
				data.set(resultAttributes[0], constant);
		} catch (ArrayIndexOutOfBoundsException ex) {
			throw new GenerationException("a:" + getArgument(0), ex);
		}
	}

	public String toString() {
		return getFunction();
	}
}
