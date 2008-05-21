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

import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.rapidminer.Process;
import com.rapidminer.ProcessListener;
import com.rapidminer.RapidMiner;
import com.rapidminer.operator.condition.InnerOperatorCondition;
import com.rapidminer.operator.condition.SimpleChainInnerOperatorCondition;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeFile;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.parameter.ParameterTypeString;
import com.rapidminer.parameter.UndefinedParameterError;
import com.rapidminer.tools.LogService;
import com.rapidminer.tools.Tools;


/**
 * Each process must contain exactly one operator of this class and it must
 * be the root operator of the process. The only purpose of this operator is
 * to provide some parameters that have global relevance.
 * 
 * @author Ingo Mierswa
 * @version $Id: ProcessRootOperator.java,v 2.25 2006/04/14 11:42:27 ingomierswa
 *          Exp $
 */
public final class ProcessRootOperator extends OperatorChain {

	/** The property name for &quot;The default random seed (-1: random random seed).&quot; */
	public static final String PROPERTY_RAPIDMINER_GENERAL_RANDOMSEED = "rapidminer.general.randomseed";
	
    public static final String PARAMETER_ENCODING = "encoding";
    
    public static final String PARAMETER_LOGVERBOSITY = "logverbosity";
    
	public static final String PARAMETER_LOGFILE = "logfile";
	
	public static final String PARAMETER_RESULTFILE = "resultfile";
	
	public static final String PARAMETER_TEMP_DIR = "temp_dir";
	
	public static final String PARAMETER_DELETE_TEMP_FILES = "delete_temp_files";
	
	public static final String PARAMETER_RANDOM_SEED = "random_seed";
	
	public static final String PARAMETER_NOTIFICATION_EMAIL = "notification_email";
    
	static {
		RapidMiner.registerRapidMinerProperty(new ParameterTypeInt(PROPERTY_RAPIDMINER_GENERAL_RANDOMSEED, "The default random seed (-1: random random seed).", 1, Integer.MAX_VALUE, 2001));
	}

    /** The list of listeners for process events. */
	private List<ProcessListener> listenerList = new LinkedList<ProcessListener>();

    /** The process which is connected to this process operator. */
    private Process process;
    
    /** Creates a new process operator without reference to an process. */
	public ProcessRootOperator(OperatorDescription description) {
		this(description, null);
	}

	/** Creates a new process operator which directly references to the given process. */
	public ProcessRootOperator(OperatorDescription description, Process process) {
		super(description);
        addValue(new Value("memory", "The current memory usage.") {
            public double getValue() {
                return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            }
        });
        setProcess(process);
        rename("Root");
	}
	
    /** Sets the process. */
	public void setProcess(Process process) {
        this.process = process;
        registerOperator(this.process);
    }
    
    /**
     * Returns the process of this operator if available. Overwrites the method
     * from the superclass.
     */
    public Process getProcess() {
        return process;
    }

	/**
	 * Returns true since this operator chain should just return the output of
	 * the last inner operator.
	 */
	public boolean shouldReturnInnerOutput() {
		return true;
	}

	/** Returns a simple chain condition. */
	public InnerOperatorCondition getInnerOperatorCondition() {
		return new SimpleChainInnerOperatorCondition();
	}

	/**
	 * Since the apply methods of the inner operators already add additional
	 * output, the handle additional output method should simply return a new
	 * container which is build from the additional output objects. Therefore
	 * this method returns true.
	 */
	public boolean getAddOnlyAdditionalOutput() {
		return true;
	}

	/**
	 * Returns the highest possible value for the maximum number of innner
	 * operators.
	 */
	public int getMaxNumberOfInnerOperators() {
		return Integer.MAX_VALUE;
	}

	/** Returns 0 for the minimum number of innner operators. */
	public int getMinNumberOfInnerOperators() {
		return 0;
	}

    /** Adds an process listener to the list of listeners. */
	public void addProcessListener(ProcessListener l) {
		listenerList.add(l);
	}

    /** Removes an process listener from the list of listeners. */
	public void removeProcessListener(ProcessListener l) {
		listenerList.remove(l);
	}

	/** Clears all process listeners. */
	public void clearListeners() {
		listenerList.clear();
	}
	
