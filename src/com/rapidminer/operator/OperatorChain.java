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

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.rapidminer.Process;
import com.rapidminer.operator.condition.CombinedInnerOperatorCondition;
import com.rapidminer.operator.condition.InnerOperatorCondition;
import com.rapidminer.operator.condition.SimpleChainInnerOperatorCondition;
import com.rapidminer.tools.Tools;


/**
 * A chain of operators that is subsequently applied. As an OperatorChain is an
 * Operator itself it can be arbitrarily nested.<br>
 * Inheritants can access inner operators by {@link #getOperator(int)}. They
 * should override {@link #getMaxNumberOfInnerOperators()} and
 * {@link #getMinNumberOfInnerOperators()} which are used for some checks. They
 * should also override {@link #getInnerOperatorCondition()} to ensure that all
 * inner operators get the desired input and return the necessary output for the
 * next inner operator. Please refer to the RapidMiner tutorial for a description how 
 * to implement your own operator chain.
 * 
 * @author Simon Fischer, Ingo Mierswa
 * @version $Id: OperatorChain.java,v 1.7 2008/05/09 19:23:19 ingomierswa Exp $
 */
public abstract class OperatorChain extends Operator {

	/** The inner operators. They are applied in their ordering in the list. */
	private List<Operator> operators = new ArrayList<Operator>();

    /** The list of listeners for adding events. */
    private List<AddListener> addListeners = new LinkedList<AddListener>();

	// --------------------------------------------------------------------------------

	/** Creates an empty operator chain. */
	public OperatorChain(OperatorDescription description) {
		super(description);
	}

	/** Returns the maximum number of innner operators. */
	public abstract int getMaxNumberOfInnerOperators();

	/** Returns the minimum number of innner operators. */
	public abstract int getMinNumberOfInnerOperators();

	/**
	 * Must return a condition of the IO behaviour of all desired inner
	 * operators. If there are no &quot;special&quot; conditions and the chain
	 * works similar to a simple operator chain this method should at least
	 * return a {@link SimpleChainInnerOperatorCondition}. More than one
	 * condition should be combined with help of the class
	 * {@link CombinedInnerOperatorCondition}.
	 */
	public abstract InnerOperatorCondition getInnerOperatorCondition();

    /** Adds the given listener. */
    public void addAddListener(AddListener listener) {
        addListeners.add(listener);
    }

    /** Removes the given listener. */
    public void removeAddListener(AddListener listener) {
        addListeners.remove(listener);
    }

    /** Notifies all added add listeners that a new child was added. */
    private void fireAddEvent(Operator child) {
        Iterator<AddListener> i = addListeners.iterator();
        while (i.hasNext()) {
            i.next().operatorAdded(child);
        }
    }
    
	/**
	 * Performs a deep clone of this operator chain. Use this method only if you
	 * are sure what you are doing.
	 */
	public Operator cloneOperator(String name) {
		OperatorChain clone = (OperatorChain) super.cloneOperator(name);
		clone.operators = new ArrayList<Operator>();
		Iterator<Operator> i = operators.iterator();
		while (i.hasNext()) {
			Operator originalChild = i.next();
			Operator clonedChild = originalChild.cloneOperator(originalChild.getName());
			clonedChild.setParent(clone);
			clone.addOperator(clonedChild);
		}
		return clone;
	}

	/**
	 * This method checks if inner operators can handle their input and deliver
	 * the necessary output. Depending on the return value of the method
	 * {@link #shouldReturnInnerOutput()} this method returns
	 * <ul>
	 * <li>the result of <code>getDeliveredOutputClasses()</code> if the
	 * output of the inner operators should not be returned.</li>
	 * <li>the result of <code>getAllOutputClasses(Class[] innerOutput)</code>
	 * if the output of the inner operators (innerOutput) should also be
	 * returned.</li>
	 * </ul>
	 */
	public final Class[] checkIO(Class[] input) throws IllegalInputException, WrongNumberOfInnerOperatorsException {
		InnerOperatorCondition condition = getInnerOperatorCondition();
		Class[] innerOutput = condition.checkIO(this, input);

		if (shouldReturnInnerOutput()) {
			return getAllOutputClasses(innerOutput);
		} else {
			return getDeliveredOutputClasses();
		}
	}

