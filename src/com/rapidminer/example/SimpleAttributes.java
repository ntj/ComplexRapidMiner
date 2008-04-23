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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


/**
 * A very basic and simple implementation of the {@link com.rapidminer.example.Attributes} 
 * interface based on a linked list of {@link com.rapidminer.example.AttributeRole}s
 * and simply delivers the same {@link com.rapidminer.example.AttributeRoleIterator}
 * which might skip either regular or special attributes.
 *  
 * @author Ingo Mierswa
 * @version $Id: SimpleAttributes.java,v 1.3 2007/06/06 11:21:32 ingomierswa Exp $
 */
public class SimpleAttributes extends AbstractAttributes {
    
	private static final long serialVersionUID = 6388263725741578818L;
	
	private List<AttributeRole> attributes = new LinkedList<AttributeRole>();
	
	public SimpleAttributes() {}
	
	private SimpleAttributes(SimpleAttributes attributes) {
        this.attributes.clear();
        Iterator<AttributeRole> a = attributes.allAttributeRoles();
        while (a.hasNext()) {
            AttributeRole role = a.next();
            this.attributes.add((AttributeRole)role.clone());
        }
	}
	
	public Object clone() {
		return new SimpleAttributes(this);
	}
	
	public boolean equals(Object o) {
		if (!(o instanceof SimpleAttributes)) {
			return false;
		}
		SimpleAttributes other = (SimpleAttributes)o;
		return attributes.equals(other.attributes);
	}
	
	public int hashCode() {
		return attributes.hashCode();
	}
	
	public Iterator<AttributeRole> allAttributeRoles() {
		return attributes.iterator();
	}
	
	public void add(AttributeRole attributeRole) {
		this.attributes.add(attributeRole);
	}
	
	public boolean remove(AttributeRole attributeRole) {
		return this.attributes.remove(attributeRole);
	}
}
