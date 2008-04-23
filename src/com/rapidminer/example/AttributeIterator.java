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


/**
 * Iterates over either only the regular attribute, only the special attributes, or
 * over all all attributes.
 * 
 * @author Ingo Mierswa
 * @version $Id: AttributeIterator.java,v 1.3 2007/07/08 13:14:52 stiefelolm Exp $
 */
public class AttributeIterator implements Iterator<Attribute> {
	
	private Iterator<AttributeRole> parent;
	
	private int type = Attributes.REGULAR;
	
	private Attribute current = null;
	
	private boolean hasNextInvoked = false;

	private AttributeRole currentRole = null;
	
	public AttributeIterator(Iterator<AttributeRole> parent, int type) {
		this.parent = parent;
		this.type = type;
	}
	
	public boolean hasNext() {
		this.hasNextInvoked = true;
		if (!parent.hasNext() && currentRole == null) {
			current = null;
			return false;
		} else {
			AttributeRole role;
			if (currentRole  == null) {
				role = parent.next();
			} else {
				role = currentRole;
			}
			switch (type) {
			case Attributes.REGULAR:
				if (!role.isSpecial()) {
					current = role.getAttribute();
					currentRole = role;
					return true;
				} else {
					return hasNext();
				}
			case Attributes.SPECIAL:
				if (role.isSpecial()) {
					current = role.getAttribute();
					currentRole = role;					
					return true;
				} else {
					return hasNext();
				}
			case Attributes.ALL:
				current = role.getAttribute();
				currentRole = role;				
				return true;
			default:
				current = null;
				return false;
			}
		}
	}

	public Attribute next() {
		if (!this.hasNextInvoked)
			hasNext();
		this.hasNextInvoked = false;
		this.currentRole = null;
		return current;
	}

	public void remove() {
		parent.remove();
		this.currentRole = null;
		this.hasNextInvoked = false;
	}
}
