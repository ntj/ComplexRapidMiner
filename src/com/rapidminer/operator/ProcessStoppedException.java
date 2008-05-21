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
package com.rapidminer.operator;

/**
 * Exception class whose instances are thrown by instances of the class
 * 'Operator' or of one of its subclasses.
 * 
 * @author Ingo Mierswa
 * @version $Id: ProcessStoppedException.java,v 1.3 2008/05/09 19:23:19 ingomierswa Exp $
 */
public class ProcessStoppedException extends OperatorException {

	private static final long serialVersionUID = 8299515313202467747L;

	public ProcessStoppedException(Operator o) {
		super("Process stopped in " + o.getName());
	}
}
