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
package com.rapidminer.gui.processeditor;

import com.rapidminer.operator.Operator;

/**
 * Interface for a GUI component that can display and/or edit an process.
 * (e.g. the tree, the xml-text...) Thus, several views on the process can be
 * added to a tabbed pane. The methods of this interface are mainly used to perform
 * checks and to notify that the process has changed.
 * 
 * @author Ingo Mierswa
 * @version $Id: ProcessEditor.java,v 1.1 2007/06/07 17:12:24 ingomierswa Exp $
 */
public interface ProcessEditor {

	/** Notifies the component that the process has changed. */
	public void processChanged(Operator rootOperator);

	/** Makes the component check all changes it made to the process. In this method the
	 *  changes should also be finalized. */
	public void validateProcess() throws Exception;
	
	/** Sets the currently selected operator. */
	public void setCurrentOperator(Operator operator);
}
