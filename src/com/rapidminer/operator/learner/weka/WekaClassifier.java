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
package com.rapidminer.operator.learner.weka;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JToolBar;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.learner.PredictionModel;
import com.rapidminer.tools.Tools;
import com.rapidminer.tools.WekaInstancesAdaptor;
import com.rapidminer.tools.WekaTools;

import weka.classifiers.Classifier;
import weka.classifiers.UpdateableClassifier;
import weka.core.Drawable;
import weka.core.Instance;
import weka.core.Instances;
import weka.gui.graphvisualizer.GraphVisualizer;
import weka.gui.treevisualizer.PlaceNode2;
import weka.gui.treevisualizer.TreeDisplayEvent;
import weka.gui.treevisualizer.TreeDisplayListener;
import weka.gui.treevisualizer.TreeVisualizer;

/**
 * A Weka {@link weka.classifiers.Classifier} which can be used to classify
 * {@link Example}s. It is learned by the {@link GenericWekaLearner} and the
 * {@link GenericWekaMetaLearner}.
 * 
 * @author Ingo Mierswa
 * @version $Id: WekaClassifier.java,v 1.7 2008/05/09 19:23:19 ingomierswa Exp $
 */
public class WekaClassifier extends PredictionModel {

	private static final long serialVersionUID = -2684252543419537079L;

	/** The used weka classifier. */
	private Classifier classifier;

	/** The name of the classifier. */
	private String name;

	public WekaClassifier(ExampleSet exampleSet, String name, Classifier classifier) {
		super(exampleSet);
		this.name = name;
		this.classifier = classifier;
	}

	/** Returns true if the Weka classifier is updatable. */
	public boolean isUpdatable() { 
		return (classifier instanceof UpdateableClassifier); 
	}
	
	/** Updates the model if the classifier is updatable. Otherwise, an 
	 *  {@link UnsupportedOperationException} is thrown. */
	public void updateModel(ExampleSet updateExampleSet) throws OperatorException {
		if (classifier instanceof UpdateableClassifier) {
			UpdateableClassifier updateableClassifier = (UpdateableClassifier)classifier;
			updateClassifier(updateableClassifier, updateExampleSet);
		} else {
			throw new UserError(null, 135, getClass().getName() + " (" + classifier.getClass() + ")");
		}
	}
	
	private void updateClassifier(UpdateableClassifier classifier, ExampleSet exampleSet) throws OperatorException {
		log("Update Weka classifier.");
		log("Converting to Weka instances.");
		Instances instances = WekaTools.toWekaInstances(exampleSet, "UpdateInstances", WekaInstancesAdaptor.LEARNING);
		log("Actually updating Weka classifier.");
		try {
			for (int i = 0; i < instances.numInstances(); i++) {
				Instance instance = instances.instance(i++);
				classifier.updateClassifier(instance);
			}
		} catch (Exception e) {
			throw new UserError(null, 310, "updating Weka model", e.getMessage());
		}
	}
	
	public ExampleSet performPrediction(ExampleSet exampleSet, Attribute predictedLabel) throws OperatorException {
		log("Applying Weka classifier.");
		log("Converting to Weka instances.");
		Instances instances = WekaTools.toWekaInstances(exampleSet, "ApplierInstances", WekaInstancesAdaptor.PREDICTING);
		log("Actually applying Weka classifier.");
		int i = 0;
		Iterator<Example> r = exampleSet.iterator();
		while (r.hasNext()) {
			Example e = r.next();
			Instance instance = instances.instance(i++);
			applyModelForInstance(instance, e, predictedLabel);
		}
		
		return exampleSet;
	}

