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

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.rapidminer.gui.viewer.AssociationRuleVisualization;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.ResultObjectAdapter;
import com.rapidminer.tools.Tools;

/**
 * A set of {@link AssociationRule}s which can be constructed from frequent item sets.
 * 
 * @author Sebastian Land, Ingo Mierswa
 * @version $Id: AssociationRules.java,v 1.3 2007/06/22 22:14:16 ingomierswa Exp $
 */
public class AssociationRules extends ResultObjectAdapter implements Iterable<AssociationRule> {
	
	private static final long serialVersionUID = 3734387908954857589L;
	
	private static final int MAXIMUM_NUMBER_OF_RULES_IN_OUTPUT = 100;
	
	private List<AssociationRule> associationRules = new ArrayList<AssociationRule>();

	public void addItemRule(AssociationRule rule) {
		associationRules.add(rule);
	}

	public int getNumberOfRules() {
		return associationRules.size();
	}
	
	public AssociationRule getRule(int index) {
		return associationRules.get(index);
	}
	
	public String getExtension() {
		return "asr";
	}

	public String getFileDescription() {
		return "Association Rules";
	}

	public String toResultString() {
		return toString(-1);
	}
	
	public String toString() {
		return toString(MAXIMUM_NUMBER_OF_RULES_IN_OUTPUT);
	}
	
	public String toString(int maxNumber) {
		Collections.sort(associationRules);
		StringBuffer buffer = new StringBuffer("Association Rules" + Tools.getLineSeparator());
		int counter = 0;
		for (AssociationRule rule : associationRules) {
			if ((maxNumber >= 0) && (counter > maxNumber)) {
				buffer.append("... " + (associationRules.size() - maxNumber) + " other rules ...");
				break;
			}
			buffer.append(rule.toString());
			buffer.append(Tools.getLineSeparator());
			counter++;
		}
		return buffer.toString();
	}
	
    /** Returns the visualization component. */
    public Component getVisualizationComponent(IOContainer container) {
        return new AssociationRuleVisualization(this);
    }

	public Iterator<AssociationRule> iterator() {
		return associationRules.iterator();
	}
}
