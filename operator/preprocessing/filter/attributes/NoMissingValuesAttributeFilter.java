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

/**
 * This class implements a no missing value filter for attributes. Attributes are filtered and hence be removed from 
 * exampleSet if there are missing values in one of the examples in this attribute.
 * 
 * @author Sebastian Land, Ingo Mierswa
 * @version $Id: NoMissingValuesAttributeFilter.java,v 1.5 2008/07/19 16:31:17 ingomierswa Exp $
 */
public class NoMissingValuesAttributeFilter implements AttributeFilterCondition {
	
	private boolean invert;
	
	public boolean check(Attribute attribute, Example example) {
		return Double.isNaN(example.getValue(attribute)) ^ invert;
	}

	public boolean beforeScanCheck(Attribute attribute, String parameter, boolean invert) {
		this.invert = invert;
		return false;
	}
	
	/** Initializes the check for the scan. */
	public void initScanCheck() {}
	
	public boolean isNeedingScan() {
		return true;
	}
}
