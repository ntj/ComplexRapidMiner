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
package com.rapidminer.operator.learner.meta;

import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorCreationException;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.learner.lazy.AttributeBasedVotingLearner;
import com.rapidminer.tools.OperatorService;


/**  
 * This class uses n+1 inner learners and generates n different models
 * by using the last n learners. The predictions of these n models are
 * taken to create n new features for the example set, which is finally
 * used to serve as an input of the first inner learner.  
 * 
 * @author Ingo Mierswa, Helge Homburg
 * @version $Id: Vote.java,v 1.2 2007/06/07 22:26:39 ingomierswa Exp $
 */
public class Vote extends AbstractStacking {
    
	public Vote(OperatorDescription description) {
		super(description);
	}		

	public String getModelName() {
		return "Vote Model";
	}
	
	public int getFirstBaseModelLearnerIndex() {
		return 0;
	}

	public int getLastBaseModelLearnerIndex() {
		return getNumberOfOperators() - 1;
	}

	public Operator getStackingLearner() throws OperatorException {
		try {
			return OperatorService.createOperator(AttributeBasedVotingLearner.class);
		} catch (OperatorCreationException e) {
			throw new OperatorException(getName() + ": Not possible to create vote operator.");
		}
	}

	public boolean keepOldAttributes() {
		return false;
	}
}
