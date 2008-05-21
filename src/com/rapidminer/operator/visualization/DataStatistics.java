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
package com.rapidminer.operator.visualization;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.Statistics;
import com.rapidminer.operator.ResultObjectAdapter;
import com.rapidminer.tools.Tools;


/**
 * This class encapsulates some very simple statistics about the given
 * attributes. These are the ranges of the attributes and the average or mode
 * values for numerical or nominal attributes respectively. This information
 * is automatically calculated and displayed by the graphical user interface of
 * RapidMiner. Since they cannot be displayed with the command line version of RapidMiner
 * the operator {@link DataStatisticsOperator} can be used as a workaround in
 * cases where the graphical user interface cannot be used.
 * 
 * @author Ingo Mierswa
 * @version $Id: DataStatistics.java,v 1.5 2008/05/09 19:23:14 ingomierswa Exp $
 */
public class DataStatistics extends ResultObjectAdapter {

	private static final long serialVersionUID = -9182815345498602690L;
	
	private List<String> statistics = new LinkedList<String>();

	public String getName() {
		return "Data Statistics";
	}

	public void addInfo(ExampleSet exampleSet, Attribute attribute) {
        StringBuffer result = new StringBuffer(attribute.toString() + ": ");
        if (attribute.isNominal()) {
            result.append("mode = " + exampleSet.getStatistics(attribute, Statistics.MODE));
        } else {
            result.append("avg = " + exampleSet.getStatistics(attribute, Statistics.AVERAGE) + " +/- " + Math.sqrt(exampleSet.getStatistics(attribute, Statistics.VARIANCE)));
        }
        result.append("; unknown = " + exampleSet.getStatistics(attribute, Statistics.UNKNOWN));
		statistics.add(result.toString());
	}

	public String toString() {
		StringBuffer result = new StringBuffer();
		Iterator i = statistics.iterator();
		while (i.hasNext()) {
			result.append((String) i.next() + Tools.getLineSeparator());
		}
		return result.toString();
	}
    
    public String getExtension() { return "sta"; }
    
    public String getFileDescription() { return "data statistics"; }
}
