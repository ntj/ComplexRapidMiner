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
package com.rapidminer.operator.similarity;

import java.util.Iterator;

import com.rapidminer.operator.ResultObject;

/**
 * Represents an interface to a similarity/distance calculation. As similarity and distance a very closely related concepts, they are covered in the
 * same interface. Every similarity measure has the responsibility to implement the isDistance method, that indicates whether it behaves as a distance
 * or as similarity measure.
 * 
 * @author Michael Wurst, Ingo Mierswa
 * @version $Id: SimilarityMeasure.java,v 1.6 2008/05/09 19:22:52 ingomierswa Exp $
 */
public interface SimilarityMeasure extends ResultObject {

	/**
	 * Return the similarity of two objects given their ids.
	 * 
	 * @param x
	 * @param y
	 * @return double
	 */
	public double similarity(String x, String y);

	/**
	 * Is the similarity between the two objects with the given ids defined?.
	 * 
	 * @param x
	 * @param y
	 * @return boolean
	 */
	public boolean isSimilarityDefined(String x, String y);

	/**
	 * Return all object ids on which this similarity is defined.
	 * 
	 * @return Iterator
	 */
	public Iterator<String> getIds();

	/**
	 * Explain the similarity value, returned for the two given object ids.
	 * 
	 * @param x
	 * @param y
	 * @return String
	 */
	public String explainSimilarity(String x, String y);

	/**
	 * Determine whether the given class represents a distance or a similarity measure
	 * 
	 * @return true, for distance measure, false otherwise
	 */
	public boolean isDistance();
    
    /** Delivers the number of items covered by this measure. */
    public int getNumberOfIds();
    
}
