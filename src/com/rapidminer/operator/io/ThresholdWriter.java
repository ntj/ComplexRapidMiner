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
package com.rapidminer.operator.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.List;

import com.rapidminer.RapidMiner;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.postprocessing.Threshold;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeFile;


/**
 * Writes the given threshold into a file. The first line holds the threshold,
 * the second the value of the first class, and the second the value of the
 * second class. This file can be read in another process using the
 * {@link ThresholdLoader}.
 * 
 * @author Ingo Mierswa
 * @version $Id: ThresholdWriter.java,v 1.4 2007/06/23 00:09:30 ingomierswa Exp $
 */
public class ThresholdWriter extends Operator {


	/** The parameter name for &quot;Filename for the threshold file.&quot; */
	public static final String PARAMETER_THRESHOLD_FILE = "threshold_file";
	private static final Class[] INPUT_CLASSES = { Threshold.class };

	private static final Class[] OUTPUT_CLASSES = { Threshold.class };

	public ThresholdWriter(OperatorDescription description) {
		super(description);
	}

	/** Writes the threshold to a file. */
	public IOObject[] apply() throws OperatorException {
		File thresholdFile = getParameterAsFile(PARAMETER_THRESHOLD_FILE);
		Threshold threshold = getInput(Threshold.class);
        PrintWriter out = null;
		try {
			out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(thresholdFile), getEncoding()));
            out.println("<?xml version=\"1.0\" encoding=\"" + getEncoding() + "\"?>");
            out.println("<threshold version=\"" + RapidMiner.getVersion() + "\" value=\"" +
                    threshold.getThreshold() + "\" first=\"" + threshold.getZeroClass() + 
                    "\" second=\"" + threshold.getOneClass() + "\"/>");
			out.close();
		} catch (IOException e) {
			throw new UserError(this, e, 303, new Object[] { thresholdFile, e.getMessage() });
		} finally {
			if (out != null)
				out.close();
        }

		return new IOObject[] { threshold };
	}

	public Class[] getInputClasses() {
		return INPUT_CLASSES;
	}

	public Class[] getOutputClasses() {
		return OUTPUT_CLASSES;
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		types.add(new ParameterTypeFile(PARAMETER_THRESHOLD_FILE, "Filename for the threshold file.", "thr", false));
		return types;
	}
}
