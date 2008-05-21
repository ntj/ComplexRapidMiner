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
package com.rapidminer.gui.dialog;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.set.AttributeWeightedExampleSet;
import com.rapidminer.operator.features.Individual;
import com.rapidminer.operator.features.Population;
import com.rapidminer.operator.performance.PerformanceCriterion;
import com.rapidminer.operator.performance.PerformanceVector;

/**
 * The table model for the individual selector dialog.
 * 
 * @author Ingo Mierswa
 * @version $Id: IndividualSelectorTableModel.java,v 1.2 2008/05/09 19:23:21 ingomierswa Exp $
 */
public class IndividualSelectorTableModel extends AbstractTableModel {
	
	private static final long serialVersionUID = -4666469076881936719L;

	private Population population;
	
	private List<String> columnNames = new ArrayList<String>();
	
	private int columnOffset = 3;
	
	public IndividualSelectorTableModel(Population population) {
		this.population = population;
		if (population.getNumberOfIndividuals() > 0) {
			columnNames.add("Index");
			columnNames.add("Features");
			columnNames.add("Names");
			
			Individual individual = population.get(0);
			PerformanceVector performanceVector = individual.getPerformance();
			for (int i = 0; i < performanceVector.getSize(); i++) {
				PerformanceCriterion criterion = performanceVector.getCriterion(i);
				columnNames.add(criterion.getName());
			}
		}
	}
	
	public Class<?> getColumnClass(int c) {
		switch (c) {
		case 0:
			return Integer.class;
		case 1:
			return Integer.class;
		case 2:
			return String.class;
		default: 
			return Double.class;
		}
	}
	
	public String getColumnName(int c) {
		return columnNames.get(c);
	}
	
	public int getColumnCount() {
		return columnNames.size();
	}

	public int getRowCount() {
		return population.getNumberOfIndividuals();
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		switch (columnIndex) {
		case 0:
			return (rowIndex + 1);
		case 1:
			Individual individual = population.get(rowIndex);
			return individual.getExampleSet().getNumberOfUsedAttributes();
		case 2:
			individual = population.get(rowIndex);
			StringBuffer names = new StringBuffer();
			boolean first = true;
			AttributeWeightedExampleSet exampleSet = individual.getExampleSet();
			for (Attribute attribute : exampleSet.getAttributes()) {
				if (exampleSet.isAttributeUsed(attribute)) {
					if (!first) {
						names.append(", ");
					}
					names.append(attribute.getName());
					first = false;
				}
			}
			return names.toString();
		default:
			int perfIndex = columnIndex - columnOffset;
			individual = population.get(rowIndex);
			PerformanceCriterion criterion = individual.getPerformance().getCriterion(perfIndex);
			return criterion.getAverage();
		}
	}
}
