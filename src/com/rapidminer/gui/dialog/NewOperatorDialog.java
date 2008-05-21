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
package com.rapidminer.gui.dialog;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.rapidminer.gui.RapidMinerGUI;
import com.rapidminer.gui.operatortree.OperatorTree;
import com.rapidminer.gui.tools.ExtendedJScrollPane;
import com.rapidminer.gui.tools.OperatorList;
import com.rapidminer.gui.tools.SwingTools;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorCreationException;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.learner.Learner;
import com.rapidminer.operator.learner.LearnerCapability;
import com.rapidminer.tools.GroupTree;
import com.rapidminer.tools.OperatorService;


/**
 * A dialog for adding new operators to the currently selected operator chain of
 * the operator tree. The new operator can be searched by name, by groups, by
 * input types, and by output types (or combinations). A short description of
 * the operator is also shown. Therefore this dialog might be usefull for less
 * experienced RapidMiner users. A shorter way for adding operators is to use the
 * context menu in the tree view.
 * 
 * @author Ingo Mierswa, Helge Homburg
 * @version $Id: NewOperatorDialog.java,v 2.16 2006/04/05 08:57:23 ingomierswa
 *          Exp $
 */
public class NewOperatorDialog extends JDialog {

	private static final long serialVersionUID = 390653805759799295L;

    private OperatorTree operatorTree;
    
	private transient OperatorDescription description = null;

	private JPanel mainPanel = new JPanel();

	private OperatorInfoPanel operatorInfo = null;

	private JList operatorList = new OperatorList();

	private String searchText = "";
	
	private Class inputClass = null;

	private Class outputClass = null;

	private String group = null;

	private transient LearnerCapability firstCapability = null;