	/**
	 * Indicates if inner output should be delivered by this operator chain.
	 * Default is false. Operators which want to change this default behaviour
	 * should override this method and should return true. In this case the
	 * method checkIO would not longer return the result of
	 * {@link #getDeliveredOutputClasses()} but of
	 * {@link #getAllOutputClasses(Class[])}.
	 */
	public boolean shouldReturnInnerOutput() {
		return false;
	}

	/**
	 * Helper method if in addition to the created output the inner output
	 * should also be returned. Can be used in {@link #checkIO(Class[] input)}.
	 */
	private Class[] getAllOutputClasses(Class[] innerOutput) {
		Class[] deliveredOutput = getDeliveredOutputClasses();
		Class[] result = new Class[deliveredOutput.length + innerOutput.length];
		System.arraycopy(deliveredOutput, 0, result, 0, deliveredOutput.length);
		System.arraycopy(innerOutput, 0, result, deliveredOutput.length, innerOutput.length);
		return result;
	}

	/**
	 * Adds a new inner operator at the last position. The returned index is the
	 * position of the added operator with respect to all operators (including
	 * the disabled operators).
	 */
	public final int addOperator(Operator o) {
        return addOperator(o, getNumberOfAllOperators());
	}

	/**
	 * Adds the given operator at the given position. Please note that all
	 * operators (including the disabled operators) are used for position
	 * calculations.
	 */
	public final int addOperator(Operator operator, int index) {
        if (operator == null)
            return -1;
        operator.setParent(this);
		operators.add(index, operator);
        Process process = getProcess();
        if (process != null)
            operator.registerOperator(process);
		if (getNumberOfOperators() == getMaxNumberOfInnerOperators() + 1) {
			logWarning("More than " + getMaxNumberOfInnerOperators() + " inner operators!");
		}
        fireAddEvent(operator);
        return index;
	}

    /** Register this operator chain and all of its children in the given process. This might change the
     *  name of the operator. */
    protected void registerOperator(Process process) {
        super.registerOperator(process);
        Iterator<Operator> i = operators.iterator();
        while (i.hasNext()) {
            Operator child = i.next();
            child.registerOperator(process);
        }
    }
    
    /** Unregisters this chain and all of its children from the given process. */
    protected void unregisterOperator(Process process) {
        super.unregisterOperator(process);
        Iterator<Operator> i = operators.iterator();
        while (i.hasNext()) {
            Operator operator = i.next();
            operator.unregisterOperator(process);
        }
    }
    
	/** Removes the given operator from this operator chain. Do not use this method to actually remove
     *  an operator from an operator chain. Use operator.remove() instead. This method will be invoked
     *  by the remove() method (which also performs some other actions). */
	protected final void removeOperator(Operator operator) {
		operators.remove(operator);
	}

	/** Returns the i-th inner operator. */
	public Operator getOperator(int i) {
		if ((i < 0) || (i >= getNumberOfOperators())) {
			throw new RuntimeException("Illegal operator index in getOperator() (" + getName() + "): " + i);
		}
		int counter = 0;
		Iterator<Operator> o = operators.iterator();
		while (o.hasNext()) {
			Operator operator = o.next();
			if (operator.isEnabled()) {
				if (counter == i)
					return operator;
				counter++;
			}
		}
		return null;
	}

	/** Returns an iterator over all Operators. */
	public Iterator<Operator> getOperators() {
		return operators.iterator();
	}

	/** Returns recursively all child operators independently if they are activated or not. */
	public List<Operator> getAllInnerOperators() {
		List<Operator> children = new LinkedList<Operator>();
		for (int i = 0; i < operators.size(); i++) {
			Operator innerOp = operators.get(i);
			children.add(innerOp);
			if (innerOp instanceof OperatorChain)
				children.addAll(((OperatorChain) innerOp).getAllInnerOperators());
		}
		return children;
	}

