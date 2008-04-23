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
package com.rapidminer.operator.similarity.attributebased;

import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.similarity.SimilarityMeasure;


/**
 * Interface for similarities, that can be directly evaluated on examples.
 * 
 * @author Michael Wurst
 * @version $Id: ExampleBasedSimilarityMeasure.java,v 1.1 2007/05/27 21:59:46 ingomierswa Exp $
 */
public interface ExampleBasedSimilarityMeasure extends SimilarityMeasure {

	/**
	 * Initialize the similarity measure with an example set. Note: In general it is assumed that the example set is not copied.
	 * 
	 * @param exampleSet
	 *            the examples set
	 * @throws OperatorException
	 */
	public void init(ExampleSet exampleSet) throws OperatorException;

	/**
	 * Calculated the similarity of two examples.
	 * 
	 * @param x
	 *            the first example
	 * @param y
	 *            the second examples
	 * @return the similarity/distance or NaN if this value is undefined
	 */
	public double similarity(Example x, Example y);
}
