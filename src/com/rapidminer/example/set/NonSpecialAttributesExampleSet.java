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
package com.rapidminer.example.set;

import java.util.Iterator;

import com.rapidminer.example.AttributeRole;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.ExampleTable;


/**
 * Thie example set treats all special attributes as regular attributes.
 * 
 * @author Ingo Mierswa
 * @version $Id: NonSpecialAttributesExampleSet.java,v 1.7 2008/05/09 19:22:49 ingomierswa Exp $
 */
public class NonSpecialAttributesExampleSet extends AbstractExampleSet {
    
	private static final long serialVersionUID = -4782316585512718459L;

	/** The parent example set. */
	private ExampleSet parent;
	
    public NonSpecialAttributesExampleSet(ExampleSet exampleSet) {
    	this.parent = (ExampleSet)exampleSet.clone();
        Iterator<AttributeRole> s = this.parent.getAttributes().specialAttributes();
        while (s.hasNext()) {
        	AttributeRole attributeRole = s.next();
        	if (attributeRole.isSpecial()) {
        		attributeRole.changeToRegular();
        	}
        }
    }
    
    /** Clone constructor. */
    public NonSpecialAttributesExampleSet(NonSpecialAttributesExampleSet exampleSet) {
    	this.parent = (ExampleSet)exampleSet.parent.clone();
    }
    
    public Attributes getAttributes() {
    	return this.parent.getAttributes();
    }

    /**
     * Creates an iterator over all examples.
     */
    public Iterator<Example> iterator() {
        return new AttributesExampleReader(parent.iterator(), this);
    }
    
	public ExampleTable getExampleTable() {
		return parent.getExampleTable();
	}

    public Example getExample(int index) {
    	return this.parent.getExample(index);
    }
    
	public int size() {
		return parent.size();
	}
}
