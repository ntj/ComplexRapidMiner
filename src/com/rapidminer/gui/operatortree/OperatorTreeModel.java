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
package com.rapidminer.gui.operatortree;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import com.rapidminer.gui.RapidMinerGUI;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorChain;

/**
 * A TreeModel hiding an operator (which itself already has a tree-like
 * structure). Most of the method calls delegate to an operator. Events can be
 * fired when operators are inserted or removed. Operator trees are the main
 * process editor of the RapidMiner GUI.
 * 
 * @author Ingo Mierswa, Simon Fischer
 * @version $Id: OperatorTreeModel.java,v 2.10 2006/03/27 13:21:58 ingomierswa
 *          Exp $
 */
public class OperatorTreeModel implements TreeModel {

	/** The root operator of the model (usually a root operator). */
	private Operator root;

	/** The list of all tree model listeners. */
	private List<TreeModelListener> treeModelListeners = new LinkedList<TreeModelListener>();

	/** The operator tree. */
	private OperatorTree operatorTree;
	
	/** Indicates if disabled operators should be shown by the model (default: true). */
	private boolean showDisabledOperators = true;
	
	
	/** Creates a new operator tree model. */
	public OperatorTreeModel(Operator root, OperatorTree tree) {
		this.root = root;
		this.operatorTree = tree;
	}

	/** Indicates if disabled operators should be displayed by the tree. */
	public boolean showDisabledOperators() {
		return this.showDisabledOperators;
	}
	
	/** Sets the state of showing diabled operators. */
	public void setShowDisabledOperators(boolean show) {
		this.showDisabledOperators = show;	
	}
	
	/** Returns the root operator. */
	public Object getRoot() {
		return root;
	}

	/** Returns the child with the given index. This method checks if disabled operators
	 *  should be regarded or not. */
	public Object getChild(Object parent, int index) {
		if (parent instanceof OperatorChain) {
			if (showDisabledOperators)
				return ((OperatorChain) parent).getOperatorFromAll(index);
			else
				return ((OperatorChain) parent).getOperator(index);
		} else
			return null;
	}
	
	/** Returns the number of children operators. This method checks if disabled operators
	 *  should be regarded or not. */
	public int getChildCount(Object parent) {
		if (parent instanceof OperatorChain) {
			if (showDisabledOperators)
				return ((OperatorChain) parent).getNumberOfAllOperators();
			else 
				return ((OperatorChain) parent).getNumberOfOperators();
		} else
			return 0;
	}

	/** Returns the index of the child operator with respect to the given parent. This method
	 *  checks if disabled operators should be regarded or not. */
	public int getIndexOfChild(Object parent, Object child) {
		if (parent instanceof OperatorChain) {
			return ((OperatorChain) parent).getIndexOfOperator((Operator) child, showDisabledOperators);
		} else
			return -1;
	}

	/** Returns true if the operator is a leaf node, i.e. it does not have any children. */
	public boolean isLeaf(Object node) {
		return getChildCount(node) == 0;
	}

	/** Will be invoked after editing changes of nodes, i.e. after renaming. This method also 
	 *  causes a refresh since the bounding box of the changed node would not be updated otherwise. */
	public void valueForPathChanged(TreePath path, Object node) {
		Operator op = (Operator) path.getLastPathComponent();
		String desiredName = ((String) node).trim();
		op.rename(desiredName);
		operatorTree.refresh(path);
		RapidMinerGUI.getMainFrame().processChanged();
	}

	/** Adds a tree model listener. */
	public void addTreeModelListener(TreeModelListener l) {
		treeModelListeners.add(l);
	}

	/** Removes the tree model listener. */
	public void removeTreeModelListener(TreeModelListener l) {
		treeModelListeners.remove(l);
	}

	/** Must be used to indicate that a new operator was added. Notifies the listeners by invoking the method 
	 *  treeNodesInserted(...). */
	public void fireOperatorInserted(Object source, TreePath path, int index, Operator operator) {
        if (index >= 0) {
            Iterator i = treeModelListeners.iterator();
            while (i.hasNext()) {
                ((TreeModelListener) i.next()).treeNodesInserted(new TreeModelEvent(source, path, new int[] { index }, new Object[] { operator }));
            }
        }
	}

	/** Must be used to indicate that an operator was removed. Notifies the listeners by invoking the method 
	 *  treeNodesRemoved(...). */
	public void fireOperatorRemoved(Object source, TreePath path, int index, Operator operator) {
        if (index >= 0) {
            Iterator i = treeModelListeners.iterator();
            while (i.hasNext()) {
                ((TreeModelListener) i.next()).treeNodesRemoved(new TreeModelEvent(source, path, new int[] { index }, new Object[] { operator }));
            }
        }
	}

	/** Must be used to indicate that an operator was renamed. Notifies the listeners by invoking the method 
	 *  treeNodesChanged(...). */
	public void fireOperatorRenamed(Object source, TreePath path) {
		Iterator i = treeModelListeners.iterator();
		while (i.hasNext()) {
			((TreeModelListener) i.next()).treeNodesChanged(new TreeModelEvent(source, path));
		}
	}

	/** Must be used to indicate that an operator was changed in some way. Notifies the listeners by invoking the method 
	 *  treeNodesChanged(...). */
	public void fireOperatorChanged(Object source, TreePath path) {
		Iterator i = treeModelListeners.iterator();
		while (i.hasNext()) {
			((TreeModelListener) i.next()).treeNodesChanged(new TreeModelEvent(source, path));
		}
	}

	/** Must be used to indicate that the tree structure has changed. Notifies the listeners by invoking the method 
	 *  treeStructureChanged(...). */
	public void fireStructureChanged(Object source, TreePath path) {
		Iterator i = treeModelListeners.iterator();
		while (i.hasNext()) {
			((TreeModelListener) i.next()).treeStructureChanged(new TreeModelEvent(source, path));
		}
	}
}
