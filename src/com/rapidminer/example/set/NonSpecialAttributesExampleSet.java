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
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.ExampleTable;


/**
 * Thie example set treats all special attributes as regular attributes.
 * 
 * @author Ingo Mierswa
 * @version $Id: NonSpecialAttributesExampleSet.java,v 1.2 2007/05/28 15:17:27 ingomierswa Exp $
 */
public class NonSpecialAttributesExampleSet extends AbstractExampleSet {
    
	private static final long serialVersionUID = -4782316585512718459L;

	/** The parent example set. */
	private ExampleSet parent;
	
	/** The used attributes. */
	private Attributes attributes;
	
    public NonSpecialAttributesExampleSet(ExampleSet delegate) {
    	this.parent = delegate;
        this.attributes = (Attributes)delegate.getAttributes().clone();
        Iterator<AttributeRole> s = this.attributes.specialAttributes();
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
        this.attributes = (Attributes)exampleSet.attributes.clone();
    }
    
    public Attributes getAttributes() {
    	return this.attributes;
    }

    /**
     * Creates an iterator over all examples.
     */
    public Iterator<Example> iterator() {
        return new AttributesExampleReader(parent.iterator(), this);
    }

    /**
     * Returns the example with the given index.
     */
    public Example getExample(int index) {
        return new Example(parent.getExample(index).getDataRow(), this);        
    }
    
    /**
     * Returns the example with the given index.
     */
    public Example getExampleFromId(double id) {
        return new Example(parent.getExampleFromId(id).getDataRow(), this);        
    }
    
	public ExampleTable getExampleTable() {
		return parent.getExampleTable();
	}

	public int size() {
		return parent.size();
	}

	public void remapIds() {
		parent.remapIds();
	}
}
