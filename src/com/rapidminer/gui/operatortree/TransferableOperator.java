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


import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.rapidminer.operator.Operator;

/** Provides a transferable wrapper for Operators in order to drag-n-drop them in the
 *  Process-Tree. 
 *
 *  @see com.rapidminer.gui.operatortree.OperatorTree
 *  @author Helge Homburg
 *  @version $Id: TransferableOperator.java,v 1.5 2008/05/09 19:23:26 ingomierswa Exp $
 */
public class TransferableOperator implements Transferable {

	public static final DataFlavor TRANSFERRED_OPERATOR_FLAVOR = new DataFlavor(Operator.class, "transferedOperator");

	public static final DataFlavor LOCAL_TRANSFERRED_OPERATOR_FLAVOR = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType, "transferedOperatorTreeNode");

	public static final DataFlavor[] DATA_FLAVORS = { 
        TransferableOperator.TRANSFERRED_OPERATOR_FLAVOR, 
        TransferableOperator.LOCAL_TRANSFERRED_OPERATOR_FLAVOR 
	};

	private final List flavors = Arrays.asList(DATA_FLAVORS);

	private Operator transferedOperator;

	public TransferableOperator(Operator operator) {
		this.transferedOperator = operator;
	}

	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
		if (isDataFlavorSupported(flavor)) {
			return this.transferedOperator;
		} else {
			throw new UnsupportedFlavorException(flavor);
		}
	}

	public boolean isDataFlavorSupported(DataFlavor flavor) {
		return (flavors.contains(flavor));
	}

	public DataFlavor[] getTransferDataFlavors() {
		return DATA_FLAVORS;
	}
}
