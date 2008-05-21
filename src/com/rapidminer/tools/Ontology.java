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
package com.rapidminer.tools;

/**
 * Very simple ontology class. Two static ontologies are available:
 * <tt>ATTRIBUTE_BLOCK_TYPE</tt> and <tt>ATTRIBUTE_VALUE_TYPE</tt>. It
 * provides a single method <tt>boolean isA(int sub, int super)</tt> which
 * does what isA-methods are usually expected to do. Legal parameters are the
 * constants.
 * 
 * @author Simon Fischer, Ingo Mierswa
 * @version $Id: Ontology.java,v 1.3 2008/05/09 19:22:55 ingomierswa Exp $
 */
public class Ontology {

	public static final int VALUE_TYPE = 0;

	public static final int BLOCK_TYPE = 1;

	/**
	 * The parent's index in the array. Root has parent -1.
	 */
	private int parentId[];

	/** Human readable string representations. */
	private String names[];

	public static final int NO_PARENT = -1;

	// -------------------- VALUE TYPE --------------------

	public static final int ATTRIBUTE_VALUE = 0;

	public static final int NOMINAL = 1;

	public static final int NUMERICAL = 2;

	public static final int INTEGER = 3;

	public static final int REAL = 4;

	public static final int STRING = 5;

	public static final int ORDERED = 6;

	public static final int CLUSTER = 7;

	public static final int BINOMINAL = 8; // nominal, only +1 and -1

	public static final int POLYNOMINAL = 9;

	public static final int FILE_PATH = 10; // path to a file

    public static final int DATE = 11;
    
	public static final String[] VALUE_TYPE_NAMES = { 
        "attribute_value", 
        "nominal", 
        "numeric", 
        "integer", 
        "real", 
        "string", 
        "ordered", 
        "cluster", 
        "binominal", 
        "polynominal", 
        "file_path",
        "date"
	};

	/** An ontology for value types (nominal, numerical...) */
	public static final Ontology ATTRIBUTE_VALUE_TYPE = 
        new Ontology(new int[] { 
                NO_PARENT,       // attribute_value (parent type) 
                ATTRIBUTE_VALUE, // nominal
                ATTRIBUTE_VALUE, // numeric
                NUMERICAL,       // integer
                NUMERICAL,       // real
                NOMINAL,         // string 
                NOMINAL,         // ordered
                NOMINAL,         // cluster
                NOMINAL,         // binominal (boolean classification)
                NOMINAL,         // polynominal
                NOMINAL,         // file_path
                ORDERED          // date
        }, VALUE_TYPE_NAMES);

	// -------------------- BLOCK TYPE --------------------

	public static final int ATTRIBUTE_BLOCK = 0;

	public static final int VALUE_SERIES = 1;

	public static final int NON_SERIES = 2;

	public static final int SINGLE_VALUE = 3;

	public static final int INTERVAL = 4;

	public static final int X = 5;

	public static final int Y = 6;

	public static final int VALUE_SERIES_START = 7;

	public static final int VALUE_SERIES_END = 8;

	public static final int INTERVAL_START = 9;

	public static final int INTERVAL_END = 10;
	
	public static final int VALUE_MATRIX = 11;
	
	public static final int VALUE_MATRIX_START = 12;
	
	public static final int VALUE_MATRIX_END = 13;
	
	public static final int VALUE_MATRIX_ROW_START = 14;
	
	public static final String[] BLOCK_TYPE_NAMES = { 
        "attribute_block", 
        "value_series", 
        "non_series", 
        "single_value", 
        "interval", 
        "x", 
        "y", 
        "value_series_start", 
        "value_series_end", 
        "interval_start", 
        "interval_end",
        "value_matrix",
        "value_matrix_start",
        "value_matrix_end",
        "value_matrix_row_start"
	};

	/** An ontology for block types (single, time series...) */
	public static final Ontology ATTRIBUTE_BLOCK_TYPE = 
        new Ontology(new int[] { 
                NO_PARENT, 
                ATTRIBUTE_BLOCK, 
                ATTRIBUTE_BLOCK, 
                NON_SERIES, 
                NON_SERIES, 
                SINGLE_VALUE, 
                SINGLE_VALUE, 
                VALUE_SERIES, 
                VALUE_SERIES, 
                INTERVAL, 
                INTERVAL,
                ATTRIBUTE_BLOCK,
                VALUE_MATRIX,
                VALUE_MATRIX,
                VALUE_MATRIX 
        }, BLOCK_TYPE_NAMES);

    
	/** Constructs a new ontology where each of the entries points to its parent. */
	private Ontology(int[] parents, String[] names) {
		this.parentId = parents;
		this.names = names;
	}

	/** Returns true if child is a parent. */
	public boolean isA(int child, int parent) {
		while (child != parent) {
			child = parentId[child];
			if (child == -1)
				return false;
		}
		return true;
	}

	/** Maps the name of a class to its index or -1 if unknown. */
	public int mapName(String name) {
		for (int i = 0; i < names.length; i++) {
			if (names[i].equals(name))
				return i;
		}
		return -1;
	}

	/** Maps an index to its name. */
	public String mapIndex(int index) {
		if ((index >= 0) && (index < names.length))
			return names[index];
		else
			return null;
	}

	public String[] getNames() {
		return names;
	}
}
