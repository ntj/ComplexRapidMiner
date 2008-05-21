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

import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.dnd.InvalidDnDOperationException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.List;

import javax.swing.Timer;
import javax.swing.tree.TreePath;

import com.rapidminer.gui.RapidMinerGUI;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorChain;


/** 
 * Provides all necessary implementations of standard DRAG & DROP features 
 * for the main process view of the RapidMiner GUI.
 *
 * @see com.rapidminer.gui.operatortree.OperatorTree
 * @author Helge Homburg 
 * @version $Id: DnDSupport.java,v 1.5 2008/05/09 19:23:26 ingomierswa Exp $
 */
public class DnDSupport implements DropTargetListener, DragSourceListener, DragGestureListener {

	/** The OperatorTree associated to this class */
	private OperatorTree operatorTree;

	/** Several fields for storage of mouse positions */
	private Point previousCursorLocation = new Point();

	private Point currentCursorLocation;

	private Point operatorPosition;

	/** Fields used for placing droplines at the right position */
	private static String[] markedOperator = new String[3];

	private int previousDnDMarker = 0;

	private int currentDnDMarker = 0;

	private boolean changeHappened;
	
	public static final int fullMarker = 0;

	public static final int upperMarker = 1;

	public static final int lowerMarker = 2;

	/** Several variables needed for temporary storage of path locations */
	private TreePath originPath;

	private TreePath currentPath;

	private TreePath previousPath;

	/** A timer needed for automatic node expansion */
	private Timer nodeExpandDelay;

	/** Defines all actions that are allowed for drag & drop */
	private final Integer[] myDnDActions = { 
		Integer.valueOf(DnDConstants.ACTION_COPY), 
		Integer.valueOf(DnDConstants.ACTION_MOVE), 
		Integer.valueOf(DnDConstants.ACTION_COPY_OR_MOVE) 
	};

	/** A list of all acceptable DnD-actions */
	private final List acceptedDnDActions = Arrays.asList(myDnDActions);

	// ------ Constructor ------------------------------------------------------------------

	public DnDSupport(OperatorTree tree) {
		this.operatorTree = tree;
		setupNodeExpandTimer();
	}

	public static void setMarkedOperator(String[] marked) {
		markedOperator = marked;
	}
	
	//----- methods from DropTargetListener -------------------------------------------------

	public void dragEnter(DropTargetDragEvent e) {
		if (dragAllowed(e) == false) {
			e.rejectDrag();
			return;
		}
		previousCursorLocation = e.getLocation();
		e.acceptDrag(e.getDropAction());
	}

	public void drop(DropTargetDropEvent e) {
		// test if DataFlavor and origin of the dragged object is acceptable
		DataFlavor[] currentFlavors = TransferableOperator.DATA_FLAVORS;
		DataFlavor acceptedFlavor = null;
		if (e.isLocalTransfer() == false) {
			acceptedFlavor = TransferableOperator.TRANSFERRED_OPERATOR_FLAVOR;
		} else {			
			for (int i = 0; i < currentFlavors.length; i++) {
				if (e.isDataFlavorSupported(currentFlavors[i])) {
					acceptedFlavor = currentFlavors[i];
					break;
				}
				;
			}
		}
		if (acceptedFlavor == null) {
			e.rejectDrop();
			return;
		}
		// test if the drag action is supported
		if (acceptedDnDActions.contains(Integer.valueOf(e.getDropAction())) == false) {
			e.rejectDrop();
			return;
		}
		// receive an operator from the transferable object
		Transferable receivedOperator = null;
		Operator newOperator = null;
		try {
			e.acceptDrop(e.getDropAction());
			receivedOperator = e.getTransferable();
			if (receivedOperator == null)
				throw new NullPointerException();
			if (receivedOperator.getTransferData(acceptedFlavor) instanceof Operator) {
				newOperator = (Operator) receivedOperator.getTransferData(acceptedFlavor);
			} else {
				newOperator = null;
			}
		} catch (Throwable t) {
			t.printStackTrace();
			e.dropComplete(false);
			return;
		}
		// determine a location for the drop action
		Operator targetOperator, newParentOperator;
		Point dropLocation = e.getLocation();
		TreePath dropLocationPath = operatorTree.getClosestPathForLocation(dropLocation.x, dropLocation.y);
		if (dropLocationPath != null) {
			targetOperator = (Operator) dropLocationPath.getLastPathComponent();
		} else {
			e.dropComplete(false);
			return;
		}
		if (targetOperator == null) {
			e.dropComplete(false);
			return;
		}
		// test if operator(chain) is illegally moved or copied into itself
		boolean notDownDrag = true;
		if (targetOperator.equals(newOperator))
			notDownDrag = false;
		OperatorChain moveUP = targetOperator.getParent();
		while (true) {
			if (moveUP == null)
				break;
			if (moveUP.equals(newOperator))
				notDownDrag = false;
			moveUP = moveUP.getParent();
		}
		if (notDownDrag) {
			if (e.getDropAction() == DnDConstants.ACTION_MOVE) {
				int indexDelete = newOperator.getParent().getIndexOfOperator(newOperator, true);
				TreePath oldLocation = originPath.getParentPath();
				newOperator.remove();
				((OperatorTreeModel) operatorTree.getModel()).fireOperatorRemoved(this, oldLocation, indexDelete, newOperator);
			} else if (e.getDropAction() == DnDConstants.ACTION_COPY) {
                newOperator = newOperator.cloneOperator(newOperator.getName());
            }
               
			int indexOfTargetOperator = 0;
			// finally insert the operator at the intended position
			newParentOperator = null;
			if (targetOperator.getParent() != null) {
				indexOfTargetOperator = targetOperator.getParent().getIndexOfOperator(targetOperator, true);
				newParentOperator = targetOperator.getParent();
			}
			int droplineIndicator = this.getOperatorMarker(targetOperator.getName());
			int indexInsert = 0;
            
			if (droplineIndicator < 0) {
				e.dropComplete(false);
				return;
			}
           
            // actually adding of operator into parent
			if (droplineIndicator == 0 || droplineIndicator > 2) {
				indexInsert = ((OperatorChain) targetOperator).addOperator(newOperator);
			}
			if (droplineIndicator == 1) {
				if (indexOfTargetOperator >= 0)
					indexInsert = indexOfTargetOperator;
				((OperatorChain) newParentOperator).addOperator(newOperator, indexInsert);
				dropLocationPath = dropLocationPath.getParentPath();
			}
			if (droplineIndicator == 2) {
				if (indexOfTargetOperator < ((OperatorChain) newParentOperator).getNumberOfAllOperators())
					indexInsert = indexOfTargetOperator + 1;
				((OperatorChain) newParentOperator).addOperator(newOperator, indexInsert);
				dropLocationPath = dropLocationPath.getParentPath();
			}
            // end of adding
            
			((OperatorTreeModel) operatorTree.getModel()).fireOperatorInserted(this, dropLocationPath, indexInsert, newOperator);
			operatorTree.scrollPathToVisible(dropLocationPath.pathByAddingChild(newOperator));
			RapidMinerGUI.getMainFrame().processChanged();
		} else {			
			e.dropComplete(true);
			return;
		}		
		clearOperatorMarker();
		e.dropComplete(true);
	}

