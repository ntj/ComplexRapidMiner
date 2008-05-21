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

import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.Statistics;

/**
 * This criterion terminates if only one single label is left. 
 * 
 * @author Sebastian Land, Ingo Mierswa
 * @version $Id: SingleLabelTermination.java,v 1.5 2008/05/09 19:22:52 ingomierswa Exp $
 */
public class SingleLabelTermination implements Terminator {

    public SingleLabelTermination() {}
    
    public boolean shouldStop(ExampleSet exampleSet, int depth) {
        Attribute label = exampleSet.getAttributes().getLabel();
        exampleSet.recalculateAttributeStatistics(label);
        return exampleSet.size() == exampleSet.getStatistics(label, Statistics.COUNT, label.getMapping().mapIndex((int)exampleSet.getStatistics(label, Statistics.MODE)));
    }    
}
