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
import com.rapidminer.example.ExampleSet;


/**
 * This class lazily wraps all attributes into a {@link RemappedAttribute}.
 *  
 * @author Ingo Mierswa
 * @version $Id: RemappedAttributeRoles.java,v 1.1 2007/07/13 22:52:12 ingomierswa Exp $
 */
public class RemappedAttributeRoles extends DelegateAttributes {
    
    private static final long serialVersionUID = -483627198007028149L;

    private ExampleSet mappingSet;
    
    public RemappedAttributeRoles(Attributes parent, ExampleSet mappingSet) {
        super(parent);
        this.mappingSet = mappingSet;
    }

    /** Returns a {@link RemappedAttributeRoleIterator} which wraps all 
     *  attributes into {@link RemappedAttribute}s. */
    public Iterator<AttributeRole> allAttributeRoles() {
        return new RemappedAttributeRoleIterator(super.allAttributeRoles(), mappingSet);
    }
    
    /** Iterates through all attribute roles and delete the one with the same name (cannot use
     *  super method since the attributes are wrapped into RenamedAttributes). */
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
