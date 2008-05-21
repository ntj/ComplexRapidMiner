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

import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;

/**
 * This example reader is based on the given mapping and skips all examples which are not
 * part of the mapping. This implementation is quite inefficient on databases and other
 * non-memory example tables and should therefore only be used for small data sets.
 * 
 * @author Ingo Mierswa
 * @version $Id: SortedExampleReader.java,v 1.5 2008/05/09 19:22:49 ingomierswa Exp $
 */
public class SortedExampleReader extends AbstractExampleReader {

	/** The parent example set. */
	private ExampleSet parent;

    /** The current index in the mapping. */
    private int currentIndex;
    
    /** Indicates if the current example was &quot;delivered&quot; by a call of {@link #next()}. */
    private boolean nextInvoked = true;
    
    /** The example that will be returned by the next invocation of next(). */
    private Example currentExample = null;
    
    /** Constructs a new mapped example reader. */
    public SortedExampleReader(ExampleSet parent) {
        this.parent = parent;
        this.currentIndex = 0;
    }

    public boolean hasNext() {
        if (this.nextInvoked) {
            this.nextInvoked = false;
        	if (this.currentIndex < parent.size()) {
            	this.currentExample = this.parent.getExample(this.currentIndex);
            	this.currentIndex++;
        	} else {
        		return false;
        	}
        }
        return true;
    }

    public Example next() {
        if (hasNext()) {
            this.nextInvoked = true;
            return currentExample;
        } else {
            return null;
        }
    }
}

