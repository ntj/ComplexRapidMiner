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
package com.rapidminer.operator.learner.clustering.constrained.constraints;

import java.util.ArrayList;
import java.util.Iterator;

import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.ResultObjectAdapter;
import com.rapidminer.operator.learner.clustering.ClusterModel;
import com.rapidminer.tools.Tools;

/**
 * This is a data structure that keeps cluster constraints and offers checking
 * these constraints for violations.
 * 
 * @author Alexander Daxenberger
 * @version $Id: ClusterConstraintList.java,v 1.7 2008/05/09 19:23:17 ingomierswa Exp $
 */
public class ClusterConstraintList extends ResultObjectAdapter {

	private static final long serialVersionUID = -6119240785166619598L;

	protected String name;

	protected ArrayList<ClusterConstraint> constraintList;

	public ClusterConstraintList(String name) {
		this(name, 200);
	}

	public ClusterConstraintList(String name, int initialCapacity) {
		this.name = name;
		this.constraintList = new ArrayList<ClusterConstraint>(initialCapacity);
	}

	public ArrayList getConstraints() {
		return this.constraintList;
	}

	/**
	 * Returns a list of all violated constraints for the given ClusterModel.
	 * 
	 * @param cm
	 */
	public ArrayList<ClusterConstraint> getViolatedConstraints(ClusterModel cm) {
		ArrayList<ClusterConstraint> list = new ArrayList<ClusterConstraint>(
				this.constraintList.size());
		ClusterConstraint c;

		for (int i = 0; i < this.constraintList.size(); i++) {
			c = this.constraintList.get(i);
			if (c.constraintViolated(cm))
				list.add(c);
		}

		return list;
	}

	/**
	 * Returns the number of violated constraints for the given ClusterModel.
	 * 
	 * @param cm
	 */
	public int numberOfViolatedConstraints(ClusterModel cm) {
		ClusterConstraint c;
		int n = 0;

		for (int i = 0; i < this.constraintList.size(); i++) {
			c = this.constraintList.get(i);
			if (c.constraintViolated(cm))
				n++;
		}

		return n;
	}

	/**
	 * Returns the sum of the weights of violated constraints for the given
	 * ClusterModel.
	 * 
	 * @param cm
	 */
	public double weightOfViolatedConstraints(ClusterModel cm) {
		ClusterConstraint c;
		double w = 0.0;

		for (int i = 0; i < this.constraintList.size(); i++) {
			c = this.constraintList.get(i);
			if (c.constraintViolated(cm))
				w += c.getConstraintWeight(cm);
		}

		return w;
	}

	public IOObject copy() {
		ClusterConstraintList ccl = new ClusterConstraintList(this.name);
		for (int i = 0; i < this.constraintList.size(); i++) {
			ccl.addConstraint(this.constraintList.get(i).clone());
		}
		return ccl;
	}

	public boolean addConstraint(ClusterConstraint cc) {
		ClusterConstraint c;

		c = this.findEqualConstraint(cc);
		if (c == null)
			return this.constraintList.add(cc);
		else
			return false;
	}

	public ClusterConstraint removeConstraint(ClusterConstraint cc) {
		ClusterConstraint c;

		c = this.findEqualConstraint(cc);
		if (c != null)
			return c;
		else
			return null;
	}

	public boolean containsConstraint(ClusterConstraint cc) {
		ClusterConstraint c;

		c = this.findEqualConstraint(cc);
		if (c != null)
			return true;
		else
			return false;
	}

	public int numberOfConstraints() {
		return this.constraintList.size();
	}

	public Iterator getConstraintIterator() {
		return this.constraintList.iterator();
	}

	public String getName() {
		return this.name;
	}

	public String toResultString() {
		StringBuffer sb = new StringBuffer();
		sb.append(this.name);
		sb.append(":" + Tools.getLineSeparator());
		for (int i = 0; i < this.constraintList.size(); i++) {
			if (i > 0)
				sb.append("," + Tools.getLineSeparator());
			sb.append(this.constraintList.get(i).toString());
		}
		return sb.toString();
	}

	/**
	 * Returns the first constraint, that is equal to 'cc' or null.
	 * 
	 * @param cc
	 */
	private ClusterConstraint findEqualConstraint(ClusterConstraint cc) {
		for (int i = 0; i < this.constraintList.size(); i++) {
			if (this.constraintList.get(i).equals(cc))
				return this.constraintList.get(i);
		}
		return null;
	}

	public String getExtension() {
	    return "ccl";
	}

	public String getFileDescription() {
		return "cluster constraint list";
	}
}
