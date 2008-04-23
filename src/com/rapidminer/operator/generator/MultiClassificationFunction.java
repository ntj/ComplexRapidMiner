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
package com.rapidminer.operator.generator;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.tools.Ontology;


/**
 * The label is the first class, if the sum of all arguments modulo 2 is 0,
 * it is the second class if the sum modulo 3 is 0 and the third class if
 * the sum modulo 5 is 0. In all other cases the label is the fourth class.
 * 
 * @author Ingo Mierswa
 * @version $Id: MultiClassificationFunction.java,v 1.1 2007/05/27 21:58:45 ingomierswa Exp $
 */
public class MultiClassificationFunction extends ClassificationFunction {

	Attribute nominalLabel = AttributeFactory.createAttribute("label", Ontology.NOMINAL);

	public MultiClassificationFunction() {
		getLabel().getMapping().mapString("one");
		getLabel().getMapping().mapString("two");
		getLabel().getMapping().mapString("three");
		getLabel().getMapping().mapString("four");
	}

	public Attribute getLabel() {
		return nominalLabel;
	}

	public double calculate(double[] args) throws FunctionException {
		double sumD = 0.0d;
		for (int i = 0; i < args.length; i++)
			sumD += args[i];
		int sum = Math.abs((int) Math.round(sumD));
		if ((sum % 2) == 0)
			return getLabel().getMapping().mapString("one");
		else if ((sum % 3) == 0)
			return getLabel().getMapping().mapString("two");
		else if ((sum % 5) == 0)
			return getLabel().getMapping().mapString("three");
		else
			return getLabel().getMapping().mapString("four");
	}
}
