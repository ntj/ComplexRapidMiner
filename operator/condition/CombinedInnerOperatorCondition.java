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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.rapidminer.operator.IllegalInputException;
import com.rapidminer.operator.OperatorChain;
import com.rapidminer.operator.WrongNumberOfInnerOperatorsException;


/**
 * This condition is a container for other (simple) inner operator conditions.
 * This can for example be used to define several
 * {@link SpecificInnerOperatorCondition} conditions for a chain. The output of
 * the last added condition is used as total output.
 * 
 * @author Ingo Mierswa
 * @version $Id: CombinedInnerOperatorCondition.java,v 2.3 2006/03/27 13:21:58
 *          ingomierswa Exp $
 */
public class CombinedInnerOperatorCondition implements InnerOperatorCondition {

	private List<InnerOperatorCondition> conditions = new LinkedList<InnerOperatorCondition>();

	public CombinedInnerOperatorCondition() {}

	public void addCondition(InnerOperatorCondition condition) {
		this.conditions.add(condition);
	}

	public Class[] checkIO(OperatorChain chain, Class[] input) throws IllegalInputException, WrongNumberOfInnerOperatorsException {
		Class[] innerOutput = input;
		Iterator<InnerOperatorCondition> i = conditions.iterator();
		while (i.hasNext()) {
			InnerOperatorCondition condition = i.next();
			innerOutput = condition.checkIO(chain, input);
		}
		return innerOutput;
	}

	public String toHTML() {
		StringBuffer result = new StringBuffer("<ul>");
		Iterator<InnerOperatorCondition> i = conditions.iterator();
		while (i.hasNext()) {
			result.append("<li>" + i.next().toHTML() + "</li>");
		}
		result.append("</ul>");
		return result.toString();
	}
}
