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
package com.rapidminer.tools;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.rapidminer.operator.OperatorDescription;


/**
 * A group tree manages operator descriptions in a tree like manner. This is
 * useful to present the operators in groups and subgroups and eases operator
 * selection in the GUI.
 * 
 * @author Ingo Mierswa
 * @version $Id: GroupTree.java,v 1.6 2008/05/09 19:22:55 ingomierswa Exp $
 */
public class GroupTree implements Comparable<GroupTree> {

	/** The list of operators in this group. */
	private Set<OperatorDescription> operators = new TreeSet<OperatorDescription>();

	/** The subgroups of this group. */
	private Map<String, GroupTree> children = new TreeMap<String, GroupTree>();

	/** The name of this group. */
	private String name = null;

	/** The parent of this group. */
	private GroupTree parent = null;

	/** Creates a new group tree with no operators and children. */
	public GroupTree(String name) {
		this.name = name;
	}

    /** Clone constructor. The parent will be automatically set after this tree is added to
     *  another. */
    public GroupTree(GroupTree other) {
        this.name = other.name;
        this.operators.addAll(other.operators);
        Iterator<GroupTree> g = other.getSubGroups().iterator();
        while (g.hasNext()) {
            GroupTree child = g.next();
            addSubGroup((GroupTree)child.clone());
        }
    }
    
    /** Returns a deep clone of this tree. */
    public Object clone() {
        return new GroupTree(this);
    }
    
	/** Returns the name of this group. */
	public String getName() {
		return name;
	}

    /** Returns the main group name, i.e. the name of the first parent group under the root. */
    public String getMainGroupName() {
        if (getParent() == null) {
            return "Root";
        } else {
            if (getParent().getParent() == null)
                return getName();
            else
                return getParent().getMainGroupName();
        }
    }
    
	/** Sets the parent of this group. */
	private void setParent(GroupTree parent) {
		this.parent = parent;
	}

	/** Returns the parent of this group. Returns null if no parent does exist. */
	private GroupTree getParent() {
		return parent;
	}

	/** Adds a subgroup to this group. */
	public void addSubGroup(GroupTree child) {
		children.put(child.getName(), child);
		child.setParent(this);
	}

	/** Returns the subgroup with the given name. */
	public GroupTree getSubGroup(String name) {
		return children.get(name);
	}

	/** Returns a set of all children group trees. */
	public Collection<GroupTree> getSubGroups() {
		return children.values();
	}

	/** Returns the index of the given subgroup or -1 if the sub group is not a child of this node. */
	public int getIndexOfSubGroup(GroupTree child) {
		Iterator<GroupTree> i = getSubGroups().iterator();
		int index = 0;
		while (i.hasNext()) {
			GroupTree current = i.next();
			if (current.equals(child))
				return index;
			index++;
		}
		return -1;
	}
	
	/** Returns the i-th sub group. */
	public GroupTree getSubGroup(int index) {
		Collection<GroupTree> allChildren = getSubGroups();
		if (index >= allChildren.size()) {
			return null;
		} else {
			Iterator<GroupTree> i = allChildren.iterator();
			int counter = 0;
			while (i.hasNext()) {
				GroupTree current = i.next();
				if (counter == index)
					return current;
				counter++;
			}
			return null;
		}
	}
	
	/** Adds an operator to this group. */
	public void addOperatorDescription(OperatorDescription description) {
		operators.add(description);
	}

	/**
	 * Returns all operator descriptions in this group or an empty list if this
	 * group does not contain any operators.
	 */
	public Set<OperatorDescription> getOperatorDescriptions() {
		return operators;
	}

	/**
	 * Returns all operator in this group and recursively the operators of all
	 * children.
	 */
	public Set<OperatorDescription> getAllOperatorDescriptions() {
		Set<OperatorDescription> result = new TreeSet<OperatorDescription>();
		addAllOperatorDescriptions(result);
		return result;
	}

	private void addAllOperatorDescriptions(Set<OperatorDescription> operators) {
		operators.addAll(this.operators);
		Iterator<GroupTree> i = children.values().iterator();
		while (i.hasNext()) {
			GroupTree child = i.next();
			child.addAllOperatorDescriptions(operators);
		}
	}

	public String toString() {
		String result = name;
		if (getParent() == null) 
			result = "Root";
		return result + (getOperatorDescriptions().size() > 0 ? " (" + getOperatorDescriptions().size() + ")" : "");
	}

    public int compareTo(GroupTree o) {
        return this.name.compareTo(o.name);
    }
    
    public boolean equals(Object o) {
		if (!(o instanceof GroupTree))
			return false;
		GroupTree a = (GroupTree) o;
		if (!this.name.equals(a.getName()))
			return false;
		return true;
    }
    
    public int hashCode() {
    	return this.name.hashCode();
    }
}
