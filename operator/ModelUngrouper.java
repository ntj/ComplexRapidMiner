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
package com.rapidminer.operator;

/**
 * <p>This operator ungroups a previously grouped model ({@link ModelGrouper}) and
 * delivers the grouped input models.</p>
 * 
 * <p>This operator replaces the automatic model grouping known from previous
 * versions of RapidMiner. The explicit usage of this ungrouping operator gives
 * the user more control about the ungrouping procedure. Single models can 
 * be grouped with the {@link ModelGrouper} operator.</p>
 * 
 * @author Ingo Mierswa
 * @version $Id: ModelUngrouper.java,v 1.3 2008/07/13 16:39:42 ingomierswa Exp $
 */
public class ModelUngrouper extends Operator {

	public ModelUngrouper(OperatorDescription description) {
		super(description);
	}

	@Override
	public IOObject[] apply() throws OperatorException {
		Model model = getInput(Model.class);
		
		if (!(model instanceof GroupedModel)) {
			throw new UserError(this, 122, "GroupedModel");
		}
		
		GroupedModel groupedModel = (GroupedModel) model;
		IOObject[] result = new IOObject[groupedModel.getNumberOfModels()];
		int index = 0;
		for (Model inner : groupedModel) {
			result[index++] = inner;
		}
		return result;
	}

	@Override
	public Class<?>[] getInputClasses() {
		return new Class[] { Model.class };
	}

	@Override
	public Class<?>[] getOutputClasses() {
		return new Class[] { Model.class };
	}
}
