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
package com.rapidminer.operator.preprocessing;

import java.util.HashMap;

import javax.swing.Icon;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.ModelViewExampleSet;
import com.rapidminer.gui.tools.SwingTools;
import com.rapidminer.operator.AbstractModel;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ViewModel;

/**
 * Returns a more appropriate result icon. This model allows preprocessing
 * Operators to be applied through a view without changing the underlying data.
 * Since ModelApplier dont know the models, because they are wrapped within a 
 * container model, its necessary to ask for the parameter PARAMETER_CREATE_VIEW.
 * This must be setted by the modelapplier, and should be the default behavior.
 * 
 * @author Ingo Mierswa, Sebastian Land
 * @version $Id: PreprocessingModel.java,v 1.7 2008/05/09 19:22:54 ingomierswa Exp $
 */
public abstract class PreprocessingModel extends AbstractModel implements ViewModel {
	
	private static final String RESULT_ICON_NAME = "transform.png";
	
	private static Icon resultIcon = null;
	
	private HashMap<String, Object> parameterMap = new HashMap<String, Object>();
	
	static {
		resultIcon = SwingTools.createIcon("16/" + RESULT_ICON_NAME);
	}
	
	protected PreprocessingModel(ExampleSet exampleSet) {
		super(exampleSet);
	}
	
	public abstract ExampleSet applyOnData(ExampleSet exampleSet) throws OperatorException;
	
	public ExampleSet apply(ExampleSet exampleSet) throws OperatorException {
		boolean createView = false;
		if (parameterMap.containsKey(PreprocessingOperator.PARAMETER_CREATE_VIEW)) {
			Boolean booleanObject = ((Boolean)parameterMap.get(PreprocessingOperator.PARAMETER_CREATE_VIEW)); 
			createView = false;
			if (booleanObject != null)
				createView = booleanObject.booleanValue();
		}
		
		if (createView) {
			// creating only view
			return new ModelViewExampleSet(exampleSet, this);
		} else {
			return applyOnData(exampleSet);
		}
	}
	
	public void setParameter(String key, Object value) {
		parameterMap.put(key, value);
	}
	
	public Icon getResultIcon() {
		return resultIcon;
	}
}
