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
package com.rapidminer.operator.postprocessing;

import com.rapidminer.operator.ResultObjectAdapter;
import com.rapidminer.operator.Saveable;
import com.rapidminer.tools.Tools;


/**
 * A threshold for soft2crisp classifying.
 * 
 * @author Ingo Mierswa, Martin Scholz
 * @version $Id: Threshold.java,v 1.1 2007/05/27 22:02:47 ingomierswa Exp $
 */
public class Threshold extends ResultObjectAdapter implements Saveable {

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
