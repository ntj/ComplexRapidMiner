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
package com.rapidminer.operator.learner.tree;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.tools.Tools;

/**
 * Returns true if the value of the desired attribute is greater then a given threshold.
 * 
 * @author Ingo Mierswa
 * @version $Id: GreaterSplitCondition.java,v 1.7 2008/05/09 19:22:53 ingomierswa Exp $
 */
public class GreaterSplitCondition extends AbstractSplitCondition {
    
    private static final long serialVersionUID = 7094464803196955502L;
    
	private double value;
    
    public GreaterSplitCondition(Attribute attribute, double value) {
        super(attribute.getName());
        this.value = value;
    }
    
    public boolean test(Example example) {
        return example.getValue(example.getAttributes().get(getAttributeName())) > value;
    }
    
	public String getRelation() {
		return ">";
	}

	public String getValueString() {
		return Tools.formatIntegerIfPossible(this.value);
	}
}
