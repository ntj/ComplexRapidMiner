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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.rapidminer.datatable.DataTable;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.IllegalInputException;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorChain;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ProcessRootOperator;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.WrongNumberOfInnerOperatorsException;
import com.rapidminer.parameter.UndefinedParameterError;
import com.rapidminer.tools.LogService;
import com.rapidminer.tools.OperatorService;
import com.rapidminer.tools.RandomGenerator;
import com.rapidminer.tools.ResultService;
import com.rapidminer.tools.Tools;
import com.rapidminer.tools.XMLException;

/**
 * <p>This class was introduced to avoid confusing handling of operator maps and
 * other stuff when a new process definition is created. It is also necessary for file
 * name resolving and breakpoint handling.</p>
 * 
 * <p>If you want to use RapidMiner from your own application the best way is often to
 * create a process definition from the scratch (by adding the complete operator tree
 * to the process' root operator) or from a file (for example created with the
 * GUI beforehand) and start it by invoking the {@link #run()} method.</p>
 * 
 * @author Ingo Mierswa
 * @version $Id: Process.java,v 1.14 2008/05/09 19:23:19 ingomierswa Exp $
 */
public class Process implements Cloneable {

	public static final int PROCESS_STATE_UNKNOWN = -1;
	public static final int PROCESS_STATE_STOPPED = 0;
	public static final int PROCESS_STATE_PAUSED  = 1;
	public static final int PROCESS_STATE_RUNNING = 2;
	
    /** The root operator of the process. */
	private ProcessRootOperator rootOperator;

    /** This is the operator which is currently applied. */
	private Operator currentOperator;

    /** The process might be connected to this file which is then used to resolve relative
     *  file names which might be defined as parameters. */
	private File processFile;

    /** The listeners for breakpoints. */
	private List<BreakpointListener> breakpointListeners = new LinkedList<BreakpointListener>();

    /** Indicates if the process should stop. */
	private boolean stopProcess = false;

    /** The macro handler can be used to replace (user defined) macro strings. */
	private MacroHandler macroHandler = new MacroHandler(this);
    
    /** This map holds the names of all operators in the process. Operators are
     *  automatically registered during adding and unregistered after removal. */
    private Map<String, Operator> operatorNameMap = new HashMap<String, Operator>();
    
	/**
	 * Maps names of ProcessLog operators to Objects, that these Operators use
	 * for collecting statistics (objects of type {@link DataTable}).
	 */
	private Map<String, DataTable> dataTableMap = new HashMap<String, DataTable>();
	
	/** Indicates the current process state. */
	private int processState = PROCESS_STATE_STOPPED;
	
    /** The logging for this process. */
    private LogService logService;
    
    
	// -------------------
	// Constructors
	// -------------------
	
	/** Constructs an process consisting only of a SimpleOperatorChain. */
	public Process() {
		try {
			ProcessRootOperator root = (ProcessRootOperator) OperatorService.createOperator("Process");
            root.rename("Root");
            setRootOperator(root);
		} catch (Exception e) {
			throw new RuntimeException("Cannot initialize root operator of the process: " + e.getMessage(), e);
		}
		initLogging();
	}

    /** Creates a new process from the given URL. */
	public Process(URL url) throws IOException, XMLException {
		InputStream in = url.openStream();
		readProcess(in);
		in.close();
        initLogging();
	}

    /** Creates a new process from the given process file. This might have been created 
     *  with the GUI beforehand. */
	public Process(File file) throws IOException, XMLException {
		InputStream in = null;
		try {
			in = new FileInputStream(file);
			readProcess(in);
		} catch (IOException e) {
			throw e;
		} finally {
			if (in != null)
				in.close();
		}
		this.processFile = file;
        initLogging();
	}

	/** Reads an process configuration from an XML String. */
	public Process(String xmlString) throws IOException, XMLException {
		ByteArrayInputStream in = new ByteArrayInputStream(xmlString.getBytes());
		readProcess(in);
		in.close();
        initLogging();
	}

	/** Reads an process configuration from the given file. */
	public Process(InputStream in) throws IOException, XMLException {
		readProcess(in);
        initLogging();
	}

	/** Clone constructor. Makes a deep clone of the operator tree and the process file. 
	 *  The same applies for the operatorNameMap. The breakpoint listeners are copied by reference
	 *  and all other fields are initialized like for a fresh process. */
	private Process(Process other) {
		this();
		setRootOperator((ProcessRootOperator)other.rootOperator.cloneOperator(other.rootOperator.getName()));
		this.currentOperator = null;
		if (other.processFile != null)
			this.processFile = new File(other.processFile.getAbsolutePath());
		else
			this.processFile = null;
        initLogging();
	}

