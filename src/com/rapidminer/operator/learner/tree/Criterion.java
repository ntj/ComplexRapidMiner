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
package com.rapidminer.operator.learner.tree;

import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.SplittedExampleSet;

/**
 * The criterion for a splitted example set. Possible implementations are
 * for example accuracy or information gain.
 * 
 * @author Sebastian Land, Ingo Mierswa
 * @version $Id: Criterion.java,v 1.5 2008/05/09 19:22:52 ingomierswa Exp $
 */
public interface Criterion {

    public double getBenefit(SplittedExampleSet exampleSet);
    
    
    public boolean supportsIncrementalCalculation();
    
    public void startIncrementalCalculation(ExampleSet exampleSet);
    
    public void swapExample(Example example);
    
	public double getIncrementalBenefit();

}
