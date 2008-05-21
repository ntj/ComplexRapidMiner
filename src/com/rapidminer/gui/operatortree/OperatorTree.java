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

import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragSource;
import java.awt.dnd.DropTarget;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.ToolTipManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

import com.rapidminer.BreakpointListener;
import com.rapidminer.gui.MainFrame;
import com.rapidminer.gui.actions.NewBuildingBlockAction;
import com.rapidminer.gui.actions.NewOperatorAction;
import com.rapidminer.gui.dialog.OperatorInfoScreen;
import com.rapidminer.gui.operatormenu.OperatorMenu;
import com.rapidminer.gui.operatortree.actions.AddAllBreakpointsAction;
import com.rapidminer.gui.operatortree.actions.CollapseAllAction;
import com.rapidminer.gui.operatortree.actions.CopyAction;
import com.rapidminer.gui.operatortree.actions.CutAction;
import com.rapidminer.gui.operatortree.actions.DeleteOperatorAction;
import com.rapidminer.gui.operatortree.actions.ExpandAllAction;
import com.rapidminer.gui.operatortree.actions.InfoOperatorAction;
import com.rapidminer.gui.operatortree.actions.LockTreeStructureAction;
import com.rapidminer.gui.operatortree.actions.PasteAction;
import com.rapidminer.gui.operatortree.actions.RemoveAllBreakpointsAction;
import com.rapidminer.gui.operatortree.actions.RenameOperatorAction;
import com.rapidminer.gui.operatortree.actions.SaveBuildingBlockAction;
import com.rapidminer.gui.operatortree.actions.ToggleActivationItem;
import com.rapidminer.gui.operatortree.actions.ToggleBreakpointItem;
import com.rapidminer.gui.operatortree.actions.ToggleShowDisabledItem;
import com.rapidminer.gui.templates.NewBuildingBlockMenu;
import com.rapidminer.gui.tools.IconSize;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorChain;
import com.rapidminer.operator.ProcessRootOperator;


/**
 * Displays the process definition as a JTree. This is the main process view of the
 * RapidMiner GUI and can be used to edit processes. New operators can be added by
 * selecting a new operator from the context menu of the currently selected
 * operator. This editor also supports cut and paste and drag and drop.
 * 
 * @see com.rapidminer.gui.operatortree.OperatorTreeModel
 * @author Ingo Mierswa
 * @version $Id: OperatorTree.java,v 1.20 2008/05/09 19:23:26 ingomierswa Exp $
 */
public class OperatorTree extends JTree implements TreeSelectionListener, TreeExpansionListener, MouseListener {

	private static final long serialVersionUID = -6934683725946634563L;

	// ======================================================================
	// Operator Menu Actions and Items
	// ======================================================================

	public final Action NEW_OPERATOR_ACTION_24 = new NewOperatorAction(this, IconSize.SMALL);
	public final Action NEW_OPERATOR_ACTION_32 = new NewOperatorAction(this, IconSize.MIDDLE);

	public final Action NEW_BUILDING_BLOCK_ACTION_24 = new NewBuildingBlockAction(this, IconSize.SMALL);
	public final Action NEW_BUILDING_BLOCK_ACTION_32 = new NewBuildingBlockAction(this, IconSize.MIDDLE);

	public final Action CUT_ACTION_24 = new CutAction(this, IconSize.SMALL);
	public final Action CUT_ACTION_32 = new CutAction(this, IconSize.MIDDLE);

	public final Action COPY_ACTION_24 = new CopyAction(this, IconSize.SMALL);
	public final Action COPY_ACTION_32 = new CopyAction(this, IconSize.MIDDLE);

	public final Action PASTE_ACTION_24 = new PasteAction(this, IconSize.SMALL);
	public final Action PASTE_ACTION_32 = new PasteAction(this, IconSize.MIDDLE);

	public final Action DELETE_OPERATOR_ACTION_24 = new DeleteOperatorAction(this, IconSize.SMALL);
	public final Action DELETE_OPERATOR_ACTION_32 = new DeleteOperatorAction(this, IconSize.MIDDLE);

	public final Action RENAME_OPERATOR_ACTION_24 = new RenameOperatorAction(this, IconSize.SMALL);
	public final Action RENAME_OPERATOR_ACTION_32 = new RenameOperatorAction(this, IconSize.MIDDLE);

