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

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.rapidminer.BreakpointListener;
import com.rapidminer.Process;
import com.rapidminer.RapidMiner;
import com.rapidminer.gui.wizards.ConfigurationListener;
import com.rapidminer.gui.wizards.PreviewListener;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.Parameters;
import com.rapidminer.parameter.UndefinedParameterError;
import com.rapidminer.tools.LogService;
import com.rapidminer.tools.LoggingHandler;
import com.rapidminer.tools.OperatorService;
import com.rapidminer.tools.Tools;
import com.rapidminer.tools.WekaTools;
import com.rapidminer.tools.XMLException;
import com.rapidminer.tools.math.StringToMatrixConverter;


/**
 * <p>
 * An operator accepts an array of input objects and generates an array of
 * output objects that can be processed by other operators. Both must implement
 * the IOObject interface. This is the superclass which must be extended by all
 * RapidMiner operators. Please refer to the RapidMiner tutorial for a detailed description
 * how to implement your operator.
 * </p>
 * 
 * <p>
 * As default, operators consume their input by using it. This is often a useful
 * behavior especially in complex processes. For example, a learning operator
 * consumes an example set to produce a model and so does a cross validation to
 * produce a performance value of the learning method. To receive the input
 * {@link IOObject} of a certain class simply use {@link #getInput(Class class)}.
 * This method delivers the first object of the desired class which is in the
 * input of this operator. The delivered object is consumed afterwards and thus
 * is removed from input. If the operator alters this object, it should return
 * the altered object as output again. Therefore, you have to add the object to
 * the output array which is delivered by the {@link #apply()} method of the
 * operator. You also have to declare it in {@link #getOutputClasses()}. All
 * input objects which are not used by your operator will be automatically
 * passed to the next operators.
 * </p>
 * 
 * <p>
 * In some cases it would be useful if the user can define if the input object
 * should be consumed or not. For example, a validation chain like cross
 * validation should estimate the performance but should also be able to return
 * the example set which is then used to learn the overall model. Operators can
 * change the default behavior for input consumation and a parameter will be
 * automatically defined and queried. The default behavior is defined in the
 * method {@link #getInputDescription(Class cls)} and should be overriden in
 * these cases. Please note that input objects with a changed input description
 * must not be defined in {@link #getOutputClasses()} and must not be returned
 * at the end of apply. Both is automatically done with respect to the value of
 * the automatically created parameter. Please refer to the Javadoc comments of
 * this method for further explanations.
 * </p>
 * 
 * @see com.rapidminer.operator.OperatorChain
 * 
 * @author Ralf Klinkenberg, Ingo Mierswa, Simon Fischer
 * @version $Id: Operator.java,v 1.16 2008/05/09 19:23:19 ingomierswa Exp $
 */
public abstract class Operator implements ConfigurationListener, PreviewListener, LoggingHandler {

	/** Indicates if before / within / after this operator a breakpoint is set. */
	private boolean breakPoint[] = new boolean[BreakpointListener.BREAKPOINT_POS_NAME.length];
    
	/**
	 * The thread which runs this operator. Must be notified when the process
	 * is resumed.
	 */
	private Thread breakpointThread = null;

	/** Indicates if this operator is enabled. */
	private boolean enabled = true;

	/** Indicates if the tree node is expanded (only operator chains). */
	private boolean expanded = true;
	
	/** Name of the operators (for logging). */
	private String name;

	/**
	 * A user defined description for this operator instance. Will be filled
	 * from comments in the XML files.
	 */
	private String userDescription;

	/** Its parent, if part of an operator chain. */
	private OperatorChain parent;

	/** Its input as set by the apply method. */
	private IOContainer inputContainer;

	/** Number of times the operator was applied. */
	private int applyCount;

	/** System time when execution started. */
	private long startTime;

	/** System time when the current loop of execution started. */
	private long loopStartTime;

	/** Parameters for this Operator. */
	private Parameters parameters = null;

	/**
	 * The values for this operator. The current value of a Value can be asked
	 * by the ProcessLogOperator.
	 */
	private Map<String, Value> valueMap = new TreeMap<String, Value>();

	/**
	 * The list which stores the errors of this operator (parameter not set,
	 * wrong children number, wrong IO).
	 */
	private List<String> errorList = new LinkedList<String>();

	/**
	 * The operator description of this operator (icon, classname, description,
	 * ...).
	 */
	private OperatorDescription operatorDescription = null;

	// -------------------- INITIALISATION --------------------

	/**
	 * <p>
	 * Creates an unnamed operator.
	 * Subclasses must pass the given description object to this
	 * super-constructor (i.e. invoking super(OperatorDescription)). They might
	 * also add additional values for process logging.
	 * </p>
	 * <p>
	 * NOTE: the preferred way for operator creation is using one of the factory
	 * methods of {@link com.rapidminer.tools.OperatorService}.
	 * </p>
	 */
	public Operator(OperatorDescription description) {
		this.operatorDescription = description;
		this.parameters = new Parameters(getParameterTypes());
		this.name = operatorDescription.getName();
        
		addValue(new Value("applycount", "The number of times the operator was applied.", false) {
			public double getValue() {
				return applyCount;
			}
		});
		addValue(new Value("time", "The time elapsed since this operator started.", false) {
			public double getValue() {
				return System.currentTimeMillis() - startTime;
			}
		});
		addValue(new Value("looptime", "The time elapsed since the current loop started.", false) {
			public double getValue() {
				return System.currentTimeMillis() - loopStartTime;
			}
		});
	}
    
	/** Returns the operator description of this operator. */
	public final OperatorDescription getOperatorDescription() {
		return operatorDescription;
	}

	/**
	 * Returns the &quot;class name&quot; of this operator from the operator
	 * description of the operator. This is the name which is defined in the
	 * operator.xml file.
	 */
	public final String getOperatorClassName() {
		return operatorDescription.getName();
	}
    
	/**
	 * Returns the experiment (process) of this operator by asking the parent operator. If the
     * operator itself and all of its parents are not part of an process, this
     * method will return null. Please note that some operators (e.g. ProcessLog)
     * must be part of an process in order to work properly.
     * @deprecated Please use {@link #getProcess()} instead
	 */
	@Deprecated
	public Process getExperiment() {
		return getProcess();
	}
	
	/**
	 * Returns the process of this operator by asking the parent operator. If the
     * operator itself and all of its parents are not part of an process, this
     * method will return null. Please note that some operators (e.g. ProcessLog)
     * must be part of an process in order to work properly.
	 */
	public Process getProcess() {
        Operator parent = getParent();
        if (parent == null)
            return null;
        else
            return parent.getProcess();
	}

    /** Returns the logging of the process if this operator is part of an process 
     *  and the global logging service otherwise. */
    public LogService getLog() {
        Process process = getProcess();
        if (process != null) {
            return process.getLog();
        } else {
            return LogService.getGlobal();
        }
    }
    
    /** Logs a status message with the correct log service. */
    public void log(String message) {
        getLog().log(getName() + ": " + message, LogService.STATUS);
    }

