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
import com.rapidminer.tools.LogService;
import com.rapidminer.tools.Ontology;


/**
 * This class has two numerical input attributes and one output attribute.
 * Depending on the mode specified in the constructor the result will be the
 * minimum or maximum of the input attributes.
 * 
 * @author Ingo Mierswa
 * @version $Id: MinMaxGenerator.java,v 1.3 2008/05/09 19:23:15 ingomierswa Exp $
 */
public class MinMaxGenerator extends BinaryNumericalGenerator {

	public static final int MIN = 0;

	public static final int MAX = 1;

	private static final String[] FUNCTION_NAMES = { "min", "max" };

	private int mode;

	public MinMaxGenerator(int mode) {
		this.mode = mode;
	}

	public MinMaxGenerator() {}

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
		return new MinMaxGenerator(mode);
	}

	public double calculateValue(double value1, double value2) {
		double r = 0;
		switch (mode) {
			case MIN:
				r = Math.min(value1, value2);
				break;
			case MAX:
				r = Math.max(value1, value2);
				break;
		}
		return r;
	}

	public void setFunction(String name) {
		for (int i = 0; i < FUNCTION_NAMES.length; i++) {
			if (FUNCTION_NAMES[i].equals(name)) {
				this.mode = i;
				return;
			}
		}
		LogService.getGlobal().log("Illegal function name '" + name + "' for " + getClass().getName() + ".", LogService.ERROR);
	}

	public String getFunction() {
		return FUNCTION_NAMES[mode];
	}

	public boolean equals(Object o) {
		return (super.equals(o) && (this.mode == ((MinMaxGenerator) o).mode));
	}
	
	public int hashCode() {
		return super.hashCode() ^ Integer.valueOf(mode).hashCode();
	}
}
