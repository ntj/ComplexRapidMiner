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
 * This example set is a clone of the attributes without reference to any data.
 * Therefore it can be used as a data header description. Since no data reference
 * exist, all example based methods will throw an {@link UnsupportedOperationException}.
 *  
 * @author Ingo Mierswa
 * @version $Id: HeaderExampleSet.java,v 1.1 2007/07/13 22:52:12 ingomierswa Exp $
 */
public class HeaderExampleSet extends AbstractExampleSet {

    private static final long serialVersionUID = -255270841843010670L;
    
    /** The parent example set. */
    private Attributes attributes;
    
    public HeaderExampleSet(ExampleSet parent) {
        this.attributes = (Attributes)parent.getAttributes().clone();
    }
    
    /** Header example set clone constructor. */
    public HeaderExampleSet(HeaderExampleSet other) {
        this.attributes = (Attributes)other.attributes.clone();
    }
    
    public Attributes getAttributes() {
        return attributes;
    }

    public Example getExample(int index) {
        return null;
    }

    public Example getExampleFromId(double value) {
        throw new UnsupportedOperationException("The method getExampleFromId(double) is not supported by the header example set.");
    }

    public ExampleTable getExampleTable() {
        throw new UnsupportedOperationException("The method getExampleTable() is not supported by the header example set.");
    }

    public void remapIds() {
        throw new UnsupportedOperationException("The method remapIds() is not supported by the header example set.");
    }

    public int size() {
        return 0;
    }

    public Iterator<Example> iterator() {
        throw new UnsupportedOperationException("The method iterator() is not supported by the header example set.");
    }
}
