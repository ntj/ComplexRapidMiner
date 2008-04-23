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
package com.rapidminer.operator.similarity.clustermodel;

import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.learner.clustering.ClusterModel;
import com.rapidminer.operator.similarity.SimilarityMeasure;

/**
 * Interface that represents a similarity that is based on a cluster model.
 * 
 * @author Michael Wurst
 * @version $Id: ClusterModelSimilarity.java,v 1.1 2007/05/27 22:01:36 ingomierswa Exp $
 */
public interface ClusterModelSimilarity extends SimilarityMeasure {

	/**
	 * @param cm
	 *            a cluster model
	 * @throws an
	 *             OperatorException, if the given measure is not applicable. This is the case, e.g. if a measure for a hierarchical cluster model is
	 *             applied to a flat cluster model.
	 */
	public void init(ClusterModel cm) throws OperatorException;
}
