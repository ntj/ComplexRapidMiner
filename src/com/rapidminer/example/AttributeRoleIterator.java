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
 * An iterator for attribute roles which is able to iterate over all
 * attributes or skip either regular or special attributes.
 * 
 * @author Ingo Mierswa
 * @version $Id: AttributeRoleIterator.java,v 1.2 2007/05/28 15:17:27 ingomierswa Exp $
 */
public class AttributeRoleIterator implements Iterator<AttributeRole> {
	
	private Iterator<AttributeRole> parent;
	
	private int type = Attributes.REGULAR;
	
	private AttributeRole current = null;
	
	private boolean hasNextInvoked = false;
	
	public AttributeRoleIterator(Iterator<AttributeRole> parent, int type) {
		this.parent = parent;
		this.type = type;
	}
	
	public boolean hasNext() {
		this.hasNextInvoked = true;
        
        AttributeRole role = null;
        while ((role == null) && (parent.hasNext())) {
            AttributeRole candidate = parent.next();
            switch (type) {
                case Attributes.REGULAR:
                    if (!candidate.isSpecial())
                        role = candidate;
                    break;
                case Attributes.SPECIAL:
                    if (candidate.isSpecial())
                        role = candidate;
                    break;
                case Attributes.ALL:
                    role = candidate;
                    break;
                default:
                    break;
            }
        }
        current = role;
        return current != null;
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
