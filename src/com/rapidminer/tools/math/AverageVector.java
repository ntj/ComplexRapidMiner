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
package com.rapidminer.tools.math;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.rapidminer.gui.tools.ExtendedJScrollPane;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.ResultObjectAdapter;
import com.rapidminer.tools.Tools;


/**
 * Handles several averagables.
 * 
 * @author Ingo Mierswa
 * @version $Id: AverageVector.java,v 1.5 2008/05/09 19:23:03 ingomierswa Exp $
 */
public abstract class AverageVector extends ResultObjectAdapter implements Comparable, Cloneable {

	private List<Averagable> averagesList = new ArrayList<Averagable>();

	public abstract Object clone() throws CloneNotSupportedException;

	public boolean equals(Object o) {
		if (!(o instanceof AverageVector))
			return false;
		AverageVector	v = (AverageVector)o;
		return averagesList.equals(v.averagesList);
	}

	public int hashCode() {
		return this.averagesList.hashCode();
	}

	/** Returns the number of averages in the list. */
	public int size() {
		return averagesList.size();
	}

	/** Adds an {@link Averagable} to the list of criteria. */
	public void addAveragable(Averagable avg) {
		averagesList.add(avg);
	}

	/** Removes an {@link Averagable} from the list of criteria. */
	public void removeAveragable(Averagable avg) {
		averagesList.remove(avg);
	}

	/** Returns the Averagable by index. */
	public Averagable getAveragable(int index) {
		return averagesList.get(index);
	}

	/** Returns the Averagable by name. */
	public Averagable getAveragable(String name) {
		Iterator<Averagable> i = averagesList.iterator();
		while (i.hasNext()) {
			Averagable a = i.next();
			if (a.getName().equals(name))
				return a;
		}
		return null;
	}

	/** Returns the number of averagables in this vector. */
	public int getSize() {
		return averagesList.size();
	}

	public String toResultString() {
		StringBuffer result = new StringBuffer(getName());
		result.append(":");
		result.append(Tools.getLineSeparator());
		Iterator<Averagable> i = averagesList.iterator();
		while (i.hasNext()) {
			result.append(i.next().toResultString());
			result.append(Tools.getLineSeparator());
		}
		return result.toString();
	}

	public String toString() {
		StringBuffer result = new StringBuffer("AverageVector [");
		for (int i = 0; i < size(); i++) {
			Averagable avg = getAveragable(i);
			if (i > 0)
				result.append(", ");
			result.append(avg);
		}
		result.append("]");
		return result.toString();
	}

	public Component getVisualizationComponent(IOContainer container) {
		JPanel mainPanel = new JPanel();
		GridBagLayout layout = new GridBagLayout();
		mainPanel.setLayout(layout);
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.insets = new Insets(11,11,11,11);
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weightx = 1.0d;
		c.weighty = 0.0d;
		
		JLabel mainLabel = new JLabel("<html><h2>" + getName() + " (" + size() + ")</h2></html>");
		layout.setConstraints(mainLabel, c);
		mainPanel.add(mainLabel);
		
		for (int i = 0; i < size(); i++) {
			Averagable avg = getAveragable(i);
			Component visualizationComponent = avg.getVisualizationComponent(container); 
			layout.setConstraints(visualizationComponent, c);
			mainPanel.add(visualizationComponent);

		}
		return new ExtendedJScrollPane(mainPanel);
	}
    
	public void buildAverages(AverageVector av) {
		if (this.size() != av.size())
			throw new IllegalArgumentException("Performance vectors have different size!");
		for (int i = 0; i < size(); i++)
			this.getAveragable(i).buildAverage(av.getAveragable(i));
	}
}
