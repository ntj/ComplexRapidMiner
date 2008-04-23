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
import com.rapidminer.example.Attributes;
import com.rapidminer.example.DelegateAttributes;


/**
 * This class can skip all attributes with weight zero. Additionally, all attributes are lazily wrapped
 * into a {@link WeightedAttribute}.
 *  
 * @author Ingo Mierswa
 * @version $Id: AttributeWeightedAttributeRoles.java,v 1.3 2007/07/14 12:31:38 ingomierswa Exp $
 */
public class AttributeWeightedAttributeRoles extends DelegateAttributes {
	
	private static final long serialVersionUID = -483627198007028149L;

	private AttributeWeightedExampleSet exampleSet;
	
	private boolean ignoreAttributesWithZeroWeight = false;
	
	public AttributeWeightedAttributeRoles(Attributes parent, AttributeWeightedExampleSet exampleSet, boolean ignoreZeroWeights) {
		super(parent);
		this.exampleSet = exampleSet;
		this.ignoreAttributesWithZeroWeight = ignoreZeroWeights;
	}

	/** Returns a {@link WeightedAttributeRoleIterator} which is able to skip zero weighted attributes
	 *  and wraps all remaining attributes into {@link WeightedAttribute}s. */
    public Iterator<AttributeRole> allAttributeRoles() {
		return new WeightedAttributeRoleIterator(super.allAttributeRoles(), exampleSet.getAttributeWeights(), ignoreAttributesWithZeroWeight);
	}

	/** Adds the attribute role and set the corresponding weight in the parent example set to 1. */
	public void add(AttributeRole attributeRole) {
		super.add(attributeRole);
		exampleSet.setWeight(attributeRole.getAttribute(), 1.0d);
	}
	
	/** Iterates through all attribute roles and delete the one with the same name (cannot use
	 *  super method since the attributes are wrapped into WeightedAttributes). */
	public boolean remove(AttributeRole role) {
		if (role != null) {
			Iterator<AttributeRole> a = allAttributeRoles();
			while (a.hasNext()) {
				AttributeRole candidate = a.next();
				if (candidate.getAttribute().getName().equals(role.getAttribute().getName())) {
					a.remove();
					return true;
				}
			}
		}
		return false;
	}
}
