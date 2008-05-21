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

import com.rapidminer.operator.preprocessing.MaterializeDataInMemory;

/**
 * Cleans up unused memory resources. Might be very useful in combination with the
 * {@link MaterializeDataInMemory} operator after large preprocessing trees using
 * lot of views or data copies. Internally, this operator simply invokes a 
 * garbage collection from the underlying Java programming language.  
 * 
 * @author Ingo Mierswa
 * @version $Id: MemoryCleanUp.java,v 1.2 2008/05/09 19:23:19 ingomierswa Exp $
 */
public class MemoryCleanUp extends Operator {

	public MemoryCleanUp(OperatorDescription description) {
		super(description);
	}

	@Override
	public IOObject[] apply() throws OperatorException {
		System.gc();
		return new IOObject[0];
	}

	@Override
	public Class[] getInputClasses() {
		return new Class[0];
	}

	@Override
	public Class[] getOutputClasses() {
		return new Class[0];
	}
}
