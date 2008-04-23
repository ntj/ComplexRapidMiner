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
package com.rapidminer.operator.learner.functions.neuralnet;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;

import org.joone.engine.Layer;
import org.joone.engine.Matrix;
import org.joone.engine.Synapse;
import org.joone.io.MemoryInputSynapse;
import org.joone.io.MemoryOutputSynapse;
import org.joone.net.NeuralNet;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.gui.tools.ExtendedJScrollPane;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.learner.PredictionModel;
import com.rapidminer.tools.Tools;


/**
 * This is the model for the neural net learner.
 * 
 * @author Ingo Mierswa
 * @version $Id: NeuralNetModel.java,v 1.3 2007/07/13 22:52:15 ingomierswa Exp $
 */
public class NeuralNetModel extends PredictionModel {
	
	private static final long serialVersionUID = 2973355500655266534L;

	private NeuralNet neuralNet;
	
	private int numberOfInputAttributes;
	
	private double minLabel;
	
	private double maxLabel;
	
	public NeuralNetModel(ExampleSet exampleSet, NeuralNet neuralNet, int numberOfInputAttributes, double minLabel, double maxLabel) {
		super(exampleSet);
		this.neuralNet = neuralNet;
		this.numberOfInputAttributes = numberOfInputAttributes;
		this.minLabel = minLabel;
		this.maxLabel = maxLabel;
	}
	
	public void performPrediction(ExampleSet exampleSet, Attribute predictedLabel) throws OperatorException {
		// remove input layer inputs
		Layer input = this.neuralNet.getInputLayer();
		input.removeAllInputs();
		
		MemoryInputSynapse memInp = new MemoryInputSynapse();
		memInp.setFirstRow(1);
		memInp.setAdvancedColumnSelector("1-" + this.numberOfInputAttributes);

		input.addInputSynapse(memInp);
		memInp.setInputArray(createInputData(exampleSet));
		
		// remove output layer outputs
		Layer output = this.neuralNet.getOutputLayer();
		output.removeAllOutputs();
		
		// Now we interrogate the net once with the input patterns
		this.neuralNet.getMonitor().setTotCicles(1);
		this.neuralNet.getMonitor().setTrainingPatterns(exampleSet.size());
		this.neuralNet.getMonitor().setLearning(false);

		double[] predictions = recall(this.neuralNet);
		
		Iterator<Example> i = exampleSet.iterator();
		int counter = 0;
		while (i.hasNext()) {
			Example example = i.next();
			double prediction = predictions[counter];
			if (predictedLabel.isNominal()) {
				double scaled = (prediction - 0.5d) * 2;
				int index = scaled > 0 ? predictedLabel.getMapping().getPositiveIndex() : predictedLabel.getMapping().getNegativeIndex();
				example.setValue(predictedLabel, index);
				example.setConfidence(predictedLabel.getMapping().getPositiveString(), 1.0d / (1.0d + java.lang.Math.exp(-scaled)));
				example.setConfidence(predictedLabel.getMapping().getNegativeString(), 1.0d / (1.0d + java.lang.Math.exp(scaled)));			
			} else {
				example.setValue(predictedLabel, prediction * (this.maxLabel - this.minLabel) + this.minLabel);
			}
			counter++;
		}
	}

	private double[] recall(NeuralNet net) {
		MemoryOutputSynapse output = new MemoryOutputSynapse();

		// inject the input and get the output
		neuralNet.addOutputSynapse(output);
		neuralNet.start(); // init layers
		neuralNet.getMonitor().Go();
		neuralNet.join();
		int cc = neuralNet.getMonitor().getTrainingPatterns();
		double[] result = new double[cc];
		for (int i = 0; i < cc; i++) {
			double[] pattern = output.getNextPattern();
			result[i] = pattern[0];
		}
		neuralNet.stop();
		return result;
	}

	private double[][] createInputData(ExampleSet exampleSet) {
		double[][] result = new double[exampleSet.size()][exampleSet.getAttributes().size()];
		int counter = 0;
		Iterator<Example> i = exampleSet.iterator();
		while (i.hasNext()) {
			Example example = i.next();
			int a = 0; 
			for (Attribute attribute : exampleSet.getAttributes())
				result[counter][a++] = example.getValue(attribute);
			counter++;
		}
		return result;
	}
		
	public Component getVisualizationComponent(final IOContainer ioContainer) {
        final JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        
        final JScrollPane graphView = new ExtendedJScrollPane(new NeuralNetVisualizer(this.neuralNet));
        
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

        final Component textView = super.getVisualizationComponent(ioContainer);
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
	
	public String toString() {
		StringBuffer result = new StringBuffer("NeuralNet" + Tools.getLineSeparator());
		Vector layers = this.neuralNet.getLayers();
		Iterator i = layers.iterator();
		int layerIndex = 0;
		while (i.hasNext()) {
			Layer layer = (Layer)i.next();
			String nodeString = layer.getRows() == 1 ? "1 node" : layer.getRows() + " nodes";
			result.append("Layer '" + layer.getLayerName() + "' (" + nodeString + ")" + Tools.getLineSeparator());
			if (layerIndex > 0) {
				result.append("Input Weights:" + Tools.getLineSeparator());
				Vector inputs = layer.getAllInputs();
				Iterator o = inputs.iterator();
				while (o.hasNext()) {
					Object object = o.next();
					if (object instanceof Synapse) {
						Synapse synapse = (Synapse)object;
						Matrix weights = synapse.getWeights();
						// #rows --> input nodes
						// #columns --> output nodes
						if (weights != null) {
							int inputRows  = weights.getM_rows();
							int outputRows = weights.getM_cols();
							for (int y = 0; y < outputRows; y++) {
								result.append("Node " + (y + 1) + Tools.getLineSeparator());
								for (int x = 0; x < inputRows; x++) {
									result.append(weights.value[x][y] + Tools.getLineSeparator());
								}
							}
						}
					}
				}
			}
			layerIndex++;
		}
		return result.toString();
	}
}
