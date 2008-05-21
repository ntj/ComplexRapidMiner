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
package com.rapidminer.parameter;

import com.rapidminer.operator.UserError;

/**
 * This exception will be thrown if a non-optional parameter has no default
 * value and was not defined by the user.
 * 
 * @author Ingo Mierswa
 * @version $Id: UndefinedParameterError.java,v 2.2 2006/03/21 15:35:49
 *          ingomierswa Exp $
 */
public class UndefinedParameterError extends UserError {

	private static final long serialVersionUID = -2861031839668411515L;

	/** Creates a new UndefinedParameterError. */
	public UndefinedParameterError(String key) {
		super(null, 205, key);
	}
}
