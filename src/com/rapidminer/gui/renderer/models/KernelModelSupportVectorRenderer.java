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
package com.rapidminer.gui.renderer.models;

import com.rapidminer.datatable.DataTable;
import com.rapidminer.datatable.DataTableKernelModelAdapter;
import com.rapidminer.gui.renderer.AbstractDataTableTableRenderer;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.learner.functions.kernel.KernelModel;

/**
 * A renderer for the support vector table view of a kernel model. 
 * 
 * @author Ingo Mierswa
 * @version $Id: KernelModelSupportVectorRenderer.java,v 1.2 2008/07/13 16:39:41 ingomierswa Exp $
 */
public class KernelModelSupportVectorRenderer extends AbstractDataTableTableRenderer {

	public String getName() {
		return "Support Vector Table";
	}
	
	public boolean isAutoresize() {
		return false;
	}
	
	public DataTable getDataTable(Object renderable, IOContainer ioContainer) {
		KernelModel kernelModel = (KernelModel)renderable;
		return new DataTableKernelModelAdapter(kernelModel);
	}
}