    /** Logs a note message with the correct log service. */
    public void logNote(String message) {
        getLog().log(getName() + ": " + message, LogService.NOTE);
    }
    
    /** Logs a warning message with the correct log service. */
    public void logWarning(String message) {
        getLog().log(getName() + ": " + message, LogService.WARNING);
    }
    
    /** Logs an error message with the correct log service. */
    public void logError(String message) {
        getLog().log(getName() + ": " + message, LogService.ERROR);
    }
    
	// --------------------------------------------------------------------------------

	/** Returns the name of the operator. */
	public final String getName() {
		return this.name;
	}

    /** This method simply sets the name to the given one. Please note that it is not checked if the name
     *  was already used in the process. Please use the method {@link #rename(String)} for usual renaming.
     */
    private final void setName(String newName) {
        this.name = newName;
    }
    
    /** This method unregisters the old name if this operator is already part of a {@link Process}. Afterwards,
     *  the new name is set and registered in the process. Please note that the name might be changed
     *  during registering in order to ensure that each operator name is unique in its process. 
     *  The new name will be returned. */
    public final String rename(String newName) {
        Process process = getProcess();
        if (process != null) {
            process.unregisterName(this.name);
            this.name = process.registerName(newName, this);
        } else {
            this.name = newName;
        }
        return this.name;
	}

	/** Sets the user specified comment for this operator. */
	public void setUserDescription(String description) {
		this.userDescription = description;
	}

	/** The user specified comment for this operator. */
	public String getUserDescription() {
		return userDescription;
	}

	/**
	 * Returns null if this operator is not deprecated. This implementation
	 * returns the return value of OperatorDescription.getDeprecationInfo() which
	 * is usually null. If a non-null value is returned this should describe a
	 * a workaround for a user. In this case the workaround is displayed during 
	 * the validation of the process.
	 */
	public final String getDeprecationInfo() {
		return this.operatorDescription.getDeprecationInfo();
	}

	/** Sets the enclosing operator chain. */
	public final void setParent(OperatorChain parent) {
		this.parent = parent;
	}

	/** Returns the parent of this operator which must be an operator chain. */
	public final OperatorChain getParent() {
		return parent;
	}

	/** Removes this operator from its parent. */
	public void remove() {
		if (parent != null) {
			parent.removeOperator(this);
		}
        Process process = getProcess();
        if (process != null)
            unregisterOperator(process);
	}

    /** This methods was used in older RapidMiner version for registering the operator in
     *  the process and to ensure that all operator names are unique. This is now
     *  automatically done during operator adding and therefore this method is now 
     *  deprecated.
     *  
     *  @deprecated No longer necessary since the registering / unregistering will
     *  be performed during operator adding
     */
    @Deprecated public void register(Process process, String name) {}
    
    /** Registers this operator in the given process. Please note that this might change the name 
     *  of the operator. */
    protected void registerOperator(Process process) {
        if (process != null)
            setName(process.registerName(getName(), this));
    }
    
	/** Deletes this operator removing it from the name map of the process. */
	protected void unregisterOperator(Process process) {
        process.unregisterName(name);
	}

	/** Sets the activation mode. Inactive operators do not perform their action. */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	/** Sets the expansion mode which indicates if this operator is drawn expanded or not. */
	public void setExpanded(boolean expanded) {
		this.expanded = expanded;
	}

	/** Returns true if this operator should be painted expanded. */
	public boolean isExpanded() {
		return expanded;
	}
    
    /** Returns true if this operator is enabled and the parent (if not null) is also enabled. */
    public boolean isEnabled() {
        if (getParent() == null) {
            return enabled;
        } else {
            return (enabled && getParent().isEnabled());
        }
    }

	/** Returns human readable status information. */
	public String getStatus() {
		return name + " [" + applyCount + "]";
	}

	/** Returns the number of times this operator was already applied. */
	public int getApplyCount() {
		return applyCount;
	}

	// --------------------------------------------------------------------------------

	/**
	 * Performs a deep clone on the most parts of this operator. The breakpointThread
     * is empty (as it is in initialization). The parent will be clone in the method
     * of OperatorChain overwriting this one.
	 * The in- and output containers and the error list are only cloned by
	 * reference copying. Use this method only if you are sure what you are
	 * doing.
	 */
	public Operator cloneOperator(String name) {
		Operator clone = null;
		try {
			clone = operatorDescription.createOperatorInstance();
		} catch (Exception e) {
			e.printStackTrace();
			getLog().log("Can not create clone of operator '" + getName() + "': " + e, LogService.ERROR);
			return null;
		}
        clone.setName(getName());
		clone.breakPoint = new boolean[] { breakPoint[0], breakPoint[1], breakPoint[2] };
        clone.enabled = enabled;
        clone.expanded = expanded;
        if (userDescription != null)
            clone.userDescription = userDescription;
		clone.inputContainer = inputContainer; // reference
		clone.applyCount = applyCount;
		clone.startTime = startTime;
		clone.loopStartTime = loopStartTime;
		clone.parameters = (Parameters) parameters.clone();
		clone.errorList = errorList; // reference
		return clone;
	}

	// --------------------- Apply ---------------------

	/** Implement this method in subclasses. */
	public abstract IOObject[] apply() throws OperatorException;

	// -------------------- Nesting --------------------

	/**
	 * Returns the classes that are needed as input. May be null or an empty (no
	 * desired input). As default, all delivered input objects are consumed and
	 * must be also delivered as output in both {@link #getOutputClasses()} and
	 * {@link #apply()} if this is necessary. This default behavior can be
	 * changed by overriding {@link #getInputDescription(Class)}. Subclasses
	 * which implement this method should not make use of parameters since this
	 * method is invoked by getParameterTypes(). Therefore, parameters are not
	 * fully available at this point of time and this might lead to exceptions.
	 * Please use InputDescriptions instead.
	 */
	public abstract Class[] getInputClasses();

	/**
	 * <p>Returns the classes that are guaranteed to be returned by
	 * <tt>apply()</tt> as additional output. Please note that input objects
	 * which should not be consumed must also be defined by this method (e.g.
	 * an example set which is changed but not consumed in the case of a preprocessing 
	 * operator must be defined in both, the methods {@link #getInputClasses()} and
	 * {@link #getOutputClasses()}). The default behavior for input consumation
	 * is defined by {@link #getInputDescription(Class)} and can be changed by
	 * overwriting this method. Objects which are not consumed (defined by changing
	 * the implementation in {@link #getInputDescription(Class)}) must not be
	 * defined as additional output in this method.</p>
	 * 
	 * <p>May deliver null or an empy array (no additional output is produced or 
	 * guaranteed). Must return the class array of delivered output objects
	 * otherwise.</p>
	 */
	public abstract Class[] getOutputClasses();

	/**
	 * Returns the classes that are needed as input. Returns the result of
	 * {@link #getInputClasses()}.
	 */
	public final Class[] getDesiredInputClasses() {
		Class[] inputClasses = getInputClasses();
		if (inputClasses == null)
			return new Class[0];
		else
			return inputClasses;
	}

