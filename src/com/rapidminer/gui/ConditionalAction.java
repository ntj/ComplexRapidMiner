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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Icon;

/**
 * An action that must be enabled/disabled depending on certain conditions.
 * These conditions can be mandatrory, disallowed, or irrelevant. All
 * ConditionalActions created are added to a collection and there status is
 * automatically checked if the condition premises might have changed.
 * 
 * @author Ingo Mierswa, Simon Fischer
 * @version $Id: ConditionalAction.java,v 2.10 2006/03/27 13:21:58 ingomierswa
 *          Exp $
 */
public abstract class ConditionalAction extends AbstractAction {

	private static final List<ConditionalAction> ALL_ACTIONS = new LinkedList<ConditionalAction>();

	/* The possible states. */
	public static final int DISALLOWED = -1;

	public static final int DONT_CARE = 0;
	
	public static final int MANDATORY = 1;

	/* The possible conditions. */
	public static final int OPERATOR_SELECTED = 0;

	public static final int OPERATOR_CHAIN_SELECTED = 1;

	public static final int ROOT_SELECTED = 2;
	
	public static final int SIBLINGS_EXIST = 3;

	public static final int CLIPBOARD_FILLED = 4;

	public static final int PROCESS_STOPPED = 5;
	
	public static final int PROCESS_PAUSED = 6;
	
	public static final int PROCESS_RUNNING = 7;
	
	public static final int XML_VIEW = 8;
	
	public static final int DESCRIPTION_VIEW = 9;
	
	public static final int NUMBER_OF_CONDITIONS = 10;
	
	
	private int[] conditions = new int[NUMBER_OF_CONDITIONS];

	public ConditionalAction(String name) {
		this(name, null);
	}

	public ConditionalAction(String name, Icon icon) {
		super(name, icon);
		ALL_ACTIONS.add(this);
	}

	/**
	 * @param index
	 *            one out of OPERATOR_SELECTED, OPERATOR_CHAIN_SELECTED,
	 *            ROOT_SELECTED, CLIPBOARD_FILLED, and PROCESS_RUNNING
	 * @param condition
	 *            one out of DISALLOWED, DONT_CARE, and MANDATORY
	 */
	public void setCondition(int index, int condition) {
		conditions[index] = condition;
	}

	/** Updates all actions. */
	public static void updateAll(boolean[] states) {
		Iterator<ConditionalAction> i = ALL_ACTIONS.iterator();
		while (i.hasNext()) {
			i.next().update(states);
		}
	}

	/**
	 * Updates an action given the set of states that can be true or false.
	 * States refer to OPERATOR_SELECTED... An action is enabled iff for all
	 * states the condition is DONT_CARE or MANDATORY and state is true or
	 * DISALLOWED and state is false.
	 */
	private void update(boolean[] state) {
		boolean ok = true;
		for (int i = 0; i < conditions.length; i++) {
			if (conditions[i] != DONT_CARE) {
				if (((conditions[i] == MANDATORY) && (state[i] == false)) || ((conditions[i] == DISALLOWED) && (state[i] == true))) {
					ok = false;
					break;
				}
			}
		}
		setEnabled(ok);
	}
}
