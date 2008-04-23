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
 * This class delegates all method calls to the delegate object.
 * Subclasses might want to override only some of the methods.
 * 
 * @author Ingo Mierswa
 * @version $Id: DelegateAttributes.java,v 1.2 2007/07/13 22:52:14 ingomierswa Exp $
 */
public class DelegateAttributes extends AbstractAttributes {

	private static final long serialVersionUID = 8476188336349012916L;
	
	protected Attributes delegate;
	
	public DelegateAttributes(Attributes delegate) {
		this.delegate = delegate;
	}
	
	private DelegateAttributes(DelegateAttributes roles) {
		this.delegate = (Attributes)roles.delegate.clone();
	}
	
	public void add(AttributeRole attributeRole) {
		this.delegate.add(attributeRole);
	}

	public boolean remove(AttributeRole attributeRole) {
		return this.delegate.remove(attributeRole);
	}

	/** This method is usually overridden by subclasses. */
	public Iterator<AttributeRole> allAttributeRoles() {
		return this.delegate.allAttributeRoles();
	}

	@Override
	public Object clone() {
		return new DelegateAttributes(this);
	}
}
