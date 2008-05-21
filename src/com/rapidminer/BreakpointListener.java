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

import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.Operator;

/**
 * The method {@link #breakpointReached(Operator, IOContainer, int)} is invoked
 * every time a breakpoint is reached during a process run.
 * 
 * @author Ingo Mierswa, Simon Fischer
 * @version $Id: BreakpointListener.java,v 2.11 2006/03/21 15:35:36 ingomierswa
 *          Exp $
 */
public interface BreakpointListener {

	/** Indicates a breakpoint before the operator. */
	public static final int BREAKPOINT_BEFORE = 0;

	/** Indicates a breakpoint after the operator. */
	public static final int BREAKPOINT_WITHIN = 1;

	/** Indicates a breakpoint after the operator. */
	public static final int BREAKPOINT_AFTER = 2;

	public static final String[] BREAKPOINT_POS_NAME = { "before", "within", "after" };
	
	public static final String[] BREAKPOINT_POS_NAME_UPPERCASE = { "Before", "Within", "After" };

	/**
	 * This method is invoked every time a breakpoint is reached during the
	 * process. The location is one out of BREAKPOINT_BEFORE or
	 * BREAKPOINT_AFTER.
	 */
	public void breakpointReached(Operator op, IOContainer iocontainer, int location);

	/** This method is invoked after the process was resumed. */
	public void resume();
}
