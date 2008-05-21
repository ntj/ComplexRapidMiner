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
package com.rapidminer.gui;

import com.rapidminer.NoBugError;
import com.rapidminer.Process;
import com.rapidminer.ProcessListener;
import com.rapidminer.RapidMiner;
import com.rapidminer.gui.tools.SwingTools;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.ProcessRootOperator;
import com.rapidminer.operator.ProcessStoppedException;
import com.rapidminer.parameter.UndefinedParameterError;
import com.rapidminer.tools.LogService;
import com.rapidminer.tools.Tools;


/**
 * A Thread for running an process in the RapidMinerGUI. This thread is necessary in order to
 * keep the GUI running (and working). Please note that this class can only be 
 * used from a running RapidMiner GUI since several dependencies to the class 
 * {@link RapidMinerGUI} and {@link MainFrame} exist. If you want to perform an
 * process in its own thread from your own program simply use a Java Thread
 * peforming the method process.run() in its run()-method.
 * 
 * @author Ingo Mierswa, Simon Fischer
 * @version $Id: ProcessThread.java,v 2.15 2006/03/21 15:35:40 ingomierswa
 *          Exp $
 */
public class ProcessThread extends Thread implements ProcessListener {

	private Process process;

	public ProcessThread(Process process) {
		this.process = process;
		this.process.getRootOperator().addProcessListener(this);
	}

	public void run() {
		try {
            IOContainer results = process.run();
			beep("success");
			process.getRootOperator().sendEmail(results, null);
            RapidMinerGUI.getMainFrame().processEnded(results);
		} catch (ProcessStoppedException ex) {
			//beep("error");
            process.getLog().log(ex.getMessage(), LogService.STATUS);
            // here the process ended method is not called ! let the thread finish the
            // current operator and send no events to the main frame...
            // also no beep...
		} catch (Throwable e) {
			beep("error");
			String debugProperty = System.getProperty(RapidMiner.PROPERTY_RAPIDMINER_GENERAL_DEBUGMODE);
			boolean debugMode = Tools.booleanValue(debugProperty, false);
			String message = e.getMessage();
			if (!debugMode) {
				if (e instanceof RuntimeException) {
					message = "operator cannot be executed. Check the log messages...";
				}
			}
			process.getLog().logFinalException("Process failed: " + message, process, e, debugMode);
			try {
				process.getRootOperator().sendEmail(null, e);
			} catch (UndefinedParameterError ex) {
                // cannot happen
				process.getLog().log("Problems during sending result mail: " + ex.getMessage(), LogService.WARNING);
			} 
			if (e instanceof NoBugError) { // no bug? Show nice error screen (user error infos)...
				SwingTools.showFinalErrorMessage("Process failed", e);
			} else {
				if (debugMode) {
					SwingTools.showFinalErrorMessage("Process failed", e);
				} else {
					// perform process check. No bug report if errors...
					int oldVerbosity = process.getLog().getVerbosityLevel();
					process.getLog().setVerbosityLevel(LogService.OFF);
					boolean errors = process.checkProcess(new IOContainer());
					process.getLog().setVerbosityLevel(oldVerbosity);
					if (errors) {
						// ugly runtime exceptions (NPE, ArrayIndexOutOfBound...) should
						// not be shown...
						if ((e instanceof NullPointerException) || 
							(e instanceof ArrayIndexOutOfBoundsException)) {
							SwingTools.showVerySimpleErrorMessage("Process failed!\n" + 
									                              "The setup does not seem to contain any obvious errors,\n" +
									                              "but you should check the log messages or activate the\n"+
									                              "debug mode in the settings dialog in order to get more\n"+
									                              "information about this problem.");
						} else {
							SwingTools.showSimpleErrorMessage("Process failed", e);
						}
					} else {
						SwingTools.showVerySimpleErrorMessage("Process failed!\nSince the setup seem to contain errors you should check the setup and the log messages.");
					}
				}
			}
            RapidMinerGUI.getMainFrame().processEnded(null);
		}
	}

	public static void beep(String reason) {
		if (Tools.booleanValue(System.getProperty("rapidminer.gui.beep." + reason), false)) {
			java.awt.Toolkit.getDefaultToolkit().beep();
		}
	}

	public void stopProcess() {
		this.process.stop();
	}
	
	/** Registers the data tables in the result display. */
	public void processStarts() {
		RapidMinerGUI.getMainFrame().getResultDisplay().setDataTables(process.getDataTables());
	}
	
	public void processStep(ProcessRootOperator op) {}

	public void processEnded() {}
	
	public String toString() {
		return "ProcessThread (" + process.getProcessFile() + ")";
	}
}
