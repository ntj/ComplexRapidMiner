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
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import com.rapidminer.RapidMiner;
import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeFile;


/**
 * Writes all attributes of an example set to a file. Each line holds the
 * construction description of one attribute. This file can be read in another
 * process using the
 * {@link com.rapidminer.operator.features.construction.FeatureGenerationOperator} or
 * {@link AttributeConstructionsLoader}.
 * 
 * @author Ingo Mierswa
 * @version $Id: AttributeConstructionsWriter.java,v 1.11 2006/04/05 08:57:25 ingomierswa
 *          Exp $
 */
public class AttributeConstructionsWriter extends Operator {


	/** The parameter name for &quot;Filename for the attribute construction description file.&quot; */
	public static final String PARAMETER_ATTRIBUTE_CONSTRUCTIONS_FILE = "attribute_constructions_file";
	private static final Class[] INPUT_CLASSES = { ExampleSet.class };

	private static final Class[] OUTPUT_CLASSES = { ExampleSet.class };

	public AttributeConstructionsWriter(OperatorDescription description) {
		super(description);
	}

	/** Writes the attribute set to a file. */
	public IOObject[] apply() throws OperatorException {
		File generatorFile = getParameterAsFile(PARAMETER_ATTRIBUTE_CONSTRUCTIONS_FILE);

		ExampleSet eSet = getInput(ExampleSet.class);
        PrintWriter out = null;
		try {
		    out = new PrintWriter(new FileWriter(generatorFile));
            out.println("<?xml version=\"1.0\" encoding=\"" + getEncoding() + "\"?>");
            out.println("<constructions version=\"" + RapidMiner.getVersion() + "\">");
            for (Attribute attribute : eSet.getAttributes()) {
				out.println("    <attribute name=\"" + attribute.getName() + "\" construction=\"" + attribute.getConstruction().getDescription(false) + "\"/>");
			}
            out.println("</constructions>");
		} catch (IOException e) {
			throw new UserError(this, e, 303, new Object[] { generatorFile, e.getMessage() });
		} finally {
		    if (out != null)
                out.close();
        }

		return new IOObject[] { eSet };
	}

	public Class[] getInputClasses() {
		return INPUT_CLASSES;
	}

	public Class[] getOutputClasses() {
		return OUTPUT_CLASSES;
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		types.add(new ParameterTypeFile(PARAMETER_ATTRIBUTE_CONSTRUCTIONS_FILE, "Filename for the attribute construction description file.", "att", false));
		return types;
	}

}
