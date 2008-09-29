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
package com.rapidminer.operator.preprocessing.filter.attributes;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.set.ConditionCreationException;

/**
 * This class implements the condition if an attribute is numeric. All non-numerical attributes
 * will be removed.
 * 
 * @author Sebastian Land, Ingo Mierswa
 * @version $Id: NumericalAttributeFilter.java,v 1.6 2008/07/19 16:31:17 ingomierswa Exp $
 */
public class NumericalAttributeFilter implements AttributeFilterCondition {
	
	public boolean check(Attribute attribute, Example example) {
		return false;
	}

	public boolean beforeScanCheck(Attribute attribute, String parameter, boolean invert) throws ConditionCreationException {
		return invert ^ !attribute.isNumerical();
	}
	
	/** Initializes the check for the scan. */
	public void initScanCheck() {}
	
	public boolean isNeedingScan() {
		return false;
	}
}
