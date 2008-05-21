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
package com.rapidminer;

import java.io.File;
import java.io.IOException;

import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.Operator;
import com.rapidminer.parameter.UndefinedParameterError;
import com.rapidminer.tools.ParameterService;
import com.rapidminer.tools.Tools;


/**
 * Main command line program.
 * 
 * @author Ingo Mierswa, Simon Fischer
 * @version $Id: RapidMinerCommandLine.java,v 1.7 2008/05/09 19:23:20 ingomierswa Exp $
 */
public class RapidMinerCommandLine extends RapidMiner implements BreakpointListener {

	private static final String LICENSE = "RapidMiner version " + RapidMiner.getVersion() + ", Copyright (C) 2001-2007" + Tools.getLineSeparator() + "RapidMiner comes with ABSOLUTELY NO WARRANTY; This is free software," + Tools.getLineSeparator() + "and you are welcome to redistribute it under certain conditions;" + Tools.getLineSeparator() + "see license information in the file named LICENSE.";

	private boolean showLogo = false;

	private String processFile = null;

	/**
	 * This tread waits for pressing an arbitrary key. Used for resuming an
	 * process if a breakpoint was reached in command line mode.
	 */
	private static class WaitForKeyThread extends Thread {

		private Operator operator;

		public WaitForKeyThread(Operator operator) {
			this.operator = operator;
		}

		public void run() {
			try {
				System.in.read();
			} catch (IOException e) {
				System.err.println("Error occured: " + e.getMessage());
			}
			operator.resume();
		}
	}

	public void breakpointReached(Operator operator, IOContainer container, int location) {
		System.out.println("Results in application " + operator.getApplyCount() + " of " + operator.getName() + ":" + Tools.getLineSeparator() + container);
		System.out.println("Breakpoint reached " + (location == BreakpointListener.BREAKPOINT_BEFORE ? "before " : "after ") + operator.getName() + ", press enter...");
		new WaitForKeyThread(operator).start(); // must be extra thread to
												// ensure that wait is invoked
												// before notify...
	}

	/** Does nothing. */
	public void resume() {}

	/** Parses the commandline arguments. */
	private void parseArguments(String[] argv) {
		processFile = null;

		for (int i = 0; i < argv.length; i++) {
			if (argv[i].equals("-l")) {
				showLogo = true;
			} else {
				processFile = argv[i];
			}
		}

		if (processFile == null) {
			printUsage();
		}
	}

	private static void printUsage() {
		System.err.println("Usage: " + RapidMinerCommandLine.class.getName() + " PROCESSFILE");
		System.exit(1);
	}

	private void run() {
		ParameterService.ensureRapidMinerHomeSet();
		
		if (showLogo)
			RapidMiner.showSplash();
		if (showLogo)
			RapidMiner.hideSplash();
		
		// init rapidminer
		RapidMiner.init();

		Process process = null;
		try {
			process = RapidMiner.readProcessFile(new File(processFile));
		} catch (Exception e) {
			System.err.println("ERROR: Cannot read process setup '" + processFile + "'...");
			RapidMiner.quit(1);
		}

		if (process != null) {
			try {
				process.addBreakpointListener(this);
				IOContainer results = process.run();
				process.getRootOperator().sendEmail(results, null);
				System.out.println("Process finished successfully");
				RapidMiner.quit(0);
			} catch (Throwable e) {
				String debugProperty = System.getProperty(PROPERTY_RAPIDMINER_GENERAL_DEBUGMODE);
				boolean debugMode = Tools.booleanValue(debugProperty, false);
                process.getLog().logFinalException("Process failed: " + e.getMessage(), process, e, debugMode);
				try {
					process.getRootOperator().sendEmail(null, e);
				} catch (UndefinedParameterError ex) {
					// cannot happen
				}
				System.err.println("Process not successful");
				RapidMiner.quit(1);
			}
		}
	}

	public static void main(String argv[]) {		
		System.out.println(LICENSE);
		RapidMinerCommandLine main = new RapidMinerCommandLine();
		main.parseArguments(argv);
		main.run();
	}

}
