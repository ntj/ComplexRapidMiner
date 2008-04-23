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
package com.rapidminer.example;

import java.io.Serializable;

/**
 * This class holds the example set relevant information about a table attribute, i.e.
 * its role (either regular or special). If the described attribute is a special attribute, 
 * this class also contains the corresponding special attribute name.
 * 
 * @author Ingo Mierswa
 * @version $Id: AttributeRole.java,v 1.2 2007/06/06 11:21:32 ingomierswa Exp $
 */
public class AttributeRole implements Serializable {

	private static final long serialVersionUID = -4855352048163007173L;

	private boolean special = false;
	
	private String specialName = null;
	
	private Attribute attribute;
	
	public AttributeRole(Attribute attribute) {
		this.attribute = attribute;
	}
	
    /** Clone constructor. Deep clone of special fields but only a shallow clone of the attribute. */
    private AttributeRole(AttributeRole other) {
        this.attribute = other.attribute;
        this.special = other.special;
        this.specialName = other.specialName;
    }
    
    /** Performs a deep clone of the special fields but only a shallow clone of the attribute. */
    public Object clone() {
        return new AttributeRole(this);
    }
    
	public Attribute getAttribute() {
		return attribute;
	}

	public void setAttribute(Attribute attribute) {
		this.attribute = attribute;
	}
	
	public boolean isSpecial() {
		return special;
	}
	
	public String getSpecialName() {
		return specialName;
	}
	
	public void setSpecial(String specialName) {
		this.specialName = specialName;
		if (specialName != null)
			this.special = true;
		else
			this.special = false;
	}
	
	public void changeToRegular() {
		setSpecial(null);
	}
	
	public String toString() {
		if (isSpecial()) {
			return specialName + " := " + attribute.getName();
		} else {
			return attribute.getName();
		}
	}
}
