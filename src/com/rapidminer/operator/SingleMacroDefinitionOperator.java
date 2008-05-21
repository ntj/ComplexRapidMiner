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
package com.rapidminer.operator;

import java.util.List;

import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeString;

/**
 * <p>(Re-)Define macros for the current process. Macros will be replaced in the value strings
 * of parameters by the macro values defined as a parameter of this operator.
 * Please note that this features is basically only supported for string type parameter
 * values (strings or files) and not for numerical or list types. In contrast to the
 * usual MacroDefinitionOperator, this operator only supports the definition of a single
 * macro and can hence be used inside of parameter iterations.</p>
 * 
 * <p>You have to define the macro name (without the enclosing brackets) and
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
 * @version $Id: SingleMacroDefinitionOperator.java,v 1.3 2008/05/09 19:23:18 ingomierswa Exp $
 */
public class SingleMacroDefinitionOperator extends Operator {

    /** The parameter name for &quot;The values of the user defined macros.&quot; */
    public static final String PARAMETER_MACRO = "macro";
    public static final String PARAMETER_VALUE = "value";
    
    public SingleMacroDefinitionOperator(OperatorDescription description) {
        super(description);
    }

    public IOObject[] apply() throws OperatorException {
        String macro = getParameterAsString(PARAMETER_MACRO);
        String value = getParameterAsString(PARAMETER_VALUE);
        getProcess().getMacroHandler().addMacro(macro, value);
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
        types.add(new ParameterTypeString(PARAMETER_MACRO, "The macro name defined by the user.", false));
        types.add(new ParameterTypeString(PARAMETER_VALUE, "The macro value defined by the user.", false));
        return types;
    }
}