	public void dragOver(DropTargetDragEvent e) {
		if (dragAllowed(e) == false) {
			e.rejectDrag();
			return;
		}
		currentCursorLocation = e.getLocation();
		if ((!currentCursorLocation.equals(previousCursorLocation))) {
			currentPath = operatorTree.getClosestPathForLocation(currentCursorLocation.x, currentCursorLocation.y);
			Rectangle dropActionTriggerArea = operatorTree.getPathBounds(currentPath);
			if (dropActionTriggerArea != null) {			
					Operator currentDropZone = (Operator) currentPath.getLastPathComponent();
					if (currentDropZone != null) {
						// start the computation of the dropline position
						operatorPosition = operatorTree.getPathBounds(currentPath).getLocation();
						double operatorHeight = operatorTree.getPathBounds(currentPath).getHeight();
						long aQuarter = Math.round(operatorHeight / 4);
						long aHalf = Math.round(operatorHeight / 2);
						long currentPartition;
						boolean dropTargetIsOperatorChain = currentDropZone instanceof OperatorChain;
						if (dropTargetIsOperatorChain) { 
							currentPartition = aQuarter; 
						} else {currentPartition = aHalf; }
						String[] cleanMarkedOperator = { "", "", "" };
						setMarkedOperator(cleanMarkedOperator);
						if (currentDropZone.getParent() != null) {
							if (currentCursorLocation.y < (operatorPosition.y + currentPartition)) {
								markedOperator[1] = currentDropZone.getName();
								currentDnDMarker = 1;
							}
							if (currentCursorLocation.y > (operatorPosition.y + operatorHeight - currentPartition)) {
								markedOperator[2] = currentDropZone.getName();
								currentDnDMarker = 2;
							}
						}
						if (dropTargetIsOperatorChain) {
							if (((currentCursorLocation.y >= (operatorPosition.y + aQuarter)) && (currentCursorLocation.y <= (operatorPosition.y + operatorHeight - aQuarter))) || currentDropZone.getParent() == null) {
								markedOperator[0] = currentDropZone.getName();
								currentDnDMarker = 0;
								// start the timer for automatic nodeexpansion 
								nodeExpandDelay.restart();
							}
						}
						if ((previousDnDMarker != currentDnDMarker) || (previousPath != currentPath))
							changeHappened = true;
						if (changeHappened)
							operatorTree.treeDidChange();
						changeHappened = false;
						previousDnDMarker = currentDnDMarker;
						previousPath = currentPath;
						operatorTree.treeDidChange();					
					}
				}			
			// autoscroll to possible dropzone
			Insets insets = new Insets(40, 40, 40, 40);
			Rectangle currentlyVisible = operatorTree.getVisibleRect();
			Rectangle validCursorArea = new Rectangle(currentlyVisible.x + insets.left, currentlyVisible.y + insets.top, currentlyVisible.width - (insets.left + insets.right), currentlyVisible.height - (insets.top + insets.bottom));
			if (!validCursorArea.contains(currentCursorLocation)) {
				Rectangle updatedArea = new Rectangle(currentCursorLocation.x - insets.left, currentCursorLocation.y - insets.top, insets.left + insets.right, insets.top + insets.bottom);
				operatorTree.scrollRectToVisible(updatedArea);
			}
			previousCursorLocation = currentCursorLocation;
		}
		e.acceptDrag(e.getDropAction());
	}

