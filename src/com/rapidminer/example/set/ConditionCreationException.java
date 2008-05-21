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
package com.rapidminer.example.set;

/**
 * Exception class whose instances are thrown during the creation of conditions.
 * 
 * @author Ingo Mierswa
 * @version $Id: ConditionCreationException.java,v 2.5 2006/03/21 15:35:39
 *          ingomierswa Exp $
 */
public class ConditionCreationException extends Exception {

	private static final long serialVersionUID = -7648754234739697969L;

	public ConditionCreationException(String message) {
		super(message);
	}
}
