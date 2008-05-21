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
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import com.rapidminer.gui.RapidMinerGUI;
import com.rapidminer.tools.Tools;


/**
 * The search dialog for searching strings in a {@link SearchableTextComponent}.
 * 
 * @author Ingo Mierswa, Simon Fischer
 * @version $Id: SearchDialog.java,v 1.5 2008/05/09 19:23:20 ingomierswa Exp $
 */
public class SearchDialog extends JDialog {

	private static final long serialVersionUID = -1019890951712706875L;

	private static class Result {

		private int start, end;

		private Result(int start, int end) {
			this.start = start;
			this.end = end;
		}
		
		public String toString() {
			return "start: " + start + ", end: " + end;
		}
	}

	private JTextField patternField = new JTextField(20);

	private JTextField replaceField = new JTextField(20);
	
	private JCheckBox caseSensitive = new JCheckBox("Case sensitive");

	private JCheckBox regExp = new JCheckBox("Regular expression");

	private JRadioButton up = new JRadioButton("Up");

	private JRadioButton down = new JRadioButton("Down");

	private transient SearchableTextComponent textComponent;
	
	public SearchDialog(Component owner, SearchableTextComponent textComponent) {
		this(owner, textComponent, false);
	}
	
	public SearchDialog(Component owner, SearchableTextComponent textComponent, boolean allowReplace) {
		super(RapidMinerGUI.getMainFrame());
        
		this.textComponent = textComponent;
		this.textComponent.requestFocus();
		this.textComponent.setCaretPosition(0);
		if (allowReplace)
			setTitle("Search and Replace");
		else
			setTitle("Search");
		setModal(false);
		
		GridBagLayout layout = new GridBagLayout();
		getContentPane().setLayout(layout);
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.insets = new Insets(5,5,5,5);
		c.ipadx = 10;
		c.weightx = 1.0d;
		c.gridwidth = 1;
		
		JLabel label = new JLabel("Search:");
		layout.setConstraints(label, c);
		getContentPane().add(label);
		
		c.gridwidth = GridBagConstraints.RELATIVE;
		layout.setConstraints(patternField, c);
		getContentPane().add(patternField);
		
		final JButton search = new JButton("Search");
		getRootPane().setDefaultButton(search);
		search.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				search();
				getRootPane().setDefaultButton(search);
			}
		});
		c.gridwidth = GridBagConstraints.REMAINDER;
		layout.setConstraints(search, c);
		getContentPane().add(search);
		
		if (allowReplace) {
			c.gridwidth = 1;
			label = new JLabel("Replace:");
			layout.setConstraints(label, c);
			getContentPane().add(label);
			
			c.gridwidth = GridBagConstraints.RELATIVE;
			layout.setConstraints(replaceField, c);
			getContentPane().add(replaceField);
			
			final JButton replace = new JButton("Replace");
			replace.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					replace();
					search();
					getRootPane().setDefaultButton(replace);
				}
			});
			c.gridwidth = GridBagConstraints.REMAINDER;
			layout.setConstraints(replace, c);
			getContentPane().add(replace);
		}
		

		JPanel directionPanel = new JPanel();
		directionPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(7, 0, 0, 7), BorderFactory.createTitledBorder("Direction")));
		ButtonGroup directionGroup = new ButtonGroup();
		up.setMnemonic(KeyEvent.VK_U);
		directionGroup.add(up);
		directionPanel.add(up);
		down.setMnemonic(KeyEvent.VK_D);
		directionGroup.add(down);
		down.setSelected(true);
		directionPanel.add(down);
		
		c.gridwidth = 1;
		layout.setConstraints(directionPanel, c);
		getContentPane().add(directionPanel);

		JPanel optionsPanel = new JPanel();
		optionsPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(7, 0, 0, 7), BorderFactory.createTitledBorder("Options")));
		optionsPanel.add(caseSensitive);
		optionsPanel.add(regExp);

		c.gridwidth = GridBagConstraints.RELATIVE;
		layout.setConstraints(optionsPanel, c);
		getContentPane().add(optionsPanel);

		c.gridwidth = GridBagConstraints.REMAINDER;
		JPanel dummyPanel = new JPanel();
		layout.setConstraints(dummyPanel, c);
		getContentPane().add(dummyPanel);
		
		JPanel closeButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		JButton cancel = new JButton("Close");
		cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		closeButtonPanel.add(cancel);
		c.gridwidth = GridBagConstraints.REMAINDER;
		layout.setConstraints(closeButtonPanel, c);
		getContentPane().add(closeButtonPanel);
		
		pack();
		setLocationRelativeTo(owner);
	}

	private void search() {
		String pattern = patternField.getText().trim();
		if (pattern.length() == 0)
			return;
		int startPos = textComponent.getCaretPosition();
		String text = textComponent.getText();
		if (startPos > text.length())
			startPos = 0;

		if (down.isSelected()) {
			Result result = search(startPos, pattern, text, textComponent.canHandleCarriageReturn());
			if (result == null) {
				noMoreHits();
				return;
			} else {
				textComponent.select(result.start, result.end);
			}
		} else {
			Result lastResult = null;
			int pos = 0;
			while (true) {
				Result result = search(pos, pattern, text, textComponent.canHandleCarriageReturn());
				if (result != null) {
					if (result.end < startPos) {
						pos = result.start + 1;
						lastResult = result;
					} else {
						break;
					}
				} else {
					break;
				}
			}

			if (lastResult == null) {
				noMoreHits();
			} else {
				textComponent.select(lastResult.start, lastResult.end);
			}
		}
	}

	private void replace() {
		textComponent.replaceSelection(replaceField.getText());
	}
	
	private Result search(int start, String pattern, String text, boolean canHandleCarriageReturn) {
	    if (!canHandleCarriageReturn) {
	        text = Tools.transformAllLineSeparators(text);
        }
		if (regExp.isSelected()) {
			Matcher matcher = Pattern.compile(pattern, caseSensitive.isSelected() ? 0 : Pattern.CASE_INSENSITIVE).matcher(text.subSequence(start, text.length()));
			if (matcher.find()) {
				return new Result(start + matcher.start(), start + matcher.end());
			} else {
				return null;
			}
		} else {
			if (!caseSensitive.isSelected()) {
				text = text.toLowerCase();
				pattern = pattern.toLowerCase();
			}
			int result = text.indexOf(pattern, start);
			if (result == -1) {
				return null;
			} else {
				return new Result(result, result + pattern.length());
			}
		}
	}

	private void noMoreHits() {
		String restartAt = up.isSelected() ? "end" : "beginning";
		switch (JOptionPane.showConfirmDialog(this, "Search string not found. Search from " + restartAt + "?", "String not found", JOptionPane.YES_NO_OPTION)) {
			case JOptionPane.YES_OPTION:
				textComponent.setCaretPosition(up.isSelected() ? textComponent.getText().replaceAll("\r","").length() : 0);
				search();
				break;
			case JOptionPane.NO_OPTION:
			default:
				return;
		}
	}
}
