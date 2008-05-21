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
package com.rapidminer.gui.properties;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.LinkedList;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.BorderFactory;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.rapidminer.gui.RapidMinerGUI;
import com.rapidminer.gui.tools.ExtendedJList;
import com.rapidminer.gui.tools.ExtendedListModel;
import com.rapidminer.gui.tools.SwingTools;
import com.rapidminer.gui.wizards.ConfigurationListener;
import com.rapidminer.Process;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorChain;
import com.rapidminer.operator.meta.ParameterIteratingOperatorChain;
import com.rapidminer.parameter.Parameters;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeFile;
import com.rapidminer.parameter.ParameterTypeNumber;
import com.rapidminer.parameter.ParameterTypeString;
import com.rapidminer.parameter.ParameterTypeStringCategory;
import com.rapidminer.parameter.ParameterTypeValue;
import com.rapidminer.parameter.value.ParameterValues;
import com.rapidminer.parameter.value.ParameterValueGrid;
import com.rapidminer.parameter.value.ParameterValueList;
import com.rapidminer.parameter.value.ParameterValueRange;
import com.rapidminer.tools.Tools;


/**
 * A Dialog which lets the user select and configure parameter values and
 * ranges for optimization purposes.
 * 
 * @author Tobias Malbrecht
 * @version $Id: ConfigureParameterOptimizationDialog.java,v 1.5 2008/05/19 10:14:27 tobiasmalbrecht Exp $
 */
public class ConfigureParameterOptimizationDialog extends JDialog {
	
	private static final long serialVersionUID = 187660784321413390L;

	private boolean ok = false;
	
	private int mode;
	
	private ExtendedListModel operatorListModel;
	
	private ExtendedListModel parametersListModel;

	private ExtendedListModel selectedParametersListModel;
	
	private ExtendedJList operatorList;
	
	private ExtendedJList parametersList;
	
	private ExtendedJList selectedParametersList;
	
	private JLabel minValueJLabel;
	
	private JLabel maxValueJLabel;
	
	private JLabel stepsValueJLabel;
	
	private JLabel gridScaleValueJLabel;

	private JFormattedTextField minValueTextField;
	
	private JFormattedTextField maxValueTextField;
	
	private JFormattedTextField stepsValueTextField;
	
	private JComboBox gridScaleValueComboBox;
	
	private JList categoriesList;
	
	private JList selectedCategoriesList;
	
	private DefaultListModel categoriesListModel;
	
	private DefaultListModel selectedCategoriesListModel;
	
	private JTextField createValueTextField;
	
	private JButton createValueButton;
	
	private JButton addValueButton;
	
	private JButton removeValueButton;
	
	private JRadioButton choseGridRadioButton;
	
	private JRadioButton choseListRadioButton;
	
	private JLabel infoLabel;
	
	private ConfigurationListener listener;
	
	private Process process;

	private LinkedHashMap<String,ParameterValues> parameterValuesMap;
	
	public ConfigureParameterOptimizationDialog(ConfigurationListener listener) {
		super(RapidMinerGUI.getMainFrame(), "Specify parameters...", true);
		this.listener = listener;
		process = listener.getProcess();
		parameterValuesMap = new LinkedHashMap<String,ParameterValues>();

		initializeDialog();
		
		ParameterIteratingOperatorChain parameterOperatorChain = (ParameterIteratingOperatorChain)listener;
		this.mode = parameterOperatorChain.getParameterValueMode();
		List<ParameterValues> readParameterValues = null;
		try {
			List parameterValueList = parameterOperatorChain.getParameterList(ParameterIteratingOperatorChain.PARAMETER_PARAMETERS);
			readParameterValues = parameterOperatorChain.parseParameterValues(parameterValueList);
		} catch (Exception e) {
			parameterOperatorChain.logWarning(e.getMessage());
		}
		if (readParameterValues != null) {
			for (ParameterValues parameterValue : readParameterValues) {
				addParameter(parameterValue);
			}
		}
		
		updateInfoLabel();
	}
	
