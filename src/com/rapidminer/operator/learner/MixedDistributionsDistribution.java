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
package com.rapidminer.operator.learner;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * This distribution is built from a set of other distributions.
 * 
 * @author Sebastian Land
 * @version $Id: MixedDistributionsDistribution.java,v 1.1 2007/05/27 22:03:28 ingomierswa Exp $
 */
public class MixedDistributionsDistribution implements Distribution {

	private static final long serialVersionUID = 8852938552268669069L;

	private ArrayList<Distribution> distributions;

	public MixedDistributionsDistribution(ArrayList<Distribution> distributions) {
		this.distributions = distributions;
	}

	public MixedDistributionsDistribution() {
		this.distributions = new ArrayList<Distribution>();
	}

	public double getProbability(double x) {
		double probability = 0;
		Iterator<Distribution> iterator = distributions.iterator();
		while (iterator.hasNext()) {
			probability += iterator.next().getProbability(x);
		}
		return probability / distributions.size();
	}

	public void addDistribution(Distribution distribution) {
		this.distributions.add(distribution);
	}
}
