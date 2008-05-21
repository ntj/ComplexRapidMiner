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
package com.rapidminer.gui.processeditor;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.tools.GroupTree;


/**
 * This is the model for the group selection tree in the new operator editor panel.
 * 
 * @author Ingo Mierswa
 * @version $Id: NewOperatorGroupTreeModel.java,v 1.4 2008/05/09 19:23:16 ingomierswa Exp $
 */
public class NewOperatorGroupTreeModel implements TreeModel {

	private GroupTree completeTree;
    
    private GroupTree displayedTree;
	
	/** The list of all tree model listeners. */
	private List<TreeModelListener> treeModelListeners = new LinkedList<TreeModelListener>();
	
	public NewOperatorGroupTreeModel(GroupTree root) {
		this.completeTree = root;
        this.displayedTree = this.completeTree;
	}
	
	public void addTreeModelListener(TreeModelListener l) {
		treeModelListeners.add(l);
	}

	public Object getChild(Object parent, int index) {
		return ((GroupTree)parent).getSubGroup(index);
	}

	public int getChildCount(Object parent) {
		return ((GroupTree)parent).getSubGroups().size();
	}

	public int getIndexOfChild(Object parent, Object child) {
		return ((GroupTree)parent).getIndexOfSubGroup((GroupTree)child);
	}

	public Object getRoot() {
		return displayedTree;
	}

	public boolean isLeaf(Object node) {
		return getChildCount(node) == 0;
	}

	public void removeTreeModelListener(TreeModelListener l) {
		treeModelListeners.remove(l);
	}
	
	/** Will be invoked after editing changes of nodes. */
	public void valueForPathChanged(TreePath path, Object node) {
		fireTreeChanged(node, path);
	}
	
	private void fireTreeChanged(Object source, TreePath path) {
		Iterator i = treeModelListeners.iterator();
		while (i.hasNext()) {
			((TreeModelListener) i.next()).treeStructureChanged(new TreeModelEvent(source, path));
		}
	}

    private void fireCompleteTreeChanged(Object source) {
        Iterator i = treeModelListeners.iterator();
        while (i.hasNext()) {
            ((TreeModelListener) i.next()).treeStructureChanged(new TreeModelEvent(this, new TreePath(getRoot())));
        }
    }
    
    public void applyFilter(String filter) {
        if ((filter == null) || (filter.trim().length() == 0)) {
            this.displayedTree = completeTree;
        } else {
            GroupTree filteredTree = (GroupTree)this.completeTree.clone();
            removeFilteredInstances(filter, filteredTree);
            this.displayedTree = filteredTree;
        }
        fireCompleteTreeChanged(this);
    }
    
    private void removeFilteredInstances(String filter, GroupTree filteredTree) {
        Iterator<GroupTree> g = filteredTree.getSubGroups().iterator();
        while (g.hasNext()) {
            GroupTree child = g.next();
            if (child.getName().toLowerCase().indexOf(filter.toLowerCase()) < 0) {
                removeFilteredInstances(filter, child);
                if (child.getAllOperatorDescriptions().size() == 0)
                    g.remove();
            }
        }
        
        // remove non matching operator descriptions if the group does not match, keep all in matching group
        if (filteredTree.getName().toLowerCase().indexOf(filter.toLowerCase()) < 0) {
            Iterator<OperatorDescription> o = filteredTree.getOperatorDescriptions().iterator();
            while (o.hasNext()) {
                OperatorDescription description = o.next();
                if (description.getName().toLowerCase().indexOf(filter.toLowerCase()) < 0)
                    o.remove();
            } 
        }
    }
}
