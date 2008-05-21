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
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

import com.rapidminer.gui.tools.ExtendedJScrollPane;
import com.rapidminer.gui.tools.SwingTools;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorChain;
import com.rapidminer.operator.condition.InnerOperatorCondition;
import com.rapidminer.operator.learner.Learner;
import com.rapidminer.operator.learner.LearnerCapability;


/**
 * An info screen for operators. Shows all important meta data about an operator
 * like name, group, expected input and delivered output. In case of an operator
 * chain the desired numbers of inner operators are also shown.
 * 
 * @author Ingo Mierswa
 * @version $Id: OperatorInfoScreen.java,v 2.16 2006/04/12 18:04:24 ingomierswa
 *          Exp $
 */
public class OperatorInfoScreen extends JDialog {

	private static final long serialVersionUID = -6566133238783779634L;

	public OperatorInfoScreen(Frame owner, Operator operator) {
		super(owner, "Operator Info", true);

		JPanel rootPanel = new JPanel(new BorderLayout());
		rootPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		GridBagLayout layout = new GridBagLayout();
		JPanel mainPanel = new JPanel(layout);
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.ipadx = 10;
		c.weightx = 1.0d;

		JLabel label = null;

		// =======
		// icon
		// =======
		Icon icon = operator.getOperatorDescription().getIcon();
		if (icon != null) {
			label = new JLabel(icon);
			c.gridwidth = GridBagConstraints.REMAINDER;
			layout.setConstraints(label, c);
			mainPanel.add(label);
		}

		Component sep = Box.createVerticalStrut(10);
		c.gridwidth = GridBagConstraints.REMAINDER;
		layout.setConstraints(sep, c);
		mainPanel.add(sep);

		// =======
		// general informations
		// =======
		label = new JLabel("Name");
		label.setToolTipText("The name of the operator");
		c.gridwidth = GridBagConstraints.RELATIVE;
		layout.setConstraints(label, c);
		mainPanel.add(label);

		label = new JLabel(operator.getName());
		label.setToolTipText("The name of the operator");
		c.gridwidth = GridBagConstraints.REMAINDER;
		layout.setConstraints(label, c);
		mainPanel.add(label);

		label = new JLabel("Group");
		label.setToolTipText("The group of the operator");
		c.gridwidth = GridBagConstraints.RELATIVE;
		layout.setConstraints(label, c);
		mainPanel.add(label);

		label = new JLabel(operator.getOperatorDescription().getGroup());
		label.setToolTipText("The group of the operator");
		c.gridwidth = GridBagConstraints.REMAINDER;
		layout.setConstraints(label, c);
		mainPanel.add(label);

		label = new JLabel("Class");
		label.setToolTipText("The type of the operator");
		c.gridwidth = GridBagConstraints.RELATIVE;
		layout.setConstraints(label, c);
		mainPanel.add(label);

		label = new JLabel(operator.getOperatorDescription().getName());
		label.setToolTipText("The type of the operator");
		c.gridwidth = GridBagConstraints.REMAINDER;
		layout.setConstraints(label, c);
		mainPanel.add(label);

		sep = Box.createVerticalStrut(10);
		c.gridwidth = GridBagConstraints.REMAINDER;
		layout.setConstraints(sep, c);
		mainPanel.add(sep);

		label = new JLabel("Input");
		label.setToolTipText("Expected input");
		c.gridwidth = GridBagConstraints.RELATIVE;
		layout.setConstraints(label, c);
		mainPanel.add(label);

		label = new JLabel(SwingTools.getStringFromClassArray(operator.getInputClasses()));
		label.setToolTipText("Expected input");
		c.gridwidth = GridBagConstraints.REMAINDER;
		layout.setConstraints(label, c);
		mainPanel.add(label);

		label = new JLabel("Output");
		label.setToolTipText("Delivered output");
		c.gridwidth = GridBagConstraints.RELATIVE;
		layout.setConstraints(label, c);
		mainPanel.add(label);

		label = new JLabel(SwingTools.getStringFromClassArray(operator.getOutputClasses()));
		label.setToolTipText("Delivered output");
		c.gridwidth = GridBagConstraints.REMAINDER;
		layout.setConstraints(label, c);
		mainPanel.add(label);

		// =======
		// learner
		// =======
		if (operator instanceof Learner) {

			sep = Box.createVerticalStrut(10);
			c.gridwidth = GridBagConstraints.REMAINDER;
			layout.setConstraints(sep, c);
			mainPanel.add(sep);

			Learner learner = (Learner) operator;
			StringBuffer learnerCapabilities = new StringBuffer();
			Iterator i = LearnerCapability.getAllCapabilities().iterator();
			boolean first = true;
			while (i.hasNext()) {
				LearnerCapability capability = (LearnerCapability) i.next();
				try {
					if (learner.supportsCapability(capability)) {
						if (!first)
							learnerCapabilities.append(", ");
						learnerCapabilities.append(capability.getDescription());
						first = false;
					}
				} catch (Exception e) {
					break;
				}
			}
			String result = learnerCapabilities.toString();
			if (result.length() > 0) {
				JTextArea capabilities = new JTextArea("");
				capabilities.setToolTipText("The capabilities of this learning scheme.");
				capabilities.setEditable(false);
				capabilities.setLineWrap(true);
				capabilities.setWrapStyleWord(true);
				capabilities.setBackground(this.getBackground());
				capabilities.setText(result);
				JScrollPane textScrollPane = new ExtendedJScrollPane(capabilities);
				textScrollPane.setBorder(BorderFactory.createTitledBorder("Learner Capabilities"));
				c.weighty = 1.0d;
				c.gridwidth = GridBagConstraints.REMAINDER;
				layout.setConstraints(textScrollPane, c);
				mainPanel.add(textScrollPane);
			}
		}

		// =======
		// operator chain
		// =======
		if (operator instanceof OperatorChain) {
			OperatorChain chain = (OperatorChain) operator;

			sep = Box.createVerticalStrut(10);
			c.gridwidth = GridBagConstraints.REMAINDER;
			layout.setConstraints(sep, c);
			mainPanel.add(sep);

			if (chain.getMinNumberOfInnerOperators() == chain.getMaxNumberOfInnerOperators()) {
				label = new JLabel("Inner Operators");
				label.setToolTipText("Number of inner operators");
				c.gridwidth = GridBagConstraints.RELATIVE;
				layout.setConstraints(label, c);
				mainPanel.add(label);

				label = new JLabel(((OperatorChain) operator).getMinNumberOfInnerOperators() + "");
				label.setToolTipText("Number of inner operators");
				c.gridwidth = GridBagConstraints.REMAINDER;
				layout.setConstraints(label, c);
				mainPanel.add(label);
			} else {
				label = new JLabel("Min Inner");
				label.setToolTipText("Minimum number of inner operators");
				c.gridwidth = GridBagConstraints.RELATIVE;
				layout.setConstraints(label, c);
				mainPanel.add(label);

				label = new JLabel(((OperatorChain) operator).getMinNumberOfInnerOperators() + "");
				label.setToolTipText("Minimum number of inner operators");
				c.gridwidth = GridBagConstraints.REMAINDER;
				layout.setConstraints(label, c);
				mainPanel.add(label);

				label = new JLabel("Max Inner");
				label.setToolTipText("Maximum number of inner operators");
				c.gridwidth = GridBagConstraints.RELATIVE;
				layout.setConstraints(label, c);
				mainPanel.add(label);

				int maxInner = ((OperatorChain) operator).getMaxNumberOfInnerOperators();
				label = new JLabel(maxInner == Integer.MAX_VALUE ? "Max" : (maxInner + ""));
				label.setToolTipText("Maximum number of inner operators");
				c.gridwidth = GridBagConstraints.REMAINDER;
				layout.setConstraints(label, c);
				mainPanel.add(label);
			}

			sep = Box.createVerticalStrut(10);
			c.gridwidth = GridBagConstraints.REMAINDER;
			c.weighty = 0.0d;
			layout.setConstraints(sep, c);
			mainPanel.add(sep);

			InnerOperatorCondition condition = chain.getInnerOperatorCondition();
			JEditorPane conditions = new JEditorPane("text/html", "");
			conditions.setToolTipText("Conditions which must be fulfilled by inner operators");
			conditions.setEditable(false);
			conditions.setBackground(this.getBackground());
			conditions.setForeground(java.awt.Color.black);
			conditions.setText(SwingTools.text2DisplayHtml(condition.toHTML()));
			JScrollPane conditionsScrollPane = new ExtendedJScrollPane(conditions);
			conditionsScrollPane.setBorder(BorderFactory.createTitledBorder("Inner operator conditions"));
			c.gridwidth = GridBagConstraints.REMAINDER;
			c.weighty = 1.0d;
			layout.setConstraints(conditionsScrollPane, c);
			mainPanel.add(conditionsScrollPane);
		}

		sep = Box.createVerticalStrut(10);
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weighty = 0.0d;
		layout.setConstraints(sep, c);
		mainPanel.add(sep);

		// =======
		// description
		// =======
		JEditorPane description = new JEditorPane("text/html", "");
		StyleSheet css = ((HTMLEditorKit)description.getEditorKit()).getStyleSheet();
		css.addRule("P { margin : 0; font-family : sans-serif; font-size : 9px; font-style : normal; }");
		description.setToolTipText("The description of this operator");
		description.setEditable(false);
		description.setBackground(this.getBackground());
		String descriptionString = operator.getOperatorDescription().getLongDescriptionHTML();
		if (descriptionString == null) {
			descriptionString = operator.getOperatorDescription().getShortDescription();
		}
		description.setText("<P>" + descriptionString + "<P>");
		JScrollPane textScrollPane = new ExtendedJScrollPane(description);
		textScrollPane.setBorder(BorderFactory.createTitledBorder("Description"));
		c.weighty = 1.0d;
		c.gridwidth = GridBagConstraints.REMAINDER;
		layout.setConstraints(textScrollPane, c);
		mainPanel.add(textScrollPane);

		// =======
		// errors
		// =======
		List<String> errorList = operator.getErrorList();
		if (errorList.size() > 0) {
			sep = Box.createVerticalStrut(10);
			c.gridwidth = GridBagConstraints.REMAINDER;
			c.weighty = 0.0d;
			layout.setConstraints(sep, c);
			mainPanel.add(sep);

			JEditorPane errors = new JEditorPane("text/html", "");
			errors.setToolTipText("Errors of this operator");
			errors.setEditable(false);
			errors.setBackground(this.getBackground());
			errors.setForeground(java.awt.Color.red);

			StringBuffer errorText = new StringBuffer("<ul>");
			Iterator<String> i = errorList.iterator();
			while (i.hasNext()) {
				errorText.append("<li>");
				errorText.append(i.next());
				errorText.append("</li>");
			}
			errorText.append("</ul>");

			errors.setText(SwingTools.text2DisplayHtml(errorText.toString()));

			JScrollPane errorsScrollPane = new ExtendedJScrollPane(errors);
			errorsScrollPane.setBorder(BorderFactory.createTitledBorder("Errors"));
			c.gridwidth = GridBagConstraints.REMAINDER;
			c.weighty = 1.0d;
			layout.setConstraints(errorsScrollPane, c);
			mainPanel.add(errorsScrollPane);
		}

		sep = Box.createVerticalStrut(10);
		c.weighty = 0.0d;
		c.gridwidth = GridBagConstraints.REMAINDER;
		layout.setConstraints(sep, c);
		mainPanel.add(sep);

		rootPanel.add(mainPanel, BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		JButton okButton = new JButton("Ok");
		okButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				ok();
			}
		});
		buttonPanel.add(okButton);

		rootPanel.add(buttonPanel, BorderLayout.SOUTH);

		getContentPane().add(rootPanel);

		// pack();
		setSize(600, 600);
		setLocationRelativeTo(owner);
	}

	private void ok() {
		dispose();
	}
}