	public void dropActionChanged(DropTargetDragEvent e) {
		if (dragAllowed(e) == false) {
			e.rejectDrag();
			return;
		}
		e.acceptDrag(e.getDropAction());
	}

	public void dragExit(DropTargetEvent e) {
		clearOperatorMarker();
	}

	// ------ methods from DragSourceListener ------------------------------------------------
	public void dragDropEnd(DragSourceDropEvent e) {
		clearOperatorMarker();
		operatorTree.setEditable(true);
		if (!e.getDropSuccess()) {			
			DataFlavor acceptedFlavor = TransferableOperator.TRANSFERRED_OPERATOR_FLAVOR;
			try {	
				Transferable currentlyDraggedOperator = e.getDragSourceContext().getTransferable();
				if (currentlyDraggedOperator == null)
					throw new NullPointerException();
				if (currentlyDraggedOperator.getTransferData(acceptedFlavor) instanceof Operator) {
					Operator newOperator = (Operator) currentlyDraggedOperator.getTransferData(acceptedFlavor);
					int indexDelete = newOperator.getParent().getIndexOfOperator(newOperator, true);
					TreePath oldLocation = originPath.getParentPath();
					newOperator.remove();
					((OperatorTreeModel) operatorTree.getModel()).fireOperatorRemoved(this, oldLocation, indexDelete, newOperator);
					RapidMinerGUI.getMainFrame().processChanged();
				}
			} catch (Throwable t) {
				t.printStackTrace();			
				return;
			}			
		}
	}

	public void dragEnter(DragSourceDragEvent e) {
	}

	public void dragExit(DragSourceEvent e) {
	}

	public void dragOver(DragSourceDragEvent e) {
	}

	public void dropActionChanged(DragSourceDragEvent e) {
	}

	//------ methods from DragGestureListener -----------------------------------------------
	public void dragGestureRecognized(DragGestureEvent e) {
		// determine the operator at the current postion of the mouse pointer and make it transferable
		if (acceptedDnDActions.contains(Integer.valueOf(e.getDragAction())) == false)
			return;
		if (operatorTree.isStructureLocked())
			return;
		Point dragOrigin = e.getDragOrigin();
		originPath = operatorTree.getPathForLocation(dragOrigin.x, dragOrigin.y);
        // make clear that the originPath variable points to an existing entity AND is different to the root node
		if (originPath != null && originPath.getParentPath() != null) {
            Operator selectedOperator = (Operator) originPath.getLastPathComponent();
            if (selectedOperator != null) {
                TransferableOperator selectedTransferableOperator = new TransferableOperator(selectedOperator);
                try {
                    operatorTree.setEditable(false);
                	e.startDrag(null, null, new Point(0, 0), selectedTransferableOperator, this);
                } catch (InvalidDnDOperationException dndE) {
                    dndE.printStackTrace();
                }
            }
        }
	}

	//------ methods from DnDSupport ----------------------------------------------------
	private boolean dragAllowed(DropTargetDragEvent e) {
		if (operatorTree.isStructureLocked())
			return false;
		DataFlavor[] currentFlavors = TransferableOperator.DATA_FLAVORS;
		DataFlavor acceptedFlavor = null;
		for (int i = 0; i < currentFlavors.length; i++) {
			if (e.isDataFlavorSupported(currentFlavors[i])) {
				acceptedFlavor = currentFlavors[i];
				break;
			}
			;
		}
		if (acceptedFlavor == null)
			return false;
		if (acceptedDnDActions.contains(Integer.valueOf(e.getSourceActions())) == false)
			return false;
		return true;
	}

	public int getOperatorMarker(String operatorName) {
		List markerList = Arrays.asList(markedOperator);
		return markerList.indexOf(operatorName);
	}
	
    private void clearOperatorMarker(){
        nodeExpandDelay.stop();
        String[] cleanMarkedOperator = { "", "", "" };
        markedOperator = cleanMarkedOperator;
        operatorTree.treeDidChange();                
    }

	private void setupNodeExpandTimer() {
		nodeExpandDelay = new Timer(1500, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (operatorTree.isRootVisible() && operatorTree.getRowForPath(currentPath) == 0) {
					return;
				} else {
					if (operatorTree.isExpanded(currentPath)) {
						operatorTree.collapsePath(currentPath);
					} else {
						operatorTree.expandPath(currentPath);
					}

				}
			}
		});
		nodeExpandDelay.setRepeats(false);
	}
}
