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
 * <p>This operator groups all input models together into a grouped (combined)
 * model. This model can be completely applied on new data or written into
 * a file as once. This might become useful in cases where preprocessing and
 * prediction models should be applied together on new and unseen data.</p>
 * 
 * <p>This operator replaces the automatic model grouping known from previous
 * versions of RapidMiner. The explicit usage of this grouping operator gives
 * the user more control about the grouping procedure. A grouped model can
 * be ungrouped with the {@link ModelUngrouper} operator.</p>
 * 
 * <p>Please note that the input models will be added in reverse order, i.e. the 
 * last created model, which is usually the first one at the start of the io object, 
 * queue will be added as the last model to the combined group model.</p>
 *   
 * @author Ingo Mierswa
 * @version $Id: ModelGrouper.java,v 1.4 2008/08/15 12:53:30 ingomierswa Exp $
 */
public class ModelGrouper extends Operator {

	public ModelGrouper(OperatorDescription description) {
		super(description);
	}

	@Override
	public IOObject[] apply() throws OperatorException {
		GroupedModel groupedModel = new GroupedModel();
		try {
			while (true) {
				Model model = getInput(Model.class);
				groupedModel.prependModel(model);
			}
		} catch (MissingIOObjectException e) {
			// do nothing
		}
		return new IOObject[] { groupedModel };
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
