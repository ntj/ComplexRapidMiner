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
package com.rapidminer.operator.learner.clustering.constrained.constraints;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;

import com.rapidminer.operator.learner.clustering.Cluster;

/**
 * This is a specialized ClusterConstraintList to keep LinkClusterConstraints that includes an additional mapping from object ids to lists of LinkClusterConstraints to allow efficient tests, if an
 * object is part of a link constraint.
 * 
 * @author Alexander Daxenberger
 * @version $Id: LinkClusterConstraintList.java,v 1.6 2008/05/09 19:23:17 ingomierswa Exp $
 */
public class LinkClusterConstraintList extends ClusterConstraintList {

    private static final long serialVersionUID = 8137809668796376937L;

    private static class SetComparator implements Comparator<Set>, Serializable {

        private static final long serialVersionUID = 6940643923899884839L;

		public int compare(Set s1, Set s2) {
            if (s1.size() < s2.size())
                return 1;
            else if (s1.size() > s2.size())
                return -1;
            else if (s1 != s2)
                return 1;
            else
                return 0;
        }
    }
    
    private HashMap<String, ArrayList<LinkClusterConstraint>> idConstraintsMap;

    public static final float initialSizeOfIdMapFactor = 1.5f;

    public static final int minSizeOfConstraintList = 4;

    public LinkClusterConstraintList(String name) {
        this(name, 200);
    }

    public LinkClusterConstraintList(String name, int initialCapacity) {
        super(name, initialCapacity);
        this.idConstraintsMap = new HashMap<String, ArrayList<LinkClusterConstraint>>((int) ((initialCapacity * initialSizeOfIdMapFactor) / 0.75f), 0.75f);
    }

    /**
     * Returns a list of all LinkClusterConstraints that involve the object with id 'id'
     * 
     * @param id
     */
    public ArrayList<LinkClusterConstraint> getLinkConstraintsFor(String id) {
        return idConstraintsMap.get(id);
    }

    /**
     * Returns a list of all LinkClusterConstraints that involve objects of the cluster 'c'.
     */
    public ArrayList getLinkConstraintsFor(Cluster c) {
        HashSet<LinkClusterConstraint> set = new HashSet<LinkClusterConstraint>(this.constraintList.size(), 0.75f);
        LinkClusterConstraint lcc;

        if (c.getNumberOfObjects() > this.constraintList.size()) {
            for (int i = 0; i < this.constraintList.size(); i++) {
                lcc = (LinkClusterConstraint) this.constraintList.get(i);
                if ((c.contains(lcc.getId0())) || (c.contains(lcc.getId1())))
                    set.add(lcc);
            }
        } else {
            Iterator<String> clusterIdIterator = c.getObjects();
            while (clusterIdIterator.hasNext()) {
                String id = clusterIdIterator.next();
                ArrayList<LinkClusterConstraint> list = idConstraintsMap.get(id);
                if (list != null) {
                    for (int i = 0; i < list.size(); i++) {
                        lcc = list.get(i);
                        set.add(lcc);
                    }
                }
            }
        }

        ArrayList<LinkClusterConstraint> list = new ArrayList<LinkClusterConstraint>(set.size());
        Iterator<LinkClusterConstraint> setIterator = set.iterator();
        while (setIterator.hasNext()) {
            list.add(setIterator.next());
        }

        return list;
    }

