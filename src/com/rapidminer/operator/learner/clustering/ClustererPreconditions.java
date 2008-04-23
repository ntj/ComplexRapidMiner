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
package com.rapidminer.operator.learner.clustering;

import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;

/**
 * This class is used to check diverse preconditions on CluserModel objects.
 * 
 * @author Michael Wurst, Ingo Mierswa
 * @version $Id: ClustererPreconditions.java,v 1.1 2007/05/27 21:58:38 ingomierswa Exp $
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
