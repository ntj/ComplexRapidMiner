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
package com.rapidminer.tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Date;

import com.rapidminer.NoBugError;
import com.rapidminer.Process;
import com.rapidminer.RapidMiner;
import com.rapidminer.gui.RapidMinerGUI;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.ProcessRootOperator;
import com.rapidminer.parameter.UndefinedParameterError;
import com.rapidminer.tools.log.DeleteLogFormatFilter;
import com.rapidminer.tools.log.DummyLogFormatFilter;
import com.rapidminer.tools.log.FormattedFilterStream;
import com.rapidminer.tools.log.StreamMultiplier;


/**
 * <p>Utility class providing static methods for logging.<br>
 * Parameters read from the XML process configuration file:</p>
 * <ul>
 * <li>logfile (filename or "stdout" or "stderr")</li>
 * <li>logverbosity (possible values are in
 * {@link LogService#LOG_VERBOSITY_NAMES}</li>
 * </ul>
 * 
 * <p>Beside the <b>local</b> log service associated with a concrete process and which will
 * be automatically initialized during the setup phase, one <b>global</b> log service exist 
 * which is used for generic log messages not bound to the operators used in a process.
 * This global log service is usually initialized to log messages on system out (at least during the 
 * basic initialization phase of RapidMiner). After the basic intialization phase, 
 * the global messages will be presented in the message viewer (if the RapidMiner GUI
 * is used) or still printed to system out or in any other stream defined via the
 * method {@link #initGlobalLogging(OutputStream, int)}. Alternatively, one could also define an 
 * environment variable named {@link RapidMiner#PROPERTY_RAPIDMINER_GLOBAL_LOG_FILE}.</p>
 * 
 * <p>Usually, operators should only use the log verbosities MINIMUM for messages
 * with a low priority and STATUS for normal information messages. In rare cases, the 
 * verbosity level NOTE could be used for operators stating some message more
 * important then STATUS (hence the user should see the message for the default
 * log verbosity level of INIT) but not as important then WARNING. The verbosity
 * levels WARNING, EXCEPTION, and ERROR should be used in error cases. All other 
 * log verbosity levels should only be used by internal RapidMiner classes and not by 
 * user written operators.</p>
 * 
 * <p>We recommend to set the parameter for the log verbosity level to INIT for the 
 * process design phase (eventually STATUS for debugging) and to the log verbosity 
 * level WARNING in the production phase. This way it is ensured that not too many logging
 * messages are produced in the production phase.</p>
 * 
 * <p>Log messages can be formatted by using the following macros:</p>
 * <ul>
 *  <li>&quot;$b&quot; and &quot;^b&quot; start and end bold mode respectively</li>
 *  <li>&quot;$i&quot; and &quot;^i&quot; start and end italic mode respectively</li>
 *  <li>&quot;$m&quot; and &quot;^m&quot; start and end monospace mode respectively</li>
 *  <li>&quot;$n&quot; and &quot;^n&quot; start and end note color mode respectively</li>
 *  <li>&quot;$w&quot; and &quot;^w&quot; start and end warning color mode respectively</li>
 *  <li>&quot;$e&quot; and &quot;^e&quot; start and end error color mode respectively</li>
 * </ul>
 * 
 * @author Ingo Mierswa
 * @version $Id: LogService.java,v 1.12 2008/05/09 19:22:55 ingomierswa Exp $
 */
public class LogService implements LoggingHandler {

    /** The prefix used to indicate the global logger. */
    public static final String GLOBAL_PREFIX = "$gG^g";
    
	// -------------------- Verbosity Level --------------------

    /** Indicates an unknown verbosity level. */
    public static final int UNKNOWN_LEVEL = -1;
    
    /**
     * Indicates the lowest log verbosity. Should only be used for very detailed
     * but not necessary logging.
     */
    public static final int MINIMUM = 0;

	/**
	 * Indicates log messages concerning in- and output. Should only be used by
	 * the class Operator itself and not by its subclasses.
	 */
	public static final int IO = 1;

	/** The default log verbosity for all logging purposes of operators. */
	public static final int STATUS = 2;

	/**
	 * Only the most important logging messaged should use this log verbosity.
	 * Currently used only by the LogService itself.
	 */
	public static final int INIT = 3;

