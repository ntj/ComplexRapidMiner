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
package com.rapidminer.operator.features.weighting;

import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.learner.tree.Criterion;
import com.rapidminer.operator.learner.tree.GainRatioCriterion;

/**
 * This operator calculates the relevance of a feature by computing the 
 * information gain ratio for the class distribution (if exampleSet would 
 * have been splitted according to each of the given features).
 * 
 * @author Ingo Mierswa
 * @version $Id: InfoGainRatioWeighting.java,v 1.5 2007/06/16 03:28:12 ingomierswa Exp $
 */
public class InfoGainRatioWeighting extends AbstractEntropyWeighting {

	public InfoGainRatioWeighting(OperatorDescription description) {
		super(description);
	}

	public Criterion getEntropyCriterion() {
		return new GainRatioCriterion();
	}
}