    private void initLogging() {
        initLogging(LogService.UNKNOWN_LEVEL);
    }
    
    private void initLogging(int logVerbosity) {
        try {
            this.logService = new LogService(this, logVerbosity);
        } catch (UndefinedParameterError e) {
            // cannot happen
            this.logService = LogService.getGlobal();
        }
    }
    
	public Object clone() {
		return new Process(this);
	}
	
	/**
	 * @deprecated Use {@link #setProcessState(int)} instead
	 */
	@Deprecated
	public synchronized void setExperimentState(int state) {
		setProcessState(state);
	}

	public synchronized void setProcessState(int state) {
		this.processState = state;
	}
	
	/**
	 * @deprecated Use {@link #getProcessState()} instead
	 */
	@Deprecated
	public synchronized int getExperimentState() {
		return getProcessState();
	}

	public synchronized int getProcessState() {
		return this.processState;
	}

    // -------------------------
    // Logging
    // -------------------------

    public LogService getLog() {
        return this.logService;
    }
    
	// -------------------------
	// Macro Handler
	// -------------------------
	
    /** Returns the macro handler. */
	public MacroHandler getMacroHandler() {
		return this.macroHandler;
	}
	
    /** Clears all macros. */
	public void clearMacros() {
		this.macroHandler.clear();
	}
	
	// -------------------------
	// Data Tables
	// -------------------------
	
	/** Returns true if a data table object with the given name exists. */
	public boolean dataTableExists(String name) {
		return dataTableMap.get(name) != null;
	}

	
	/**
	 * Returns the data table associated with the given name. If the name was
	 * not used yet, an empty DataTable object is created with the given columnNames.
	 */
	public DataTable addDataTable(DataTable table) {
		dataTableMap.put(table.getName(), table);
		return table;
	}

	/**
	 * Returns the data table associated with the given name. If the name was
	 * not used yet, an empty DataTable object is created with the given columnNames.
	 */
	public DataTable getDataTable(String name) {
		return dataTableMap.get(name);
	}
	
    /** Returns all data tables. */
	public Collection<DataTable> getDataTables() {
		return dataTableMap.values();
	}

	/** Removes all data tables before running a new process. */
	public void clearDataTables() {
		dataTableMap.clear();
	}

	
	// ----------------------
	// Operator Handling
	// ----------------------
	
    /** Sets the current root operator. This might lead to a new registering of operator names. */
    public void setRootOperator(ProcessRootOperator root) {
        this.rootOperator = root;
        this.operatorNameMap.clear();
        this.rootOperator.setProcess(this);
    }
    
    /** Delivers the current root operator. */
	public ProcessRootOperator getRootOperator() {
		return rootOperator;
	}

    /** Creates the process from the given XML. */
	public void setupFromXML(String xmlString) throws IOException, XMLException {
		ByteArrayInputStream in = new ByteArrayInputStream(xmlString.getBytes());
		readProcess(in);
		in.close();
	}

    /** Returns the current process file. 
	 * @deprecated Use {@link #getProcessFile()} instead*/
	@Deprecated
	public File getExperimentFile() {
		return getProcessFile();
	}

	/** Returns the current process file. */
	public File getProcessFile() {
		return processFile;
	}

	/** Returns the operator with the given name. */
	public Operator getOperator(String name) {
        return operatorNameMap.get(name);
	}

	/** Returns the operator that is currently being executed. */
	public Operator getCurrentOperator() {
		return currentOperator;
	}

	/** Returns a Collection view of all operators. */
	public Collection<Operator> getAllOperators() {
        List<Operator> result = rootOperator.getAllInnerOperators();
        result.add(0, rootOperator);
        return result;
	}

	/** Returns a Set view of all operator names (i.e. Strings). */
	public Collection<String> getAllOperatorNames() {
        Collection<String> allNames = new LinkedList<String>();
        for (Operator o : getAllOperators()) {
            allNames.add(o.getName());
        }
        return allNames;
	}

	/** Returns the operator that is currently being executed. */
	public void setCurrentOperator(Operator operator) {
		this.currentOperator = operator;
	}

	
	// --------------------
	// Breakpoint Handling
	// --------------------
	
    /** Removes a breakpoint listener. */
	public void addBreakpointListener(BreakpointListener listener) {
		breakpointListeners.add(listener);
	}