	/** Use this log verbosity for logging of important notes, i.e. things less important than warnings 
	 *  but important enough to see for all not interested in the detailed status messages. */
	public static final int NOTE = 4;
	
	/** Use this log verbosity for logging of warnings. */
	public static final int WARNING = 5;

	/** Use this log verbosity for logging of errors. */
	public static final int ERROR = 6;

	/**
	 * Use this log verbosity for logging of fatal errors which will stop
	 * process running somewhere in the future.
	 */
	public static final int FATAL = 7;
    
	/**
	 * Normally this log verbosity should not be used by operators. Messages
	 * with this verbosity will always be displayed.
	 */
    public static final int MAXIMUM = 8;
    
    /** For switching off logging during testing. */
    public static final int OFF = 9;



	public static final String LOG_VERBOSITY_NAMES[] = { "all", "io", "status", "init", "notes", "warning", "error", "fatal", "almost_none", "off" };

    private static final String[] VERBOSITYLEVEL_START = { "", "", "", "", "$n$b[NOTE]^b ", "$w$b[Warning]^b ", "$e$b[Error]^b ", "$e$b[Fatal]^b ", "", "" };

    private static final String[] VERBOSITYLEVEL_END   = { "", "", "", "", "^n",            "^w",               "^e",             "^e"            , "", "" };
    
    
    /** The global logging. */
    private static LogService globalLogging = null;
    
	/** The PrintStream to write the messages to. */
	private PrintStream logOut = System.out;

	/** The minimal verbosity level. Message below this level are ignored. */
	private int minVerbosityLevel = INIT;

	/** The last printed message. */
	private String lastMessage;

	/** Counts how often a message was repeated. */
	private int equalMessageCount;

	private File logFile = null;
    
    private String prefix = GLOBAL_PREFIX;
    
    private Process process = null;
    
	// ------ methods for init -------

    /** Creates a log service for this process. The properties 
     * (possible log file and log verbosity) are taken from the
     * process parameters.*/
    public LogService(Process process) throws UndefinedParameterError {
        this(process, UNKNOWN_LEVEL);
    }
    
    /** Creates a log service for this process. The properties 
     * (possible log file and log verbosity) are taken from the
     * process parameters. If the given logVerbosity is a valid 
     * verbosity level, then this level is used instead of the
     * level defined in the process. Otherwise, the given log 
     * verbosity must have value UNKNOWN_LEVEL and the process
     * level will be used. */
    public LogService(Process process, int logVerbosity) throws UndefinedParameterError {
        this.process = process;
        int verbosityLevel = logVerbosity;
        if (verbosityLevel == UNKNOWN_LEVEL) {
            verbosityLevel = process.getRootOperator().getParameterAsInt(ProcessRootOperator.PARAMETER_LOGVERBOSITY);
        }
        String fileName = process.getRootOperator().getParameterAsString(ProcessRootOperator.PARAMETER_LOGFILE);  
        init(fileName, verbosityLevel, "$gP^g");
    }
    
    /** Creates a log service with verbosity INIT logging on system out 
     *  (default global logging). */
    private LogService() {
        init(System.out, INIT, false, GLOBAL_PREFIX);
    }
        
    /** Returns the global logging. If no logging was otherwise create, this
     *  method creates the default standard out log service if no log file
     *  was defined in the property {@link RapidMiner#PROPERTY_RAPIDMINER_GLOBAL_LOG_FILE}.
     *  Alternatively, developers can invoke the method {@link #initGlobalLogging(OutputStream, int)}. */
    public static synchronized LogService getGlobal() {
    	if (globalLogging == null) {
    		globalLogging = new LogService();
    		String globalLogFile = System.getProperty(RapidMiner.PROPERTY_RAPIDMINER_GLOBAL_LOG_FILE);
    		String globalLogVerbosity = System.getProperty(RapidMiner.PROPERTY_RAPIDMINER_GLOBAL_LOG_VERBOSITY);
    		int logVerbosity = getVerbosityLevel(globalLogVerbosity);
    		if ((logVerbosity < UNKNOWN_LEVEL) || (logVerbosity >= LOG_VERBOSITY_NAMES.length)) {
    			globalLogging.logError("Only numbers between " + MINIMUM + " and " + MAXIMUM + " or one of the log verbosity level names are allowed as value for " + RapidMiner.PROPERTY_RAPIDMINER_GLOBAL_LOG_VERBOSITY + ". Was: '" + globalLogVerbosity + "'. Using INIT instead...");
    			logVerbosity = INIT;
    		}
    		globalLogging.init(globalLogFile, logVerbosity, GLOBAL_PREFIX);
    	}
    	return globalLogging;
    }

