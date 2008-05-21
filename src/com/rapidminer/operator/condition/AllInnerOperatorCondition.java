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
package com.rapidminer.operator.condition;

import com.rapidminer.operator.IODescription;
import com.rapidminer.operator.IllegalInputException;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorChain;
import com.rapidminer.operator.WrongNumberOfInnerOperatorsException;
import com.rapidminer.tools.Tools;


/**
 * Checks if all inner operators are able to handle the given input and will
 * deliver the desired output.
 * 
 * @author Ingo Mierswa
 * @version $Id: AllInnerOperatorCondition.java,v 2.2 2006/03/21 15:35:42
 *          ingomierswa Exp $
 */
public class AllInnerOperatorCondition implements InnerOperatorCondition {

	/**
	 * The array of classes which is given to the inner chain of operators
	 * described by this InnerOpDesc object.
	 */
	private Class[] willGet;

	/**
	 * The array of classes which must be delivered by the inner chain of
	 * operators described by this InnerOpDesc object.
	 */
	private Class[] mustDeliver;

	public AllInnerOperatorCondition(Class[] willGet, Class[] mustDeliver) {
		this.willGet = willGet;
		this.mustDeliver = mustDeliver;
	}

	public Class[] checkIO(OperatorChain chain, Class[] input) throws IllegalInputException, WrongNumberOfInnerOperatorsException {
		if (chain.getNumberOfOperators() == 0) {
			throw new WrongNumberOfInnerOperatorsException(chain, chain.getMinNumberOfInnerOperators(), chain.getMaxNumberOfInnerOperators(), 0);
		}
		Class[] innerOutput = input;
		for (int i = 0; i < chain.getNumberOfOperators(); i++) {
			Operator operator = chain.getOperator(i);
			innerOutput = operator.checkIO(willGet);
			for (int j = 0; j < mustDeliver.length; j++) {
				if (!IODescription.containsClass(mustDeliver[j], innerOutput))
					throw new IllegalInputException(chain, operator, mustDeliver[j]);
			}
		}
		return innerOutput;
	}

	public String toHTML() {
		StringBuffer result = new StringBuffer("Each inner operator must be able to handle [");
		for (int i = 0; i < willGet.length; i++) {
			if (i != 0)
				result.append(", ");
			result.append(Tools.classNameWOPackage(willGet[i]));
		}
		result.append("] and must deliver [");
		for (int i = 0; i < mustDeliver.length; i++) {
			if (i != 0)
				result.append(", ");
			result.append(Tools.classNameWOPackage(mustDeliver[i]));
		}
		result.append("].");
		return result.toString();
	}
}
