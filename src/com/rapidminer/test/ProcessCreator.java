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
package com.rapidminer.test;

import com.rapidminer.Process;
import com.rapidminer.RapidMiner;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.tools.OperatorService;


/**
 * Test and example class for the creation of RapidMiner processes from other
 * applications.
 * 
 * @author Ingo Mierswa
 * @version $Id: ProcessCreator.java,v 1.4 2008/05/09 19:22:48 ingomierswa Exp $
 */
public class ProcessCreator {

	public static Process createProcess() {
		// create process
		Process process = new Process();
		try {
			// create operator
			Operator inputOperator = OperatorService.createOperator("ExampleSetGenerator");
			// set parameters
			inputOperator.getParameters().setParameter("target_function", "sum classification");
			// register operator and set name
			inputOperator.rename("Input");
			// add operator to process
			process.getRootOperator().addOperator(inputOperator);
			// add other operators and set parameters
			// [...]
		} catch (Exception e) {
			e.printStackTrace();
		}
		return process;
	}

	public static void main(String[] argv) {
		// invoke init before using the OperatorService
		RapidMiner.init();

		// create process
		Process exp = createProcess();
		// print process setup
		System.out.println(exp.getRootOperator().createProcessTree(0));
		try {
			// perform process
			exp.run();
			// to run the process with an input from you application use
			// exp.run(new IOContainer(new IOObject[] { ... your IO objects ...
			// });
		} catch (OperatorException e) {
			e.printStackTrace();
		}
	}
}