	/** Returns the number of all enabled inner operators. */
	public int getNumberOfOperators() {
		int number = 0;
		Iterator<Operator> i = operators.iterator();
		while (i.hasNext()) {
			Operator op = i.next();
			if (op.isEnabled())
				number++;
		}
		return number;
	}

	/**
	 * Returns the number of all inner operators (including the disabled
	 * operators). Mainly used for GUI purposes. Operators should use
	 * {@link #getNumberOfOperators()}.
	 */
	public int getNumberOfAllOperators() {
		return operators.size();
	}

	/**
	 * Returns the i-th operator. In contrast to the method
	 * {@link #getOperator(int i)} this method also uses disabled operators.
	 * Mainly used for GUI purposes. Other operators should use the method
	 * {@link #getOperator(int i)} which only delivers enabled inner operators.
	 */
	public Operator getOperatorFromAll(int i) {
		return operators.get(i);
	}

    /** Returns the index of the given operator in the list of children. If useDisabled is true,
     *  disabled operators are also used for index calculations. */
	public int getIndexOfOperator(Operator operator, boolean useDisabled) {
		if (useDisabled) {
			return operators.indexOf(operator);
		} else {
			int index = 0;
			Iterator<Operator> i = operators.iterator();
			while (i.hasNext()) {
				Operator current = i.next();
				if (current.isEnabled()) {
					if (current.equals(operator))
						return index;
					index++;
				}
			}
			return -1;
		}
	}

	/**
	 * Returns the inner operator named <tt>name</tt> or null if no such
	 * operator exists.
	 */
	public Operator getInnerOperatorForName(String name) {
		if (name == null)
			return null;
		if (name.equals(this.getName()))
			return this;
		Iterator<Operator> i = operators.iterator();
		while (i.hasNext()) {
			Operator inner = i.next();
			if (name.equals(inner.getName()))
				return inner;
			if (inner instanceof OperatorChain) {
				Operator innerinner = ((OperatorChain) inner).getInnerOperatorForName(name);
				if (innerinner != null)
					return innerinner;
			}
		}
		return null;
	}

    /** Returns the result of the super method if this operator does not have a parent.
     *  Otherwise this method returns true if it is enabled and the parent is also enabled. */
    /*
	public boolean isEnabled() {
		if (getParent() == null) {
			return super.isEnabled();
		} else {
			return (super.isEnabled() && getParent().isEnabled());
		}
	}
    */

	/** Invokes the super method and the method for all children. */
	public void processStarts() throws OperatorException {
		super.processStarts();
		for (int i = 0; i < getNumberOfOperators(); i++)
			getOperator(i).processStarts();
	}

    /** Invokes the super method and the method for all children. */
	public void processFinished() throws OperatorException {
		super.processFinished();
		for (int i = 0; i < getNumberOfOperators(); i++)
			getOperator(i).processFinished();
	}

	// -------------------- implementierte abstrakte Methoden
	// --------------------

	/**
	 * Applies all inner operators. The input to this operator becomes the input
	 * of the first inner operator. The latter's output is passed to the second
	 * inner operator and so on. Note to subclassers: If subclasses (for example
	 * wrappers) want to make use of this method remember to call exactly this
	 * method <tt>(super.apply())</tt> and do not call
	 * <tt>super.apply(IOContainer)</tt> erroneously which will result in an
	 * infinite loop.
	 * 
	 * @return the last inner operator's output or the input itself if the chain
	 *         is empty.
	 */
	public IOObject[] apply() throws OperatorException {
		IOContainer input = getInput();
		Iterator<Operator> i = operators.iterator();
		while (i.hasNext()) {
            try {
                input = i.next().apply(input);
            } catch (ConcurrentModificationException e) {
                if (isDebugMode())
                    e.printStackTrace();
                throw new UserError(this, 923);
            }
		}
		return input.getIOObjects();
	}

	// --------------------------------------------------------------------------------