	/**
	 * Returns the classes that are guaranteed to be returned by
	 * <tt>apply()</tt>. These are all input classes which are not consumed
	 * and all guranteed additional output classes.
	 */
	public final Class[] getDeliveredOutputClasses() {
		List<Class> result = new LinkedList<Class>();
		Class[] inputClasses = getDesiredInputClasses();
		for (int i = 0; i < inputClasses.length; i++) {
			InputDescription description = getInputDescription(inputClasses[i]);
			if ((description.showParameter() && getParameterAsBoolean(description.getParameterName())) || (description.getKeepDefault()))
				result.add(inputClasses[i]);
		}
		Class[] additionalOutput = getOutputClasses();
		if (additionalOutput != null) {
			for (int i = 0; i < additionalOutput.length; i++)
				result.add(additionalOutput[i]);
		}
		Class[] resultArray = new Class[result.size()];
		result.toArray(resultArray);
		return resultArray;
	}

	/**
	 * The default implementation returns an input description that consumes the
	 * input IOObject without a user parameter. Subclasses may override this
	 * method to allow other input handling behaviors.
	 */
	public InputDescription getInputDescription(Class inputClass) {
		return new InputDescription(inputClass);
	}

	/**
	 * If you find the <tt>getInputClasses()</tt> and
	 * <tt>getOuputClasses()</tt> methods for some reason not useful, you may
	 * override this method. Otherwise it returns a default IODescription
	 * containing the classes returned by the first.
	 */
	private IODescription getIODescription() {
		return new IODescription(getDesiredInputClasses(), getDeliveredOutputClasses());
	}

	/**
	 * Subclasses will throw an exception if something isn't ok. Returns the
	 * output that this operator returns when provided with the given input.
	 */
	public Class[] checkIO(Class[] input) throws IllegalInputException, WrongNumberOfInnerOperatorsException {
		if (isEnabled())
			return getIODescription().getOutputClasses(input, this);
		else
			return input;
	}

	/**
	 * This method is invoked during the validation checks. It is invoked as a
	 * last check. The default implementation does nothing. Subclasses might
	 * want to override this method to perform some specialized checks, e.g. if
	 * an inner operator is of a specific class.
	 */
	public void performAdditionalChecks() throws UserError {}

	/**
	 * Will count an error if a non optional property has no default value and
	 * is not defined by user. Returns the total number of errors.
	 */
	public int checkProperties() {
		int errorCount = 0;
		if (isEnabled()) {
			Iterator<ParameterType> i = getParameterTypes().iterator();
			while (i.hasNext()) {
				ParameterType type = i.next();
				try {
					Object value = getParameters().getParameter(type.getKey());
					if (!type.isOptional() && (type.getDefaultValue() == null) && (value == null)) {
						addError(getName() + ": " + type.getKey() + " is not defined!");
						errorCount++;
					}
				} catch (UndefinedParameterError e) {
					addError(getName() + ": " + type.getKey() + " is not defined!");
					errorCount++;
				}
			}
		}
		return errorCount;
	}

	/**
	 * Will count an the number of deprecated operators, i.e. the operators
	 * which {@link #getDeprecationInfo()} method does not return null. Returns
	 * the total number of deprecations.
	 */
	public int checkDeprecations() {
		String deprecationString = getDeprecationInfo();
		int deprecationCount = 0;
		if (deprecationString != null) {
			addWarning(deprecationString);
			deprecationCount = 1;
		}
		return deprecationCount;
	}

	// -------------------- Apply and Input-providing --------------------

	/**
	 * Applies the operator. Don't override this method, but <tt>apply()</tt>.
	 * 
	 * @return An IOContainer containing all IOObjects returned by apply() plus
	 *         the unused IOObjects of input, i.e. those IOObjects that were not
	 *         returned by one of <tt>input.getInput(Class)</tt> or
	 *         <tt>input.getInput(Class, int)</tt>.
	 */
	public synchronized final IOContainer apply(IOContainer input) throws OperatorException {
        Process process = getProcess();
		if (process == null) {
			log("Process of operator " + this + " is not set, probably not registered! Trying to use this operator (should work in most cases anyway)...");
		}

		if (getDeprecationInfo() != null)
			logWarning(getDeprecationInfo());

		if (isEnabled()) {
			// check for stop
			checkForStop(process);
			
			if (process != null)
				process.setCurrentOperator(this);   
			applyCount++;
			startTime = loopStartTime = System.currentTimeMillis();

			if (input == null)
				throw new IllegalArgumentException("Input is null!");
			this.inputContainer = input;
			if (process != null)
				process.getRootOperator().processStep();

			if (breakPoint[BreakpointListener.BREAKPOINT_BEFORE]) {
				processBreakpoint(inputContainer, BreakpointListener.BREAKPOINT_BEFORE);
			}

			IOObject[] ioo = inputContainer.getIOObjects();
            for (IOObject ioObject : ioo) {
            	if (ioObject != null) {
            		ioObject.setWorkingOperator(this);
            	}
            }
            
			// logging?
			if (getLog().isSufficientLogVerbosity(LogService.IO)) {
				if (ioo.length == 0) {
					getLog().log("$b" + getName() + " called^b " + Tools.ordinalNumber(applyCount) + " time without input", LogService.IO);
				} else {
					StringBuffer iLog = new StringBuffer("$b");
					iLog.append(getName());
					iLog.append(" called^b ");
					iLog.append(Tools.ordinalNumber(applyCount));
					iLog.append(" time with input:");
					for (int i = 0; i < ioo.length; i++) {
						iLog.append(Tools.getLineSeparator());
						iLog.append("  ");
						iLog.append(i + 1);
						iLog.append(". ");
						iLog.append(ioo[i]);
					}
					getLog().log(iLog.toString(), LogService.IO);
				}
			}

            // actually applying
            IOObject[] output = null;
            try { 
                output = apply();
            } catch (UserError e) {
                if (e.getOperator() == null)
                    e.setOperator(this);
                throw e;
            }

            // set source to the output
            for (IOObject ioObject : output) {
            	if (ioObject != null) {
            		if (ioObject.getSource() == null) {
            			ioObject.setSource(getName());
            			ioObject.setWorkingOperator(null);
            		}
            	}
            }
            
			// logging?
			if (getLog().isSufficientLogVerbosity(LogService.IO)) {
				if (output.length == 0) {
					getLog().log(getName() + " returned no additional output", LogService.IO);
				} else {
					StringBuffer oLog = new StringBuffer(getName());
					oLog.append(" returned additional output:");
					for (int i = 0; i < output.length; i++) {
						oLog.append(Tools.getLineSeparator());
						oLog.append("  ");
						oLog.append((i + 1) + ". ");
						oLog.append(output[i]);
					}
					getLog().log(oLog.toString(), LogService.IO);
				}
			}
			getLog().log(getName() + ": execution time was " + (System.currentTimeMillis() - startTime) + " ms", LogService.MINIMUM);

            // add new output IOObjects to input container
			IOContainer outputContainer = handleAdditionalOutput(inputContainer, output);

			if (breakPoint[BreakpointListener.BREAKPOINT_AFTER]) {
				processBreakpoint(outputContainer, BreakpointListener.BREAKPOINT_AFTER);
			}
			return outputContainer;
		} else {
			return input;
		}
	}

