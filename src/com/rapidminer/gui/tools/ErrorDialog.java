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
package com.rapidminer.gui.tools;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import com.rapidminer.NoBugError;
import com.rapidminer.gui.RapidMinerGUI;
import com.rapidminer.gui.dialog.BugAssistant;
import com.rapidminer.tools.Tools;
import com.rapidminer.tools.XMLException;


/**
 * The error message dialog. Several buttons are provided in addition to the
 * error message. Details about the exception can be shown and an edit button
 * can jump to the source code if an editor was defined in the properties /
 * settings. In case of a non-expected error (i.e. all non-user errors) a button
 * for sending a bugreport is also provided.
 * 
 * @author Ingo Mierswa, Simon Fischer
 * @version $Id: ErrorDialog.java,v 1.4 2008/05/09 19:22:58 ingomierswa Exp $
 */
public class ErrorDialog extends JDialog {

	private static final long serialVersionUID = -8136329951869702133L;

	JButton editButton = new JButton("Edit");

	private static class FormattedStackTraceElement {

		private StackTraceElement ste;

		private FormattedStackTraceElement(StackTraceElement ste) {
			this.ste = ste;
		}

		public String toString() {
			return "  " + ste;
		}
	}

	private class StackTraceList extends JList {

		private static final long serialVersionUID = -2482220036723949144L;

		public StackTraceList(Throwable t) {
			super(new DefaultListModel());
			setFont(getFont().deriveFont(Font.PLAIN));
			setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			appendAllStackTraces(t);
			addListSelectionListener(new ListSelectionListener() {

				public void valueChanged(ListSelectionEvent e) {
					if (getSelectedIndex() >= 0) {
						if (!(getSelectedValue() instanceof FormattedStackTraceElement)) {
							editButton.setEnabled(false);
						} else {
							editButton.setEnabled(true);
						}
					} else {
						editButton.setEnabled(true);
					}
				}
			});
		}

		private DefaultListModel model() {
			return (DefaultListModel) getModel();
		}

		private void appendAllStackTraces(Throwable throwable) {
			while (throwable != null) {
				appendStackTrace(throwable);
				throwable = throwable.getCause();
				if (throwable != null) {
					model().addElement("");
					model().addElement("Cause");
				}
			}
		}

		private void appendStackTrace(Throwable throwable) {
			model().addElement("Exception: " + throwable.getClass().getName());
			model().addElement("Message: " + throwable.getMessage());
			model().addElement("Stack trace:" + Tools.getLineSeparator());
			for (int i = 0; i < throwable.getStackTrace().length; i++) {
				model().addElement(new FormattedStackTraceElement(throwable.getStackTrace()[i]));
			}
		}

		private void edit() {
			try {
				Object o = getSelectedValue();
				if (!(o instanceof FormattedStackTraceElement))
					return;
				StackTraceElement ste = ((FormattedStackTraceElement) o).ste;
				java.io.File file = Tools.findSourceFile(ste);
				if (file != null)
					Tools.launchFileEditor(file, ste.getLineNumber());
			} catch (Throwable t) {
				SwingTools.showSimpleErrorMessage("Cannot launch editor.", t);
			}
		}
	}