    /** Initializes the global logging, i.e. the global log service not bound
     *  to a concrete process. Usually, system out or the message viewer (GUI) are
     *  used for global log messages. */
    public static void initGlobalLogging(OutputStream out, int logVerbosity) {
        getGlobal().init(out, logVerbosity, false, GLOBAL_PREFIX);
    }
    
	/** Initialises the LogService using the given parameters. */
	private void init(String fileName, int verbosityLevel, String prefix) {
		if (fileName == null) {
			logFile = null;
			init(System.out, verbosityLevel, false, prefix);
		} else if (fileName.equals("stderr")) {
			logFile = null;
			init(System.err, verbosityLevel, false, prefix);
			log("Logging: log file name is 'stderr', using system err for logging.", INIT);
		} else if (fileName.equals("stdout")) {
			logFile = null;
			init(System.out, verbosityLevel, false, prefix);
			log("Logging: log file name is 'stdout', using system out for logging.", INIT);
		} else {		
			if (process != null)
				logFile = process.resolveFileName(fileName);
			else
				logFile = new File(fileName);
				
			OutputStream out = null;
			try {
				out = new FileOutputStream(logFile);
			} catch (IOException e) {
				log("Cannot create logfile '" + fileName + "': " + e.getClass() + ":" + e.getMessage(), LogService.MAXIMUM);
				log("using stdout", LogService.MAXIMUM);
				out = System.out;
			}
			init(out, verbosityLevel, false, prefix);
			log("Logging: log file is '" + logFile.getName() + "'...", INIT);
		}
	}

	/**
	 * Initialises the LogService.
	 * 
	 * @param out
	 *            The stream to write the messages to.
	 * @param verbosityLevel
	 *            Only messages with message.verbosityLevel >= verbosityLevel
	 *            are logged
	 * @param format
	 *            must be true if the output should be formatted by the
	 *            FormattedPrintStream
	 */
	private void init(OutputStream out, int verbosityLevel, boolean format, String prefix) {
        if (format) {
            this.logOut = new PrintStream(new FormattedFilterStream(out, new DummyLogFormatFilter()));
        } else {
            this.logOut = new PrintStream(new FormattedFilterStream(out, new DeleteLogFormatFilter()));
        }
        this.prefix = prefix;
        if (verbosityLevel == UNKNOWN_LEVEL)
        	this.minVerbosityLevel = INIT;
        else
        	this.minVerbosityLevel = verbosityLevel;
        
		lastMessage = "";
		equalMessageCount = 0;
        
        if (RapidMinerGUI.getMainFrame() != null) {
            try {
                initGUI();
            } catch (UndefinedParameterError e) {
                log("Cannot initialize GUI logging: " + e.getMessage(), ERROR);
                e.printStackTrace();
            }
        }
	}

	public void initGUI() throws UndefinedParameterError {
        if (RapidMinerGUI.getMainFrame() != null) {
            if (process != null) {
                this.minVerbosityLevel = process.getRootOperator().getParameterAsInt(ProcessRootOperator.PARAMETER_LOGVERBOSITY); 
                String logFileName = process.getRootOperator().getParameterAsString(ProcessRootOperator.PARAMETER_LOGFILE); 
                this.logFile = process.resolveFileName(logFileName);
            } else {
                this.logFile = null;
            }
            
            if (this.logFile != null) {
                try {
                    this.logOut = 
                        new PrintStream(new StreamMultiplier(new FormattedFilterStream(new FileOutputStream(logFile), new DeleteLogFormatFilter()), 
                                        new FormattedFilterStream(RapidMinerGUI.getMainFrame().getMessageViewer().outputStream, new DummyLogFormatFilter())));
                } catch (java.io.IOException e) {
                    throw new RuntimeException("Cannot create log file: " + e);
                }
            } else {
                this.logOut = new PrintStream(RapidMinerGUI.getMainFrame().getMessageViewer().outputStream);
            }
        }
	}

	/** Closes the stream. ATTENTION: Invoking this method might close System.out / System.err !!! 
	 *  Don't use it for now (maybe in finalize of LogService objects after they are tied to Process). */
	/*
	public static void close() {
		if ((!System.out.equals(logOut)) && (!System.err.equals(logOut)))
			logOut.close();
	}
	*/

