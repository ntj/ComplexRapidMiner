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
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;

import com.rapidminer.Process;
import com.rapidminer.gui.MainFrame;
import com.rapidminer.gui.dialog.boxviewer.ProcessRenderer;
import com.rapidminer.gui.properties.WizardPropertyTable;
import com.rapidminer.gui.templates.Template;
import com.rapidminer.gui.tools.ExtendedJScrollPane;
import com.rapidminer.gui.tools.SwingTools;
import com.rapidminer.tools.ParameterService;
import com.rapidminer.tools.XMLException;


/**
 * The wizard dialog assists the user in creating a new process setup. Template
 * processes are loaded from the etc/templates directory or from the user
 * directory &quot;.rapidminer&quot;. Instances of all processes are created and the
 * parameters can be set.
 * 
 * @author Ingo Mierswa, Simon Fischer
 * @version $Id: WizardDialog.java,v 1.8 2008/05/09 19:23:21 ingomierswa Exp $
 */
public class WizardDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    private static final String WIZARD_ICON_NAME = "wizard.jpg";
    
    private static Icon wizardIcon;

	static {
		// init icon
		wizardIcon = SwingTools.createImage(WIZARD_ICON_NAME);
	}

	private MainFrame mainFrame;

	private JButton delete = new JButton("Delete");

	private JButton next = new JButton("Next >");

	private JButton previous = new JButton("< Previous");

	private CardLayout cardLayout = new CardLayout();

	private JPanel mainPanel = new JPanel(cardLayout);

	private ProcessRenderer processRenderer = new ProcessRenderer();

	private WizardPropertyTable propertyTable = new WizardPropertyTable();

	private int currentStep = 0;

	private int numberOfSteps = 0;

	private transient Process[] processes;

	private transient Template[] templates;

	private int selectedTemplateIndex;
    
    
	public WizardDialog(MainFrame mainFrame) {
		super(mainFrame, "Wizard", true);
		this.mainFrame = mainFrame;

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		buttonPanel.add(next);
		buttonPanel.add(previous);
		previous.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				step(-1);
			}
		});
		buttonPanel.add(next);
		next.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				step(1);
			}
		});
		buttonPanel.add(Box.createHorizontalStrut(11));
		JButton cancel = new JButton("Cancel");
		buttonPanel.add(cancel);
		cancel.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				cancel();
			}
		});

		getContentPane().add(buttonPanel, BorderLayout.SOUTH);

		getContentPane().add(mainPanel, BorderLayout.CENTER);
		mainPanel.setBorder(BorderFactory.createEmptyBorder(11, 11, 11, 11));

		addTitle();
		JPanel panel = createChooseTemplate();
		addStep(panel);
		addParameters();

		step(0);
		pack();
		setSize(800, 600);
		setLocationRelativeTo(mainFrame);
	}

	private void addTitle() {
		JPanel panel = SwingTools.createTextPanel("Welcome to the RapidMiner Process Wizard", "This wizard will guide you setting up a new process definition.</p>");

		JPanel content = new JPanel(new FlowLayout(FlowLayout.LEFT));

		JLabel image = new JLabel(wizardIcon);
		image.setBorder(BorderFactory.createLoweredBevelBorder());
		content.add(image);

		JLabel label = new JLabel("<html>Using the wizard will involve the following steps:" + "<ul>" + "<li>Choose a template process setup</li>" + "<li>Set some of the important parameters</li>" + "<li>Create an attribute description file if necessary</li>" + "</ul>"
				+ "<p>Completing these steps you can immediately start the process or " + "go on editing and make advanced settings.</p><p>Please check also the sample directory of RapidMiner containing a huge number of additional processes.</p></html>");
		label.setPreferredSize(new java.awt.Dimension(300, 200));
		label.setFont(label.getFont().deriveFont(java.awt.Font.PLAIN));
		content.add(label);

		panel.add(content, BorderLayout.CENTER);
		addStep(panel);
	}

	private JPanel createChooseTemplate() {
		File[] preDefinedTemplateFiles = ParameterService.getConfigFile("templates").listFiles(new FileFilter() {

			public boolean accept(File file) {
				return file.getName().endsWith(".template");
			}
		});

		File[] userDefinedTemplateFiles = ParameterService.getUserRapidMinerDir().listFiles(new FileFilter() {

			public boolean accept(File file) {
				return file.getName().endsWith(".template");
			}
		});

		File[] templateFiles = new File[preDefinedTemplateFiles.length + userDefinedTemplateFiles.length];
		System.arraycopy(preDefinedTemplateFiles, 0, templateFiles, 0, preDefinedTemplateFiles.length);
		System.arraycopy(userDefinedTemplateFiles, 0, templateFiles, preDefinedTemplateFiles.length, userDefinedTemplateFiles.length);

		JPanel panel = SwingTools.createTextPanel("Select a template", "Please select one of the following template processes listed below. " + "The image on the right is a schematic figure of the process setup showing " + "operators as blue boxes and chains "
				+ "and wrappers as brownish boxes containing their inner operators.");

		Box radioBox = new Box(BoxLayout.Y_AXIS);
		ButtonGroup group = new ButtonGroup();
		templates = new Template[templateFiles.length];
		processes = new Process[templateFiles.length];
		for (int i = 0; i < templates.length; i++) {
			try {
				templates[i] = new Template(templateFiles[i]);
				processes[i] = new Process(new File(templateFiles[i].getParent(), templates[i].getFilename()));
				processes[i].setProcessFile(null);
			} catch (InstantiationException e) {
				SwingTools.showSimpleErrorMessage("Cannot load template file '" + templateFiles[i] + "'", e);
				processes[i] = new Process();
				templates[i] = new Template();
			} catch (IOException e) {
				SwingTools.showSimpleErrorMessage("Cannot load template process file '" + templateFiles[i] + "'", e);
				processes[i] = new Process();
				templates[i] = new Template();
			} catch (XMLException e) {
				SwingTools.showSimpleErrorMessage("Cannot load template process file '" + templateFiles[i] + "'", e);
				processes[i] = new Process();
				templates[i] = new Template();
			}

			JRadioButton b = new JRadioButton("<html>" + templates[i].toHTML() + "</html>");
			b.setFont(b.getFont().deriveFont(java.awt.Font.PLAIN));
			if (i == 0)
				b.setSelected(true);
			final int j = i;
			b.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					select(j);
				}
			});
			radioBox.add(b);
			group.add(b);
		}

		GridBagLayout layout = new GridBagLayout();
		JPanel centerPanel = new JPanel(layout);
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 0.0d;
		c.weighty = 0.0d;

		c.gridwidth = GridBagConstraints.RELATIVE;
		layout.setConstraints(radioBox, c);
		centerPanel.add(radioBox);

		c.weightx = 1.0d;
		c.weighty = 1.0d;
		c.gridwidth = GridBagConstraints.REMAINDER;
		layout.setConstraints(processRenderer, c);
		centerPanel.add(processRenderer);

        JScrollPane centerScrollPane = new ExtendedJScrollPane(centerPanel); 
		panel.add(centerScrollPane, BorderLayout.CENTER);
        
        select(0);
        
		return panel;
	}

	private void addParameters() {
		JPanel panel = SwingTools.createTextPanel("Make Settings", "In the following, you find a list of the most important parameters of this process definition. " + "Some of the parameters may have default values. The parameters set in bold face are mandatory. "
				+ "Pointing with the mouse on one of the parameters you will get some information about the meaning " + "of the parameter.");

		panel.add(new ExtendedJScrollPane(propertyTable), BorderLayout.CENTER);
		JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER));
		panel.add(buttons, BorderLayout.SOUTH);
		addStep(panel);
	}

	private void addStep(Component c) {
		mainPanel.add(c, numberOfSteps + "");
		numberOfSteps++;
	}

	private void select(int index) {
		selectedTemplateIndex = index;
		Process process = processes[index];
		Template template = templates[index];
		if ((process != null) && (template != null)) {
			processRenderer.setOperator(process.getRootOperator());
			propertyTable.setProcess(process, template.getParameters());
		} else {
			processRenderer.setOperator(null);
			propertyTable.setProcess(null, null);
		}
	}

	private void step(int dir) {
		currentStep += dir;

		if (currentStep == 1)
			delete.setEnabled(true);
		else
			delete.setEnabled(false);

		if (currentStep < 0)
			currentStep = 0;
		if (currentStep == 0)
			previous.setEnabled(false);
		else
			previous.setEnabled(true);

		if (currentStep >= numberOfSteps)
			finish();
		if (currentStep == numberOfSteps - 1) {
			next.setText("Finish");
		} else {
			next.setText("Next >");
		}

		cardLayout.show(mainPanel, currentStep + "");
	}

	private void finish() {
		mainFrame.setProcess(processes[selectedTemplateIndex], true);
        mainFrame.processChanged();
		dispose();
	}

	private void cancel() {
		dispose();
	}
}
