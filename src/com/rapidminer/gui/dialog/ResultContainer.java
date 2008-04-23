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
package com.rapidminer.gui.dialog;

import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.MissingIOObjectException;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.performance.PerformanceVector;

/**
 * This container class contains an operator tree and the results delivered by the tree.
 * It will be used by the class {@link ResultHistory}.
 * 
 * @author Ingo Mierswa
 * @version $Id: ResultContainer.java,v 1.1 2007/07/15 22:06:25 ingomierswa Exp $
 */
public class ResultContainer {

	private String name;
	private Operator root;
	private String resultString;
	private PerformanceVector performanceVector;
	
	public ResultContainer(String name, Operator root, IOContainer ioContainer) {
		this.name = name;
		this.root = root;
		this.resultString = ioContainer.toString();
		try {
			performanceVector = ioContainer.get(PerformanceVector.class);
		} catch (MissingIOObjectException e) {
           // tries to find a performance. Ok if this does not work
		}
	}
	
	public String getName() {
		return this.name;
	}
	
	public Operator getRootOperator() {
		return this.root;
	}
	
	public String getResults() {
		return this.resultString;
	}
	
	public PerformanceVector getPerformance() {
		return this.performanceVector;
	}
	
	public String toString() {
		return this.name;
	}
}
