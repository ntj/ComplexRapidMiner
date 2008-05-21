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
package com.rapidminer.example;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.rapidminer.tools.Tools;


/**
 * This is the abstract superclass for all attribute set implementations. It is sufficient
 * for subclasses to overwrite the method {@link Attributes#allAttributeRoles()} and the
 * corresponding add and remove methods.
 * 
 * @author Ingo Mierswa
 * @version $Id: AbstractAttributes.java,v 1.5 2008/05/09 19:22:42 ingomierswa Exp $
 */
public abstract class AbstractAttributes implements Attributes {

	public abstract Object clone();
	
	public Iterator<Attribute> iterator() {
		return new AttributeIterator(allAttributeRoles(), REGULAR);
	}
	
	public Iterator<Attribute> allAttributes() {
		return new AttributeIterator(allAttributeRoles(), ALL);
	}
	
	public Iterator<AttributeRole> specialAttributes() {
		return new AttributeRoleIterator(allAttributeRoles(), SPECIAL);
	}

	public boolean contains(Attribute attribute) {
		return findAttributeRole(attribute.getName(), allAttributeRoles()) != null;
	}
	
	public int allSize() {
		return calculateSize(allAttributes());
	}
	
	public int size() {
		return calculateSize(iterator());
	}

	public int specialSize() {
		return calculateSize(specialAttributes());
	}
	
	private int calculateSize(Iterator i) {
		int counter = 0;
		while (i.hasNext()) {
			i.next();
			counter++;
		}
		return counter;
	}
	
	public void addRegular(Attribute attribute) {
		add(new AttributeRole(attribute));
	}

	public boolean remove(Attribute attribute) {
        AttributeRole role = getRole(attribute);
        if (role != null)
            return remove(getRole(attribute));
        else
            return false;
	}
	
	public void clearRegular() {
        List<AttributeRole> toRemove = new LinkedList<AttributeRole>();
		Iterator<AttributeRole> i = allAttributeRoles();
		while (i.hasNext()) {
            AttributeRole role = i.next();
            if (!role.isSpecial())
                toRemove.add(role);
		}
        
        for (AttributeRole role : toRemove) {
            remove(role);
        }
	}
	
	public Attribute replace(Attribute first, Attribute second) {
		AttributeRole role = getRole(first);
		if (role != null) {
			role.setAttribute(second);
		} else {
			throw new java.util.NoSuchElementException("Attribute " + first + " cannot be replaced by attribute " + second + ": " + first + " is not part of the example set!");
		}
		return second;
	}
	
	public Attribute get(String name) {
		Attribute result = findAttribute(name, allAttributes());
        if (result == null) {
            result = getSpecial(name);
        }
        return result;
	}
	
	public Attribute getRegular(String name) {
		return findAttribute(name, iterator());
	}
	
	public Attribute getSpecial(String name) {
		AttributeRole role = findAttributeRole(name, specialAttributes());
		if (role == null) return null;
		else return role.getAttribute();
	}

	public AttributeRole getRole(Attribute attribute) {
		return getRole(attribute.getName());
	}
	
	public AttributeRole getRole(String name) {
		return findAttributeRole(name, allAttributeRoles());
	}
	
	public Attribute getLabel() {
		return getSpecial(LABEL_NAME);
	}

	public void setLabel(Attribute label) {
		setSpecialAttribute(label, LABEL_NAME);
	}
	
	public Attribute getPredictedLabel() {
		return getSpecial(PREDICTION_NAME);
	}

	public void setPredictedLabel(Attribute predictedLabel) {
		setSpecialAttribute(predictedLabel, PREDICTION_NAME);
	}
	
	public Attribute getId() {
		return getSpecial(ID_NAME);
	}
	
	public void setId(Attribute id) {
		setSpecialAttribute(id, ID_NAME);
	}

	public Attribute getWeight() {
		return getSpecial(WEIGHT_NAME);
	}
	
	public void setWeight(Attribute weight) {
		setSpecialAttribute(weight, WEIGHT_NAME);
	}
	
	public Attribute getCluster() {
		return getSpecial(CLUSTER_NAME);
	}
	
	public void setCluster(Attribute cluster) {
		setSpecialAttribute(cluster, CLUSTER_NAME);
	}

	public Attribute getOutlier() {
		return getSpecial(OUTLIER_NAME);
	}
	
	public void setOutlier(Attribute outlier) {
		setSpecialAttribute(outlier, OUTLIER_NAME);
	}
	
	public Attribute getCost() {
		return getSpecial(CLASSIFICATION_COST);
	}
	
	public void setCost(Attribute cost) {
		setSpecialAttribute(cost, CLASSIFICATION_COST);
	}
	
	public void setSpecialAttribute(Attribute attribute, String specialName) {
		AttributeRole oldRole = getRole(specialName);
		if (oldRole != null)
			remove(oldRole);
		if (attribute != null) {
			remove(attribute);
			AttributeRole role = new AttributeRole(attribute);
			role.setSpecial(specialName);
			add(role);
		}
        /*
        AttributeRole oldRole = getRole(specialName);
        if (oldRole != null) {
            remove(oldRole);
            if (attribute != null) {
                oldRole.setAttribute(attribute);
                add(oldRole);
            }
        } else {
            if (attribute != null) {
                remove(attribute);
                AttributeRole role = new AttributeRole(attribute);
                role.setSpecial(specialName);
                add(role);
            }
        }
        */
	}
	
	public Attribute[] createRegularAttributeArray() {
		int index = 0;
		Attribute[] result = new Attribute[size()];
		for (Attribute attribute : this)
			result[index++] = attribute;
		return result;
	}
    
    private Attribute findAttribute(String name, Iterator<Attribute> i) {
        while (i.hasNext()) {
            Attribute attribute = i.next();
            // ignoring cases does not work for some preprocessing operators
            //if (attribute.getName().equalsIgnoreCase(name))
            if (attribute.getName().equals(name))
                return attribute;
        }
        return null;
    }

    private AttributeRole findAttributeRole(String name, Iterator<AttributeRole> i) {
        while (i.hasNext()) {
            AttributeRole role = i.next();
            //if (role.getAttribute().getName().equalsIgnoreCase(name) || ((role.getSpecialName() != null) && role.getSpecialName().equalsIgnoreCase(name)))
            if (role.getAttribute().getName().equals(name) || ((role.getSpecialName() != null) && role.getSpecialName().equals(name)))
                return role;
        }
        return null;
    }
    
	/** Returns a string representation of this attribute set. */
	public String toString() {
		StringBuffer result = new StringBuffer(Tools.classNameWOPackage(getClass()) + ": ");
		Iterator<AttributeRole> r = allAttributeRoles();
		boolean first = true;
		while (r.hasNext()) {
			if (!first)
				result.append(", ");
			result.append(r.next());
			first = false;
		}
		return result.toString();
	}
}