	private ErrorDialog(String title, String mainText, Icon icon, final Throwable error, boolean bugReport) {
		super(RapidMinerGUI.getMainFrame(), title);
		setTitle(title);

		// from JOptionPane.java
		if (JDialog.isDefaultLookAndFeelDecorated()) {
			if (UIManager.getLookAndFeel().getSupportsWindowDecorations()) {
				setUndecorated(true);
				getRootPane().setWindowDecorationStyle(JRootPane.ERROR_DIALOG);
			}
		}

		getContentPane().setLayout(new BorderLayout());

		final JPanel box = new JPanel();
		final GridBagLayout gbl = new GridBagLayout();
		box.setLayout(gbl);
		final GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 0.0;
		c.weighty = 0.0;
		c.insets = new Insets(5, 11, 5, 11);
		c.gridwidth = GridBagConstraints.RELATIVE;

		// icon
		JLabel iconLabel = new JLabel(icon);
		iconLabel.setBorder(BorderFactory.createEmptyBorder(11, 11, 11, 11));
		gbl.setConstraints(iconLabel, c);
		box.add(iconLabel);

		// message
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weightx = 1.0;
		
		SimpleAttributeSet attributeSet = new SimpleAttributeSet();
		StyleConstants.setFontFamily(attributeSet, "SansSerif");
		StyleConstants.setFontSize(attributeSet, 11);

		JTextPane text = new JTextPane();
		text.setBorder(null);
		text.setEditable(false);
		text.setBackground(this.getBackground());
		text.setContentType("text/html");
		text.setFont(new Font("SansSerif", Font.PLAIN, 11));
		text.setText(mainText);
		text.getStyledDocument().setCharacterAttributes(0, text.getStyledDocument().getLength(), attributeSet, false);
        
		JScrollPane textScrollPane = new ExtendedJScrollPane(text);
		textScrollPane.setBorder(null);
		text.setPreferredSize(new Dimension((int) (RapidMinerGUI.getMainFrame().getWidth() * 0.5d), Math.max(150, (int) (RapidMinerGUI.getMainFrame().getHeight() * 0.15d))));
		
		gbl.setConstraints(textScrollPane, c);
		box.add(textScrollPane);

		// details
		final Box detailBox = new Box(BoxLayout.Y_AXIS);
		final StackTraceList stl = new StackTraceList(error);
		JScrollPane detailPane = new ExtendedJScrollPane(stl);
		detailPane.setPreferredSize(new Dimension((int) (RapidMinerGUI.getMainFrame().getWidth() * 0.5d), Math.max(150, (int) (RapidMinerGUI.getMainFrame().getHeight() * 0.25d))));
		detailBox.add(detailPane);
		editButton.setEnabled(false);
		editButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				stl.edit();
			}
		});
		JPanel detailButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		detailButtonPanel.add(editButton);
		detailBox.add(detailButtonPanel);

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		final JButton detailButton = new JButton() {

			private static final long serialVersionUID = -8444237609983095359L;
			
			private boolean more = true;
			// anonymous class constructor
			{
				setText("Details >>");
				addActionListener(new ActionListener() {

					public void actionPerformed(ActionEvent e) {
						if (more) {
							getContentPane().add(detailBox, BorderLayout.SOUTH);
							setText("<< Hide");
						} else {
							getContentPane().remove(detailBox);
							setText("Details >>");
						}
						pack();
						more = !more;
					}
				});
			}
		};
		buttonPanel.add(detailButton);

		if (bugReport) {
			JButton report = new JButton("Send bug report...");
			report.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					new BugAssistant(error).setVisible(true);
				}
			});
			buttonPanel.add(report);
		}

		JButton close = new JButton("Close");
		close.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		buttonPanel.add(close);
		c.weighty = 0.0;
		gbl.setConstraints(buttonPanel, c);
		box.add(buttonPanel);
		getContentPane().add(box, BorderLayout.CENTER);

		pack();
	}

	/**
	 * Creates a dialog showing information about the given error.
	 * 
	 * @param message
	 *            This message is only used if t does not inherit from
	 *            {@link NoBugError}
	 * @param t
	 *            the exception
	 */
	/* pp */ static ErrorDialog create(String message, Throwable t) {
		if (t instanceof NoBugError) {
			NoBugError ue = (NoBugError) t;
			return new ErrorDialog(
					"Error " + ue.getCode() + ": " + ue.getErrorName(), 
					ue.getHTMLMessage(), 
					UIManager.getIcon("OptionPane.warningIcon"), 
					t, 
					false);
		} else {
			return new ErrorDialog(
					"Error occured", 
					"<html><b>" + Tools.classNameWOPackage(t.getClass()) + "</b><br>" + Tools.escapeXML(message) + "<br>" + "Message: " + Tools.escapeXML(t.getMessage()) + "</html>", 
					UIManager.getIcon("OptionPane.errorIcon"), 
					t, 
					!(t instanceof XMLException));
		}
	}
}
