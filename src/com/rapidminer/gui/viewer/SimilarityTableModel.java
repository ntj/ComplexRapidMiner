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
package com.rapidminer.gui.viewer;

import java.util.Arrays;
import java.util.Iterator;

import javax.swing.table.AbstractTableModel;

import com.rapidminer.operator.similarity.SimilarityMeasure;

/**
 * The table model for the similarity visualization.
 *
 * @author Ingo Mierswa
 * @version $Id: SimilarityTableModel.java,v 1.5 2008/05/09 19:23:01 ingomierswa Exp $
 */
public class SimilarityTableModel extends AbstractTableModel {

	private static final long serialVersionUID = 7451178433975831387L;

	private static final int COLUMN_FIRST      = 0;
	private static final int COLUMN_SECOND     = 1;
	private static final int COLUMN_SIMILARITY = 2;
	
	private SimilarityMeasure similarity;
	
	private String[] ids;
	
	public SimilarityTableModel(SimilarityMeasure similarity) {
		this.similarity = similarity;
		this.ids = new String[similarity.getNumberOfIds()];
		int counter = 0;
		Iterator<String> i = this.similarity.getIds();
		while (i.hasNext()) {
			ids[counter++] = i.next();
		}
		
		Arrays.sort(ids);
	}
	
	public String getColumnName(int column) {
		switch(column) {
		case 0: 
			return "First";
		case 1: 
			return "Second";
		case 2: 
			if (similarity.isDistance()) {
				return "Distance";
			}else {
				return "Similarity";
			}
		}
		return "";
	}

	public Class<?> getColumnClass(int column) {
		if (column == COLUMN_SIMILARITY) {
			return Double.class;
		} else {
			return String.class;
		}
	}
	
	public int getColumnCount() {
		return 3;
	}

	public int getRowCount() {
		int n = ids.length - 1;
		return (n * (n + 1)) / 2;
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		int[] actualRows = getActualRows(rowIndex);
		String first  = ids[actualRows[0]];
		String second = ids[actualRows[1]];
		switch (columnIndex) {
		case COLUMN_FIRST:
			return first;
		case COLUMN_SECOND:
			return second;
		case COLUMN_SIMILARITY:
			return Double.valueOf(this.similarity.similarity(first, second));
		default:
			// cannot happen
			return "?";
		}
	}

	private int[] getActualRows(int rowIndex) {
		int sum = 0;
		int currentLength = ids.length - 1;
		int result = 0;
		while ((sum + currentLength) <= rowIndex) {
			sum += currentLength;
			currentLength--;
			result++;
		}
		return new int[] { result, ids.length - (sum + currentLength - rowIndex) };
	}
}
