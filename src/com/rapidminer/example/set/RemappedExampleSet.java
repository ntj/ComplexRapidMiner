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

import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.ExampleTable;

/**
 * This example set uses the mapping given by another example set and
 * "remaps" on the fly the nominal values according to the given set.
 *
 * @author Ingo Mierswa
 * @version $Id: RemappedExampleSet.java,v 1.1 2007/07/13 22:52:13 ingomierswa Exp $
 */
public class RemappedExampleSet extends AbstractExampleSet {

    private static final long serialVersionUID = 3460640319989955936L;

    private ExampleSet parent;
    
    private ExampleSet mappingSet;
    
    public RemappedExampleSet(ExampleSet exampleSet, ExampleSet mappingSet) {
        this.parent = exampleSet;
        this.mappingSet = mappingSet;
    }

    /** Clone constructor. */
    public RemappedExampleSet(RemappedExampleSet other) {
        this.parent = (ExampleSet)other.parent.clone();
        this.mappingSet = (ExampleSet)other.mappingSet.clone();
    }

    public Attributes getAttributes() {
        return new RemappedAttributeRoles(parent.getAttributes(), mappingSet);
    }

    public Example getExample(int index) {
        return new Example(parent.getExample(index).getDataRow(), this);
    }

    public Example getExampleFromId(double id) {
        return new Example(parent.getExampleFromId(id).getDataRow(), this);
    }

    public ExampleTable getExampleTable() {
        return parent.getExampleTable();
    }

    public void remapIds() {
        parent.remapIds();
    }

    public int size() {
        return parent.size();
    }

    public Iterator<Example> iterator() {
        return new AttributesExampleReader(parent.iterator(), this);
    }
}