    /**
     * Returns a list of sets of object ids that form the connected components in the graph spanned by the Must-Link-constraints (neighbourhoodsets).
     */
    public ArrayList<Set<String>> getNeighbourhoodSets() {
        LinkedList<LinkClusterConstraint> queue = new LinkedList<LinkClusterConstraint>();
        LinkedList<LinkClusterConstraint> conList = new LinkedList<LinkClusterConstraint>();
        HashSet<LinkClusterConstraint> seenConstraint = new HashSet<LinkClusterConstraint>(this.constraintList.size() + 1, 1.0f);
        HashSet<String> neighbourhood;
        LinkClusterConstraint lcc1;
        LinkClusterConstraint lcc2;
        TreeSet<Set<String>> sets = new TreeSet<Set<String>>(new SetComparator());

        for (int i = 0; i < this.constraintList.size(); i++) {
            lcc1 = (LinkClusterConstraint) this.constraintList.get(i);
            if ((lcc1.getType() == LinkClusterConstraint.MUST_LINK) && (!seenConstraint.contains(lcc1))) {
                conList.clear();
                queue.clear();
                queue.add(lcc1);
                seenConstraint.add(lcc1);

                while (queue.size() > 0) {
                    lcc1 = queue.removeFirst();
                    conList.add(lcc1);

                    ArrayList<LinkClusterConstraint> list = idConstraintsMap.get(lcc1.getId0());
                    if (list != null) {
                        for (int j = 0; j < list.size(); j++) {
                            lcc2 = list.get(j);
                            if ((lcc2.getType() == LinkClusterConstraint.MUST_LINK) && (!seenConstraint.contains(lcc2))) {
                                queue.add(lcc2);
                                seenConstraint.add(lcc2);
                            }
                        }
                    }

                    list = idConstraintsMap.get(lcc1.getId1());
                    if (list != null) {
                        for (int j = 0; j < list.size(); j++) {
                            lcc2 = list.get(j);
                            if ((lcc2.getType() == LinkClusterConstraint.MUST_LINK) && (!seenConstraint.contains(lcc2))) {
                                queue.add(lcc2);
                                seenConstraint.add(lcc2);
                            }
                        }
                    }
                }

                neighbourhood = new HashSet<String>(2 * conList.size(), 0.75f);
                Iterator<LinkClusterConstraint> constraintIterator = conList.iterator();
                while (constraintIterator.hasNext()) {
                    lcc1 = constraintIterator.next();
                    neighbourhood.add(lcc1.getId0());
                    neighbourhood.add(lcc1.getId1());
                }

                sets.add(neighbourhood);
            }
        }

        ArrayList<Set<String>> list = new ArrayList<Set<String>>(sets.size());
        Iterator<Set<String>> setsIterator = sets.iterator();
        while (setsIterator.hasNext()) {
            list.add(setsIterator.next());
        }

        return list;
    }

    public boolean addConstraint(ClusterConstraint cc) {
        LinkClusterConstraint lcc;

        if (cc instanceof LinkClusterConstraint) {
            lcc = (LinkClusterConstraint) cc;

            if (super.addConstraint(lcc)) {
                this.addConstraintForId(lcc.getId0(), lcc);
                this.addConstraintForId(lcc.getId1(), lcc);
                return true;
            }
        }

        return false;
    }

    public ClusterConstraint removeConstraint(ClusterConstraint cc) {
        LinkClusterConstraint lcc;

        if (cc instanceof LinkClusterConstraint) {
            lcc = (LinkClusterConstraint) super.removeConstraint(cc);

            if (lcc != null) {
                this.removeConstraintForId(lcc.getId0(), lcc);
                this.removeConstraintForId(lcc.getId1(), lcc);
                return lcc;
            }
        }

        return null;
    }

    /**
     * Adds a LinkClusterConstraint for an object id to the list pointed to by the idConstraintsMap.
     * 
     * @param id
     * @param lcc
     */
    private void addConstraintForId(String id, LinkClusterConstraint lcc) {
        ArrayList<LinkClusterConstraint> list = this.idConstraintsMap.get(id);

        if (list == null) {
            list = new ArrayList<LinkClusterConstraint>(calculateInitialConstraintListSize());
            idConstraintsMap.put(id, list);
        }
        list.add(lcc);
    }

    /**
     * Removes a LinkClusterConstraint for an object id from the list pointed to by the idConstraintsMap.
     * 
     * @param id
     * @param lcc
     */
    private boolean removeConstraintForId(String id, LinkClusterConstraint lcc) {
        ArrayList list;

        list = this.idConstraintsMap.get(id);

        if (list != null)
            return list.remove(lcc);
        else
            return false;
    }

    /**
     * Should calculate a reasonable value for the initial size of a LinkClusterConstraint list used by the idConstraintsMap to avoid creating too large ArrayLists and save memory.
     */
    private int calculateInitialConstraintListSize() {
        Iterator iter;
        ArrayList list;
        int a = 0;

        if (this.idConstraintsMap.size() > 0) {
            iter = this.idConstraintsMap.values().iterator();
            while (iter.hasNext()) {
                list = (ArrayList) iter.next();
                a += list.size();
            }
            a = a / this.idConstraintsMap.size();
        }

        if (a < LinkClusterConstraintList.minSizeOfConstraintList)
            return LinkClusterConstraintList.minSizeOfConstraintList;
        else
            return a;
    }
}
