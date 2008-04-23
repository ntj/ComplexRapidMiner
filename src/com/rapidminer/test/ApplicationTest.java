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
package com.rapidminer.test;

import com.rapidminer.Process;
import com.rapidminer.RapidMiner;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.tools.LogService;


/**
 * Extends the JUnit test case by a method for checking the creation and
 * runnning of an process from an external application.
 * 
 * @author Ingo Mierswa
 * @version $Id: ApplicationTest.java,v 1.2 2007/06/07 17:12:22 ingomierswa Exp $
 */
public class ApplicationTest extends TestCase {

	public String getName() {
		return "Application test";
	}

	public void setUp() throws Exception {
		super.setUp();
		LogService.getGlobal().setVerbosityLevel(LogService.OFF);
		RapidMiner.init();
		LogService.getGlobal().setVerbosityLevel(LogService.OFF);
	}

	public void testProcessCreation() throws Exception {
		Process exp = ProcessCreator.createProcess();
		LogService.getGlobal().setVerbosityLevel(LogService.OFF);
		exp.run(new IOContainer(), LogService.OFF);
	}
}
