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

import java.awt.Component;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JLabel;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.gui.tools.ExtendedJScrollPane;
import com.rapidminer.gui.viewer.ContainerModelViewer;
import com.rapidminer.tools.Tools;


/**
 * This model is a container for all models which should be applied in a
 * sequence.
 * 
 * @author Ingo Mierswa
 * @version $Id: ContainerModel.java,v 1.7 2008/05/09 19:23:19 ingomierswa Exp $
 */
public class ContainerModel extends AbstractModel {
	
	private static final long serialVersionUID = -2509295741893318568L;
	
	/** Contains all models. */
	private List<Model> models = new ArrayList<Model>();

	public ContainerModel() {
		super(null);
	}

	/** Applies all models. */
	public ExampleSet apply(ExampleSet exampleSet) throws OperatorException {
		for(Model model: models) {
			exampleSet = model.apply(exampleSet);
		}
        return exampleSet;
	}

	/** Returns true. NOTE: It might be that one of the contained models is not
     *  updatable, in this case the method updateModel() will throw an UserError. */
	public boolean isUpdatable() {
		return true;
	}
	
	/** Updates the model if the classifier is updatable. Otherwise, an 
	 *  {@link UserError} is thrown. */
	public void updateModel(ExampleSet updateExampleSet) throws OperatorException {		
		Iterator<Model> i = models.iterator();
		while (i.hasNext()) {
			i.next().updateModel(updateExampleSet);
		}
	}
	
	public String getName() {
		return "Model";
	}

	/** Adds the given model to the container. */
	public void addModel(Model model) {
		models.add(model);
	}

	/** Removes the given model from the container. */
	public void removeModel(Model model) {
		models.remove(model);
	}

	/** Returns the total number of models. */
	public int getNumberOfModels() {
		return models.size();
	}

	/** Returns the i-th model. */
	public Model getModel(int index) {
		return models.get(index);
	}
	
	/** Returns the first model in this container with the desired class. A cast is not necessary. */
	@SuppressWarnings("unchecked")
	public <T extends Model> T getModel(Class<T> desiredClass) {
		Iterator<Model> i = models.iterator();
		while (i.hasNext()) {
			Model model = i.next();
			if (desiredClass.isAssignableFrom(model.getClass()))
				return (T)model;
		}
		return null;
	}

	/**
	 * Invokes the method for all models. Please note that this method will only
	 * throw an exception if no model was able to handle the given parameter.
	 */
	public void setParameter(String key, Object value) throws OperatorException {
		boolean ok = false;
		Iterator<Model> i = models.iterator();
		while (i.hasNext()) {
			try {
				i.next().setParameter(key, value);
				ok = true;
			} catch (OperatorException e) {}
		}
		if (!ok)
			throw new UserError(null, 204, getName(), key);
	}

	public String toString() {
		StringBuffer result = new StringBuffer("Model [");
		for (int i = 0; i < getNumberOfModels(); i++) {
			if (i != 0)
				result.append(", ");
			result.append(getModel(i).toString());
		}
		result.append("]");
		return result.toString();
	}

	public String toResultString() {
		StringBuffer result = new StringBuffer();
		for (int i = 0; i < getNumberOfModels(); i++) {
			result.append((i + 1) + ". " + getModel(i).toResultString() + Tools.getLineSeparator());
		}
		return result.toString();
	}

	/** Returns a visualization component with a model selector. */
	public Component getVisualizationComponent(IOContainer container) {
		if (getNumberOfModels() == 0) {
			return new JLabel("<html><h1>Empty model container.</h1></html>");
		} else if (getNumberOfModels() == 1) {
			return getModel(0).getVisualizationComponent(container);
		} else {
			return new ExtendedJScrollPane(new ContainerModelViewer(this, container));
		}
	}
}
