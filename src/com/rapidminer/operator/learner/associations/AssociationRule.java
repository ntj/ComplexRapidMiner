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
package com.rapidminer.operator.learner.associations;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;

import com.rapidminer.tools.Tools;

/**
 * <p>An association rule which can be created from a frequent item set.</p>
 * 
 * <p>Note: this class has a natural ordering that is inconsistent with equals.</p>
 * 
 * @author Sebastian Land, Ingo Mierswa
 * @version $Id: AssociationRule.java,v 1.4 2007/06/24 14:30:54 ingomierswa Exp $
 */
public class AssociationRule implements Serializable, Comparable<AssociationRule> {

	private static final long serialVersionUID = -4788528227281876533L;

	private double confidence;

	private double totalSupport;
	
	private Collection<Item> premise;

	private Collection<Item> conclusion;

	public AssociationRule(Collection<Item> premise, Collection<Item> conclusion, double confidence, double totalSupport) {
		this.confidence = confidence;
		this.premise = premise;
		this.conclusion = conclusion;
		this.totalSupport = totalSupport;
	}

	public double getConfidence() {
		return this.confidence;
	}
	
	public double getTotalSupport() {
		return this.totalSupport;
	}
	
	public Iterator<Item> getPremiseItems() {
		return premise.iterator();
	}
	
	public Iterator<Item> getConclusionItems() {
		return conclusion.iterator();
	}
	
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(premise.toString());
		buffer.append(" -> ");
		buffer.append(conclusion.toString());
		buffer.append(" (confidence: ");
		buffer.append(Tools.formatNumber(confidence));
		buffer.append(")");
		return buffer.toString();
	}

	public int compareTo(AssociationRule o) {
		return Double.compare(this.confidence, o.confidence);
	}
	
	public boolean equals(Object o) {
		if (!(o instanceof AssociationRule)) 
			return false;
		AssociationRule other = (AssociationRule)o;
		return
           premise.toString().equals(other.premise.toString()) &&
           conclusion.toString().equals(other.conclusion.toString()) &&
           this.confidence == other.confidence;
	}
	
	public int hashCode() {
		return premise.toString().hashCode() ^ conclusion.toString().hashCode() ^ Double.valueOf(this.confidence).hashCode();
	}
}
