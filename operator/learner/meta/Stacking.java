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
package com.rapidminer.operator.learner.meta;

import java.util.List;

import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;


/**  
 * This class uses n+1 inner learners and generates n different models
 * by using the last n learners. The predictions of these n models are
 * taken to create n new features for the example set, which is finally
 * used to serve as an input of the first inner learner.  
 * 
 * @author Ingo Mierswa, Helge Homburg
 * @version $Id: Stacking.java,v 1.4 2008/05/09 19:22:46 ingomierswa Exp $
 */
public class Stacking extends AbstractStacking {

    public static final String PARAMETER_KEEP_ALL_ATTRIBUTES = "keep_all_attributes";
    
	public Stacking(OperatorDescription description) {
		super(description);
	}		

	public String getModelName() {
		return "Stacking Model";
	}
	
	public int getFirstBaseModelLearnerIndex() {
		return 1;
	}

	public int getLastBaseModelLearnerIndex() {
		return getNumberOfOperators() - 1;
	}

	public Operator getStackingLearner() {
		return getOperator(0);
	}

	public boolean keepOldAttributes() {
		return getParameterAsBoolean(PARAMETER_KEEP_ALL_ATTRIBUTES);
	}
	
	public int getMinNumberOfInnerOperators() {
		return 2;
	}
	
    public List<ParameterType> getParameterTypes() {
        List<ParameterType> types = super.getParameterTypes();
        types.add(new ParameterTypeBoolean(PARAMETER_KEEP_ALL_ATTRIBUTES, 
                "Indicates if all attributes (including the original ones) in order to learn the stacked model.", true));
        return types;
    }
}
