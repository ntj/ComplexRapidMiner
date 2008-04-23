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
import java.util.Vector;

import javax.swing.JList;
import javax.swing.ListSelectionModel;

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
 * @version $Id: OperatorList.java,v 1.2 2007/06/07 17:12:23 ingomierswa Exp $
 */
public class OperatorList extends JList implements DragSourceListener, DragGestureListener  {
      
	private static final long serialVersionUID = 1L;

	/** Creates a special CellRenderer for this class */
    private OperatorListCellRenderer operatorDialogCellRenderer;
    
    /** The drag source of the NewOperatorDialog */
    private DragSource dragSource;
    
    
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
}
