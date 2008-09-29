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
 * @version $Id: CSVExampleSource.java,v 1.5 2008/07/07 07:06:38 ingomierswa Exp $
 */
public class CSVExampleSource extends SimpleExampleSource {

	public CSVExampleSource(OperatorDescription description) {
		super(description);
	}
    
	public Class<?>[] getInputClasses() {
		return new Class[0];
	}

	public Class<?>[] getOutputClasses() {
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
