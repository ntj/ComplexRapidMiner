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
package com.rapidminer.operator;

import java.util.Iterator;
import java.util.List;

import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeList;
import com.rapidminer.parameter.ParameterTypeString;


/**
 * <p>(Re-)Define macros for the current process. Macros will be replaced in the value strings
 * of parameters by the macro values defined in the parameter list of this operator.
 * Please note that this features is basically only supported for string type parameter
 * values (strings or files) and not for numerical or list types.</p>
 * 
 * <p>In the parameter list of this operator, you have to define the macro name (without the enclosing brackets) and
 * the macro value. The defined macro can then be used in all succeeding operators as parameter
 * value for string type parameters. A macro must then be enclosed by &quot;MACRO_START&quot; and 
 * &quot;MACRO_END&quot;.</p>
 *  
 * <p>There are several predefined macros:</p>
 * <ul>
 * <li>MACRO_STARTprocess_nameMACRO_END: will be replaced by the name of the process (without path and extension)</li>
 * <li>MACRO_STARTprocess_fileMACRO_END: will be replaced by the file name of the process (with extension)</li> 
 * <li>MACRO_STARTprocess_pathMACRO_END: will be replaced by the complete absolute path of the process file</li>
 * </ul>
 * 
 * <p>In addition to those the user might define arbitrary other macros which will be replaced
 * by arbitrary string during the process run. Please note also that several other short macros
 * exist, e.g. MACRO_STARTaMACRO_END for the number of times the current operator was applied.
 * Please refer to the section about macros in the RapidMiner tutorial.</p>
 *  
 * @author Ingo Mierswa
 * @version $Id: MacroDefinitionOperator.java,v 1.3 2007/06/15 16:58:40 ingomierswa Exp $
 */
public class MacroDefinitionOperator extends Operator {


	/** The parameter name for &quot;The values of the user defined macros.&quot; */
	public static final String PARAMETER_VALUES = "values";
	public static final String PARAMETER_MACROS = "macros";
	
	public MacroDefinitionOperator(OperatorDescription description) {
		super(description);
	}

	public IOObject[] apply() throws OperatorException {
		List macros = getParameterList(PARAMETER_MACROS);
		Iterator i = macros.iterator();
		while (i.hasNext()) {
			Object[] macroDefinition = (Object[])i.next();
			String macro = (String)macroDefinition[0];
			String value = (String)macroDefinition[1];
			getProcess().getMacroHandler().addMacro(macro, value);
		}
		return new IOObject[0];
	}

	public Class[] getInputClasses() {
		return new Class[0];
	}

	public Class[] getOutputClasses() {
		return new Class[0];
	}
			
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		types.add(new ParameterTypeList(PARAMETER_MACROS, "The list of macros defined by the user.", new ParameterTypeString(PARAMETER_VALUES, "The values of the user defined macros.", false)));
		return types;
	}
}