	public final Action INFO_OPERATOR_ACTION_24 = new InfoOperatorAction(this, IconSize.SMALL);
	public final Action INFO_OPERATOR_ACTION_32 = new InfoOperatorAction(this, IconSize.MIDDLE);

	public final Action SAVE_BUILDING_BLOCK_ACTION_24 = new SaveBuildingBlockAction(this, IconSize.SMALL);
	public final Action SAVE_BUILDING_BLOCK_ACTION_32 = new SaveBuildingBlockAction(this, IconSize.MIDDLE);

	public final ToggleBreakpointItem TOGGLE_BREAKPOINT[] = { 
			new ToggleBreakpointItem(this, BreakpointListener.BREAKPOINT_BEFORE, IconSize.SMALL), 
			new ToggleBreakpointItem(this, BreakpointListener.BREAKPOINT_WITHIN, IconSize.SMALL),
			new ToggleBreakpointItem(this, BreakpointListener.BREAKPOINT_AFTER, IconSize.SMALL) 
	};

	// ======================================================================
	// Operator Menu Actions and Items
	// ======================================================================

	public final ToggleShowDisabledItem TOGGLE_SHOW_DISABLED = new ToggleShowDisabledItem(this, true);

	public transient final Action ADD_ALL_BREAKPOINTS_24 = new AddAllBreakpointsAction(this, IconSize.SMALL);
	public transient final Action ADD_ALL_BREAKPOINTS_32 = new AddAllBreakpointsAction(this, IconSize.MIDDLE);

	public transient final Action REMOVE_ALL_BREAKPOINTS_24 = new RemoveAllBreakpointsAction(this, IconSize.SMALL);
	public transient final Action REMOVE_ALL_BREAKPOINTS_32 = new RemoveAllBreakpointsAction(this, IconSize.MIDDLE);

	public transient final Action EXPAND_ALL_ACTION_24 = new ExpandAllAction(this, IconSize.SMALL);
	public transient final Action EXPAND_ALL_ACTION_32 = new ExpandAllAction(this, IconSize.MIDDLE);

	public transient final Action COLLAPSE_ALL_ACTION_24 = new CollapseAllAction(this, IconSize.SMALL);
	public transient final Action COLLAPSE_ALL_ACTION_32 = new CollapseAllAction(this, IconSize.MIDDLE);

	public transient final LockTreeStructureAction TOGGLE_STRUCTURE_LOCK_ACTION_24 = new LockTreeStructureAction(this, IconSize.SMALL);
	public transient final LockTreeStructureAction TOGGLE_STRUCTURE_LOCK_ACTION_32 = new LockTreeStructureAction(this, IconSize.MIDDLE);
	
	/** The main frame. Used for conditional action updates and property table settings. */
	private MainFrame mainFrame;

	/** The tree model of the operator tree. */
	private transient OperatorTreeModel treeModel;

	/** The current clip board, i.e. the selected operator before cut or copy was applied. */
	private transient Operator clipBoard = null;

	/** The drag source of the operator tree */
	private DragSource dragSource;

	/** The utilities supporting DRAG & DROP operations */
	private transient DnDSupport associatedDnDSupport;

	/** Indicates if the structure is locked. This means that the structure cannot be 
	 *  changed via drag and drop and only parameters can be changed. */
	private boolean isStructureLocked = false;
	
	// ======================================================================