    /** Adds a breakpoint listener. */
	public void removeBreakpointListener(BreakpointListener listener) {
		breakpointListeners.remove(listener);
	}

    /** Fires the event that the process was paused. */
	public void fireBreakpointEvent(Operator operator, IOContainer ioContainer, int location) {
		Iterator i = breakpointListeners.iterator();
		while (i.hasNext()) {
			((BreakpointListener) i.next()).breakpointReached(operator, ioContainer, location);
		}
	}

    /** Fires the event that the process was resumed. */
	public void fireResumeEvent() {
		Iterator i = breakpointListeners.iterator();
		while (i.hasNext()) {
			((BreakpointListener) i.next()).resume();
		}
	}

	
	// -----------------
	// Checks
	// -----------------
	
    /** Checks the nesting (compatible in- and output types) of the current process. */
	private int checkIO(IOContainer inputContainer) {
        IOObject[] inputObjects = inputContainer.getIOObjects();
        Class[] inputClasses = new Class[inputObjects.length];
        for (int i = 0; i < inputObjects.length; i++) {
            inputClasses[i] = inputObjects[i].getClass();
        }
		logService.log("Checking i/o classes...", LogService.INIT);
		try {
			Class[] output = rootOperator.checkIO(inputClasses);
			if (output.length == 0) {
				logService.log("i/o classes are ok.", LogService.INIT);
			} else {
				StringBuffer left = new StringBuffer();
				for (int i = 0; i < output.length; i++) {
					left.append(Tools.classNameWOPackage(output[i]));
					if (i < output.length - 1)
						left.append(", ");
				}
				logService.log("i/o classes are ok. Process output: " + left.toString() + ".", LogService.INIT);
			}
			return 0;
		} catch (IllegalInputException e) {
			if (e.getOperator() != null)
				e.getOperator().addError(e.getMessage());
			return 1;
		} catch (WrongNumberOfInnerOperatorsException e) {
			if (e.getOperator() != null)
				e.getOperator().addError(e.getMessage());
			return 1;
		}
	}
    
    /** Checks the nesting (number of inner operators) of the current process. */
	private int checkNumberOfInnerOperators() {
		logService.log("Checking process setup...", LogService.INIT);
		int errorCount = ((OperatorChain) rootOperator).checkNumberOfInnerOperators();
		if (errorCount == 0)
			logService.log("Inner operators are ok.", LogService.INIT);
		else
			logService.log("Process setup not ok", LogService.ERROR);
		return errorCount;
	}

    /** Checks the properties (parameter values) of the current process. */
	private int checkProperties() {
		logService.log("Checking properties...", LogService.INIT);
		int errorCount = rootOperator.checkProperties();
		if (errorCount == 0)
			logService.log("Properties are ok.", LogService.INIT);
		else
			logService.log("Properties are not ok", LogService.ERROR);
		return errorCount;
	}

    /** This method leads to some additional checks which might be defined by some operators. */
	private int performAdditionalChecks() {
		try {
			rootOperator.performAdditionalChecks();
			return 0;
		} catch (UserError e) {
			logService.log(e.getMessage(), LogService.ERROR);
			return 1;
		}
	}

	/** Checks for correct number of inner operators, properties, and io. 
	 * @deprecated Use {@link #checkProcess(IOContainer)} instead*/
	@Deprecated
	public boolean checkExperiment(IOContainer inputContainer) {
		return checkProcess(inputContainer);
	}

	/** Checks for correct number of inner operators, properties, and io. */
	public boolean checkProcess(IOContainer inputContainer) {
		boolean ok = true;
		rootOperator.clearErrorList();
		int errorCount = checkProperties();
		errorCount += checkNumberOfInnerOperators();
		if (errorCount == 0)
			errorCount += performAdditionalChecks();
		if (errorCount == 0)
			errorCount += checkIO(inputContainer);
		if (errorCount == 0) {
			logService.log("Process ok.", LogService.INIT);
		} else {
			String errorMessage = null;
			if (errorCount == 1) {
				errorMessage = "There was 1 error.";
			} else {
				errorMessage = "There were " + errorCount + " errors.";
			}
			logService.log(errorMessage, LogService.ERROR);
			ok = false;
		}

		int deprecationCount = rootOperator.checkDeprecations();
		if (deprecationCount > 0)
			logService.log("Deprecations: " + deprecationCount + (deprecationCount == 1 ? " usage" : " usages") + " of deprecated operators.", LogService.WARNING);
		return ok;
	}

	
	// ------------------
	// Running
	// ------------------
	