	/**
	 * Called at the beginning of the process. Notifies all listeners and the
	 * children operators (super method).
	 */
	public void processStarts() throws OperatorException {
		super.processStarts();
		Iterator i = listenerList.iterator();
		while (i.hasNext()) {
			((ProcessListener) i.next()).processStarts();
		}
	}

	/** Counts the step and notifies all process listeners. */
	protected void processStep() {
		Iterator i = listenerList.iterator();
		while (i.hasNext()) {
			((ProcessListener) i.next()).processStep(this);
		}
	}

	/**
	 * Called at the end of the process. Notifies all listeners and the
	 * children operators (super method).
	 */
	public void processFinished() throws OperatorException {
		super.processFinished();
		Iterator i = listenerList.iterator();
		while (i.hasNext()) {
			((ProcessListener) i.next()).processEnded();
		}
	}

    /** This method can be used to send an email after the process has finished. Currently
     *  only a working sendmail server is supported. */
	public void sendEmail(IOContainer results, Throwable e) throws UndefinedParameterError {		
		String email = getParameterAsString(PARAMETER_NOTIFICATION_EMAIL);
		if (email == null)
			return;
		log("Sending notification email to '" + email + "'");

		String name = email;
		int at = name.indexOf("@");
		if (at >= 0)
			name = name.substring(0, at);

		String subject = "Process " + getName() + " finished";
		StringBuffer content = new StringBuffer("Hello " + name + "," + Tools.getLineSeparator() + Tools.getLineSeparator());
		content.append("I'm sending you a notification message on your process '" + getName() + "'." + Tools.getLineSeparator());

		File logFile = getLog().getLogFile();
		if (logFile != null) {
			content.append("Logfile is file://" + logFile.getAbsolutePath() + Tools.getLineSeparator() + Tools.getLineSeparator());
		}

		if (e != null) {
			content.append("Process failed: " + e.toString());
			subject = "Process " + getName() + " failed";
		}

		if (results != null) {
			content.append(Tools.getLineSeparator() + Tools.getLineSeparator() + "Results:");
			ResultObject result;
			int i = 0;
			while (true) {
				try {
					result = results.get(ResultObject.class, i);
					content.append(Tools.getLineSeparator() + Tools.getLineSeparator() + Tools.getLineSeparator() + result.toResultString());
					i++;
				} catch (MissingIOObjectException exc) {
					break;
				}
			}
		}

		Tools.sendEmail(email, subject, content.toString());
	}
    
	public Class[] getInputClasses() {
		return new Class[0];
	}

	public Class[] getOutputClasses() {
		return new Class[0];
	}
	
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		ParameterType type = new ParameterTypeCategory(PARAMETER_LOGVERBOSITY, "Log verbosity level.", LogService.LOG_VERBOSITY_NAMES, LogService.INIT);
		type.setExpert(false);
		types.add(type);
		type = new ParameterTypeFile(PARAMETER_LOGFILE, "File to write logging information to.", "log", true);
		type.setExpert(false);
		types.add(type);
		types.add(new ParameterTypeFile(PARAMETER_RESULTFILE, "File to write inputs of the ResultWriter operators to.", "res", true));
		int seed = 2001;
		String seedProperty = System.getProperty(PROPERTY_RAPIDMINER_GENERAL_RANDOMSEED);
		try {
			if (seedProperty != null)
				seed = Integer.parseInt(seedProperty);
		} catch (NumberFormatException e) {
			logWarning("Bad integer in property 'rapidminer.general.randomseed', using default seed (2001).");
		}
		types.add(new ParameterTypeInt(PARAMETER_RANDOM_SEED, "Global random seed for random generators (-1 for initialization by system time).", Integer.MIN_VALUE, Integer.MAX_VALUE, seed));
		types.add(new ParameterTypeString(PARAMETER_NOTIFICATION_EMAIL, "Email address for the notification mail.", true));
		String encoding = RapidMiner.SYSTEM_ENCODING_NAME;
		String encodingProperty = System.getProperty(RapidMiner.PROPERTY_RAPIDMINER_GENERAL_DEFAULT_ENCODING);
		if (encodingProperty != null)
			encoding = encodingProperty;
        types.add(new ParameterTypeString(PARAMETER_ENCODING, "The encoding of the process XML description.", encoding));
		return types;
	}
}
