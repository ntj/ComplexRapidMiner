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

import java.util.Iterator;
import java.util.List;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeFile;


/**
 * <p>This operator can read csv files. All values must be separated by
 * &quot;,&quot;, by &quot;;&quot;, or by white space like tabs. 
 * The first line is used for attribute names as default.</p> 
 * 
 * <p>For other file formats or column separators you can
 * use in almost all cases the operator {@link SimpleExampleSource}
 * or, if this is not sufficient, the operator {@link ExampleSource}.</p>
 * 
 * @rapidminer.index csv
 * @author Ingo Mierswa
 * @version $Id: CSVExampleSource.java,v 1.2 2007/06/15 16:58:37 ingomierswa Exp $
 */
public class CSVExampleSource extends SimpleExampleSource {

	public CSVExampleSource(OperatorDescription description) {
		super(description);
	}
    
	public Class[] getInputClasses() {
		return new Class[0];
	}

	public Class[] getOutputClasses() {
		return new Class[] { ExampleSet.class };
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		Iterator<ParameterType> p = types.iterator();
		while (p.hasNext()) {
			ParameterType type = p.next();
			if (type.getKey().equals(PARAMETER_READ_ATTRIBUTE_NAMES)) {
				type.setDefaultValue(true);
			} else if (type.getKey().equals(PARAMETER_FILENAME)) {
				((ParameterTypeFile)type).setExtension("csv");
			} else if (type.getKey().equals(PARAMETER_USE_QUOTES)) {
				type.setDefaultValue(true);
			} else if (type.getKey().equals(PARAMETER_COLUMN_SEPARATORS)) {
				type.setDefaultValue(",\\s*|;\\s*");
			}
		}
		return types;
	}
}
