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

import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.OperatorDescription;


/**
 * Replaces positive and negative infinite values in examples by one of the
 * functions &quot;none&quot;, &quot;zero&quot;, &quot;max_byte&quot;,
 * &quot;max_int&quot;, &quot;max_double&quot;, and &quot;missing&quot;.
 * &quot;none&quot; means, that the value is not replaced. The max_xxx functions
 * replace plus infinity by the upper bound and minus infinity by the lower
 * bound of the range of the Java type xxx. &quot;missing&quot; means, that the
 * value is replaced by nan (not a number), which is internally used to
 * represent missing values. A {@link MissingValueReplenishment} operator can be
 * used to replace missing values by average (or the mode for nominal
 * attributes), maximum, minimum etc. afterwards.<br/> For each attribute, the
 * function can be selected using the parameter list <code>columns</code>. If
 * an attribute's name appears in this list as a key, the value is used as the
 * function name. If the attribute's name is not in the list, the function
 * specified by the <code>default</code> parameter is used.
 * 
 * @author Simon Fischer, Ingo Mierswa
 * @version $Id: InfiniteValueReplenishment.java,v 1.9 2006/04/05 08:57:27
 *          ingomierswa Exp $
 */
public class InfiniteValueReplenishment extends ValueReplenishment {

	private static final int NONE       = 0;

	private static final int ZERO       = 1;

	private static final int MAX_BYTE   = 2;

	private static final int MAX_INT    = 3;

	private static final int MAX_DOUBLE = 4;

	private static final int MISSING    = 5;
    
    private static final int VALUE      = 6;

	private static final String[] REP_NAMES = { "none", "zero", "max_byte", "max_int", "max_double", "missing", "value" };

	public InfiniteValueReplenishment(OperatorDescription description) {
		super(description);
	}

	public boolean replenishValue(double currentValue) {
		return Double.isInfinite(currentValue);
	}

	public String[] getFunctionNames() {
		return REP_NAMES;
	}

	public int getDefaultFunction() {
		return MAX_DOUBLE; 
	}

	public int getDefaultColumnFunction() {
		return ZERO;
	}

	/** Replaces the values */
	public double getReplenishmentValue(int functionIndex, ExampleSet exampleSet, Attribute attribute, double currentValue, String valueString) {
		switch (functionIndex) {
			case NONE:
				return currentValue;
			case ZERO:
				return 0.0;
			case MAX_BYTE:
				return (currentValue > 0) ? Byte.MAX_VALUE : Byte.MIN_VALUE;
			case MAX_INT:
				return (currentValue > 0) ? Integer.MAX_VALUE : Integer.MIN_VALUE;
			case MAX_DOUBLE:
				return (currentValue > 0) ? Double.MAX_VALUE : -Double.MAX_VALUE;
			case MISSING:
				return Double.NaN;
            case VALUE:
                return Double.parseDouble(valueString);
			default:
				throw new RuntimeException("Illegal value functionIndex: " + functionIndex);
		}
	}
}
