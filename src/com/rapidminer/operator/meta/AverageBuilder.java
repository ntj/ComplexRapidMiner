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
package com.rapidminer.operator.meta;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.MissingIOObjectException;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.tools.math.AverageVector;
import com.rapidminer.tools.math.RunVector;


/**
 * Collects all average vectors (e.g. PerformanceVectors) from the input and
 * average those of the same type.
 * 
 * @author Ingo Mierswa
 * @version $Id: AverageBuilder.java,v 1.3 2008/05/09 19:22:38 ingomierswa Exp $
 */
public class AverageBuilder extends Operator {

	public AverageBuilder(OperatorDescription description) {
		super(description);
	}

	public IOObject[] apply() throws OperatorException {
		Map<Class, RunVector> classMap = new HashMap<Class, RunVector>();
		while (true) {
			AverageVector vector = null;
			try {
				vector = getInput(AverageVector.class);
			} catch (MissingIOObjectException e) {
				break;
			}
			addVector(vector, classMap);
		}
		Iterator<Class> i = classMap.keySet().iterator();
		List<AverageVector> averages = new LinkedList<AverageVector>();
		while (i.hasNext()) {
			RunVector runVector = classMap.get(i.next());
			averages.add(runVector.average());
		}
		IOObject[] result = new IOObject[averages.size()];
		averages.toArray(result);
		return result;
	}

	private void addVector(AverageVector vector, Map<Class, RunVector> classMap) {
		RunVector runVector = classMap.get(vector.getClass());
		if (runVector == null) {
			runVector = new RunVector();
			classMap.put(vector.getClass(), runVector);
		}
		runVector.addVector(vector);
	}

	public Class[] getInputClasses() {
		return new Class[] { AverageVector.class };
	}

	public Class[] getOutputClasses() {
		return new Class[] { AverageVector.class };
	}

}