	/**
	 * This method invokes the additional check method for each child.
	 * Subclasses which override this method to perform a check should also
	 * invoke super.performAdditionalChecks()!
	 */
	public void performAdditionalChecks() throws UserError {
		Iterator<Operator> i = operators.iterator();
		while (i.hasNext()) {
			Operator o = i.next();
			if (o.isEnabled())
				o.performAdditionalChecks();
		}
	}

	/**
	 * Will throw an exception if a non optional property has no default value
	 * and is not defined by user.
	 */
	public int checkProperties() {
		int errorCount = super.checkProperties();
		Iterator<Operator> i = operators.iterator();
		while (i.hasNext()) {
			Operator o = i.next();
			if (o.isEnabled())
				errorCount += o.checkProperties();
		}
		return errorCount;
	}

	/**
	 * Will count an the number of deprecated operators, i.e. the operators
	 * which {@link #getDeprecationInfo()} method does not return null. Returns
	 * the total number of deprecations.
	 */
	public int checkDeprecations() {
		int deprecationCount = super.checkDeprecations();
		Iterator<Operator> i = operators.iterator();
		while (i.hasNext()) {
			Operator o = i.next();
			deprecationCount += o.checkDeprecations();
		}
		return deprecationCount;
	}

	/**
	 * Checks if the number of inner operators lies between MinInnerOps and
	 * MaxInnerOps. Performs the check for all operator chains which are
	 * children of this operator chain.
	 */
	public int checkNumberOfInnerOperators() {
		int errorCount = 0;
		if ((getNumberOfOperators() < getMinNumberOfInnerOperators()) || (getNumberOfOperators() > getMaxNumberOfInnerOperators())) {
			int maximum = getMaxNumberOfInnerOperators();
			String maximumString = maximum == Integer.MAX_VALUE ? "infinity" : (maximum + "");
			String message = "Operator has " + getNumberOfOperators() + " " + ((getNumberOfOperators() == 1) ? "child" : "children") + ", should be "
					+ ((getMinNumberOfInnerOperators() == getMaxNumberOfInnerOperators()) ? getMinNumberOfInnerOperators() + "" : " between " + getMinNumberOfInnerOperators() + " and " + maximumString);
			addError(message);
			errorCount++;
		}
		Iterator<Operator> i = operators.iterator();
		while (i.hasNext()) {
			Operator o = i.next();
			if ((o instanceof OperatorChain) && o.isEnabled())
				errorCount += ((OperatorChain) o).checkNumberOfInnerOperators();
		}
		return errorCount;
	}

	/**
	 * Returns this OperatorChain's name and class and the ExperimentTrees of
	 * the inner operators.
	 * @deprecated Use {@link #createProcessTree(int,String,String,Operator,String)} instead
	 */
	@Deprecated
	protected String createExperimentTree(int indent, String selfPrefix, String childPrefix, Operator markOperator, String mark) {
		return createProcessTree(indent, selfPrefix, childPrefix, markOperator, mark);
	}

	/**
	 * Returns this OperatorChain's name and class and the process trees of
	 * the inner operators.
	 */
	protected String createProcessTree(int indent, String selfPrefix, String childPrefix, Operator markOperator, String mark) {
		String tree = super.createProcessTree(indent, selfPrefix, childPrefix, markOperator, mark);
		Iterator<Operator> i = operators.iterator();
		while (i.hasNext()) {
			Operator o = i.next();
			tree += Tools.getLineSeparator() + o.createProcessTree(indent, childPrefix + "+- ", childPrefix + (i.hasNext() ? "|  " : "   "), markOperator, mark);
		}
		return tree;
	}

    /** Returns the XML representation for all inner operators. */
	protected final String getInnerOperatorsXML(String indent) {
		StringBuffer result = new StringBuffer();
		Iterator<Operator> i = operators.iterator();
		while ((i.hasNext())) {
			result.append(i.next().getXML(indent));
		}
		return result.toString();
	}

    /** Clears the error list for this operator (by invoking the super method) and all children. */
	public void clearErrorList() {
		Iterator<Operator> i = operators.iterator();
		while ((i.hasNext())) {
			i.next().clearErrorList();
		}
		super.clearErrorList();
	}
}
