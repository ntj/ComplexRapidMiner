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
 * This interface must be implemented by classes implementing an AttributeFilterCondition for
 * the AttributeFilter operator.
 * 
 * @author Sebastian Land, Ingo Mierswa
 * @version $Id: AttributeFilterCondition.java,v 1.5 2008/07/19 16:31:17 ingomierswa Exp $
 */
public interface AttributeFilterCondition {
	
	/**
	 * This method initializes this condition an resets all counters.
	 * It returns true, if the attribute can be removed without checking examples.
	 * If it has been removed, no checking during examples will occur.
	 * @param attribute this is the attribute, the filter will have to check for.
	 * @throws ConditionCreationException 
	 */
	public boolean beforeScanCheck(Attribute attribute, String parameter, boolean invert) throws ConditionCreationException;
	
	/** Initializes the check for the scan. */
	public void initScanCheck();
	
	/**
	 * This method checks the given example. During this method the filter might check data to 
	 * decide if attribute should be filtered out.
	 */
	public boolean check(Attribute attribute, Example example);
	
	/** Indicates if this filter needs a data scan, i.e. an invokation of the check method for each example. */
	public boolean isNeedingScan();
	
}