	/** Flush the streams. */
	public void flush() {		
		this.logOut.flush();
	}

	public void setVerbosityLevel(int level) {
	    this.minVerbosityLevel = level;
	}

	public int getVerbosityLevel() {
		return this.minVerbosityLevel;
	}

	public boolean isSufficientLogVerbosity(int level) {
		return level >= this.minVerbosityLevel;
	}

	// -------------------- Methoden zum Protokollieren --------------------
    
    /**
     * Writes the message to the output stream if the verbosity level is high
     * enough.
     * 
     * @deprecated please do not use this log method any longer, use the method {@link #log(String, int)} instead
     */
    @Deprecated
    public static void logMessage(String message, int verbosityLevel) {
        getGlobal().log(message, verbosityLevel);
    }
    
	/**
	 * Writes the message to the output stream if the verbosity level is high
	 * enough.
	 */
	public void log(String message, int verbosityLevel) {
		if (message == null)
			return;
		if (verbosityLevel < minVerbosityLevel)
			return;
		if (message.equals(lastMessage)) {
			equalMessageCount++;
			return;
		}
		if (equalMessageCount > 0) {
			logOut.println("Last message repeated " + equalMessageCount + " times.");
			equalMessageCount = 0;
		}
		lastMessage = message;
		logOut.println(prefix + " " + getTime() + " " + VERBOSITYLEVEL_START[verbosityLevel] + message + VERBOSITYLEVEL_END[verbosityLevel]);
	}

	/**
	 * Writes the message to the output stream if the verbosity level is high
	 * enough and appends the process tree with operator op marked.
	 */
	private void logMessageWithTree(String message, int verbosityLevel, Process process, Operator op) {
		if (verbosityLevel < minVerbosityLevel)
			return;
		
		log(message, verbosityLevel);
		
        String treeString = process.getRootOperator().createMarkedProcessTree(10, " here ==> ", op);
        logOut.println("$m" + treeString + "^m");
	}

	/** Writes the message and the stack trace of the exception. This method should not be used
	 *  from operators but only at the end of processes. This will be ensured by RapidMiner itself. 
	 *  If RapidMiner is not used in debug mode, the stack trace will not be shown. */
	public void logFinalException(String message, Process process, Throwable exception, boolean debugMode) {
		if (FATAL < this.minVerbosityLevel)
			return;
		
		Operator op = null;
		if (process != null)
			op = process.getCurrentOperator();
		if (op != null) {
			log(Tools.classNameWOPackage(exception.getClass()) + " occured in " + Tools.ordinalNumber(op.getApplyCount()) + " application of " + op, FATAL);
			logMessageWithTree(message, FATAL, process, op);
		} else {
			log(Tools.classNameWOPackage(exception.getClass()) + " occured.", FATAL);
			log(message, FATAL);
		}
		
		if (!(exception instanceof NoBugError)) {
			if (debugMode) {
				log(exception.getMessage(), FATAL);
				exception.printStackTrace(logOut);
			}
		}
	}

    /** Returns the current log file or null. */
	public File getLogFile() {
		return logFile;
	}

    public void log(String message) {
        log(message, STATUS);
    }

    public void logError(String message) {
        log(message, ERROR);
    }

    public void logNote(String message) {
        log(message, NOTE);
    }

    public void logWarning(String message) {
        log(message, WARNING);
    }
    
    // -------------------- private helper methods --------------------

    /** Returns the current system time nicely formatted. */
    private String getTime() {
        return java.text.DateFormat.getDateTimeInstance().format(new Date()) + ":";
    }
    
    /** Returns the parsed integer (if the given number represents a number) or the corresponding
     *  verbosity level if the given string is one of the level names or -1. */
    private static int getVerbosityLevel(String verbosityString) {
        if ((verbosityString == null) || (verbosityString.trim().length() == 0))
            return UNKNOWN_LEVEL;
        try {
            return Integer.parseInt(verbosityString);
        } catch (NumberFormatException e) {
            int counter = 0;
            for (String name : LOG_VERBOSITY_NAMES) {
                if (name.equals(verbosityString))
                    return counter;
                counter++;
            }
        }
        return UNKNOWN_LEVEL;
    }
}