	protected final void checkForStop() throws ProcessStoppedException {
		checkForStop(getProcess());
	}
	
	private final void checkForStop(Process process) throws ProcessStoppedException {
		if ((process != null) && (process.shouldStop())) {
			stop();
		}
	}
	
	private final void stop() throws ProcessStoppedException {
	    getLog().log("$b" + getName() + "^b: Process stopped.", LogService.NOTE);
		throw new ProcessStoppedException(this);
	}
	
	/**
	 * This method should only be called by the command line breakpoint listener
	 * to resume the process after a breakpoint.
	 */
	public final void resume() {
		if (breakpointThread != null) {
			synchronized (breakpointThread) {
				breakpointThread.notifyAll();
			}
            Process process = getProcess();
            if (process != null)
                process.fireResumeEvent();
		}
	}

	private void processBreakpoint(IOContainer container, int breakpointType) throws ProcessStoppedException {
		getLog().log("$b" + getName() + "^b: Breakpoint reached", LogService.NOTE);
		breakpointThread = Thread.currentThread();
		try {
            Process process = getProcess();
			process.fireBreakpointEvent(this, container, breakpointType);
			synchronized (breakpointThread) {
				while (process.getProcessState() == Process.PROCESS_STATE_PAUSED)
					breakpointThread.wait();
			}
			process.fireResumeEvent();
			if (process.shouldStop())
				stop();
		} catch (InterruptedException e) {
			logError("Error while waiting after breakpoint: " + e.getMessage());
		}
	}

	/**
	 * The method appends the given additional IOObjects to the container. If
	 * one of the new objects is a model this method tries to add the model into
	 * an existing model. If such a model exists a ContainerModel might be added
	 * to embed both models. If the existing model already is a ContainerModel
	 * the new model is simply added. <i>Attention:</i> This method depends on
	 * the result of the method {@link #getAddOnlyAdditionalOutput()} in order
	 * to decide if simply a new IOContainer should be created from the
	 * additional output objects or if these objects should be added to the
	 * given container (default).
	 */
	private final IOContainer handleAdditionalOutput(IOContainer container, IOObject[] additional) {
		if (getAddOnlyAdditionalOutput()) {
			// check only in additional for container model
			ContainerModel existingModel = null;
			int containerModelIndex = -1;
			for (int i = 0; i < additional.length; i++) {
				if (additional[i] instanceof ContainerModel) {
					existingModel = (ContainerModel) additional[i];
					containerModelIndex = i;
					break;
				}
			}
			if (existingModel == null)
				existingModel = new ContainerModel();

			List<IOObject> nonModelObjectList = new LinkedList<IOObject>();
			for (int i = 0; i < additional.length; i++) {
				if (i == containerModelIndex)
					continue;
				if (additional[i] instanceof Model) {
					existingModel.addModel((Model) additional[i]);
				} else {
					nonModelObjectList.add(additional[i]);
				}
			}
			IOObject[] nonModelObjects = new IOObject[nonModelObjectList.size()];
			nonModelObjectList.toArray(nonModelObjects);

			// create new IO container
			IOContainer result = new IOContainer();
			if (existingModel.getNumberOfModels() > 0)
				result = result.append(new IOObject[] { existingModel });
			return result.append(nonModelObjects);

		} else {
			// check in existing container for container model
			ContainerModel existingModel = null;
			try {
				existingModel = container.remove(ContainerModel.class);
			} catch (MissingIOObjectException e) {
				existingModel = new ContainerModel();
			}

			List<IOObject> nonModelObjectList = new LinkedList<IOObject>();
			for (int i = 0; i < additional.length; i++) {
				if (additional[i] instanceof Model) {
					existingModel.addModel((Model) additional[i]);
				} else {
					nonModelObjectList.add(additional[i]);
				}
			}
			IOObject[] nonModelObjects = new IOObject[nonModelObjectList.size()];
			nonModelObjectList.toArray(nonModelObjects);

			// re-use existing container
			IOContainer result = container;
			if (existingModel.getNumberOfModels() > 0)
				result = result.append(new IOObject[] { existingModel });
			return result.append(nonModelObjects);
		}
	}

	/**
	 * Indicates how additional output should be added to the IOContainer.
	 * Usually the additional output should be preprended to the input container
	 * but some operators, especially operator chains might override this method
	 * in order add only the additional output instead of the complete
	 * IOContainer. This prevents doubling the IOObjects e.g. for
	 * SimpleOperatorChains. The default implementation returns false.
	 */
	public boolean getAddOnlyAdditionalOutput() {
		return false;
	}

	/**
	 * Returns an IOObject of class cls. The object is removed from the input
	 * IOContainer if the input description defines this behavior (default).
	 */
	protected <T extends IOObject> T getInput(Class<T> cls) throws MissingIOObjectException {
		return getInput(cls, 0);
	}

	/**
	 * Returns the nr-th IOObject of class cls. The object is removed from the
	 * input IOContainer if the input description defines this behavior
	 * (default).
	 */
	protected <T extends IOObject> T getInput(Class<T> cls, int nr) throws MissingIOObjectException {
		InputDescription description = getInputDescription(cls);
		if (description.showParameter()) {
		    if (getParameterAsBoolean(description.getParameterName()))
		        return inputContainer.get(cls, nr);
		    else
		        return inputContainer.remove(cls, nr);
		} else {
		    if (description.getKeepDefault())
		        return inputContainer.get(cls, nr);
		    else
		        return inputContainer.remove(cls, nr);
		}
	}

	/**
	 * Returns true if this operator has an input object of the desired class.
	 * The object will not be removed by using this method.
	 */
	protected boolean hasInput(Class<? extends IOObject> cls) {
		return inputContainer.contains(cls);
	}

	/**
	 * Returns the complete input. Operators should usually not directly use
	 * this method but should use {@link #getInput(Class)}. However, some
	 * operator chains must handle their inner input and have to use the
	 * IOContainer directly.
	 */
	public IOContainer getInput() {
		return inputContainer;
	}

	/**
	 * ATTENTION: Use this method only if you are ABSOLUTELY sure what you are
	 * doing! This method might be useful for some meta optimization operators
	 * but wrong usage can cause serious errors.
	 */
	protected void setInput(IOContainer input) {
		if (input == null)
			throw new IllegalArgumentException("Input is null!");
		this.inputContainer = input;
	}

	/** Called when the process starts. Resets all counters. */
	public void processStarts() throws OperatorException {
		applyCount = 0;
	}

	/**
	 * Called at the end of the process. The default implementation does
	 * nothing.
	 */
	public void processFinished() throws OperatorException {}

	/**
	 * Sets or clears a breakpoint at the given position.
	 * 
	 * @param position
	 *            One out of BREAKPOINT_BEFORE and BREAKPOINT_AFTER
	 */
	public void setBreakpoint(int position, boolean on) {
		breakPoint[position] = on;
	}

