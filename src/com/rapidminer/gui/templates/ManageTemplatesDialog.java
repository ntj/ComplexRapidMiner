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
package com.rapidminer.gui.templates;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileFilter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.rapidminer.gui.MainFrame;
import com.rapidminer.gui.tools.ExtendedJScrollPane;
import com.rapidminer.gui.tools.SwingTools;
import com.rapidminer.tools.LogService;
import com.rapidminer.tools.ParameterService;


/**
 * The manage templates dialog assists the user in managing his created process templates.
 * Template processes are saved in the local &quot;.rapidminer&quot;
 * directory of the user. The name, description and additional parameters to set
 * can be specified by the user. In this dialog he can also delete one of the templates.
 * 
 * @author Ingo Mierswa
 * @version $Id: ManageTemplatesDialog.java,v 2.8 2006/04/05 08:57:23
 *          ingomierswa Exp $
 */
public class ManageTemplatesDialog extends JDialog {

	private static final long serialVersionUID = 1428487062393160289L;

	private JList templateList = new JList();

	private Map<String, Template> templateMap = new HashMap<String, Template>();

	public ManageTemplatesDialog(MainFrame mainFrame) {
		super(mainFrame, "Manage Templates", true);

		JPanel rootPanel = new JPanel(new BorderLayout());
		rootPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		GridBagLayout layout = new GridBagLayout();
		JPanel mainPanel = new JPanel(layout);
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1.0d;
		c.weighty = 0.0d;

		JPanel textPanel = SwingTools.createTextPanel("Manage Templates...", "Please select templates to delete them. Only " + "user defined templates can be removed.");
		c.gridwidth = GridBagConstraints.REMAINDER;
		layout.setConstraints(textPanel, c);
		mainPanel.add(textPanel);

		Component sep = Box.createVerticalStrut(10);
		c.gridwidth = GridBagConstraints.REMAINDER;
		layout.setConstraints(sep, c);
		mainPanel.add(sep);

		// add components to main panel
		File[] templateFiles = ParameterService.getUserRapidMinerDir().listFiles(new FileFilter() {

			public boolean accept(File file) {
				return file.getName().endsWith(".template");
			}
		});
		for (int i = 0; i < templateFiles.length; i++) {
			try {
				Template template = new Template(templateFiles[i]);
				templateMap.put(template.getName(), template);
			} catch (InstantiationException e) {
				SwingTools.showSimpleErrorMessage("Cannot load template file '" + templateFiles[i] + "'", e);
			}
		}

		JScrollPane listPane = new ExtendedJScrollPane(templateList);
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weighty = 1.0d;
		layout.setConstraints(listPane, c);
		mainPanel.add(listPane);
		c.weighty = 0.0d;

		// buttons
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		JButton deleteButton = new JButton("Delete");
		deleteButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				delete();
			}
		});
		buttonPanel.add(deleteButton);
		JButton okButton = new JButton("Ok");
		okButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				ok();
			}
		});
		buttonPanel.add(okButton);

		rootPanel.add(mainPanel, BorderLayout.CENTER);
		rootPanel.add(buttonPanel, BorderLayout.SOUTH);
		getContentPane().add(rootPanel);

		update();
		pack();
		setSize(250, 400);
		setLocationRelativeTo(mainFrame);
	}

	private void update() {
		Vector<String> data = new Vector<String>();
		Iterator<Template> i = templateMap.values().iterator();
		while (i.hasNext()) {
			Template template = i.next();
			data.add(template.getName());
		}
		templateList.setListData(data);
		repaint();
	}

	private void ok() {
		dispose();
	}

	private void delete() {
		Object[] selection = templateList.getSelectedValues();
		for (int i = 0; i < selection.length; i++) {
			String name = (String) selection[i];
			Template template = templateMap.remove(name);
			File templateFile = template.getFile();
			File expFile = new File(templateFile.getParent(), template.getFilename());
			boolean deleteResult = templateFile.delete();
			if (!deleteResult)
				LogService.getGlobal().logWarning("Unable to delete template file: " + templateFile);
			deleteResult = expFile.delete();
			if (!deleteResult)
				LogService.getGlobal().logWarning("Unable to delete template experiment file: " + expFile);
		}
		update();
	}
}
