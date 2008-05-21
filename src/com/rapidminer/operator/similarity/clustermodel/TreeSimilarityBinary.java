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
package com.rapidminer.operator.similarity.clustermodel;

/**
 * Return 1.0 exactly if two items are in the same node and zero else.
 * 
 * @author Michael Wurst
 * @version $Id: TreeSimilarityBinary.java,v 1.3 2008/05/09 19:22:49 ingomierswa Exp $
 */
public class TreeSimilarityBinary extends MostSpecificCommonNodeSimilarity {

	private static final long serialVersionUID = 4241127025872079679L;

	public double similarity(String x, String y) {
		return getNode(x) == getNode(y) ? 1.0 : 0.0;
	}
}
