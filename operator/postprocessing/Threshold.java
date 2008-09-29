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
package com.rapidminer.operator.postprocessing;

import com.rapidminer.operator.ResultObjectAdapter;
import com.rapidminer.tools.Tools;


/**
 * A threshold for soft2crisp classifying.
 * 
 * @author Ingo Mierswa, Martin Scholz
 * @version $Id: Threshold.java,v 1.4 2008/05/09 19:23:27 ingomierswa Exp $
 */
public class Threshold extends ResultObjectAdapter {

	private static final long serialVersionUID = -5929425242781926136L;

	/** The threshold. */
	private double threshold;

	/** The first class. */
	private String zeroClass;

	/** The second class. */
	private String oneClass;

	public Threshold(double threshold, String zeroClass, String oneClass) {
		this.threshold = threshold;
		this.zeroClass = zeroClass;
		this.oneClass = oneClass;
	}

	public double getThreshold() {
		return this.threshold;
	}

	public String getZeroClass() {
		return zeroClass;
	}

	public String getOneClass() {
		return oneClass;
	}
    
    public String getExtension() {
        return "thr";
    }
    
    public String getFileDescription() {
        return "threshold file";
    }
    
	public String toString() {
		return "Threshold: " + threshold + Tools.getLineSeparator() + "first class: " + zeroClass + Tools.getLineSeparator() + "second class: " + oneClass;
	}
}
