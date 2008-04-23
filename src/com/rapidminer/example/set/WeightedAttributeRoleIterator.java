/*
 *  RapidMiner
 *
 *  Copyright (C) 2001-2007 by Rapid-I and the contributors
 *
 *  Complete list of developers available at our web site:
 *
 *       http://rapid-i.com
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License as 
 *  published by the Free Software Foundation; either version 2 of the
 *  License, or (at your option) any later version. 
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 *  USA.
 */
package com.rapidminer.example.set;

import java.util.Iterator;

import com.rapidminer.example.AttributeRole;
import com.rapidminer.example.AttributeWeights;
import com.rapidminer.tools.Tools;

/**
 * This attribute role iterator is able to skip attributes with zero weight
 * and lazily wraps all other attributes into {@link WeightedAttribute}s.
 * 
 * @author Ingo Mierswa
 * @version $Id: WeightedAttributeRoleIterator.java,v 1.1 2007/07/14 12:31:38 ingomierswa Exp $
 */
public class WeightedAttributeRoleIterator implements Iterator<AttributeRole> {
	
	private Iterator<AttributeRole> parent;
	
	private AttributeRole current = null;
	
	private boolean hasNextInvoked = false;
	
	private AttributeWeights weights;
	
	private boolean ignoreAttributesWithZeroWeight = false;
	
	public WeightedAttributeRoleIterator(Iterator<AttributeRole> parent, AttributeWeights weights, boolean ignoreZeroWeights) {
		this.parent = parent;
		this.weights = weights;
		this.ignoreAttributesWithZeroWeight = ignoreZeroWeights;
	}
	
	public boolean hasNext() {
		hasNextInvoked = true;
		if (!parent.hasNext()) {
			current = null;
			return false;
		} else {
			AttributeRole role = parent.next();
			double weight = weights.getWeight(role.getAttribute().getName());
			if (ignoreAttributesWithZeroWeight) {
				if (!Double.isNaN(weight)) {
					if (Tools.isEqual(weight, 0.0d)) {
						return hasNext();
					}
				}
			}
			current = new AttributeRole(new WeightedAttribute(role.getAttribute(), weight));
			current.setSpecial(role.getSpecialName());
			return true;
		}
	}

	public AttributeRole next() {
		if (!this.hasNextInvoked)
			hasNext();
		this.hasNextInvoked = false;
		return current;
	}

	public void remove() {
		parent.remove();
	}
}