    /** This method initializes the process, the operators, and the services and must be invoked
     *  at the beginning of run. */
	private final void prepareRun(IOContainer inputContainer, int logVerbosity, boolean cleanUp) throws OperatorException {
        
        // TODO: perform this cleaning here after object visualiers, log service and 
        // temp file service are bound to a single process
		if (cleanUp)
			RapidMiner.cleanUp();
        
        initLogging(logVerbosity);
        //getLog().init(this, logVerbosity);
		stopProcess = false;
		logService.log("Initialising process setup", LogService.INIT);
		
		RandomGenerator.init(this);
		ResultService.init(this);
        
		checkProcess(inputContainer);
		clearDataTables();
		clearMacros();
		AttributeFactory.resetNameCounters();
		
		logService.log("Process initialised", LogService.INIT);
	}

	/** Starts the process with no input. */
	public final IOContainer run() throws OperatorException {
		return run(new IOContainer());
	}

	/** Starts the process with the given log verbosity. */
	public final IOContainer run(int logVerbosity) throws OperatorException {
        return run(new IOContainer(), logVerbosity, true);
    }
	
	/** Starts the process with the given input. */
	public final IOContainer run(IOContainer input) throws OperatorException {
        return run(input, LogService.UNKNOWN_LEVEL, true);
    }

	/** Starts the process with the given input. The process uses the given log verbosity. */
	public final IOContainer run(IOContainer input, int logVerbosity) throws OperatorException {
		return run(input, logVerbosity, true);
	}
	
	/** Starts the process with the given input. The process uses a default log verbosity.
	 * 	The boolean flag indicates if some static initializations should be cleaned
	 *  before the process is started. This should usually be true but it might be useful
	 *  to set this to false if, for example, several process runs uses the same
	 *  object visualizer which would have been cleaned otherwise.
	 */
	public final IOContainer run(IOContainer input, boolean cleanUp) throws OperatorException {
		return run(input, LogService.UNKNOWN_LEVEL, cleanUp);
	}
	
	/** Starts the process with the given input. The process uses the given log verbosity. 
	 *  The boolean flag indicates if some static initializations should be cleaned
	 *  before the process is started. This should usually be true but it might be useful
	 *  to set this to false if, for example, several process runs uses the same
	 *  object visualizer which would have been cleaned otherwise. */
	public final IOContainer run(IOContainer input, int logVerbosity, boolean cleanUp) throws OperatorException {
		prepareRun(input, logVerbosity, cleanUp);
		long start = System.currentTimeMillis();
		logService.log("Process starts", LogService.NOTE);
		logService.log("Process:" + Tools.getLineSeparator() + getRootOperator().createProcessTree(3), LogService.INIT);
		rootOperator.processStarts();
		try {
			IOContainer result = rootOperator.apply(input);
			long end = System.currentTimeMillis();

			logService.log("Process finished after " + ((end - start) / 1000) + " seconds", LogService.NOTE);
			logService.log("Process:" + Tools.getLineSeparator() + getRootOperator().createProcessTree(3), LogService.INIT);
			logService.log("Produced output:" + Tools.getLineSeparator() + result, LogService.INIT);
			logService.log("Process finished successfully", LogService.NOTE);

			return result;
		} catch (OperatorException e) {
			throw e;
		} finally {
			tearDown();
		}
	}

	/** Stops the process as soon as possible. */
	public void stop() {
		this.stopProcess = true;
	}

	/** Returns true iff the process should be stopped. */
	public boolean shouldStop() {
		return stopProcess;
	}
	
    /** This method is invoked after an process has finished. */
	private void tearDown() {
		try {
			if (!shouldStop())
				rootOperator.processFinished();
		} catch (OperatorException e) {
			logService.log("Problem during finishing the process: " + e.getMessage(), LogService.ERROR);
		}
		ResultService.close();
		logService.flush();
	}

	
	// ----------------------
	// Process IO
	// ----------------------
	
	/** Saves the process to the process file. */
	public void save() throws IOException {
		save(processFile);
	}