	/**
	 * Classifies ervery weka instance and sets the result as predicted label of
	 * the current example.
	 */
	public void applyModelForInstance(Instance instance, Example e, Attribute predictedLabelAttribute) {
		double predictedLabel = Double.NaN;
		try {
			double wekaPrediction = classifier.classifyInstance(instance);
			if (predictedLabelAttribute.isNominal()) {
				double confidences[] = classifier.distributionForInstance(instance);
				for (int i = 0; i < confidences.length; i++) {
					String classification = instance.classAttribute().value(i);
					e.setConfidence(classification, confidences[i]);
				}
				String classification = instance.classAttribute().value((int) wekaPrediction);
				predictedLabel = predictedLabelAttribute.getMapping().mapString(classification);
			} else {
				predictedLabel = classifier.classifyInstance(instance);
			}
		} catch (Exception exc) {
			logError("Exception occured while classifying example:" + exc.getMessage() + " [" + exc.getClass() + "]");
		}
		e.setValue(predictedLabelAttribute, predictedLabel);
	}

	public String getName() {
		return this.name;
	}

	public String toString() {
		return this.name + " (model for label " + getLabel() + ")" + Tools.getLineSeparator() + classifier.toString();
	}

	public String toResultString() {
		return classifier.toString();
	}

	private Component createTextAndGraphView(final Component textView, final Component graphView) {
		final JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		
		final JRadioButton graphViewButton = new JRadioButton("Graph View", true);
		graphViewButton.setToolTipText("Changes to a graphical view of this model.");
		graphViewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (graphViewButton.isSelected()) {
					mainPanel.remove(1);
					mainPanel.add(graphView, BorderLayout.CENTER);
					mainPanel.repaint();
				}
			}
		});

		final JRadioButton textViewButton = new JRadioButton("Text View", true);
		textViewButton.setToolTipText("Changes to a textual view of this model.");
		textViewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (textViewButton.isSelected()) {
					mainPanel.remove(1);
					mainPanel.add(textView, BorderLayout.CENTER);
					mainPanel.repaint();
				}
			}
		});
		
		ButtonGroup group = new ButtonGroup();
		group.add(textViewButton);
		group.add(graphViewButton);
		JPanel togglePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		togglePanel.add(textViewButton);
		togglePanel.add(graphViewButton);

		mainPanel.add(togglePanel, BorderLayout.NORTH);
		mainPanel.add(graphView, BorderLayout.CENTER);
		graphViewButton.setSelected(true);
		
		return mainPanel;
	}
	
	public Component getVisualizationComponent(IOContainer container) {			
		if (classifier instanceof Drawable) {
			try {
				Drawable drawable = (Drawable) classifier;
				int graphType = drawable.graphType();
				switch (graphType) {
				case Drawable.TREE:
					Component treeView = new TreeVisualizer(new TreeDisplayListener() {
						public void userCommand(TreeDisplayEvent e) {}
					}, 
					drawable.graph(), new PlaceNode2());
					return createTextAndGraphView(super.getVisualizationComponent(container), treeView);
				case Drawable.BayesNet:
					GraphVisualizer visualizer = new GraphVisualizer();
                    
                    // remove graph tool bar from original location (NORTH)
                    JToolBar graphTools = (JToolBar)visualizer.getComponent(0);
                    visualizer.remove(graphTools);
                    
                    // remove progress bar from tool bar
                    graphTools.remove(graphTools.getComponentCount() - 1);
                    
                    // add tool bar to new location (WEST)
                    JPanel toolPanel = new JPanel(new BorderLayout());
                    toolPanel.add(graphTools, BorderLayout.NORTH);
                    visualizer.add(toolPanel, BorderLayout.WEST);
                    
                    // init graph
					visualizer.readBIF(drawable.graph());
					visualizer.layoutGraph();
					return createTextAndGraphView(super.getVisualizationComponent(container), visualizer);
				case Drawable.NOT_DRAWABLE:
				default:
					return super.getVisualizationComponent(container);
				}
			} catch (Exception e) {
				return super.getVisualizationComponent(container);
			}
		} else {
			return super.getVisualizationComponent(container);
		}
	}

	public boolean equals(Object o) {
		if (!super.equals(o))
			return false;
		WekaClassifier other = (WekaClassifier) o;
		if (!other.classifier.equals(this.classifier))
			return false;
		return true;
	}
	
	public int hashCode() {
		return this.classifier.hashCode();
	}
}
