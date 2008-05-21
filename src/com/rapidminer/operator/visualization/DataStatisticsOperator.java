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
package com.rapidminer.operator.visualization;

import java.util.Iterator;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;


/**
 * This operators calculates some very simple statistics about the given example
 * set. These are the ranges of the attributes and the average or mode values
 * for numerical or nominal attributes respectively. These informations are
 * automatically calculated and displayed by the graphical user interface of
 * RapidMiner. Since they cannot be displayed with the command line version of RapidMiner
 * this operator can be used as a workaround in cases where the graphical user
 * interface cannot be used.
 * 
 * @author Ingo Mierswa
 * @version $Id: DataStatisticsOperator.java,v 1.5 2006/03/21 15:35:42
 *          ingomierswa Exp $
 */
public class DataStatisticsOperator extends Operator {

	/** Creates a new data statistics operator. */
	public DataStatisticsOperator(OperatorDescription description) {
		super(description);
	}

	/** Creates and delivers the simple statistics object. */
	public IOObject[] apply() throws OperatorException {
		ExampleSet eSet = getInput(ExampleSet.class);
		eSet.recalculateAllAttributeStatistics();
		DataStatistics statistics = new DataStatistics();
		Iterator<Attribute> i = eSet.getAttributes().allAttributes();
		while (i.hasNext()) {
			statistics.addInfo(eSet, i.next());
		}
		return new IOObject[] { eSet, statistics };
	}

	/** Requires an example set. */
	public Class[] getInputClasses() {
		return new Class[] { ExampleSet.class };
	}

	/** Returns the example set and the statistics. */
	public Class[] getOutputClasses() {
		return new Class[] { ExampleSet.class, DataStatistics.class };
	}
}
