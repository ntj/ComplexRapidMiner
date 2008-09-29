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

import com.rapidminer.tools.Tools;

/**
 * Will be thrown if an operator needs an input type which is not provided or
 * gets the wrong input. Should be thrown during validation, usually in the
 * method {@link OperatorChain#checkIO(Class[])}.
 * 
 * @author Ingo Mierswa, Simon Fischer
 * @version $Id: IllegalInputException.java,v 2.11 2006/03/21 15:35:42
 *          ingomierswa Exp $
 */
public class IllegalInputException extends Exception {

	private static final long serialVersionUID = 7043386419256147253L;

	private transient Operator operator;

	public IllegalInputException(Operator operator, Operator innerOperator, Class clazz) {
		super(operator.getName() + ": Inner operator " + innerOperator.getName() + " does not provide " + Tools.classNameWOPackage(clazz));
		this.operator = operator;
	}

	public IllegalInputException(Operator operator, Class clazz) {
		super(operator.getName() + ": Missing input: " + Tools.classNameWOPackage(clazz));
		this.operator = operator;
	}

	public IllegalInputException(Operator operator, String msg) {
		super(msg);
		this.operator = operator;
	}

	public Operator getOperator() {
		return operator;
	}
}
