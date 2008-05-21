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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeFile;


/**
 * Writes values of all examples into an ARFF file which can be used
 * by the machine learning library Weka. The ARFF format is described in the 
 * {@link ArffExampleSource} operator which is able to read ARFF files to
 * make them usable with RapidMiner.  
 * 
 * @rapidminer.index arff
 * @author Ingo Mierswa
 * @version $Id: ArffExampleSetWriter.java,v 1.10 2006/04/05 09:42:01
 *          ingomierswa Exp $
 */
public class ArffExampleSetWriter extends Operator {

	/** The parameter name for &quot;File to save the example set to.&quot; */
	public static final String PARAMETER_EXAMPLE_SET_FILE = "example_set_file";
	
	private static final Class[] INPUT_CLASSES = { ExampleSet.class };

	private static final Class[] OUTPUT_CLASSES = { ExampleSet.class };

	public ArffExampleSetWriter(OperatorDescription description) {
		super(description);
	}

	public IOObject[] apply() throws OperatorException {
		ExampleSet exampleSet = getInput(ExampleSet.class);
		try {
			File arffFile = getParameterAsFile(PARAMETER_EXAMPLE_SET_FILE);
			PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(arffFile), getEncoding()));
			
            // relation
            out.println("@RELATION RapidMinerData");
            out.println();
            
            // attribute meta data
            Iterator<Attribute> a = exampleSet.getAttributes().allAttributes();
            while (a.hasNext()) {
                printAttributeData(a.next(), out);
            }
            
            // data
            out.println();
            out.println("@DATA");
            
            for (Example example : exampleSet) {
                boolean first = true;
                a = exampleSet.getAttributes().allAttributes();
                while (a.hasNext()) {
                    Attribute current = a.next();
                    if (!first) 
                        out.print(",");
                    
                    if (current.isNominal()) {
                    	double value = example.getValue(current);
                    	if (Double.isNaN(value))
                    		out.print("?");
                    	else
                    		out.print("'" + example.getValueAsString(current) + "'");
                    } else {
                        out.print(example.getValueAsString(current));
                    }
                    first = false;
                }                
                out.println();
            }  
			out.close();
		} catch (IOException e) {
			throw new UserError(this, e, 303, new Object[] { getParameterAsString(PARAMETER_EXAMPLE_SET_FILE), e.getMessage() });
		}
		return new IOObject[] { exampleSet };
	}

    private void printAttributeData(Attribute attribute, PrintWriter out) {
        out.print("@ATTRIBUTE '" + attribute.getName() + "' ");
        if (attribute.isNominal()) {
            StringBuffer nominalValues = new StringBuffer("{");
            boolean first = true;
            for (String s : attribute.getMapping().getValues()) {
                if (!first)
                    nominalValues.append(",");
                nominalValues.append("'" + s + "'");
                first = false;
            }
            nominalValues.append("}");
            out.print(nominalValues.toString());
        } else {
            out.print("real");
        }
        out.println();
    }
    
	public Class[] getInputClasses() {
		return INPUT_CLASSES;
	}

	public Class[] getOutputClasses() {
		return OUTPUT_CLASSES;
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		types.add(new ParameterTypeFile(PARAMETER_EXAMPLE_SET_FILE, "File to save the example set to.", "arff", false));
		return types;
	}
}
