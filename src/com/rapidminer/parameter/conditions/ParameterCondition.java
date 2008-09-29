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
package com.rapidminer.parameter.conditions;

/**
 * The ParameterCondition interface can be used to define dependencies 
 * for parameter types, e.g. to show certain parameters only in cases
 * where another parameter has a specified value.
 * 
 * @author Sebastian Land, Ingo Mierswa
 * @version $Id: ParameterCondition.java,v 1.3 2008/07/13 16:39:42 ingomierswa Exp $
 */
public interface ParameterCondition {

	public boolean dependencyMet();

	public boolean becomeMandatory();
	
}