	private void initializeDialog() {
		getContentPane().setLayout(new BorderLayout());
		JPanel selectionPanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		// initialize selection lists
		operatorListModel = new ExtendedListModel();
		for (Operator op : ((OperatorChain)listener).getAllInnerOperators()) {
			String descriptionText = op.getOperatorDescription().getLongDescriptionHTML();
			if (descriptionText == null) {
				descriptionText = op.getOperatorDescription().getShortDescription();
			}
			
			StringBuffer toolTipText = new StringBuffer("<b>Description: </b>" + descriptionText);
			if (op != null) {
	        	toolTipText.append(Tools.getLineSeparator() + "<b>Input:</b> " + SwingTools.getStringFromClassArray(op.getInputClasses()));
	        	toolTipText.append(Tools.getLineSeparator() + "<b>Output:</b> " + SwingTools.getStringFromClassArray(op.getOutputClasses()));
	        }
			operatorListModel.addElement(op, SwingTools.transformToolTipText(toolTipText.toString()));
		}
		
		operatorList = new ExtendedJList(operatorListModel);
		operatorList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		operatorList.setLayoutOrientation(JList.VERTICAL);

		parametersListModel = new ExtendedListModel();
		parametersList = new ExtendedJList(parametersListModel);
		parametersList.setLayoutOrientation(JList.VERTICAL);
	
		operatorList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				int index = operatorList.getSelectedIndex();
				if (index != -1) {
					Operator op = (Operator)operatorList.getModel().getElementAt(index);
					updateParameterListModel(op);
				}
			}
		});

		selectedParametersListModel = new ExtendedListModel();
		selectedParametersList = new ExtendedJList(selectedParametersListModel);
		selectedParametersList.setLayoutOrientation(JList.VERTICAL);
		selectedParametersList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				showParameterValues((String) selectedParametersList.getSelectedValue());
			}
		});
		
		JPanel parameterSelectionButtonsPanel = new JPanel(new BorderLayout());
		JButton addParameterButton = new JButton(">");
		addParameterButton.setToolTipText("Select parameters.");
		addParameterButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addSelectedParameters();
			}
		});
		
		JButton removeParameterButton = new JButton("<");
		removeParameterButton.setToolTipText("Deselect parameters.");
		removeParameterButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				removeSelectedParameters();
			}
		});
		parameterSelectionButtonsPanel.add(addParameterButton, BorderLayout.NORTH);
		parameterSelectionButtonsPanel.add(removeParameterButton, BorderLayout.SOUTH);
	
		JScrollPane operatorListScrollPane = new JScrollPane(operatorList);
		JScrollPane parametersListScrollPane = new JScrollPane(parametersList);
		JScrollPane selectedParametersListScrollPane = new JScrollPane(selectedParametersList);

		c.gridx=0;
		c.gridy=0;
		c.weightx=0.3;
		c.weighty=1;
		c.fill = GridBagConstraints.BOTH;
        operatorListScrollPane.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(7,7,7,7), BorderFactory.createTitledBorder("Operators")));
		selectionPanel.add(operatorListScrollPane, c);

		c.gridx=1;
		c.gridy=0;
		c.weightx=0.3;
		c.weighty=1;
		c.fill = GridBagConstraints.BOTH;
        parametersListScrollPane.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(7,7,7,7), BorderFactory.createTitledBorder("Parameters")));
		selectionPanel.add(parametersListScrollPane, c);

		c.gridx=2;
		c.gridy=0;
		c.weightx=0.1;
		c.weighty=1;
		c.fill = GridBagConstraints.NONE;
		selectionPanel.add(parameterSelectionButtonsPanel,c);
		
		c.gridx=3;
		c.gridy=0;
		c.weightx=0.3;
		c.weighty=1;
		c.fill = GridBagConstraints.BOTH;
        selectedParametersListScrollPane.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(7,7,7,7), BorderFactory.createTitledBorder("Selected Parameters")));
		selectionPanel.add(selectedParametersListScrollPane,c);
		
		getContentPane().add(selectionPanel,BorderLayout.NORTH);
		
		JPanel gridPanel = new JPanel(new GridBagLayout());
		gridPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(7,7,7,7), BorderFactory.createTitledBorder("Grid/Range")));

		c.insets=new Insets(0,10,2,10);
		c.gridy=0;
		c.weightx=0.25;
		c.gridx=0;
		minValueJLabel = new JLabel("Min");
		minValueJLabel.setEnabled(false);
		gridPanel.add(minValueJLabel, c);

		c.gridx=1;
		maxValueJLabel = new JLabel("Max");
		maxValueJLabel.setEnabled(false);
		gridPanel.add(maxValueJLabel, c);

		c.gridx=2;
		stepsValueJLabel = new JLabel("Steps");
		stepsValueJLabel.setEnabled(false);
		gridPanel.add(stepsValueJLabel, c);

		c.gridx=3;
		gridScaleValueJLabel = new JLabel("Scale");
		gridScaleValueJLabel.setEnabled(false);
		gridPanel.add(gridScaleValueJLabel, c);
		
		minValueTextField = new JFormattedTextField();
		minValueTextField.setValue(Double.valueOf(0));
		minValueTextField.setToolTipText("Minimum value of grid or range.");
		minValueTextField.setEnabled(false);
		minValueTextField.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {	}
			public void focusLost(FocusEvent e) {
				finishTextFieldEdit(minValueTextField);
			}
		});
		minValueTextField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				finishTextFieldEdit(maxValueTextField);
				minValueTextField.transferFocus();
			}
		});
		c.insets=new Insets(2,10,7,10);
		c.gridx=0;
		c.gridy=1;
		c.fill = GridBagConstraints.BOTH;
		gridPanel.add(minValueTextField, c);
		
		maxValueTextField = new JFormattedTextField();
		maxValueTextField.setValue(new Double(0));
		maxValueTextField.setToolTipText("Maximum value of grid or range.");
		maxValueTextField.setEnabled(false);
		maxValueTextField.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {	}
			public void focusLost(FocusEvent e) {
				finishTextFieldEdit(maxValueTextField);
			}
		});
		maxValueTextField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				finishTextFieldEdit(maxValueTextField);
				maxValueTextField.transferFocus();
			}
		});
		c.gridx=1;
		c.fill = GridBagConstraints.BOTH;
		gridPanel.add(maxValueTextField, c);
		
		stepsValueTextField = new JFormattedTextField();
		stepsValueTextField.setValue(Integer.valueOf(0));
		stepsValueTextField.setToolTipText("Number of steps in grid.");
		stepsValueTextField.setEnabled(false);
		stepsValueTextField.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {	}
			public void focusLost(FocusEvent e) {
				finishTextFieldEdit(stepsValueTextField);
			}
		});
		stepsValueTextField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				finishTextFieldEdit(stepsValueTextField);
				stepsValueTextField.transferFocus();
			}
		});
		
		c.gridx=2;
		c.fill = GridBagConstraints.BOTH;
		gridPanel.add(stepsValueTextField, c);
		
		if (mode == ParameterIteratingOperatorChain.VALUE_MODE_DISCRETE) {
			gridScaleValueComboBox = new JComboBox(ParameterValueGrid.SCALES);
		} else {
			gridScaleValueComboBox = new JComboBox();
		}
		gridScaleValueComboBox.setToolTipText("Grid scheme.");
		gridScaleValueComboBox.setEnabled(false);
		gridScaleValueComboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateSelectedNumericalParameterValues();
			}
		});
		c.gridx=3;
		c.fill = GridBagConstraints.HORIZONTAL;
		gridPanel.add(gridScaleValueComboBox, c);
		
		JPanel listPanel = new JPanel(new GridBagLayout());
		listPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(7,7,7,7), BorderFactory.createTitledBorder("Value List")));

		categoriesListModel = new DefaultListModel();
		selectedCategoriesListModel = new DefaultListModel();
		
		createValueTextField = new JTextField();
		createValueTextField.setToolTipText("Type in a new value here.");
		createValueTextField.setEnabled(false);
		createValueTextField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				createListValue();
			}
		});
		c.insets = new Insets(2,10,7,0);
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 0.475;
		c.weighty = 0;
		c.fill = GridBagConstraints.BOTH;
		listPanel.add(createValueTextField, c);
		
		categoriesList = new JList(categoriesListModel);
		categoriesList.setToolTipText("Available (or predefined) values.");
		categoriesList.setEnabled(false);
		c.insets=new Insets(2,10,7,0);
		c.gridx=0;
		c.gridy=1;
		c.weightx=0.475;
		c.weighty = 1;
		c.fill = GridBagConstraints.BOTH;
		listPanel.add(new JScrollPane(categoriesList), c);

		createValueButton = new JButton("+");
		createValueButton.setToolTipText("Add a new value.");
		createValueButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				createListValue();
			}
		});
		createValueButton.setEnabled(false);
		c.insets=new Insets(2,0,7,0);
		c.gridx=1;
		c.gridy=0;
		c.weightx= 0.05;
		c.weighty=0;
		c.fill = GridBagConstraints.NONE;
		listPanel.add(createValueButton, c);
		
		JPanel valueSelectionButtonsPanel = new JPanel(new BorderLayout());
		addValueButton = new JButton(">");
		addValueButton.setToolTipText("Select value from list of available values.");
		addValueButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String selectedParameter = (String)selectedParametersListModel.get(selectedParametersList.getLeadSelectionIndex());
				Object[] selectedValues = categoriesList.getSelectedValues();
				for (int i = 0; i < selectedValues.length; i++) {
					categoriesListModel.removeElement(selectedValues[i]);
					selectedCategoriesListModel.addElement(selectedValues[i]);
					ParameterValues parameterValue = parameterValuesMap.get(selectedParameter);
					if (parameterValue instanceof ParameterValueList) {
						((ParameterValueList) parameterValue).add((String)selectedValues[i]);
					}
				}
				updateInfoLabel();
			}
		});
		addValueButton.setEnabled(false);
		removeValueButton = new JButton("<");
		removeValueButton.setToolTipText("Remove value from selection.");
		removeValueButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String selectedParameter = (String)selectedParametersListModel.get(selectedParametersList.getLeadSelectionIndex());
				Object[] selectedValues = selectedCategoriesList.getSelectedValues();
				for (int i = 0; i < selectedValues.length; i++) {
					selectedCategoriesListModel.removeElement(selectedValues[i]);
					ParameterValues parameterValue = parameterValuesMap.get(selectedParameter);
					if (parameterValue instanceof ParameterValueList) {
						if (((ParameterValueList)parameterValue).contains((String)selectedValues[i])) {
							categoriesListModel.addElement(selectedValues[i]);
							((ParameterValueList)parameterValue).remove((String)selectedValues[i]);
						}
					}
				}
				updateInfoLabel();
			}
		});
		removeValueButton.setEnabled(false);
		valueSelectionButtonsPanel.add(addValueButton, BorderLayout.CENTER);
		valueSelectionButtonsPanel.add(removeValueButton, BorderLayout.SOUTH);
		c.insets=new Insets(2,0,7,0);
		c.gridx = 1;
		c.gridy = 1;
		c.weightx = 0.05;
		c.gridheight=2;
		c.fill = GridBagConstraints.NONE;
		listPanel.add(valueSelectionButtonsPanel, c);
		
		selectedCategoriesList = new JList(selectedCategoriesListModel);
		selectedCategoriesList.setToolTipText("Selected values.");
		selectedCategoriesList.setEnabled(false);
		c.insets = new Insets(2,0,7,10);
		c.gridx = 2;
		c.gridy = 0;
		c.gridheight = 2;
		c.weightx = 0.475;
		c.fill = GridBagConstraints.BOTH;
		listPanel.add(new JScrollPane(selectedCategoriesList), c);
		listPanel.setEnabled(false);

		JPanel valuePanel = new JPanel(new BorderLayout());
		valuePanel.add(gridPanel, BorderLayout.NORTH);
		valuePanel.add(listPanel, BorderLayout.CENTER);
		getContentPane().add(valuePanel,BorderLayout.CENTER);

		JPanel radioButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		choseGridRadioButton = new JRadioButton("Grid", true);
		choseGridRadioButton.setToolTipText("Use a regular grid for numerical parameters.");
		choseGridRadioButton.setEnabled(false);
		choseGridRadioButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (choseGridRadioButton.isSelected()) {
					choseGridRadioButton.setSelected(true);
					choseListRadioButton.setSelected(false);
					switchToGrid();
				}
			}
		});
		radioButtonPanel.add(choseGridRadioButton);
		choseListRadioButton = new JRadioButton("List", false);
		choseListRadioButton.setToolTipText("Use a list of single values for numerical parameters.");
		choseListRadioButton.setEnabled(false);
		choseListRadioButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (choseListRadioButton.isSelected()) {
					choseListRadioButton.setSelected(true);
					choseGridRadioButton.setSelected(false);
					switchToList();
				}
			}
		});
		radioButtonPanel.add(choseListRadioButton);

		JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		infoLabel = new JLabel();
		infoPanel.add(infoLabel);
		
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JButton okButton = new JButton("Ok");
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ok();
			}
		});
		buttonPanel.add(okButton);
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cancel();
			}
		});
		buttonPanel.add(cancelButton);
		
		JPanel bottomPanel = new JPanel(new BorderLayout());
		bottomPanel.add(radioButtonPanel, BorderLayout.WEST);
		bottomPanel.add(infoPanel, BorderLayout.CENTER);
		bottomPanel.add(buttonPanel, BorderLayout.EAST);
		getContentPane().add(bottomPanel, BorderLayout.SOUTH);
				
        setSize(Math.max(640, (int)(0.66d * getOwner().getWidth())), Math.max(480, (int)(0.66d * getOwner().getHeight())));
		setResizable(false);
		setLocationRelativeTo(getOwner());
	}

	private void updateInfoLabel() {
		int size = parameterValuesMap.size();
		int combinations = 1;
		if (mode == ParameterIteratingOperatorChain.VALUE_MODE_DISCRETE) {
			for (ParameterValues parameterValues : parameterValuesMap.values()) {
				int values = parameterValues.getNumberOfValues();
				combinations *= (values == 0 ? 1 : values);
			}
			infoLabel.setText(size + " parameters / " + combinations + " combinations selected");
		} else {
			infoLabel.setText(size + " parameters selected");
		}
	}
	
	private void createListValue() {
		String selectedParameter = (String)selectedParametersListModel.get(selectedParametersList.getLeadSelectionIndex());
		String createdValue = createValueTextField.getText();
		if (createdValue.equals("")) {
			return;
		} else if (selectedCategoriesListModel.contains(createdValue)) {
			return;
		} else {
			selectedCategoriesListModel.addElement(createdValue);
			ParameterValues parameterValue = parameterValuesMap.get(selectedParameter);
			if (parameterValue instanceof ParameterValueList) {
				((ParameterValueList) parameterValue).add(createdValue);
			}
			if (categoriesListModel.contains(createdValue)) {
				categoriesListModel.removeElement(createdValue);
			}
		}
		createValueTextField.setText("");
		updateInfoLabel();
	}
	
	private void switchToGrid() {
		String key = (String) selectedParametersList.getSelectedValue();
		if (key != null) {
			ParameterValues oldParameterValues = parameterValuesMap.get(key);
			if (oldParameterValues instanceof ParameterValueList) {
				ParameterValues newParameterValues = createNumericalParameterValues(oldParameterValues.getOperator(), oldParameterValues.getParameterType());
				parameterValuesMap.put(key, newParameterValues);
				fillComponents(newParameterValues);
			}
		}
		minValueJLabel.setEnabled(true);
		maxValueJLabel.setEnabled(true);
		stepsValueJLabel.setEnabled(true);
		gridScaleValueJLabel.setEnabled(true);
		minValueTextField.setEnabled(true);
		maxValueTextField.setEnabled(true);
		stepsValueTextField.setEnabled(true);
		gridScaleValueComboBox.setEnabled(true);
		categoriesList.setEnabled(false);
		selectedCategoriesList.setEnabled(false);
		addValueButton.setEnabled(false);
		removeValueButton.setEnabled(false);
		createValueButton.setEnabled(false);
		createValueTextField.setEnabled(false);
		updateInfoLabel();
	}
	
	private void switchToList() {
		String key = (String) selectedParametersList.getSelectedValue();
		if (key != null) {
			ParameterValues oldParameterValues = parameterValuesMap.get(key);
			if (oldParameterValues instanceof ParameterValueGrid) {
				ParameterValues newParameterValues = new ParameterValueList(oldParameterValues.getOperator(), oldParameterValues.getParameterType());
				parameterValuesMap.put(key, newParameterValues);
				fillComponents(newParameterValues);
			}
		}
		minValueJLabel.setEnabled(false);
		maxValueJLabel.setEnabled(false);
		stepsValueJLabel.setEnabled(false);
		gridScaleValueJLabel.setEnabled(false);
		minValueTextField.setEnabled(false);
		maxValueTextField.setEnabled(false);
		stepsValueTextField.setEnabled(false);
		gridScaleValueComboBox.setEnabled(false);
		categoriesList.setEnabled(true);
		selectedCategoriesList.setEnabled(true);
		addValueButton.setEnabled(true);
		removeValueButton.setEnabled(true);
		createValueButton.setEnabled(true);
		createValueTextField.setEnabled(true);
		updateInfoLabel();
	}

	
	private void updateParameterListModel(Operator operator) {
		parametersListModel.removeAllElements();
		List<ParameterType> parameters = operator.getParameterTypes();
		for (ParameterType parameter : parameters) {
			// do not show parameters that are not numerical in continuous mode
			if (mode == ParameterIteratingOperatorChain.VALUE_MODE_CONTINUOUS) {
				if (!(parameter instanceof ParameterTypeNumber)) { 
					continue;
				}
			}
			if (!parameterValuesMap.containsKey(operator.getName() + "." + parameter.getKey())) {
				parametersListModel.addElement(parameter.getKey(), parameter.getDescription());
			}
		}
	}
	
	private void addSelectedParameters() {
		Object[] parameterKeys = parametersList.getSelectedValues();
		Operator operator = (Operator)operatorList.getSelectedValue();
		for (int i = 0; i < parameterKeys.length; i++) {
			String parameterKey = (String)parameterKeys[i];			
			ParameterType type = operator.getParameterType(parameterKey);

			ParameterValues parameterValue = null;
			if (type.isNumerical()) {
				parameterValue = createNumericalParameterValues(operator, type);
			} else {
				if (type instanceof ParameterTypeCategory ||
					type instanceof ParameterTypeStringCategory ||
					type instanceof ParameterTypeString ||
					type instanceof ParameterTypeBoolean ||
					type instanceof ParameterTypeFile) {
					parameterValue = new ParameterValueList(operator, type, getDefaultListParameterValues(type));
				}
			}
			if (parameterValue != null) {
				addParameter(parameterValue);
			}
		}
		updateInfoLabel();
	}

	private void addParameter(ParameterValues parameterValue) {
		String key = parameterValue.getKey();
		parameterValuesMap.put(key, parameterValue);
		selectedParametersListModel.addElement(key, parameterValue.getParameterType().getDescription());
		parametersListModel.removeElement(parameterValue.getParameterType().getKey());
	}
	
	private void removeSelectedParameters() {
		Object[] selectedParameters = selectedParametersList.getSelectedValues();
		for (int i = 0; i < selectedParameters.length; i++) {
			String operatorName = ((String)selectedParameters[i]).substring(0, ((String)selectedParameters[i]).indexOf("."));
			selectedParametersListModel.removeElement(selectedParameters[i]);
			parameterValuesMap.remove(selectedParameters[i]);
			int index = operatorList.getSelectedIndex();
			if (index != -1) {
				Operator op = (Operator)operatorList.getModel().getElementAt(index);
				if (op == process.getOperator(operatorName)) { 
					updateParameterListModel(op);
				}
			}
		}
		updateInfoLabel();
	}

	private void enableComponents(ParameterValues parameterValue) {
		minValueJLabel.setEnabled(false);
		maxValueJLabel.setEnabled(false);
		stepsValueJLabel.setEnabled(false);
		gridScaleValueJLabel.setEnabled(false);
		minValueTextField.setEnabled(false);
		maxValueTextField.setEnabled(false);
		stepsValueTextField.setEnabled(false);
		gridScaleValueComboBox.setEnabled(false);

		categoriesList.setEnabled(false);
		selectedCategoriesList.setEnabled(false);
		addValueButton.setEnabled(false);
		removeValueButton.setEnabled(false);
		createValueButton.setEnabled(false);
		createValueTextField.setEnabled(false);
		
		choseGridRadioButton.setEnabled(false);
		choseListRadioButton.setEnabled(false);

		if (parameterValue != null) {
			ParameterType type = parameterValue.getParameterType();
			if (type instanceof ParameterTypeBoolean ||
				type instanceof ParameterTypeCategory) {
				categoriesList.setEnabled(true);
				selectedCategoriesList.setEnabled(true);
				addValueButton.setEnabled(true);
				removeValueButton.setEnabled(true);
			} else if (type instanceof ParameterTypeNumber) {
				if (!(parameterValue instanceof ParameterValueRange)) {
					choseGridRadioButton.setEnabled(true);
					choseListRadioButton.setEnabled(true);
				}
				if (parameterValue instanceof ParameterValueList) {
					categoriesList.setEnabled(true);
					selectedCategoriesList.setEnabled(true);
					addValueButton.setEnabled(true);
					removeValueButton.setEnabled(true);
					createValueTextField.setEnabled(true);
					createValueButton.setEnabled(true);
					choseGridRadioButton.setSelected(false);
					choseListRadioButton.setSelected(true);
				} else {
					minValueJLabel.setEnabled(true);
					maxValueJLabel.setEnabled(true);
					minValueTextField.setEnabled(true);
					maxValueTextField.setEnabled(true);
					if (parameterValue instanceof ParameterValueGrid) {
						stepsValueJLabel.setEnabled(true);
						gridScaleValueJLabel.setEnabled(true);
						stepsValueTextField.setEnabled(true);
						gridScaleValueComboBox.setEnabled(true);
						choseGridRadioButton.setSelected(true);
						choseListRadioButton.setSelected(false);
					}
				}
			} else if (type instanceof ParameterTypeString ||
					   type instanceof ParameterTypeStringCategory ||
					   type instanceof ParameterTypeValue ||
					   type instanceof ParameterTypeFile) {
				categoriesList.setEnabled(true);
				selectedCategoriesList.setEnabled(true);
				createValueButton.setEnabled(true);
				createValueTextField.setEnabled(true);
				addValueButton.setEnabled(true);
				removeValueButton.setEnabled(true);
			}
		}
	}

	private void showGridValues(ParameterValueGrid parameterValueGrid) {
		selectedCategoriesListModel.removeAllElements();
		double[] gridValues = parameterValueGrid.getValues();
		for (int i = 0; i < gridValues.length; i++) {
			selectedCategoriesListModel.addElement(Tools.formatIntegerIfPossible(gridValues[i]));
		}		
	}
	
	private void fillComponents(ParameterValues parameterValue) {
		categoriesListModel.removeAllElements();
		selectedCategoriesListModel.removeAllElements();
		if (parameterValue instanceof ParameterValueRange) {
			ParameterValueRange parameterValueRange = (ParameterValueRange) parameterValue;
			minValueTextField.setValue(Double.valueOf(parameterValueRange.getMin()));
			maxValueTextField.setValue(Double.valueOf(parameterValueRange.getMax()));
		} else if (parameterValue instanceof ParameterValueGrid) {
			ParameterValueGrid parameterValueGrid = (ParameterValueGrid) parameterValue;
			minValueTextField.setValue(Double.valueOf(parameterValueGrid.getMin()));
			maxValueTextField.setValue(Double.valueOf(parameterValueGrid.getMax()));
			stepsValueTextField.setValue(Integer.valueOf(parameterValueGrid.getSteps()));
			gridScaleValueComboBox.setSelectedIndex(parameterValueGrid.getScale());
			showGridValues(parameterValueGrid);
		} else if (parameterValue instanceof ParameterValueList) {
			ParameterValueList parameterValueList = (ParameterValueList) parameterValue;
			ParameterType type = parameterValueList.getParameterType();
			for (Object value : parameterValueList) {
				selectedCategoriesListModel.addElement(value);
			}
			String[] categories = getDefaultListParameterValues(type);
			if (categories != null) {
				for (int i = 0; i < categories.length; i++) {
					if (!parameterValueList.contains(categories[i])) {
						categoriesListModel.addElement(categories[i]);
					}
				}
			}
		}
	}
	
	private void showParameterValues(String key) {
		if (key == null) {
			enableComponents(null);
			return;
		}
		ParameterValues parameterValues = parameterValuesMap.get(key);
		fillComponents(parameterValues);
		enableComponents(parameterValues);
	}

	private void finishTextFieldEdit(JFormattedTextField textField) {
		try {
			textField.commitEdit();
		} catch (Exception e) {
			System.err.println(e.getMessage());
			return;
		}
		updateSelectedNumericalParameterValues();
	}
	
	private void updateSelectedNumericalParameterValues() {
		int index = selectedParametersList.getSelectedIndex();
		if (index == -1) {
			enableComponents(null);
			return;
		}
		String key = (String)selectedParametersListModel.get(index);
		ParameterValues parameterValues = parameterValuesMap.get(key);
		if (parameterValues != null) {
			if (parameterValues instanceof ParameterValueGrid) {
				ParameterValueGrid parameterValueGrid = (ParameterValueGrid) parameterValues;
				parameterValueGrid.setMin((Double) minValueTextField.getValue());
				parameterValueGrid.setMax((Double) maxValueTextField.getValue());
				parameterValueGrid.setSteps((Integer) stepsValueTextField.getValue());
				parameterValueGrid.setScale(gridScaleValueComboBox.getSelectedIndex());
				showGridValues(parameterValueGrid);
			}
			if (parameterValues instanceof ParameterValueRange) {
				ParameterValueRange parameterValueRange = (ParameterValueRange) parameterValues;
				parameterValueRange.setMin((Double) minValueTextField.getValue());
				parameterValueRange.setMax((Double) maxValueTextField.getValue());
			}
		}
		updateInfoLabel();
	}
	
	private ParameterValues createNumericalParameterValues(Operator operator, ParameterType type) {
		double min = ((ParameterTypeNumber)type).getMinValue();
		double max = ((ParameterTypeNumber)type).getMaxValue();
		if (mode == ParameterIteratingOperatorChain.VALUE_MODE_DISCRETE) {
			return new ParameterValueGrid(operator, type, min, max);
		} else {
			return new ParameterValueRange(operator, type, min, max);
		}
	}

	private String[] getDefaultListParameterValues(ParameterType type) {
		if (type instanceof ParameterTypeCategory) {
			return ((ParameterTypeCategory)type).getValues();
		} else if (type instanceof ParameterTypeStringCategory) {
			return ((ParameterTypeStringCategory)type).getValues();
		} else if (type instanceof ParameterTypeBoolean) {
			return new String[] { "true", "false" };
		} else {
			return new String[] {};
		}
	}
	
	private void ok() {
		ok = true;
		List<Object[]> parameterList = new LinkedList<Object[]>();
		for (String key : parameterValuesMap.keySet()) {
			String value = parameterValuesMap.get(key).getValuesString();
			parameterList.add(new Object[] { key, value });
		}
		Parameters parameters = listener.getParameters();
		parameters.setParameter(ParameterIteratingOperatorChain.PARAMETER_PARAMETERS, parameterList);
		listener.setParameters(parameters);
		dispose();
	}

	private void cancel() {
		ok = false;
		dispose();
	}

	public boolean isOk() {
		return ok;
	}
}