	private transient LearnerCapability secondCapability = null;
	
	
	public NewOperatorDialog(OperatorTree tree) {
		super(RapidMinerGUI.getMainFrame(), "New Operator", false);
		this.operatorTree = tree;

		// main
		mainPanel.setLayout(new BorderLayout());
		getContentPane().add(mainPanel, BorderLayout.CENTER);

		// buttons
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JButton okButton = new JButton("Add");
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				add();
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
		mainPanel.add(buttonPanel, BorderLayout.SOUTH);

		// search panel
		JPanel searchPanel = new JPanel(new GridLayout(6, 2));
		searchPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10), BorderFactory.createTitledBorder("Search Constraints")));
		mainPanel.add(searchPanel, BorderLayout.NORTH);

		// input objects (for in- and output classes)
		final String[] ioObjects = convertSet2Strings(OperatorService.getIOObjectsNames());
		String[] inputObjects = new String[ioObjects.length + 1];
		inputObjects[0] = "Any";
		System.arraycopy(ioObjects, 0, inputObjects, 1, ioObjects.length);

		// search text
		final JTextField searchField = new JTextField(searchText);
		searchField.addKeyListener(new KeyListener() {
			public void keyTyped(KeyEvent e) {}
			public void keyPressed(KeyEvent e) {}
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					searchText = searchField.getText().trim();
					updateOperatorList();
				}
			}
		});
		searchPanel.add(new JLabel("Search Text: "));
		searchPanel.add(searchField);
		
		// groups
		java.util.List<String> allGroups = new LinkedList<String>();
		allGroups.add("Any");
		GroupTree groupTree = OperatorService.getGroups();
		addGroups(groupTree, null, allGroups);
		Collections.sort(allGroups);
		final String[] groupArray = new String[allGroups.size()];
		allGroups.toArray(groupArray);
		final JComboBox groupComboBox = new JComboBox(groupArray);
		groupComboBox.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				int selectedIndex = groupComboBox.getSelectedIndex();
				if (selectedIndex <= 0) {
					group = null;
				} else {
					group = groupArray[selectedIndex];
				}
				updateOperatorList();
			}
		});
		searchPanel.add(new JLabel("Operator group: "));
		searchPanel.add(groupComboBox);

		// input
		final JComboBox inputType = new JComboBox(inputObjects);
		inputType.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				int selectedIndex = inputType.getSelectedIndex();
				if (selectedIndex <= 0) {
					inputClass = null;
				} else {
					inputClass = OperatorService.getIOObjectClass(ioObjects[selectedIndex - 1]);
				}
				updateOperatorList();
			}
		});
		searchPanel.add(new JLabel("Required Input: "));
		searchPanel.add(inputType);

		// output
		final JComboBox outputType = new JComboBox(inputObjects);
		outputType.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				int selectedIndex = outputType.getSelectedIndex();
				if (selectedIndex <= 0) {
					outputClass = null;
				} else {
					outputClass = OperatorService.getIOObjectClass(ioObjects[selectedIndex - 1]);
				}
				updateOperatorList();
			}
		});
		searchPanel.add(new JLabel("Delivered Output: "));
		searchPanel.add(outputType);

		// capabilities
		List<LearnerCapability> caps = LearnerCapability.getAllCapabilities();
		String[] capabilities = new String[caps.size() + 1];
		capabilities[0] = "Any";
		int k = 1;
		Iterator<LearnerCapability> i = caps.iterator();
		while (i.hasNext()) {
			capabilities[k++] = i.next().getDescription();
		}
		
		final JComboBox firstCapabilityType = new JComboBox(capabilities);
		firstCapabilityType.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int selectedIndex = firstCapabilityType.getSelectedIndex();
				if (selectedIndex <= 0) {
					firstCapability = null;
				} else {
					firstCapability = LearnerCapability.getCapability(selectedIndex - 1);
				}
				updateOperatorList();
			}
		});
		searchPanel.add(new JLabel("First Capability: "));
		searchPanel.add(firstCapabilityType);

		final JComboBox secondCapabilityType = new JComboBox(capabilities);
		secondCapabilityType.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int selectedIndex = secondCapabilityType.getSelectedIndex();
				if (selectedIndex <= 0) {
					secondCapability = null;
				} else {
					secondCapability = LearnerCapability.getCapability(selectedIndex - 1);
				}
				updateOperatorList();
			}
		});
		searchPanel.add(new JLabel("Second Capability: "));
		searchPanel.add(secondCapabilityType);
		
		// list panel
		operatorList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                	OperatorDescription selection = (OperatorDescription) operatorList.getSelectedValue();
                    setSelectedOperator(selection);
                }
			}
		});
		operatorList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane listScrollPane = new ExtendedJScrollPane(operatorList);
		GridBagLayout layout = new GridBagLayout();
		JPanel listPanel = new JPanel(layout);
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weightx = 1;
		c.weighty = 1;
		listPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5, 10, 10, 5), BorderFactory.createTitledBorder("Matching Operators")));
		layout.setConstraints(listScrollPane, c);
		listPanel.add(listScrollPane);
		mainPanel.add(listPanel, BorderLayout.WEST);

		updateOperatorList();

		setSize(780, 580);
		setLocationRelativeTo(RapidMinerGUI.getMainFrame());
	}

	private void updateOperatorList() {
		operatorList.removeAll();
		Vector<OperatorDescription> operators = new Vector<OperatorDescription>();
		Iterator<String> i = OperatorService.getOperatorNames().iterator();
		while (i.hasNext()) {
			String name = i.next();
			OperatorDescription description = OperatorService.getOperatorDescription(name);
			if ((searchText != null) && (searchText.length() > 0) && (description.getName().toLowerCase().indexOf(searchText.toLowerCase()) < 0))
				continue;
			if ((group != null) && (group.length() > 0) && (description.getGroup().indexOf(group) < 0))
				continue;
			try {
				Operator operator = description.createOperatorInstance();
				if ((inputClass != null) && !containsClass(operator.getInputClasses(), inputClass))
					continue;
				if ((outputClass != null) && !containsClass(operator.getOutputClasses(), outputClass))
					continue;
				if ((firstCapability != null) && (operator instanceof Learner) && (!((Learner)operator).supportsCapability(firstCapability)))
					continue;
				if ((secondCapability != null) && (operator instanceof Learner) && (!((Learner)operator).supportsCapability(secondCapability)))
					continue;
			} catch (Exception e) {}
			operators.add(description);
		}
		Collections.sort(operators);
		operatorList.setListData(operators);
		if (operators.size() > 0)
			operatorList.setSelectedIndex(0);
	}

	private boolean containsClass(Class<?>[] classes, Class<?> clazz) {
		if (classes != null) {
			for (int i = 0; i < classes.length; i++) {
				if (clazz.isAssignableFrom(classes[i]))
					return true;
			}
		}
		return false;
	}

	private void addGroups(GroupTree tree, String parentName, Collection<String> names) {
		Iterator<GroupTree> i = tree.getSubGroups().iterator();
		while (i.hasNext()) {
			GroupTree subGroup = i.next();
			String name = parentName == null ? subGroup.getName() : parentName + "." + subGroup.getName();
			names.add(name);
			addGroups(subGroup, name, names);
		}
	}

	private void setSelectedOperator(OperatorDescription descriptionName) {
		if (operatorInfo != null)
			mainPanel.remove(operatorInfo);
		if (descriptionName != null) {
			this.description = descriptionName;
			operatorInfo = new OperatorInfoPanel(this.description);
		} else {
			operatorInfo = new OperatorInfoPanel(null);
		}
		if (this.description.getDeprecationInfo() != null)
			operatorInfo.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5, 5, 10, 10), BorderFactory.createTitledBorder("Operator Info (Deprecated!)")));
		else
			operatorInfo.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5, 5, 10, 10), BorderFactory.createTitledBorder("Operator Info")));
		mainPanel.add(operatorInfo, BorderLayout.CENTER);
		operatorInfo.revalidate();
	}

	private String[] convertSet2Strings(Collection ioObjects) {
		String[] objectArray = new String[ioObjects.size()];
		Iterator i = ioObjects.iterator();
		int index = 0;
		while (i.hasNext()) {
			objectArray[index++] = (String) i.next();
		}
		return objectArray;
	}

	private Operator getOperator() throws OperatorCreationException {
		if (description != null) {
			Operator operator = OperatorService.createOperator(description);
			return operator;
		} else {
			return null;
		}
	}

    private void add() {
        try {
            Operator operator = getOperator();
            if (operator != null) {
                operatorTree.insert(operator);
            }
        } catch (Exception ex) {
            SwingTools.showSimpleErrorMessage("Cannot create operator:", ex);
        }
    }

	private void cancel() {
		dispose();
	}
}
