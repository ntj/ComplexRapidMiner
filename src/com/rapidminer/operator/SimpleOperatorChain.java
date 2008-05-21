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
package com.rapidminer.operator;

import com.rapidminer.operator.condition.InnerOperatorCondition;
import com.rapidminer.operator.condition.SimpleChainInnerOperatorCondition;

/**
 * A simple operator chain which can have an arbitrary number of inner
 * operators. The operators are subsequently applied and their output is used as
 * input for the succeeding operator. The input of the operator chain is used as
 * input for the first inner operator and the output of the last operator is
 * used as the output of the operator chain.
 * 
 * @author Ingo Mierswa, Simon Fischer
 * @version $Id: SimpleOperatorChain.java,v 2.19 2006/04/14 11:42:27 ingomierswa
 *          Exp $
 */
final public class SimpleOperatorChain extends OperatorChain {

	/** Creates an empty operator chain. */
	public SimpleOperatorChain(OperatorDescription description) {
		super(description);
	}

	/**
	 * Returns true since this operator chain should just return the output of
	 * the last inner operator.
	 */
	public boolean shouldReturnInnerOutput() {
		return true;
	}

	/** Returns a simple chain condition. */
	public InnerOperatorCondition getInnerOperatorCondition() {
		return new SimpleChainInnerOperatorCondition();
	}

	/**
	 * Returns the highest possible value for the maximum number of innner
	 * operators.
	 */
	public int getMaxNumberOfInnerOperators() {
		return Integer.MAX_VALUE;
	}

	/** Returns 0 for the minimum number of innner operators. */
	public int getMinNumberOfInnerOperators() {
		return 0;
	}

	/**
	 * Since the apply methods of the inner operators already add additional
	 * output, the handle additional output method should simply return a new
	 * container which is build from the additional output objects. Therefore
	 * this method returns true.
	 */
	public boolean getAddOnlyAdditionalOutput() {
		return true;
	}

	public Class[] getInputClasses() {
		return new Class[0];
	}

	public Class[] getOutputClasses() {
		return new Class[0];
	}
}