	/** Returns true iff this operator has a breakpoint at any possible position.
	 */
	public boolean hasBreakpoint() {
		return 
			hasBreakpoint(BreakpointListener.BREAKPOINT_BEFORE) ||
			hasBreakpoint(BreakpointListener.BREAKPOINT_WITHIN) ||
			hasBreakpoint(BreakpointListener.BREAKPOINT_AFTER);
	}
	
	/**
	 * Returns true iff a breakpoint is set at the given position
	 * 
	 * @param position
	 *            One out of BREAKPOINT_BEFORE and BREAKPOINT_AFTER
	 */
	public boolean hasBreakpoint(int position) {
		return breakPoint[position];
	}

	/** Should be called if this operator performs a loop (for the loop time resetting used for Value creation
     *  used by DataTables). Also allows for intermediate results delivered by {@link #getIOContainerForInApplyLoopBreakpoint()}
     *  in case of a {@link BreakpointListener#BREAKPOINT_WITHIN}. Subclasses should invoke this method in case
     *  of a loop in their method apply(). This method also invokes {@link #checkForStop()}. */
	public void inApplyLoop() throws ProcessStoppedException {
		if (breakPoint[BreakpointListener.BREAKPOINT_WITHIN]) {
			processBreakpoint(getIOContainerForInApplyLoopBreakpoint(), BreakpointListener.BREAKPOINT_WITHIN);
		}
		loopStartTime = System.currentTimeMillis();
		checkForStop();
	}

    /** Returns the intermediate results of an in-apply-loop which will be displayed in case of a 
     *  {@link BreakpointListener#BREAKPOINT_WITHIN}. */
	protected IOContainer getIOContainerForInApplyLoopBreakpoint() {
		return IOContainer.DUMMY_IO_CONTAINER;
	}

	/** Adds an implementation of Value. */
	public void addValue(Value value) {
		valueMap.put(value.getKey(), value);
	}

	/** Returns the value of the Value with the given key. */
	public final double getValue(String key) {
		Value value = valueMap.get(key);
		if (value != null) {
			return value.getValue();
		} else {
			logWarning(getName() + ": Illegal value requested: " + key);
			return Double.NaN;
		}
	}

	/** Returns all Values sorted by key. */
	public Collection<Value> getValues() {
		return valueMap.values();
	}

	// -------------------- parameter wrapper --------------------

	/** Returns a collection of all parameters of this operator. */
	public Parameters getParameters() {
		return parameters;
	}

    /** Sets all parameters of this operator. The given parameters are not allowed to be null and must 
     *  correspond to the parameter types defined by this operator. */
    public void setParameters(Parameters parameters) {
        this.parameters = parameters;
    }
    
	/**
	 * Sets the given single parameter to the Parameters object of this
	 * operator. For parameter list the method
	 * {@link #setListParameter(String, List)} should be used.
	 */
	public void setParameter(String key, String value) {
		parameters.setParameter(key, value);
	}

	/**
	 * Sets the given parameter list to the Parameters object of this operator.
	 * For single parameters the method {@link #setParameter(String, String)}
	 * should be used.
	 */
	public void setListParameter(String key, List list) {
		parameters.setParameter(key, list);
	}

	/**
	 * Returns a single parameter retrieved from the {@link Parameters} of this
	 * Operator.
	 */
	public Object getParameter(String key) throws UndefinedParameterError {
		try {
			return parameters.getParameter(key);
		} catch (UndefinedParameterError e) {
			e.setOperator(this);
			throw e;
		}
	}

	/** Returns true iff the parameter with the given name is set. */
	public boolean isParameterSet(String key) throws UndefinedParameterError {
		return getParameter(key) != null;
	}

	/** Returns a single named parameter and casts it to String. */
	public String getParameterAsString(String key) throws UndefinedParameterError {
		return expandString(replaceMacros((String) getParameter(key)));
	}

	/** Returns a single named parameter and casts it to int. */
	public int getParameterAsInt(String key) throws UndefinedParameterError {
		return ((Integer) getParameter(key)).intValue();
	}

	/** Returns a single named parameter and casts it to double. */
	public double getParameterAsDouble(String key) throws UndefinedParameterError {
		return ((Double) getParameter(key)).doubleValue();
	}

	/**
	 * Returns a single named parameter and casts it to boolean. This method
	 * never throws an exception since there are no non-optional boolean
	 * parameters.
	 */
	public boolean getParameterAsBoolean(String key) {
		try {
			return ((Boolean) getParameter(key)).booleanValue();
		} catch (UndefinedParameterError e) {}
		return false; // cannot happen
	}

	/**
	 * Returns a single named parameter and casts it to List. The list returned
	 * by this method contains the user defined key-value pairs. Each element is
	 * an Object array of length 2. The first element is the key (String) the
	 * second the parameter value object, e.g. a Double object for
	 * ParameterTypeDouble. Since the definition of typed lists for arrays is not
	 * possible the caller have to perform the casts to the object arrays and from
	 * there to the actual types himself.
	 */
	public List getParameterList(String key) throws UndefinedParameterError {
		return (List) getParameter(key);
	}

	/** Returns a single named parameter and casts it to Color. */
	public java.awt.Color getParameterAsColor(String key) throws UndefinedParameterError {
		return com.rapidminer.parameter.ParameterTypeColor.string2Color((String) getParameter(key));
	}

	/**
	 * Returns a single named parameter and casts it to File. This file is
	 * already resolved against the process definition file. If the parameter name defines a
	 * non-optional parameter which is not set and has no default value, a
	 * UndefinedParameterError will be thrown. If the parameter is optional and
	 * was not set this method returns null. Operators should always use this
	 * method instead of directly using the method
	 * {@link Process#resolveFileName(String)}.
	 */
	public java.io.File getParameterAsFile(String key) throws UndefinedParameterError {
		String fileName = getParameterAsString(key);
		if (fileName == null)
			return null;
        Process process = getProcess();
		if (process != null)
			return process.resolveFileName(fileName);
		else
			return new java.io.File(fileName);
	}

    /** Returns a single named parameter and casts it to a double matrix. */
    public double[][] getParameterAsMatrix(String key) throws UndefinedParameterError {        
        String matrixLine = getParameterAsString(key);
        try {
            return StringToMatrixConverter.parseMatlabString(matrixLine);
        } catch (OperatorException e) {
            throw new UndefinedParameterError(e.getMessage());
        }
    }
    
