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
package com.rapidminer.operator.learner.clustering.hierarchical.upgma;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.learner.clustering.DefaultClusterNode;
import com.rapidminer.operator.learner.clustering.SimpleHierarchicalClusterModel;


/**
 * Wrapper for the UPGMA Clustering algorithm.
 * 
 * @author Michael Wurst
 * @version $Id: UPGMAHierarchicalClusterModel.java,v 1.5 2008/09/12 10:29:49 tobiasmalbrecht Exp $
 */
public class UPGMAHierarchicalClusterModel extends SimpleHierarchicalClusterModel {

	private static final long serialVersionUID = -5097937876158436565L;

	public UPGMAHierarchicalClusterModel(Tree cm, ExampleSet es) {
		super();
		this.setRootNode(copyTreeRec(cm, "0"));
	}

	private DefaultClusterNode copyTreeRec(Tree t, String id) {
		DefaultClusterNode cn = new DefaultClusterNode(id);
		cn.setDescription(t.getName());
		for (int i = 0; i < t.getNumberOfChildren(); i++) {
			Tree child = t.getChild(i);
			if (child.isLeaf())
				cn.addObject(child.getName());
			else
				cn.addSubNode(copyTreeRec(child, id + "." + i));
		}
		return cn;
	}
}
