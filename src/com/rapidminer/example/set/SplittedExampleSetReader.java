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

import com.rapidminer.example.Example;


/**
 * Returns only a subset of an example set specified by an instance of
 * {@link Partition}.
 * 
 * @author Simon Fischer, Ingo Mierswa
 * @version $Id: SplittedExampleSetReader.java,v 2.9 2006/03/21 15:35:39
 *          ingomierswa Exp $
 */
public class SplittedExampleSetReader extends AbstractExampleReader {

	/** The underlying reader. */
	private Iterator<Example> reader;

	/** Index of the current example. */
	private int current;

	/** The partition. */
	private Partition partition;

	/** The next example that will be returned. */
	private Example next;

	public SplittedExampleSetReader(Iterator<Example> reader, Partition partition) {
		this.reader = reader;
		this.partition = partition;
		current = -1;
		hasNext();
	}

	public boolean hasNext() {
		while (next == null) {
			current++;

			Example example = reader.next();
			if (example == null)
				return false;

			if (current >= partition.getTotalSize())
				return false;

			if (partition.isSelected(current))
				next = example;
		}
		return true;
	}

	public Example next() {
		if (!hasNext()) {
			return null;
		} else {
			Example dummy = next;
			next = null;
			return dummy;
		}
	}

}
