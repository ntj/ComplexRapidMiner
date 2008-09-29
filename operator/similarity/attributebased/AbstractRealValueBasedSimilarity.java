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
package com.rapidminer.operator.similarity.attributebased;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.Tools;
import com.rapidminer.operator.OperatorException;


/**
 * An adaptar for similarities defined on contineous values.
 * 
 * @author Michael Wurst, Ingo Mierswa
 * @version $Id: AbstractRealValueBasedSimilarity.java,v 1.6 2008/09/12 10:30:49 tobiasmalbrecht Exp $
 */
public abstract class AbstractRealValueBasedSimilarity extends AbstractValueBasedSimilarity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2117629661176905968L;

	public void init(ExampleSet exampleSet) throws OperatorException {
		Tools.onlyNumericalAttributes(exampleSet, "value based similarities");
		super.init(exampleSet);
	}
}
