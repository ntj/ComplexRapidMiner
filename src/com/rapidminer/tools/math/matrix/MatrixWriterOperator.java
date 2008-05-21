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
package com.rapidminer.tools.math.matrix;

import java.util.List;

import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.InputDescription;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeFile;


/**
 * Writes a matrix to a file in a the following simple format:
 * 
 * <code>
 * <matrix>
 * <xlabels>
 * ...
 * ...
 * </xlabels>
 * <ylabels>
 * ...
 * ...
 * </ylabels>
 * <data>
 * 0.0, 2.3 ... 
 * 1.0, 0.0, 4.5, ...
 * ...
 * </data>
 * </matrix>
 * 
 * </code>
 * 
 * As an alternative the data can be written in a sparse format:
 * 
 * <code>
 * <data>
 * ...
 * 0:0.0, 1:2.3 ... 
 * 0:1.0, 2:4.5, ...
 * ...
 * </data>
 * ...
 * </code> The indexes for the y dimension start with zero. All entries not mentioned are assumed to be zero.
 * 
 * @author Michael Wurst
 * @version $Id: MatrixWriterOperator.java,v 1.4 2008/05/09 19:23:23 ingomierswa Exp $
 * 
 */
public class MatrixWriterOperator extends Operator {


	/** The parameter name for &quot;the file to which the matrix is stored&quot; */
	public static final String PARAMETER_MATRIX_FILE = "matrix_file";

	/** The parameter name for &quot;store the file in sparse format&quot; */
	public static final String PARAMETER_SPARSE_FORMAT = "sparse_format";
    public MatrixWriterOperator(OperatorDescription description) {
        super(description);
    }

    public InputDescription getInputDescription(Class cls) {
        if (ExtendedMatrix.class.isAssignableFrom(cls)) {
            return new InputDescription(cls, false, true);
        }

        return super.getInputDescription(cls);

    }

    public Class[] getInputClasses() {
        return new Class[] { ExtendedMatrix.class };
    }

    public Class[] getOutputClasses() {
        return new Class[0];
    }

    @SuppressWarnings("unchecked")
    public IOObject[] apply() throws OperatorException {
    	/*
        SimpleXMLMatrixDAO matrixWriter = new SimpleXMLMatrixDAO();

        File file = getParameterAsFile(PARAMETER_MATRIX_FILE);

        ExtendedMatrix matrix = getInput(ExtendedMatrix.class);

        try {
            matrixWriter.write(matrix, new FileOutputStream(file), getParameterAsBoolean(PARAMETER_SPARSE_FORMAT));
        } catch (IOException e) {
            throw new UserError(this, 303, file, e);
        }
*/
        return new IOObject[0];
    }

    public List<ParameterType> getParameterTypes() {
        List<ParameterType> types = super.getParameterTypes();

        ParameterType type = new ParameterTypeFile(PARAMETER_MATRIX_FILE, "the file to which the matrix is stored", "mat", false);
        type.setExpert(false);
        types.add(type);
        
        types.add(new ParameterTypeBoolean(PARAMETER_SPARSE_FORMAT, "store the file in sparse format", false));

        return types;
    }

}
