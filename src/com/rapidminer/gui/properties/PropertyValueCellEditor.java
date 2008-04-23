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
package com.rapidminer.gui.properties;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import com.rapidminer.operator.Operator;
import com.rapidminer.parameter.ParameterType;


/**
 * Interface for all value cell editors of property / parameter tables. Please note
 * that the objects of this interface are constructed via reflection and that a
 * one-argument constructor taking the {@link ParameterType} must be provided.
 * Directly after initialization the method {@link #setOperator(Operator)}
 * is invoked.
 * 
 * @author Ingo Mierswa, Simon Fischer
 * @version $Id: PropertyValueCellEditor.java,v 2.8 2006/03/21 15:35:40 ingomierswa
 *          Exp $
 */
public interface PropertyValueCellEditor extends TableCellEditor, TableCellRenderer {

    /** This method can be implemented to perform operator specific settings. */
    public void setOperator(Operator operator);
    
    /** Returns true if this editor should also be used as renderer. Should not be the case
     *  for components with frames around the component like JTextFields. */
	public boolean useEditorAsRenderer();

}
