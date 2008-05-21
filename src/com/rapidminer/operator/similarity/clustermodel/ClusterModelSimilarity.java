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

import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.learner.clustering.ClusterModel;
import com.rapidminer.operator.similarity.SimilarityMeasure;

/**
 * Interface that represents a similarity that is based on a cluster model.
 * 
 * @author Michael Wurst
 * @version $Id: ClusterModelSimilarity.java,v 1.3 2008/05/09 19:22:49 ingomierswa Exp $
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
