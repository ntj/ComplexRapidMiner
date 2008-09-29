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
package com.rapidminer.operator.validation;

import java.util.List;

import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.MissingIOObjectException;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.performance.PerformanceVector;
import com.rapidminer.tools.math.AverageVector;


/**
 * Tools class for validation operators. This class provides methods for average
 * building of performance vectors and other average vectors.
 * 
 * @author Ingo Mierswa
 * @version $Id: Tools.java,v 1.3 2008/05/09 19:22:54 ingomierswa Exp $
 */
public class Tools {

	/**
	 * Searches for the average vectors in the given IOContainer and fills the
	 * list if it is empty or build the averages. Only performance vectors are
	 * averaged.
	 */
	public static void handleAverages(IOContainer evalOutput, List<AverageVector> averageVectors) throws OperatorException {
		handleAverages(evalOutput, averageVectors, true);
	}

	/**
	 * Searches for the average vectors in the given IOContainer and fills the
	 * list if it is empty or build the averages. The boolean flag
	 * onlyPerformanceVectors indicates if the average should be built from
	 * PerformanceVectors only or from other AverageVectors too. Throws a
	 * NullPointerException if averageVectors is null.
	 */
	public static void handleAverages(IOContainer evalOutput, List<AverageVector> averageVectors, boolean onlyPerformanceVectors) throws OperatorException {
		Class<? extends IOObject> requestClass = AverageVector.class;
		if (onlyPerformanceVectors)
			requestClass = PerformanceVector.class;
		if (averageVectors.size() == 0) {
			// first run --> do not calculate average values but fill the vector list
			boolean inputOk = true;
			while (inputOk) {
				try {
					AverageVector currentAverage = (AverageVector) evalOutput.remove(requestClass);
					averageVectors.add(currentAverage);
				} catch (MissingIOObjectException e) {
					inputOk = false;
				}
			}
		} else {
			// later runs --> build the average with corresponding average vectors
			for (int n = 0; n < averageVectors.size(); n++) {
				AverageVector currentAverage = (AverageVector) evalOutput.remove(requestClass);
				AverageVector oldVector = averageVectors.get(n); // get last
																	// averaged
																	// average
																	// vector
				if (!oldVector.getClass().isInstance(currentAverage))
					throw new OperatorException("ValidationChain: Average vector mismatch! Fatal error...");
				for (int i = 0; i < oldVector.size(); i++) {
					oldVector.getAveragable(i).buildAverage(currentAverage.getAveragable(i)); // build
																								// new
																								// average
																								// for
																								// all
																								// criteria
				}
			}
		}
	}
    
    /**
     * Returns the first performance vector in the given list or null if no
     * performance vectors exist.
     */
    public static PerformanceVector getPerformanceVector(List<AverageVector> averageVectors) {
        java.util.Iterator<AverageVector> i = averageVectors.iterator();
        while (i.hasNext()) {
            AverageVector currentAverage = i.next();
            if (currentAverage instanceof PerformanceVector)
                return (PerformanceVector) currentAverage;
        }
        return null;
    }
}