	/** Saves the process to the given process file. */
	public void save(File file) throws IOException {
		Charset encoding = rootOperator.getEncoding();
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), encoding));

			// write encoding
			writer.println("<?xml version=\"1.0\" encoding=\""+encoding+"\"?>");

			// write process
			writer.println("<process version=\"" + RapidMiner.getVersion() + "\">" + Tools.getLineSeparator());
			rootOperator.writeXML(writer, "  ");
			writer.println("</process>");
		} catch (IOException e) {
			throw e;
		} finally {
			if (writer != null)
				writer.close();
		}
		logService.log("Finished writing of process definition file '" + file + "'.", LogService.STATUS);
	}

    /** Sets the process file. This file might be used for resolving relative filenames. 
     *  @deprecated Please use {@link #setProcessFile(File)} instead. 
     */
	@Deprecated
	public void setExperimentFile(File file) {
		this.setProcessFile(file);
	}
	
    /** Sets the process file. This file might be used for resolving relative filenames. */
	public void setProcessFile(File file) {
		this.processFile = file;
	}

	/**
	 * Resolves the given filename against the directory containing the
	 * process file.
	 */
	public File resolveFileName(String name) {
		File workingDir = new File(System.getProperty("user.dir"));
		return Tools.getFile(processFile != null ? processFile.getParentFile() : workingDir, name);
	}

    /** Creates a new file relative to the process file path. */
	public File createFile(String name) {
		File file = resolveFileName(name);
		Tools.mkdir(file.getParentFile());
		return file;
	}

    /** Reads the process setup from the given input stream. 
	 * @deprecated Use {@link #readProcess(InputStream)} instead*/
	@Deprecated
	public void readExperiment(InputStream in) throws XMLException, IOException {
		readProcess(in);
	}

	/** Reads the process setup from the given input stream. */
	public void readProcess(InputStream in) throws XMLException, IOException {
		Map<String, Operator> nameMapBackup = operatorNameMap;
		operatorNameMap = new HashMap<String, Operator>(); // no invocation of clear (see below)

		try {
			Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in);
			Element processElement = document.getDocumentElement();
			Element rootOperatorElement = null;
			if ((processElement.getTagName().equals("process")) ||
				(processElement.getTagName().equals("experiment"))) {
				NodeList children = processElement.getChildNodes();
				for (int i = 0; i < children.getLength(); i++) {
					Node childNode = children.item(i);
					if (childNode instanceof Element) {
						Element childElement = (Element)childNode;
						if (childElement.getTagName().equals("operator")) {
							rootOperatorElement = childElement;
							break;
						}						
					}
				}
				if (rootOperatorElement == null) {
					throw new XMLException("The <process> tag must contain exactly one inner operator of type 'Process'!");					
				}
				String version = processElement.getAttribute("version");
				if (version != null) {
					LogService.getGlobal().log("Reading process definition (version: " + version + ")");
				}
			} else if (processElement.getTagName().equals("operator")) {
				rootOperatorElement = processElement;
			} else {
				throw new XMLException("Outermost tag of a process definition must be either <process> or <operator>!");
			}
            Operator root = Operator.createFromXML(rootOperatorElement); 
			if (!(root instanceof ProcessRootOperator))
				throw new XMLException("Outermost operator must be of type 'Process'!");
			rootOperator = (ProcessRootOperator) root;
            setRootOperator(rootOperator);
			nameMapBackup = operatorNameMap;
		} catch (javax.xml.parsers.ParserConfigurationException e) {
			throw new XMLException(e.toString(), e);
		} catch (SAXException e) {
			throw new XMLException("Cannot parse document: " + e, e);
		} finally {
			operatorNameMap = nameMapBackup; // if everything went fine -->
										     // map = new map, if not -->
											 // map = old map (backup)
		}
	}
    
    /** Returns a &quot;name (i)&quot; if name is already in use. This new name should then
     *  be used as operator name. */
    public String registerName(String name, Operator operator) {
        if (operatorNameMap.get(name) != null) {
            String baseName = name;
            int index = baseName.indexOf(" (");
            if (index >= 0) {
                baseName = baseName.substring(0, index);
            }
            int i = 2;
            while (operatorNameMap.get(baseName + " (" + i + ")") != null) {
                i++;
            }
            String newName = baseName + " (" + i + ")";
            operatorNameMap.put(newName, operator);
            return newName;
        } else {
            operatorNameMap.put(name, operator);
            return name;
        }
    }
    
    /** This method is used for unregistering a name from the operator name map. */
    public void unregisterName(String name) {
        operatorNameMap.remove(name);
    }
    
    public String toString() {
        if (rootOperator == null)
            return "empty process";
        else
            return "Process:" + Tools.getLineSeparator() + rootOperator.getXML("");        
    }
}
