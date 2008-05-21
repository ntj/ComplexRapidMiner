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
package com.rapidminer.operator.performance;

import java.awt.Component;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;

import com.rapidminer.gui.tools.SwingTools;
import com.rapidminer.gui.viewer.PerformanceVectorViewer;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.tools.LogService;
import com.rapidminer.tools.Tableable;
import com.rapidminer.tools.Tools;
import com.rapidminer.tools.math.Averagable;
import com.rapidminer.tools.math.AverageVector;


/**
 * Handles several performance criteria. It is possible to obtain more than one
 * criterion and therefore they are added to a criteria list.
 * 
 * @author Ingo Mierswa, Simon Fischer
 * @version $Id: PerformanceVector.java,v 2.29 2006/03/21 15:35:51 ingomierswa
 *          Exp $
 */
public class PerformanceVector extends AverageVector implements Tableable {
	
	private static final long serialVersionUID = 3123587140049371098L;

	public static final String MAIN_CRITERION_FIRST = "first";
	
	private static final String RESULT_ICON_NAME = "percent.png";
	
	private static Icon resultIcon = null;
	
	static {
		resultIcon = SwingTools.createIcon("16/" + RESULT_ICON_NAME);
	}
	
	/**
	 * The default performance comparator compares the main criterion of two
	 * performance vectors. In case that the minimum description length (mdl)
	 * criterion is also calculated and have a weight &gt; 0 the weighted sums
	 * of the main and the mdl criterion are compared.
	 */
	public static class DefaultComparator implements PerformanceComparator {

		private static final long serialVersionUID = 8632060851821885142L;

		public DefaultComparator() {}

		public int compare(PerformanceVector av1, PerformanceVector av2) {
			return av1.getMainCriterion().compareTo(av2.getMainCriterion());
		}
	}

	/** This value map will only be intialized before writing this vector to a file. This 
	 *  allows a quick human readable format in the resulting file. */
	private Map<String, Double> currentValues = null;
	
	/** Used to compare two average vectors. */
	private PerformanceComparator comparator = new DefaultComparator();

	/** Name of the main criterion. */
	private String mainCriterion = null;
	
	public void setComparator(PerformanceComparator comparator) {
		this.comparator = comparator;
	}

	public void addCriterion(PerformanceCriterion crit) {
		PerformanceCriterion pc = getCriterion(crit.getName());
		if (pc != null) {
			removeAveragable(pc);
			LogService.getGlobal().log("Performance criterion '" + crit.getName() + "' was already part of performance vector. Overwritten...", LogService.WARNING);
		}
		addAveragable(crit);
	}

	public PerformanceCriterion getCriterion(int index) {
		return (PerformanceCriterion) getAveragable(index);
	}

	public PerformanceCriterion getCriterion(String name) {
		return (PerformanceCriterion) getAveragable(name);
	}

	public String[] getCriteriaNames() {
		String[] criteriaNames = new String[getSize()];
		for (int i = 0; i < criteriaNames.length; i++) {
			criteriaNames[i] = getCriterion(i).getName();
		}
		return criteriaNames;
	}

	/**
	 * Sets the name of the main average (must be added by
	 * {@link #addAveragable(Averagable)})
	 */
	public void setMainCriterionName(String mcName) {
		if ((!mcName.equals(MAIN_CRITERION_FIRST)) && (getAveragable(mcName) == null)) {
			LogService.getGlobal().log("Main criterion not found: '" + mcName + "'.", LogService.ERROR);
		}
		this.mainCriterion = mcName;
	}

	/**
	 * Returns the main {@link PerformanceCriterion}. If the main criterion is
	 * not specified by {@link #setMainCriterionName(String)}, the first
	 * criterion is returned.
	 */
	public PerformanceCriterion getMainCriterion() {
		if (mainCriterion == null) {
			return (PerformanceCriterion) getAveragable(0);
		} else {
			PerformanceCriterion pc = (PerformanceCriterion) getAveragable(mainCriterion);
			if (pc == null)
				return (PerformanceCriterion) getAveragable(0);
			return pc;
		}
	}

	/** Returns a negative value iff o is better than this performance vector */
	public int compareTo(Object o) {
		double result = comparator.compare(this, (PerformanceVector) o);
		if (result < 0.0)
			return -1;
		else if (result > 0.0)
			return +1;
		else
			return 0;
	}
	
	public Object clone() throws CloneNotSupportedException {
		PerformanceVector av = new PerformanceVector();
		for (int i = 0; i < size(); i++) {
			Averagable avg = getAveragable(i);
			av.addAveragable((Averagable) (avg).clone());
		}
		return av;
	}

	public Component getVisualizationComponent(IOContainer container) {
		return new PerformanceVectorViewer(this, container);
	}
	
	public Icon getResultIcon() {
		return resultIcon;
	}
	
	public String toString() {
		StringBuffer result = new StringBuffer(Tools.getLineSeparator() + "PerformanceVector [");
		for (int i = 0; i < size(); i++) {
			Averagable avg = getAveragable(i);
			if ((mainCriterion != null) && (avg.getName().equals(mainCriterion))) {
				result.append(Tools.getLineSeparator() + "*****");
			} else {
				result.append(Tools.getLineSeparator() + "-----");
			}
			result.append(avg);
		}
		result.append(Tools.getLineSeparator() + "]");
		return result.toString();
	}
    
    public String getExtension() {
        return "per";
    }
    
    public String getFileDescription() {
        return "performance vector file";
    }
    
    public void save(File file) throws IOException {
    	FileOutputStream out = null;
    	try {
    		out = new FileOutputStream(file);
    		super.write(out);
    	} catch (IOException e) {
    		throw e;
    	} finally {
    		if (out != null)
    			out.close();
    	}
    }
    
    /** Init the value map which ensures an easy human readable format. */
    public void initWriting() {
    	this.currentValues = new HashMap<String, Double>();
    	for (int i = 0; i < size(); i++) {
    		Averagable averagable = getAveragable(i);
    		this.currentValues.put(averagable.getName(), averagable.getAverage());
    	}
    }

	public String getCell(int row, int column) {
		// TODO Auto-generated method stub
		return null;
	}

	public int getColumnNumber() {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getRowNumber() {
		// TODO Auto-generated method stub
		return 0;
	}
}