	/**
	 * <p>Replaces</p>
	 * <ul>
	 * <li><b>%{n}</b> with the name of this operator</li>
	 * <li><b>%{c}</b> with the class of this operator</li>
	 * <li><b>%{t}</b> with the current system date and time 
	 * <li><b>%{a}</b> with the number of times the operator was applied</li>
	 * <li><b>%{b}</b> with the number of times the operator was applied plus
	 * one (a shortcut for %{p[1]})</li>
	 * <li><b>%{p[number]}</b> with the number of times the operator was applied
	 * plus number</li>
	 * <li><b>%{v[OperatorName.ValueName]}</b> with the value &quot;ValueName&quot; of the operator &quot;OperatorName&quot;</li>
	 * <li><b>%{%}</b> with %</li>
	 * </ul>
	 * <p>Returns null if str is null. Will throw a RuntimeException if a wrong 
	 * format is used.</p> 
	 */
	private String expandString(String str) {
		if (str == null)
			return null;
		StringBuffer result = new StringBuffer();
		int totalStart = 0;
		int start = 0;
		while ((start = str.indexOf("%{", totalStart)) >= 0) {
			result.append(str.substring(totalStart, start));
			int end = str.indexOf('}', start);
			if (end >= start) {
				String command = str.substring(start, end + 1);
				switch (command.charAt(2)) {
				case 'n':
					result.append(getName());
					break;
				case 'c':
					result.append(getClass().getName());
					break;
				case 'a':
					result.append(applyCount);
					break;
				case 'b':
					result.append(applyCount + 1);
					break;
				case 'p':
					int openNumberIndex = command.indexOf('[', 3);
					if (openNumberIndex < 0)
						throw new RuntimeException("A number in [] must follow $p, for example $p[10].");
					int closeNumberIndex = command.indexOf(']', openNumberIndex);
					if (closeNumberIndex < 0)
						throw new RuntimeException("A number in [] must follow $p, for example $p[10].");
					if (closeNumberIndex <= openNumberIndex + 1)
						throw new RuntimeException("A number in [] must follow $p, for example $p[10].");
					String numberString = command.substring(openNumberIndex + 1, closeNumberIndex);
					int number = Integer.parseInt(numberString);
					result.append(applyCount + number);
					break;
				case 't':
					// Please note that Date and DateFormat cannot be used since Windows does not support the resulting file names
					Calendar calendar = new GregorianCalendar();
					// year
					result.append(calendar.get(Calendar.YEAR) + "_");
					// month
					String month = calendar.get(Calendar.MONTH) + "";
					if (month.length() < 2)
						month = "0" + month;
					result.append(month + "_");
					// day
					String day = calendar.get(Calendar.DAY_OF_MONTH) + "";
					if (day.length() < 2)
						day = "0" + day;
					result.append(day + "-");
					// am - pm
					int amPm = calendar.get(Calendar.AM_PM);
					String amPmString = amPm == Calendar.AM ? "AM" : "PM";
					result.append(amPmString + "_");
					// hour
					String hour = calendar.get(Calendar.HOUR) + "";
					if (hour.length() < 2)
						hour = "0" + hour;
					result.append(hour + "_");
					// minute
					String minute = calendar.get(Calendar.MINUTE) + "";
					if (minute.length() < 2)
						minute = "0" + minute;
					result.append(minute + "_");
					// second
					String second = calendar.get(Calendar.SECOND) + "";
					if (second.length() < 2)
						second = "0" + second;
					result.append(second);
					break;
				case 'v':
					openNumberIndex = command.indexOf('[', 3);
					if (openNumberIndex < 0)
						throw new RuntimeException("An operator name and a value name divided by '.' in [] must follow $v, for example $p[Learner.applycount].");
					closeNumberIndex = command.indexOf(']', openNumberIndex);
					if (closeNumberIndex < 0)
						throw new RuntimeException("An operator name and a value name divided by '.' in [] must follow $v, for example $p[Learner.applycount].");
					if (closeNumberIndex <= openNumberIndex + 1)
						throw new RuntimeException("An operator name and a value name divided by '.' in [] must follow $v, for example $p[Learner.applycount].");
					String operatorValueString = command.substring(openNumberIndex + 1, closeNumberIndex);
					String[] operatorValuePair = operatorValueString.split("\\.");
					if (operatorValuePair.length != 2) {
						throw new RuntimeException("An operator name and a value name divided by '.' in [] must follow $v, for example $p[Learner.applycount].");						
					}
					Operator operator = getProcess().getOperator(operatorValuePair[0]);
					double value = operator.getValue(operatorValuePair[1]);
					if (Double.isNaN(value)) {
						logError("Value '" + operatorValuePair[1] + "' of the operator '" + operatorValuePair[0] + "' not found!");
					}
					result.append(Tools.formatIntegerIfPossible(value));
					break;
				case '%':
					result.append('%');
					break;
				default:
				    result.append(command);
				    break;
				}
			} else {
				end = start + 2;
				result.append("%{");
			}
			totalStart = end + 1;
		}
		result.append(str.substring(totalStart));
		return result.toString();
	}

	/** Replaces existing macros in the given value string by the macro values defined for the
	 *  process. Please note that this is basically only supported for string type parameter
	 *  values.
	 *  
	 *  This method replaces the predefined macros like %{process_name}, %{process_file},
	 *  and %{process_path} and tries to replace macros surrounded by &quot;%{&quot; and 
	 *  &quot;}&quot; with help of the {@link com.rapidminer.MacroHandler} of the {@link Process}.
	 *  These macros might have been defined with help of a {@link MacroDefinitionOperator}.
	 *   
	 *  If any exception would be thrown it is catched and just the input string is returned.
	 *  
	 *  TODO: allow macros for numerical and other parameter types.
	 */
	private String replaceMacros(String value) {
		if (value == null)
			return null;
		try {
			String line = value;
			int startIndex = line.indexOf("%{");
			StringBuffer result = new StringBuffer();
			while (startIndex >= 0) {
				result.append(line.substring(0, startIndex));
				int endIndex = line.indexOf("}", startIndex + 2);
				String macroString = line.substring(startIndex + 2, endIndex);
				String macroValue = getProcess().getMacroHandler().getMacro(macroString);
				if (macroValue != null) {
					result.append(macroValue);
				} else {
					result.append("%{" + macroString + "}");
				}
				line = line.substring(endIndex + 1);
				startIndex = line.indexOf("%{");
			}
			result.append(line);
			return result.toString();
		} catch (Exception e) {
			return value;
		}
	}
	
