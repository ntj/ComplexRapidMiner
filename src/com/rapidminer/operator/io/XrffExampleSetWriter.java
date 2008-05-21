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
import com.rapidminer.example.AttributeRole;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeFile;
import com.rapidminer.tools.Tools;


/**
 * <p>Writes values of all examples into an XRFF file which can be used
 * by the machine learning library Weka. The XRFF format is described in the 
 * {@link XrffExampleSource} operator which is able to read XRFF files to
 * make them usable with RapidMiner.</p>
 * 
 * <p>Please note that writing attribute weights is not supported, please use
 * the other RapidMiner operators for attribute weight loading and writing for this
 * purpose.</p>
 * 
 * @rapidminer.index xrff
 * @author Ingo Mierswa
 * @version $Id: XrffExampleSetWriter.java,v 1.6 2008/05/09 19:22:37 ingomierswa Exp $
 */
public class XrffExampleSetWriter extends Operator {

	/** The parameter name for &quot;File to save the example set to.&quot; */
	public static final String PARAMETER_EXAMPLE_SET_FILE = "example_set_file";

	/** The parameter name for &quot;Indicates if the data file should be compressed.&quot; */
	public static final String PARAMETER_COMPRESS = "compress";
	
	private static final Class[] INPUT_CLASSES = { ExampleSet.class };

	private static final Class[] OUTPUT_CLASSES = { ExampleSet.class };

	public XrffExampleSetWriter(OperatorDescription description) {
		super(description);
	}

	public IOObject[] apply() throws OperatorException {
		ExampleSet exampleSet = getInput(ExampleSet.class);
		try {
			File xrffFile = getParameterAsFile(PARAMETER_EXAMPLE_SET_FILE);
			PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(xrffFile), getEncoding()));
			out.println("<?xml version=\"1.0\" encoding=\"" + getEncoding() + "\"?>");
            out.println("<dataset name=\"RapidMinerData\" version=\"3.5.4\">");
            
            out.println("  <header>");
            out.println("    <attributes>");
            
            Iterator<AttributeRole> a = exampleSet.getAttributes().allAttributeRoles();
            while (a.hasNext()) {
            	AttributeRole role = a.next();
                // ignore weight attribute in order to use instance weights directly later
                if ((role.getSpecialName() != null) && (role.getSpecialName().equals(Attributes.WEIGHT_NAME)))
                    continue;
                Attribute attribute = role.getAttribute();
                boolean label = (role.getSpecialName() != null) && (role.getSpecialName().equals(Attributes.LABEL_NAME));
                printAttribute(attribute, out, label);
            }
            out.println("    </attributes>");
            out.println("  </header>");

            out.println("  <body>");
            out.println("    <instances>");
            
            Attribute weightAttribute = exampleSet.getAttributes().getWeight();
            for (Example example : exampleSet) {
                String weightString = "";
                if (weightAttribute != null) {
                    weightString = " weight=\""+example.getValue(weightAttribute)+"\"";
                }
                out.println("      <instance"+weightString+">"); 
                a = exampleSet.getAttributes().allAttributeRoles();
                while (a.hasNext()) {
                	AttributeRole role = a.next();
                    // ignore weight attribute in order to use instance weights directly later
                	if ((role.getSpecialName() != null) && (role.getSpecialName().equals(Attributes.WEIGHT_NAME)))
                        continue;
                    Attribute attribute = role.getAttribute();
                    out.println("        <value>" + Tools.escapeXML(example.getValueAsString(attribute)) + "</value>");
                }  
                out.println("      </instance>");
            }
            
            out.println("    </instances>");
            out.println("  </body>");
            out.println("</dataset>");
			out.close();
		} catch (IOException e) {
			throw new UserError(this, e, 303, new Object[] { getParameterAsString(PARAMETER_EXAMPLE_SET_FILE), e.getMessage() });
		}
		return new IOObject[] { exampleSet };
	}

    private void printAttribute(Attribute attribute, PrintWriter out, boolean isClass) {
        String classString = isClass ? "class=\"yes\" " : "";
        if (attribute.isNominal()) {
            out.println("      <attribute name=\"" + Tools.escapeXML(attribute.getName()) + "\" " + classString + "type=\"nominal\">");
            out.println("        <labels>");
            for (String s : attribute.getMapping().getValues()) {
                out.println("          <label>" + Tools.escapeXML(s) + "</label>");
            }
            out.println("        </labels>");
            out.println("      </attribute>");
        } else {
            out.println("      <attribute name=\"" + Tools.escapeXML(attribute.getName()) + "\" " + classString + "type=\"numeric\"/>");
        }
    }
    
	public Class[] getInputClasses() {
		return INPUT_CLASSES;
	}

	public Class[] getOutputClasses() {
		return OUTPUT_CLASSES;
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		types.add(new ParameterTypeFile(PARAMETER_EXAMPLE_SET_FILE, "File to save the example set to.", "xrff", false));
        //types.add(new ParameterTypeBoolean(PARAMETER_COMPRESS, "Indicates if the data file should be compressed.", false));
		return types;
	}
}