	/** Creates a new operator tree. */
	public OperatorTree(MainFrame mainFrame) {
		super();
		
		this.mainFrame = mainFrame;
		// the next three lines are necessary to overwrite the default behavior
		// for these key strokes
		getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_MASK), CUT_ACTION_24);
		getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK), COPY_ACTION_24);
		getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_MASK), PASTE_ACTION_24);

		// init DnD Support
		associatedDnDSupport = new DnDSupport(this);
		dragSource = DragSource.getDefaultDragSource();
		dragSource.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_COPY_OR_MOVE, associatedDnDSupport);
		new DropTarget(this, DnDConstants.ACTION_MOVE, associatedDnDSupport, true);

		setCellRenderer(new OperatorTreeCellRenderer());
		setCellEditor(new OperatorTreeCellEditor(this));
		setEditable(true);
		setShowsRootHandles(true);
		addTreeSelectionListener(this);
		addTreeExpansionListener(this);
		addMouseListener(this);
		ToolTipManager.sharedInstance().registerComponent(this);
		setToggleClickCount(5);
        
        // forces the tree to ask the nodes for the correct row heights
        // must also be invoked after LaF changes...
        setRowHeight(0);
	}
	
	protected Object readResolve() {
		this.treeModel = new OperatorTreeModel(null, this);
		this.clipBoard = null;
		this.associatedDnDSupport = new DnDSupport(this);
		return this;
	}
	
	/** Registers this instance of the operator tree at the drag & drop support */
	public DnDSupport getAssociatedDnDSupport() {
		return associatedDnDSupport;
	}

	/** Creates a new operator tree model and restores the expansion state of the complete tree. */
	public void setOperator(Operator root) {
		boolean showDisabled = treeModel != null ? treeModel.showDisabledOperators() : true;
		this.treeModel = new OperatorTreeModel(root, this);
		this.treeModel.setShowDisabledOperators(showDisabled);
		setModel(treeModel);
		setRootVisible(true);
		restoreExpansionState(new TreePath(this.treeModel.getRoot()));
	}
	
	private void restoreExpansionState(TreePath path) {
		Operator operator = (Operator)path.getLastPathComponent();
		if (operator.isExpanded()) {
			expandPath(path);
			if (operator instanceof OperatorChain) {
				OperatorChain chain = (OperatorChain)operator;
				for (Operator child : chain.getAllInnerOperators()) {
					TreePath childPath = path.pathByAddingChild(child);
					restoreExpansionState(childPath);
				}
			}	
		} else {
			collapsePath(path);
		}
	}

	/** Returns the currently selected operator, i.e. the last operation in the current selection path. */
	public Operator getSelectedOperator() {
		TreePath path = getSelectionPath();
		if (path == null)
			return null;
		else
			return (Operator) path.getLastPathComponent();
	}

	/** Returns the current clip board. */
	public Operator getClipBoard() {
		return clipBoard;
	}

	/** Returns true if the tree structure is currently locked for drag and drop and
	 *  false otherwise. */
	public boolean isStructureLocked() {
		return isStructureLocked;
	}
	
	/** Sets the current lock status for the drag and drop locking. */
	public void setStructureLocked(boolean locked) {
		this.isStructureLocked = locked;
		TOGGLE_STRUCTURE_LOCK_ACTION_24.updateIcon();
		TOGGLE_STRUCTURE_LOCK_ACTION_32.updateIcon();
	}
	
	/** Expands the complete tree. */
	public void expandAll() {
		int row = 0;
		while (row < getRowCount()) {
			expandRow(row);
			row++;
		}
	}

	/** Collapses the complete tree. */
	public void collapseAll() {
		int row = getRowCount() - 1;
		while (row >= 0) {
			collapseRow(row);
			row--;
		}
	}

	/** This method fires a tree structure changed event for the root operator and causes the complete
	 *  expansion of the tree. Since the complete tree will be restructured after invoking this method this
	 *  method should only be invoked if a complete restructuring of the model occured, e.g. after changing
	 *  view filter setting like filtering disabled operators. Whenever possible
	 *  the method {@link #refresh()} should be used instead which only causes a recursive refresh of the 
	 *  already existing operators. */
	public void completeRefresh() {
		treeModel.fireStructureChanged(this, new TreePath(treeModel.getRoot()));
	}

	/** This method causes a refresh of the existing operators without restructuring. */
	public void refresh() {
		refresh(new TreePath(treeModel.getRoot()));
	}

	/** This method causes a refresh of the given path. */
	public void refresh(TreePath path) {
		treeModel.fireOperatorChanged(this, path);
		Object object = path.getLastPathComponent();
		int numberOfChildren = treeModel.getChildCount(object);
		for (int i = 0; i < numberOfChildren; i++) {
			Object child = treeModel.getChild(object, i);
			refresh(path.pathByAddingChild(child));
		}
	}

	/** Cuts the currently selected operator into the clipboard. */
	public void cut() {
		Operator selectedOperator = getSelectedOperator();
		if (selectedOperator != null) {
			clipBoard = selectedOperator;
			delete();
			if (mainFrame != null)
				mainFrame.enableActions();
		}	
	}
	
	/** Copies the currently selected operator into the clipboard. */
	public void copy() {
		Operator selectedOperator = getSelectedOperator();
		if (selectedOperator != null) {
			clipBoard = selectedOperator.cloneOperator(selectedOperator.getName());
			if (mainFrame != null)
				mainFrame.enableActions();
		}
	}
	
	/** Pastes the current clipboard into the tree. */
	public void paste() {
		if (clipBoard != null) {
			insert(clipBoard);
			clipBoard = clipBoard.cloneOperator(clipBoard.getName());
		}
		if (mainFrame != null)
			mainFrame.enableActions();	
	}
	
	/** The currently selected operator will be deleted. */
	public void delete() {
		Operator selectedOperator = getSelectedOperator();
		if (selectedOperator == null)
			return;
		int index = treeModel.getIndexOfChild(selectedOperator.getParent(), selectedOperator);
		selectedOperator.remove();
		treeModel.fireOperatorRemoved(this, getSelectionPath().getParentPath(), index, selectedOperator);
		if (mainFrame != null) {
			mainFrame.processChanged();
			mainFrame.enableActions();
		}
	}

	/** The given operator will be inserted at the last position of the currently selected operator chain. */
	public void insert(Operator newOperator) {
		Operator selectedOperator = getSelectedOperator();
		if (selectedOperator == null)
			return;
		if (selectedOperator instanceof OperatorChain) {
			int index = ((OperatorChain) selectedOperator).addOperator(newOperator);
			treeModel.fireOperatorInserted(this, getSelectionPath(), index, newOperator);
			scrollPathToVisible(getSelectionPath().pathByAddingChild(newOperator));
			if (mainFrame != null)
				mainFrame.processChanged();
		} else {
			OperatorChain parentChain = selectedOperator.getParent();
			int parentIndex = parentChain.getIndexOfOperator(selectedOperator, true) + 1;
			int index = parentChain.addOperator(newOperator, parentIndex);
			treeModel.fireOperatorInserted(this, getSelectionPath().getParentPath(), index, newOperator);
			scrollPathToVisible(getSelectionPath().getParentPath().pathByAddingChild(newOperator));
			if (mainFrame != null)
				mainFrame.processChanged();
		}
	}
	
	/** Renames the currently selected operator. */
	public void renameOperator() {
		TreePath path = getSelectionPath();
		if (path != null) {
			// returns immediately... no refresh possible after this method
			startEditingAtPath(path);
		}
	}

	/** The currently selected operator will be replaced by the given operator. */
	public void replace(Operator operator) {
		Operator selectedOperator = getSelectedOperator();
		if (selectedOperator == null)
			return;
		OperatorChain parent = selectedOperator.getParent();
		if (parent == null)
			return;
		int oldPos = treeModel.getIndexOfChild(parent, selectedOperator);

		if ((selectedOperator instanceof OperatorChain) && (operator instanceof OperatorChain)) {
			OperatorChain chain = (OperatorChain) selectedOperator;
			OperatorChain newChain = (OperatorChain) operator;
			while (chain.getNumberOfAllOperators() > 0) {
				Operator child = chain.getOperatorFromAll(0);
				child.remove();
				newChain.addOperator(child);
			}
		}

		selectedOperator.remove();
		parent.addOperator(operator, oldPos);
		TreePath path = getSelectionPath().getParentPath();
		treeModel.fireStructureChanged(this, path);
		setSelectionPath(path.pathByAddingChild(operator));
		if (mainFrame != null)
			mainFrame.processChanged();

	}

	/** Shows the info dialog for the currently selected operator. */
	public void showOperatorInfo() {
		Operator selectedOperator = getSelectedOperator();
		if (selectedOperator != null) {
			OperatorInfoScreen infoScreen = new OperatorInfoScreen(mainFrame, selectedOperator);
			infoScreen.setVisible(true);
		}	
	}
	
	public void addAllBreakpoints() {
		addAllBreakpoints((Operator) treeModel.getRoot());
		refresh();
	}

	private void addAllBreakpoints(Operator operator) {
		operator.setBreakpoint(BreakpointListener.BREAKPOINT_BEFORE, false);
		operator.setBreakpoint(BreakpointListener.BREAKPOINT_WITHIN, false);
		operator.setBreakpoint(BreakpointListener.BREAKPOINT_AFTER, true);
		if (operator instanceof OperatorChain) {
			OperatorChain chain = (OperatorChain) operator;
			for (int i = 0; i < chain.getNumberOfOperators(); i++) {
				addAllBreakpoints(chain.getOperator(i));
			}
		}
	}

	public void removeAllBreakpoints() {
		removeAllBreakpoints((Operator) treeModel.getRoot());
		refresh();
	}

	private void removeAllBreakpoints(Operator operator) {
		operator.setBreakpoint(BreakpointListener.BREAKPOINT_BEFORE, false);
		operator.setBreakpoint(BreakpointListener.BREAKPOINT_WITHIN, false);
		operator.setBreakpoint(BreakpointListener.BREAKPOINT_AFTER, false);
		if (operator instanceof OperatorChain) {
			OperatorChain chain = (OperatorChain) operator;
			for (int i = 0; i < chain.getNumberOfOperators(); i++) {
				removeAllBreakpoints(chain.getOperator(i));
			}
		}
	}
	
	/** Toggles if the currently selected operator should be enabled. */
	public void toggleOperatorActivation(boolean state) {
		Operator selectedOperator = getSelectedOperator();
		if (selectedOperator != null) {
			selectedOperator.setEnabled(state);
			//completeRefresh();
			repaint();
			if (mainFrame != null)
				mainFrame.processChanged();
		}	
	}
	
	/** Toggles if disabled operators should be shown. */
	public void toggleShowDisabledOperators() {
		treeModel.setShowDisabledOperators(!treeModel.showDisabledOperators());
		completeRefresh();
	}

	/** This method toggles the breakpoint with the given position into the given state. */
	public void toggleBreakpoint(int position, boolean state) {
		Operator selectedOperator = getSelectedOperator();
		if (selectedOperator != null) {
			selectedOperator.setBreakpoint(position, state);
			TOGGLE_BREAKPOINT[position].setSelected(state);
			refresh();
			if (mainFrame != null)
				mainFrame.processChanged();
		}
	}

	/** This method will be invoked after a user selection of an operator in the tree. Causes
	 *  a property table update and an update of the conditional action container. */
	public void valueChanged(TreeSelectionEvent e) {
		Operator selectedOperator = getSelectedOperator();
        // important in order to save the last editing:
		if (mainFrame != null) {
			mainFrame.getPropertyTable().editingStopped(new ChangeEvent(this));
			mainFrame.getMainProcessEditor().changeFromNewOperator2ParameterEditor();
			mainFrame.notifyEditorsOfChange(selectedOperator);
		}
		if (selectedOperator == null)
			return;
		if (mainFrame != null)
			mainFrame.enableActions();
		for (int i = 0; i < TOGGLE_BREAKPOINT.length; i++)
			TOGGLE_BREAKPOINT[i].setState(selectedOperator.hasBreakpoint(i));
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mouseClicked(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
		int selRow = getRowForLocation(e.getX(), e.getY());
		TreePath selPath = getPathForLocation(e.getX(), e.getY());
		if (selRow != -1) {
			if (e.getClickCount() == 1) {
				evaluateSingleClick(selRow, selPath);
			} else if (e.getClickCount() == 2) {
				evaluateDoubleClick(selRow, selPath);
			}
		}
		evaluatePopup(e);
	}

	public void mouseReleased(MouseEvent e) {
		evaluatePopup(e);
	}

	/** Invokes the selection and causes an update of the conditional action list. */
	private void evaluateSingleClick(int row, TreePath path) {
		setSelectionPath(path);
		if (mainFrame != null)
			mainFrame.enableActions();
	}

	/** Removes existing breakpoints or add a new breakpoint after the currently selected operator. */
	private void evaluateDoubleClick(int row, TreePath path) {
		setSelectionPath(path);
		if (getSelectedOperator().hasBreakpoint()) {
			toggleBreakpoint(BreakpointListener.BREAKPOINT_BEFORE, false);
			toggleBreakpoint(BreakpointListener.BREAKPOINT_WITHIN, false);
			toggleBreakpoint(BreakpointListener.BREAKPOINT_AFTER, false);
		} else {
			toggleBreakpoint(BreakpointListener.BREAKPOINT_AFTER, true);
		}
	}

	/** Checks if the given mouse event is a popup trigger and creates a new popup menu if necessary. */
	private void evaluatePopup(MouseEvent e) {
		if (e.isPopupTrigger()) {
			createOperatorPopupMenu().show(this, e.getX(), e.getY());
		}
	}

	/** Adds the operator tree actions to the given menu. */
	public void addOperatorMenuItems(JMenu menu) {
		menu.add(RENAME_OPERATOR_ACTION_24);
		menu.add(COPY_ACTION_24);
		menu.add(CUT_ACTION_24);
		menu.add(PASTE_ACTION_24);
		menu.add(DELETE_OPERATOR_ACTION_24);
		menu.addSeparator();
		menu.add(SAVE_BUILDING_BLOCK_ACTION_24);
	}

	/** Creates a new popup menu for the selected operator. */
	private JPopupMenu createOperatorPopupMenu() {
		Operator op = getSelectedOperator();

		JPopupMenu menu = new JPopupMenu();
		if ((op != null) && (op instanceof OperatorChain))
			menu.add(OperatorMenu.NEW_OPERATOR_MENU);

		if ((op != null) && (!(op instanceof ProcessRootOperator))) {
			if ((op instanceof OperatorChain) && (((OperatorChain) op).getNumberOfAllOperators() > 0)) {
				menu.add(OperatorMenu.REPLACE_OPERATORCHAIN_MENU);
			} else {
				menu.add(OperatorMenu.REPLACE_OPERATOR_MENU);
			}
		}
        
        // add building block menu
		if ((op != null) && (op instanceof OperatorChain)) {
            final NewBuildingBlockMenu buildingBlockMenu = new NewBuildingBlockMenu();
			menu.add(buildingBlockMenu);
            buildingBlockMenu.addMenuListener(new MenuListener() {
                public void menuCanceled(MenuEvent e) {}
                public void menuDeselected(MenuEvent e) {}
                public void menuSelected(MenuEvent e) {
                    buildingBlockMenu.addAllMenuItems();
                }
            });
        }

        menu.add(SAVE_BUILDING_BLOCK_ACTION_24);
		menu.addSeparator();
		menu.add(RENAME_OPERATOR_ACTION_24);
		menu.add(DELETE_OPERATOR_ACTION_24);
		menu.add(COPY_ACTION_24);
		menu.add(CUT_ACTION_24);
		menu.add(PASTE_ACTION_24);
		menu.addSeparator();
		menu.add(INFO_OPERATOR_ACTION_24);
		menu.addSeparator();
		for (int i = 0; i < TOGGLE_BREAKPOINT.length; i++)
			menu.add(TOGGLE_BREAKPOINT[i]);
		menu.add(ADD_ALL_BREAKPOINTS_24);
		menu.add(REMOVE_ALL_BREAKPOINTS_24);
		menu.addSeparator();
		menu.add(EXPAND_ALL_ACTION_24);
		menu.add(COLLAPSE_ALL_ACTION_24);
		menu.add(TOGGLE_STRUCTURE_LOCK_ACTION_24);
		menu.addSeparator();
        if ((op != null) && (!(op instanceof ProcessRootOperator))) {
            ToggleActivationItem activationItem = new ToggleActivationItem(this, op.isEnabled());
            if ((op.getParent() != null) && (!op.getParent().isEnabled()))
                activationItem.setEnabled(false);
            menu.add(activationItem);
        }
		menu.add(TOGGLE_SHOW_DISABLED);
		
		return menu;
	}

	public void treeCollapsed(TreeExpansionEvent event) {
		Operator operator = (Operator)event.getPath().getLastPathComponent();
		operator.setExpanded(false);
		if (mainFrame != null)
			mainFrame.processChanged();
	}

	public void treeExpanded(TreeExpansionEvent event) {
		Operator operator = (Operator)event.getPath().getLastPathComponent();
		operator.setExpanded(true);
		if (mainFrame != null)
			mainFrame.processChanged();
	}
}
