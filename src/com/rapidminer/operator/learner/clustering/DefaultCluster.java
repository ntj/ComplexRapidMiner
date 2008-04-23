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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Represents a default implementation of a flat cluster.
 * 
 * @author Michael Wurst
 * @version $Id: DefaultCluster.java,v 1.3 2007/07/08 17:52:41 stiefelolm Exp $
 */
public class DefaultCluster implements MutableCluster {

	private static final long serialVersionUID = 931501296059079941L;

	private String clusterId;

	private Set<String> idSet;

	private String description;

	private DefaultCluster() {
		super();
		this.idSet = new HashSet<String>();
	}
	
	public DefaultCluster(Set<String> idSet) {
		super();
		this.idSet = idSet;
	}
	/**
	 * Constructor for DefaultCluster.
	 * 
	 * @param clusterId
	 *            the id of this cluster.
	 */
	public DefaultCluster(String clusterId) {
		this();
		this.description = clusterId;
		this.clusterId = clusterId;
	}

	/**
	 * Copy constructor (deep).
	 * 
	 * @param cluster
	 *            a cluster
	 */
	public DefaultCluster(Cluster cluster) {
		this();
		clusterId = cluster.getId();
		description = cluster.getDescription();
		Iterator<String> it = cluster.getObjects();
		while (it.hasNext())
			idSet.add(it.next());
	}

	public Iterator<String> getObjects() {
		return idSet.iterator();
	}

	public String getId() {
		return clusterId;
	}

	public String getDescription() {
		return description;
	}

	/**
	 * Add a new object to the cluster.
	 * 
	 * @param objId
	 *            an object id
	 */
	public void addObject(String objId) {
		idSet.add(objId);
	}

	/**
	 * Remove an object from the cluster.
	 * 
	 * @param objId
	 *            an object id
	 */
	public void removeObject(String objId) {
		idSet.remove(objId);
	}

	public boolean contains(String objId) {
		return idSet.contains(objId);
	}

	/**
	 * Sets the description.
	 * 
	 * @param description
	 *            The description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	public int hashCode() {
		return getId().hashCode();
	}

	public boolean equals(Object arg0) {
		if (arg0 instanceof Cluster)
			return getId().equals(((Cluster) arg0).getId());
		else
			return false;
	}

	public String toString() {
		return getId();
	}

	/**
	 * Returns the number of objects in this cluster
	 * 
	 * @return number of objects
	 */
	public int getNumberOfObjects() {
		return idSet.size();
	}
}
