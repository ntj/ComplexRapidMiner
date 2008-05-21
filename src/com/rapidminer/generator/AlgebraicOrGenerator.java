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

import com.rapidminer.example.Attribute;
import com.rapidminer.example.ConstructionDescription;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.table.ExampleTable;
import com.rapidminer.tools.Ontology;


/**
 * This class has two numerical input attributes and one output attribute. The
 * result will be the algebraic or (s-norm) of the input attributes.
 * 
 * @author Ingo Mierswa
 * @version $Id: AlgebraicOrGenerator.java,v 1.2 2006/03/21 15:35:40 ingomierswa
 *          Exp $
 */
public class AlgebraicOrGenerator extends BinaryNumericalGenerator {

	public AlgebraicOrGenerator() {}

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

	public boolean isSelfApplicable() {
		return false;
	}

	public boolean isCommutative() {
		return true;
	}

	public FeatureGenerator newInstance() {
		return new AlgebraicOrGenerator();
	}

	public double calculateValue(double value1, double value2) {
		return (value1 + value2) - (value1 * value2);
	}

	public void setFunction(String name) {}

	public String getFunction() {
		return "algOr";
	}
}