	/**
	 * Returns a list of <tt>ParameterTypes</tt> describing the parameters of
	 * this operator. The default implementation returns an empty list if no
	 * input objects can be retained and special parameters for those input
	 * objects which can be prevented from being consumed.
	 */
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = new LinkedList<ParameterType>();
		Class[] inputClasses = getDesiredInputClasses();
		for (int i = 0; i < inputClasses.length; i++) {
			InputDescription description = getInputDescription(inputClasses[i]);
			if (description.showParameter()) {
				types.add(new ParameterTypeBoolean(description.getParameterName(), "Indicates if this input object should also be returned as output.", description.getKeepDefault())); 
			}
		}
		return types;
	}
	
	/** Returns the parameter type with the given name. Will return null if this operator does not
	 *  have a parameter with the given name. */
	public ParameterType getParameterType(String name) {
		Iterator<ParameterType> i = getParameterTypes().iterator();
		while (i.hasNext()) {
			ParameterType current = i.next();
			if (current.getKey().equals(name))
				return current;
		}
		return null;
	}

	// ----------------------- XML reading and writing
	// ---------------------------------------------

	/** Writes the XML representation of this operator. */
	public void writeXML(PrintWriter out, String indent) throws IOException {
		out.println(getXML(indent));
	}

	/** Returns the XML representation of this operator. */
	public String getXML(String indent) {
		StringBuffer result = new StringBuffer();
		String breakpointString = "";
		StringBuffer breakpointBuf = null;
		boolean firstBreakpoint = true;
		for (int i = 0; i < BreakpointListener.BREAKPOINT_POS_NAME.length; i++) {
			if (breakPoint[i]) {
				if (firstBreakpoint) {
					breakpointBuf = new StringBuffer(" breakpoints=\"");
					firstBreakpoint = false;
				} else {
					breakpointBuf.append(",");
				}
				breakpointBuf.append(BreakpointListener.BREAKPOINT_POS_NAME[i]);
			}
		}
		if (breakpointBuf != null) {
			breakpointBuf.append("\"");
			breakpointString = breakpointBuf.toString();
		}

		result.append(indent + 
				"<operator " + "name=\"" + name + "\" " + 
				"class=\"" + operatorDescription.getName() + "\"" + 
				breakpointString + 
				(!enabled ? " activated=\"no\"" : "") + 
				((this instanceof OperatorChain) ? (expanded ? " expanded=\"yes\"" : " expanded=\"no\"") : "") + 
				">" + Tools.getLineSeparator());
		if ((userDescription != null) && (userDescription.length() != 0))
			result.append(indent + "    <description text=\"" + userDescription + "\"/>" + Tools.getLineSeparator());
		result.append(parameters.getXML(indent + "    "));
		result.append(getInnerOperatorsXML(indent + "    "));
		result.append(indent + "</operator>" + Tools.getLineSeparator());
		return result.toString();
	}

	/**
	 * Writes the XML representation of the inner operators. Since an Operator
	 * does not have any inner operators, the default implementation does
	 * nothing. Implemented by <tt>OperatorChain</tt>.
	 */
	protected String getInnerOperatorsXML(String indent) {
		return "";
	}

	private static String createDescriptionFromXML(Element element) throws XMLException {
		Attr textAttr = element.getAttributeNode("text");
		if (textAttr == null)
			throw new XMLException("Attribute 'text' of <description> tag is not set.");
		if (element.getChildNodes().getLength() > 0)
			throw new XMLException("No inner tags allowed for <description>");
		return textAttr.getValue();
	}

	private static Object[] createParameterFromXML(Element element) throws XMLException {
		Attr keyAttr = element.getAttributeNode("key");
		Attr valueAttr = element.getAttributeNode("value");
		if (keyAttr == null)
			throw new XMLException("Attribute 'key' of <parameter> tag is not set.");
		if (valueAttr == null)
			throw new XMLException("Attribute 'value' of <parameter> tag is not set.");
		if (element.getChildNodes().getLength() > 0)
			throw new XMLException("No inner tags allowed for <parameter>");
		return new Object[] { keyAttr.getValue(), valueAttr.getValue() };
	}

	private static Object[] createParameterListFromXML(Element element) throws XMLException {
		List<Object[]> values = new LinkedList<Object[]>();
		Attr keyAttr = element.getAttributeNode("key");
		if (keyAttr == null)
			throw new XMLException("Attribute 'key' of <list> tag is not set.");
		NodeList children = element.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node node = children.item(i);
			if (node instanceof Element) {
				Element inner = (Element) node;
				if (inner.getTagName().toLowerCase().equals("parameter")) {
					values.add(createParameterFromXML(inner));
				} else if (inner.getTagName().toLowerCase().equals("list")) {
					values.add(createParameterListFromXML(inner));
				} else {
					throw new XMLException("Ilegal inner tag for <list>: " + inner.getTagName());
				}
			}
		}
		return new Object[] { keyAttr.getValue(), values };
	}

	public static Operator createFromXML(Element element) throws XMLException {
		if (!element.getTagName().toLowerCase().equals("operator")) {
			throw new XMLException("<operator> expected!");
		}

		// collect operator class and name
		String className = null;
		String name = null;

		Attr classAttr = element.getAttributeNode("class");
		if (classAttr != null) {
			className = classAttr.getValue();
		} else {
			throw new XMLException("Attribute 'class' of <operator> tag is not defined! ");
		}
		Attr nameAttr = element.getAttributeNode("name");
		if (nameAttr != null) {
			name = nameAttr.getValue();
		} else {
			throw new XMLException("Attribute 'name' of <operator> tag is not defined! ");
		}
		
		// create operator
		OperatorDescription opDescr = OperatorService.getOperatorDescription(className);
		
		// TODO: remove for some later version
		// TODO: this is only in order to ensure backwards compatibility from version 3.4 to 4.0
		if (opDescr == null) {
			if (className.startsWith("Y-")) {
				// try without leading "Y-"
				String yClassName = className.substring("Y-".length());
				opDescr = OperatorService.getOperatorDescription(yClassName);
				if (opDescr != null)
					className = yClassName;
			} else {
				// try to use one of the Weka operators starting with "W-" now
				String wClassName = WekaTools.WEKA_OPERATOR_PREFIX + className;
				opDescr = OperatorService.getOperatorDescription(wClassName);
				if (opDescr != null)
					className = wClassName;
			}
		}
		
		if (opDescr == null)
			throw new XMLException("Unknown operator class: '" + className + "'!");
		Operator operator = null;
		try {
			operator = opDescr.createOperatorInstance();
		} catch (OperatorCreationException e) {
			throw new XMLException("Cannot create operator: " + e.getMessage(), e);
		}
        operator.setName(name);

		// parameters and inner operators
		operator.setOperatorParameters(element);

		return operator;
	}
	
	/** Sets all parameters of this operator (including inner operators and their parameters). */
	public void setOperatorParameters(Element element) throws XMLException {		
		// breakpoints and enable check
		String breakpointString = null;
		String activationString = null;
		String expansionString = null;
		
		Attr breakpointAttr = element.getAttributeNode("breakpoints");
		if (breakpointAttr != null) {
			breakpointString = breakpointAttr.getValue();
		}
		Attr activationAttr = element.getAttributeNode("activated");
		if (activationAttr != null) {
			activationString = activationAttr.getValue();
		}
		Attr expansionAttr = element.getAttributeNode("expanded");
		if (expansionAttr != null) {
			expansionString = expansionAttr.getValue();
		}
		
		// breakpoints
		if (breakpointString != null) {
			boolean ok = false;
			if (breakpointString.equals("both")) {
				setBreakpoint(BreakpointListener.BREAKPOINT_BEFORE, true);
				setBreakpoint(BreakpointListener.BREAKPOINT_AFTER, true);
				ok = true;
			}
			for (int i = 0; i < BreakpointListener.BREAKPOINT_POS_NAME.length; i++) {
				if (breakpointString.indexOf(BreakpointListener.BREAKPOINT_POS_NAME[i]) >= 0) {
					setBreakpoint(i, true);
					ok = true;
				}
			}
			if (!ok)
				throw new XMLException("Breakpoint `" + breakpointString + "` is not defined!");
		}

		// is enabled?
		if (activationString != null) {
			if (activationString.equals("no"))
				setEnabled(false);
			else if (activationString.equals("yes"))
				setEnabled(true);
			else {
				throw new XMLException("Activation mode `" + activationString + "` is not defined!");
			}
		}
		
		// is expanded?
		if (expansionString != null) {
			if (expansionString.equals("no"))
				setExpanded(false);
			else if (expansionString.equals("yes"))
				setExpanded(true);
			else {
				throw new XMLException("Expansion mode `" + expansionString + "` is not defined!");
			}
		} else {
			setExpanded(true);
		}
		
		// parameters and inner operators
		NodeList innerTags = element.getChildNodes();
		for (int i = 0; i < innerTags.getLength(); i++) {
			Node node = innerTags.item(i);
			if (node instanceof Element) {
				Element inner = (Element) node;
				if (inner.getTagName().toLowerCase().equals("parameter")) {
					Object[] parameter = createParameterFromXML(inner);
					getParameters().setParameter((String) parameter[0], parameter[1]);
				} else if (inner.getTagName().toLowerCase().equals("list")) {
					Object[] parameter = createParameterListFromXML(inner);
					getParameters().setParameter((String)parameter[0], parameter[1]);
				} else if (inner.getTagName().toLowerCase().equals("operator")) {
					if (this instanceof OperatorChain) {
                        ((OperatorChain) this).addOperator(createFromXML(inner));
					} else {
						throw new XMLException("No inner operators allowed for '" + getOperatorClassName() + "'");
					}
				} else if (inner.getTagName().toLowerCase().equals("description")) {
					String description = createDescriptionFromXML(inner);
					setUserDescription(description);
				} else {
					throw new XMLException("Ilegal inner tag for <operator>: " + inner.getTagName());
				}
			}
		}
	}
	
	/**
	 * Clears the list of errors.
	 * 
	 * @see #addError(String)
	 */
	public void clearErrorList() {
		errorList.clear();
	}

	/**
	 * Adds an error message.
	 * 
	 * @see #getErrorList()
	 */
	public void addError(String message) {
		logError(this.getName() + ": " + message);
		errorList.add(message);
	}

	/**
	 * Adds a warning message to the error list.
	 * 
	 * @see #getErrorList()
	 */
	public void addWarning(String message) {
		logWarning(this.getName() + ": " + message);
		errorList.add(message);
	}

	/**
	 * Returns a List of Strings containing error messages.
	 * 
	 * @see #addError(String)
	 */
	public List<String> getErrorList() {
		return errorList;
	}

	/** Returns the system time when the operator was started. */
	public long getStartTime() {
		return startTime;
	}

	/**
	 * Convenience method for logging a message prefixed by the operator name.
	 * 
	 * @see LogService
	 */
    /*
	public void logMessage(String message, int verbosityLevel) {
		LogService.logMessage(getName() + ": " + message, verbosityLevel);
	}
    */

	// --------------------- to string and other outputs
	// ---------------------------------

	/** Returns the name. */
	public String toString() {
		String type = null;
		if (getOperatorDescription() != null)
			type = getOperatorClassName();
		else
			type = getClass().getName();
		return ((breakPoint[0] || breakPoint[1]) ? "* " : "") + name + " (" + type + ")";
	}

	/** Returns this operator's name and class. 
	 * @deprecated Use {@link #createProcessTree(int)} instead*/
	@Deprecated
	public String createExperimentTree(int indent) {
		return createProcessTree(indent);
	}

	/** Returns this operator's name and class. */
	public String createProcessTree(int indent) {
		return createProcessTree(indent, "", "", null, null);
	}

	/** Returns this operator's name and class. 
	 * @deprecated Use {@link #createMarkedProcessTree(int,String,Operator)} instead*/
	@Deprecated
	public String createMarkedExperimentTree(int indent, String mark, Operator markOperator) {
		return createMarkedProcessTree(indent, mark, markOperator);
	}

	/** Returns this operator's name and class. */
	public String createMarkedProcessTree(int indent, String mark, Operator markOperator) {
		return createProcessTree(indent, "", "", markOperator, mark);
	}

	/** Returns this operator's name and class. 
	 * @deprecated Use {@link #createProcessTree(int,String,String,Operator,String)} instead*/
	@Deprecated
	protected String createExperimentTree(int indent, String selfPrefix, String childPrefix, Operator markOperator, String mark) {
		return createProcessTree(indent, selfPrefix, childPrefix, markOperator, mark);
	}

	/** Returns this operator's name and class. */
	protected String createProcessTree(int indent, String selfPrefix, String childPrefix, Operator markOperator, String mark) {
		if ((markOperator != null) && (getName().equals(markOperator.getName())))
			return indent(indent - mark.length()) + mark + selfPrefix + getName() + "[" + applyCount + "]" + " (" + getOperatorClassName() + ")";
		else
			return indent(indent) + selfPrefix + getName() + "[" + applyCount + "]" + " (" + getOperatorClassName() + ")";
	}

	/** Returns a whitespace with length indent. */
	private String indent(int indent) {
		StringBuffer s = new StringBuffer();
		for (int i = 0; i < indent; i++)
			s.append(" ");
		return s.toString();
	}
    
	/** Returns the encoding if defined by the root operator if this operator is part of a process 
	 *  or the standard encoding defined via the system property. If both is not possible or if 
	 *  the defined encoding name is 'SYSTEM', the default encoding of the underlying operating
	 *  system is returned.
	 */
    public final Charset getEncoding() {
        String encoding = null;

        Process process = getProcess();
        if (process != null) {
            try {
                encoding = process.getRootOperator().getParameterAsString(ProcessRootOperator.PARAMETER_ENCODING);
            } catch (UndefinedParameterError e) {
                // do nothing and simply use system encoding
            }
        }
        
        if (encoding == null) {
        	encoding = System.getProperty(RapidMiner.PROPERTY_RAPIDMINER_GENERAL_DEFAULT_ENCODING);
        	if ((encoding == null) || (encoding.trim().length() == 0)) {
        		logWarning("No default encoding set. Using system encoding as default and trying to find an encoding defined by the process...");
        		encoding = RapidMiner.SYSTEM_ENCODING_NAME;
        	}
        }
        
        Charset result = null;
        if (RapidMiner.SYSTEM_ENCODING_NAME.equals(encoding)) {
        	result = Charset.defaultCharset();
        } else {
        	try {
        		result = Charset.forName(encoding);
        	} catch (IllegalCharsetNameException e) {
        		logWarning("Unknown encoding name: " + encoding + ", using system encoding instead.");
        		result = Charset.defaultCharset();
        	} catch (UnsupportedCharsetException e) {
        		logWarning("The encoding '" + encoding + "' is not supported, using system encoding instead.");
        		result = Charset.defaultCharset();
        	} catch (IllegalArgumentException e) {
        		logWarning("Empty encoding name, using system encoding instead.");
        		result = Charset.defaultCharset();
        	}
        }
        return result;
    }
    
    public boolean isDebugMode() {
        String debugProperty = System.getProperty(RapidMiner.PROPERTY_RAPIDMINER_GENERAL_DEBUGMODE);
        return Tools.booleanValue(debugProperty, false);
    }
}
