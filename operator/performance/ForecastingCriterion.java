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
package com.rapidminer.operator.performance;

import com.rapidminer.operator.Operator;

/**
 * Indicates that this criterion is able to calculate a performance for a forecasting problem.
 * For this reason, those criteria must be able to take the horizon into account which can be
 * derived from the parent operator during calculation time.
 * 
 * WARNING: Do not use the parameter getting during creation time since the corresponding
 * performance criteria will be created in the performance evaluator creation.
 * 
 * @author Ingo Mierswa
 * @version $Id: ForecastingCriterion.java,v 1.1 2008/09/10 15:18:59 ingomierswa Exp $
 */
public interface ForecastingCriterion {

	public void setParent(Operator parent);
	
}
