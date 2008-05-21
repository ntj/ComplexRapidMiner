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

import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.ReplaceMissingExampleSet;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;

/**
 * This operator simply creates a new view on the input data without
 * changing the actual data or creating a new data table. The new view 
 * will not contain any missing values for regular attributes but 
 * the mean value (or mode) of the non-missing values instead.
 *
 * @author Ingo Mierswa
 * @version $Id: MissingValueReplenishmentView.java,v 1.6 2008/05/09 19:22:58 ingomierswa Exp $
 */
public class MissingValueReplenishmentView extends Operator {

	public MissingValueReplenishmentView(OperatorDescription description) {
		super(description);
	}

	public IOObject[] apply() throws OperatorException {
		ExampleSet exampleSet = getInput(ExampleSet.class);
		ExampleSet result = new ReplaceMissingExampleSet(exampleSet);
		return new IOObject[] { result };
	}

	public Class[] getInputClasses() {
		return new Class[] { ExampleSet.class };
	}

	public Class[] getOutputClasses() {
		return new Class[] { ExampleSet.class };
	}
}
