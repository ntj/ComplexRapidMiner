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
package com.rapidminer.operator.learner.clustering;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * An index for cluster nodes (find a cluster by item).
 * 
 * @author Michael Wurst, Ingo Mierswa
 * @version $Id: ClusterModelItemIndex.java,v 1.3 2007/06/20 12:30:03 ingomierswa Exp $
 */
public class ClusterModelItemIndex implements Serializable {

	private static final long serialVersionUID = 3463135366403002504L;
	
	private final Map<String, ClusterNode> index;

	public ClusterModelItemIndex(HierarchicalClusterModel cm) {
		index = new HashMap<String, ClusterNode>();
		ClusterIterator ci = new ClusterIterator(cm);
		while (ci.hasMoreClusters()) {
			ClusterNode cl = (ClusterNode) ci.nextCluster();
			Iterator<String> it = cl.getObjects();
			while (it.hasNext())
				index.put(it.next(), cl);
		}
	}

	public ClusterNode getCluster(String objId) {
		return index.get(objId);
	}

	public Iterator<String> getAllIds() {
		return index.keySet().iterator();
	}
    
    public int getNumberOfIds() {
        return index.size();
    }
}
