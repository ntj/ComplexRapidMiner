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
package com.rapidminer.operator.learner.associations.fpgrowth;

/**
 * A stack for frequencies.
 * 
 * @author Sebastian Land
 * @version $Id: FrequencyStack.java,v 1.1 2007/05/27 22:02:33 ingomierswa Exp $
 */
public interface FrequencyStack {

	/**
	 * Increases the frequency stored on stackHeight level of stack by value, if stackHeight is the top of stack, or stackHeight is top of stack + 1
	 * 
	 * @param stackHeight
	 *            describes the level of stack, counted from bottom on which the value is added
	 * @param value
	 *            is the amount added
	 */
	public void increaseFrequency(int stackHeight, int value);

	/**
	 * This method deletes the heightTH element of stack.
	 */
	public void popFrequency(int height);

	/**
	 * Returns the frequency stored on height of stack.
	 */
	public int getFrequency(int height);
}
