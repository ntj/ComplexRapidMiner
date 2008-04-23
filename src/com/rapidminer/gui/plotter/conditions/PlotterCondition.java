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
package com.rapidminer.gui.plotter.conditions;

import com.rapidminer.datatable.DataTable;

/**
 * This condition class can be used to indicate that a plotter is only available for certain types
 * of data tables, e.g. only for tables with a maximal number of columns. 
 * 
 * @author Ingo Mierswa
 * @version $Id: PlotterCondition.java,v 1.1 2007/05/27 22:01:04 ingomierswa Exp $
 */
public interface PlotterCondition {

    public boolean acceptDataTable(DataTable dataTable);
 
    public String getRejectionReason(DataTable dataTable);
}
