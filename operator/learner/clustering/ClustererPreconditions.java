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
package com.rapidminer.operator.learner.clustering;

import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;

/**
 * This class is used to check diverse preconditions on CluserModel objects.
 * 
 * @author Michael Wurst, Ingo Mierswa
 * @version $Id: ClustererPreconditions.java,v 1.5 2008/09/12 10:30:41 tobiasmalbrecht Exp $
 */
public class ClustererPreconditions {

	/**
	 * The cluster model has to contain at least one object.
	 * 
	 * @param cm
	 *            the cluster model
	 */
	public static void isNonEmpty(FlatClusterModel cm) throws OperatorException {
		hasClusters(cm);
		boolean result = false;
		for (int i = 0; i < cm.getNumberOfClusters(); i++) {
			if (cm.getClusterAt(i).getObjects().hasNext())
				result = true;
		}
		if (!result)
			throw new UserError(null, 928);
	}

	/**
	 * The cluster model has to contain at least one cluster.
	 * 
	 * @param cm
	 *            the cluster model
	 */
	public static void hasClusters(FlatClusterModel cm) throws OperatorException {
		if (cm.getNumberOfClusters() == 0)
			throw new UserError(null, 929);
	}
}
