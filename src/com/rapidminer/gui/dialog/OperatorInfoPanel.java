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

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Icon;
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
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.tools.LogService;


/**
 * An info panel for operators. Shows all important meta data about an operator
 * like name, group, expected input and delivered output. In case of an operator
 * chain the desired numbers of inner operators are also shown. In contrast to
 * the info screen {@link OperatorInfoScreen} this panel can not handle user
 * comments and is mainly used for short informations like those displayed in
 * the {@link NewOperatorDialog}.
 * 
 * @author Ingo Mierswa
 * @version $Id: OperatorInfoPanel.java,v 2.7 2006/03/21 15:35:40 ingomierswa
 *          Exp $
 */
public class OperatorInfoPanel extends JPanel {

	private static final long serialVersionUID = 3610550973456646750L;

	public OperatorInfoPanel(OperatorDescription description) {
		if (description == null) {
			add(new JLabel("No operator selected!"));
		} else {
			Operator operator = null;
			try {
				operator = description.createOperatorInstance();
			} catch (Exception e) {
                LogService.getGlobal().log("Cannot create operator: " + e.getMessage(), LogService.WARNING);
			}

			GridBagLayout layout = new GridBagLayout();
			setLayout(layout);
			GridBagConstraints c = new GridBagConstraints();
			c.fill = GridBagConstraints.BOTH;
			c.ipadx = 10;
			c.weightx = 1.0d;

			JLabel label = null;

			Icon icon = description.getIcon();
			if (icon != null) {
				label = new JLabel(icon);
				c.gridwidth = GridBagConstraints.REMAINDER;
				layout.setConstraints(label, c);
				add(label);
			}

			Component sep = Box.createVerticalStrut(10);
			c.gridwidth = GridBagConstraints.REMAINDER;
			layout.setConstraints(sep, c);
			add(sep);

			String deprecationInfo = description.getDeprecationInfo();
			if (deprecationInfo != null) {
				label = new JLabel("Deprecation Info");
				label.setToolTipText("The info about the deprecation state of this operator");
				c.gridwidth = GridBagConstraints.RELATIVE;
				layout.setConstraints(label, c);
				add(label);
				
				JTextArea deprecationArea = new JTextArea(deprecationInfo);
				deprecationArea.setBorder(BorderFactory.createEmptyBorder(0, 7, 10, 7));
				deprecationArea.setEditable(false);
				deprecationArea.setLineWrap(true);
				deprecationArea.setWrapStyleWord(true);
				deprecationArea.setBackground(this.getBackground());
				deprecationArea.setToolTipText("The info about the deprecation state of this operator");
				c.gridwidth = GridBagConstraints.REMAINDER;
				layout.setConstraints(deprecationArea, c);
				add(deprecationArea);
			}
			
			label = new JLabel("Group");
			label.setToolTipText("The group of the operator");
			c.gridwidth = GridBagConstraints.RELATIVE;
			layout.setConstraints(label, c);
			add(label);

			label = new JLabel(description.getGroup());
			label.setToolTipText("The group of the operator");
			c.gridwidth = GridBagConstraints.REMAINDER;
			layout.setConstraints(label, c);
			add(label);

			label = new JLabel("Class");
			label.setToolTipText("The type of the operator");
			c.gridwidth = GridBagConstraints.RELATIVE;
			layout.setConstraints(label, c);
			add(label);

			label = new JLabel(description.getName());
			label.setToolTipText("The type of the operator");
			c.gridwidth = GridBagConstraints.REMAINDER;
			layout.setConstraints(label, c);
			add(label);

			sep = Box.createVerticalStrut(10);
			c.gridwidth = GridBagConstraints.REMAINDER;
			layout.setConstraints(sep, c);
			add(sep);

			label = new JLabel("Input");
			label.setToolTipText("Expected input");
			c.gridwidth = GridBagConstraints.RELATIVE;
			layout.setConstraints(label, c);
			add(label);

			if (operator != null) {
				label = new JLabel(SwingTools.getStringFromClassArray(operator.getInputClasses()));
				label.setToolTipText("Expected input");
				c.gridwidth = GridBagConstraints.REMAINDER;
				layout.setConstraints(label, c);
				add(label);

				label = new JLabel("Output");
				label.setToolTipText("Delivered output");
				c.gridwidth = GridBagConstraints.RELATIVE;
				layout.setConstraints(label, c);
				add(label);

				label = new JLabel(SwingTools.getStringFromClassArray(operator.getOutputClasses()));
				label.setToolTipText("Delivered output");
				c.gridwidth = GridBagConstraints.REMAINDER;
				layout.setConstraints(label, c);
				add(label);

				if (operator instanceof OperatorChain) {

					OperatorChain chain = (OperatorChain) operator;

					sep = Box.createVerticalStrut(10);
					c.gridwidth = GridBagConstraints.REMAINDER;
					layout.setConstraints(sep, c);
					add(sep);

					if (chain.getMinNumberOfInnerOperators() == chain.getMaxNumberOfInnerOperators()) {
						label = new JLabel("Inner Operators");
						label.setToolTipText("Number of inner operators");
						c.gridwidth = GridBagConstraints.RELATIVE;
						layout.setConstraints(label, c);
						add(label);

						label = new JLabel(((OperatorChain) operator).getMinNumberOfInnerOperators() + "");
						label.setToolTipText("Number of inner operators");
						c.gridwidth = GridBagConstraints.REMAINDER;
						layout.setConstraints(label, c);
						add(label);
					} else {
						label = new JLabel("Min Inner");
						label.setToolTipText("Minimum number of inner operators");
						c.gridwidth = GridBagConstraints.RELATIVE;
						layout.setConstraints(label, c);
						add(label);

						label = new JLabel(((OperatorChain) operator).getMinNumberOfInnerOperators() + "");
						label.setToolTipText("Minimum number of inner operators");
						c.gridwidth = GridBagConstraints.REMAINDER;
						layout.setConstraints(label, c);
						add(label);

						label = new JLabel("Max Inner");
						label.setToolTipText("Maximum number of inner operators");
						c.gridwidth = GridBagConstraints.RELATIVE;
						layout.setConstraints(label, c);
						add(label);

						int maxInner = ((OperatorChain) operator).getMaxNumberOfInnerOperators();
						label = new JLabel(maxInner == Integer.MAX_VALUE ? "Max" : (maxInner + ""));
						label.setToolTipText("Maximum number of inner operators");
						c.gridwidth = GridBagConstraints.REMAINDER;
						layout.setConstraints(label, c);
						add(label);
					}
				}
			}
			
			c.weighty = 1.0;

			JEditorPane descriptionText = new JEditorPane("text/html", "");
			StyleSheet css = ((HTMLEditorKit)descriptionText.getEditorKit()).getStyleSheet();
			css.addRule("P { margin : 0; font-family : sans-serif; font-size : 9px; font-style : normal; }");
			descriptionText.setToolTipText("The description of this operator");
			descriptionText.setEditable(false);
			descriptionText.setBackground(this.getBackground());
			String descriptionString = description.getLongDescriptionHTML();
			if (descriptionString == null) {
				descriptionString = description.getShortDescription();
			}
			descriptionText.setText("<P>" + descriptionString + "</P>");
			JScrollPane textScrollPane = new ExtendedJScrollPane(descriptionText);
			textScrollPane.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
			c.gridwidth = GridBagConstraints.REMAINDER;
			layout.setConstraints(textScrollPane, c);
			add(textScrollPane);
		}
	}
}
