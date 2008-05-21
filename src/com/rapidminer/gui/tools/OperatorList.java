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
package com.rapidminer.gui.tools;

import java.awt.Point;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.InvalidDnDOperationException;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Vector;

import javax.swing.Action;
import javax.swing.JList;
import javax.swing.JPopupMenu;
import javax.swing.ListSelectionModel;

import com.rapidminer.gui.MainFrame;
import com.rapidminer.gui.RapidMinerGUI;
import com.rapidminer.gui.dialog.OperatorInfoScreen;
import com.rapidminer.gui.operatortree.TransferableOperator;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorCreationException;
import com.rapidminer.operator.OperatorDescription;

/**
 * This class specifies a special JList which is capable of showing all available
 * kinds of RapidMiner operators, allowing the user to drag a copy of them into his own
 * process tree. The list elements must be of type {@link OperatorDescription}.
 *
 * @author Helge Homburg, Ingo Mierswa
 * @version $Id: OperatorList.java,v 1.11 2008/05/09 19:22:58 ingomierswa Exp $
 */
public class OperatorList extends JList implements DragSourceListener, DragGestureListener, MouseListener  {
   
	private static final long serialVersionUID = -2719941529572427942L;

	// ======================================================================
	// Operator Menu Actions and Items
	// ======================================================================		
	
	public transient final Action INFO_OPERATOR_ACTION_24 = new OperatorListInfoOperatorAction(this, IconSize.SMALL);
	public transient final Action INFO_OPERATOR_ACTION_32 = new OperatorListInfoOperatorAction(this, IconSize.MIDDLE);
	
	/** The main frame. Used for conditional action updates and property table settings. */
	private MainFrame mainFrame;
	
	/** Creates a special CellRenderer for this class */
    private OperatorListCellRenderer operatorDialogCellRenderer;
    
    /** The drag source of the NewOperatorDialog */    
    private DragSource dragSource;
    
    private transient Operator selectedOperator;    
    
    /** Creates a new instance of OperatorList */
    public OperatorList() {
        this(false, true);
    }
    
    /** Creates a new instance of OperatorList */
    public OperatorList(boolean horizontalWrap, boolean coloredCellBackgrounds) {
        
    	operatorDialogCellRenderer = new OperatorListCellRenderer(coloredCellBackgrounds);
        if (horizontalWrap) {
            setLayoutOrientation(HORIZONTAL_WRAP);
            setVisibleRowCount(-1);
        }
        setCellRenderer(operatorDialogCellRenderer);
        dragSource = DragSource.getDefaultDragSource(); 
        dragSource.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_COPY, this);
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        addMouseListener(this);
        this.mainFrame = RapidMinerGUI.getMainFrame();
        
    }
    
    public void setOperatorDescriptions(Vector<OperatorDescription> descriptions) {
        setListData(descriptions);
    }
    
    //  ------ methods from DragSourceListener ------------------------------------------------
    public void dragDropEnd(DragSourceDropEvent e) {           
    }

    public void dragEnter(DragSourceDragEvent e) {
    }

    public void dragExit(DragSourceEvent e) {
    }

    public void dragOver(DragSourceDragEvent e) {
    }

    public void dropActionChanged(DragSourceDragEvent e) {
    }
    
    //  ------ method from DragGestureListener -------------------------------------------------    
    /** 
     * This method determines the operator which is currently located near the mouse cursor, 
     * selects its corresponding list cell, makes the operator itself transferable and finally
     * starts the drag operation. 
     */
    public void dragGestureRecognized(DragGestureEvent e) {        
        if (Integer.valueOf(e.getDragAction()) != DnDConstants.ACTION_COPY)
            return;
        Point dragOrigin = e.getDragOrigin();
        int selectedIndex = locationToIndex(dragOrigin);
        if (selectedIndex != -1) {
            setSelectedIndex(selectedIndex);
        }
        OperatorDescription selectedListElement = (OperatorDescription)getSelectedValue();
        Operator selectedOperator = null;
        if (selectedListElement != null) {
            try {
                selectedOperator = selectedListElement.createOperatorInstance();
            } catch (OperatorCreationException ocE) {
                ocE.printStackTrace();
            }
            if (selectedOperator != null) {
                TransferableOperator selectedTransferableOperator = new TransferableOperator(selectedOperator);
                try {
                    e.startDrag(null, null, new Point(0, 0), selectedTransferableOperator, this);
                } catch (InvalidDnDOperationException dndE) {
                    dndE.printStackTrace();
                }
            }
        }
    }
    
    /** Returns the currently selected operator. */
	private Operator getSelectedOperator() {		
		
		Point clickOrigin = getMousePosition();
		if (clickOrigin == null) {
			return null;
		}
        int selectedIndex = locationToIndex(clickOrigin);
        if (selectedIndex != -1) {
            setSelectedIndex(selectedIndex);
        }
        OperatorDescription selectedListElement = (OperatorDescription)getSelectedValue();
        Operator selectedOperator = null;
        if (selectedListElement != null) {
            try {
                selectedOperator = selectedListElement.createOperatorInstance();
            } catch (OperatorCreationException ocE) {
                ocE.printStackTrace();
            }
         }		
        return selectedOperator;
	}
    	
	/** Shows the info dialog for the currently selected operator. */
	public void showOperatorInfo() {		
		if (selectedOperator != null) {
			OperatorInfoScreen infoScreen = new OperatorInfoScreen(mainFrame, selectedOperator);
			infoScreen.setVisible(true);
		}	
	}
	
	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mouseClicked(MouseEvent e) {		
	}

	public void mousePressed(MouseEvent e) {		
	}

	public void mouseReleased(MouseEvent e) {
		selectedOperator = getSelectedOperator();
		evaluatePopup(e);
	}	
	
	/** Checks if the given mouse event is a popup trigger and creates a new popup menu if necessary. */
	private void evaluatePopup(MouseEvent e) {
		if (e.isPopupTrigger()) {
			createOperatorPopupMenu().show(this, e.getX(), e.getY());
		}
	}	
	
	/** Creates a new popup menu for the selected operator. */
	private JPopupMenu createOperatorPopupMenu() {
		JPopupMenu menu = new JPopupMenu();        
		menu.add(this.INFO_OPERATOR_ACTION_24);				
		return menu;
	}
}
