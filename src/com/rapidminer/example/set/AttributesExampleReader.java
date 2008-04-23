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

import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;


/**
 * This reader simply uses all examples from the parent and all available attributes.
 * 
 * @author Ingo Mierswa
 * @version $Id: AttributesExampleReader.java,v 1.1 2007/05/27 21:59:00 ingomierswa Exp $
 */
public class AttributesExampleReader extends AbstractExampleReader {

    /** The parent example reader. */
    private Iterator<Example> parent;

    /** The used attributes are described in this example set. */
    private ExampleSet exampleSet;

    /** Creates a simple example reader. */
    public AttributesExampleReader(Iterator<Example> parent, ExampleSet exampleSet) {
        this.parent = parent;
        this.exampleSet = exampleSet;
    }

    /** Returns true if there are more data rows. */
    public boolean hasNext() {
        return this.parent.hasNext();
    }

    /** Returns a new example based on the current data row. */
    public Example next() {
        if (!hasNext())
            return null;
        Example example = this.parent.next();
        if (example == null)
            return null;
        return new Example(example.getDataRow(), exampleSet);
    }
}
