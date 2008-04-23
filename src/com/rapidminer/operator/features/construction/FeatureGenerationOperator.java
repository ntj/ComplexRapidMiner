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
package com.rapidminer.operator.features.construction;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.AttributeParser;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeFile;
import com.rapidminer.parameter.ParameterTypeList;
import com.rapidminer.parameter.ParameterTypeString;


/**
 * This operator generates new user specified features. The new features are
 * specified by their function names (prefix notation) and their arguments using
 * the names of existing features.<br/> Legal function names include +, -, *, /,
 * norm, sin, cos, tan, atan, exp, log, min, max, floor, ceil, round, sqrt, abs,
 * and pow. Constant values can be defined by &quot;const[value]()&quot; where
 * value is the desired value. Do not forget the empty round brackets. Example:
 * <code>+(a1, *(a2, a3))</code> will calculate the sum of the attribute
 * <code>a1</code> and the product of the attributes <code>a2</code> and
 * <code>a3</code>. <br/> Features are generated in the following order
 * <ol>
 * <li>Features specified by the file referenced by the parameter "filename"
 * are generated</li>
 * <li>Features specified by the parameter list "functions" are generated</li>
 * <li>If "keep_all" is false, all of the old attributes are removed now</li>
 * </ol>
 * 
 * @see com.rapidminer.generator.FeatureGenerator
 * @author Simon Fischer, Ingo Mierswa
 * @version $Id: FeatureGenerationOperator.java,v 1.12 2006/04/05 08:57:27
 *          ingomierswa Exp $
 */
public class FeatureGenerationOperator extends Operator {


	/** The parameter name for &quot;Create the attributes listed in this file (written by an AttributeConstructionsWriter).&quot; */
	public static final String PARAMETER_FILENAME = "filename";

	/** The parameter name for &quot;List of functions to generate.&quot; */
	public static final String PARAMETER_FUNCTIONS = "functions";

	/** The parameter name for &quot;If set to true, all the original attributes are kept, otherwise they are removed from the example set.&quot; */
	public static final String PARAMETER_KEEP_ALL = "keep_all";
	private static final Class[] INPUT_CLASSES = { ExampleSet.class };

	private static final Class[] OUTPUT_CLASSES = { ExampleSet.class };

	public FeatureGenerationOperator(OperatorDescription description) {
		super(description);
	}

	public Class[] getInputClasses() {
		return INPUT_CLASSES;
	}

	public Class[] getOutputClasses() {
		return OUTPUT_CLASSES;
	}

	public IOObject[] apply() throws OperatorException {
		ExampleSet exampleSet = getInput(ExampleSet.class);
		boolean keepAll = getParameterAsBoolean(PARAMETER_KEEP_ALL);

		File file = getParameterAsFile(PARAMETER_FILENAME);
		try {
			AttributeParser parser = new AttributeParser(exampleSet.getExampleTable());

			if (file != null) {
                InputStream in = new FileInputStream(file);
				parser.parseAll(in);
                in.close();
			}

			Iterator j = getParameterList(PARAMETER_FUNCTIONS).iterator();
			while (j.hasNext()) {
				Object[] nameFunctionPair = (Object[]) j.next();
				Attribute attribute = parser.parseAttribute((String) nameFunctionPair[1]);
				attribute.setName((String) nameFunctionPair[0]);
				checkForStop();
			}

			if (!keepAll) {
				exampleSet.getAttributes().clearRegular();
			}
			parser.generateAll(exampleSet);

		} catch (java.io.IOException e) {
			throw new UserError(this, e, 302, new Object[] { file, e.getMessage() });
		}

		return new IOObject[] { exampleSet };
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		ParameterType type = new ParameterTypeFile(PARAMETER_FILENAME, "Create the attributes listed in this file (written by an AttributeConstructionsWriter).", "att", true);
		type.setExpert(false);
		types.add(type);
		type = new ParameterTypeList(PARAMETER_FUNCTIONS, "List of functions to generate.", new ParameterTypeString("function", "Function and arguments to use for generation."));
		type.setExpert(false);
		types.add(type);
		types.add(new ParameterTypeBoolean(PARAMETER_KEEP_ALL, "If set to true, all the original attributes are kept, otherwise they are removed from the example set.", false));
		return types;
	}

}
