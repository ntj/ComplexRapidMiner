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
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

import com.rapidminer.Process;
import com.rapidminer.gui.MainFrame;
import com.rapidminer.gui.RapidMinerGUI;
import com.rapidminer.gui.tools.ExtendedJScrollPane;
import com.rapidminer.gui.tools.SwingTools;
import com.rapidminer.tools.ParameterService;


/**
 * The RapidMiner online tutorial. This dialog loads a set of sample process definitions which
 * can be performed and altered by the user.
 * 
 * @author Ingo Mierswa
 * @version $Id: Tutorial.java,v 1.8 2008/05/09 19:23:20 ingomierswa Exp $
 */
public class Tutorial extends JDialog {

	private static final long serialVersionUID = 2826577972132069114L;

	static {
		UIDefaults uiDefaults = UIManager.getDefaults();
		Font f = new Font("SansSerif", Font.PLAIN, 12);
		Font font = new FontUIResource(f);

		uiDefaults.put("EditorPane.font", font);
	}

	private static final String START_TEXT = "<h2>Welcome to the RapidMiner online tutorial!</h2><p>This tutorial demonstrates basic concepts of RapidMiner and simple process setups which can be performed. The user should have some knowledge in the domain of machine learning and data mining.</p><p>Whenever this tutorial refers to the &quot;RapidMiner Tutorial&quot;, it means the printed version available at<br><center><code>http://rapid-i.com</code></center></p><p>You should read the first chapter of the RapidMiner Tutorial for better motivation, but you can also try to start with the online tutorial without reading the printed version. Please read the texts carefully and try at least the suggested steps. The online tutorial will take about one hour.</p><h4>Please note:</h4><p>Most parts of RapidMiner provide additional information if you hold the mouse pointer a few moments on the part (tool tip texts). In this way all operators and parameters are described too.</p>";

	private static final String END_TEXT = "<h2>Congratulations!</h2><p>You have finished the RapidMiner online tutorial. You should be able to perform many of the possible process definitions. Now, you know the most important building blocks of the possible data mining process definitions. Of course these building blocks can be arbitrarily nested in RapidMiner as long as their input and output types fits. For a reference of all operators please refer to the RapidMiner Tutorial. Check also the other sample process setups which can be found in the sample directory of RapidMiner.</p><p>We have added many known preprocessing steps and learning operators to RapidMiner. Most data formats can also be handled. If you need to adapt RapidMiner you should read the chapter of the RapidMiner Tutorial which describes the creation of operators and the extension mechanism. RapidMiner can easily be extended. Have fun!</p>";

	private static final String[] PROCESSES = new String[] {
            "Empty.xml",
            "01_IO" + File.separator + "01_ExampleSource.xml",
            "02_Learner" + File.separator + "01_DecisionTree.xml",
            "01_IO" + File.separator + "18_ModelWriter.xml",
            "01_IO" + File.separator + "19_ModelLoader.xml",
            "02_Learner" + File.separator + "12_AssociationRules.xml",
            "02_Learner" + File.separator + "19_Stacking.xml",
            "08_Clustering" + File.separator + "01_KMeans.xml",
            "01_IO" + File.separator + "03_Sparse.xml",
            "01_IO" + File.separator + "02_ArffExampleSource.xml",
            "01_IO" + File.separator + "26_ExcelExampleSource.xml",
            "06_Visualisation" + File.separator + "08_SVMVisualisation.xml",
            "03_Preprocessing" + File.separator + "07_MissingValueReplenishment.xml",
            "03_Preprocessing" + File.separator + "08_NoiseGenerator.xml",
            "03_Preprocessing" + File.separator + "15_ExampleSetJoin.xml",
            "04_Validation" + File.separator + "03_XValidation_Numerical.xml",
            "02_Learner" + File.separator + "14_CostSensitiveLearningAndROCPlot.xml",
            "02_Learner" + File.separator + "13_AsymmetricCostLearning.xml",
            "02_Learner" + File.separator + "18_SimpleCostSensitiveLearning.xml",
            "05_Features" + File.separator + "03_PrincipalComponents.xml",
            "05_Features" + File.separator + "10_ForwardSelection.xml",
            //"05_Features" + File.separator + "09_FeatureSelectionFilter.xml",
            "05_Features" + File.separator + "12_WeightGuidedFeatureSelection.xml",
            "05_Features" + File.separator + "18_MultiobjectiveSelection.xml",
            "04_Validation" + File.separator + "12_WrapperValidation.xml",
            "05_Features" + File.separator + "19_YAGGA.xml",
            "05_Features" + File.separator + "20_YAGGAResultAttributeSetting.xml",
            "03_Preprocessing" + File.separator + "12_FeatureGenerationByUser.xml",
            "05_Features" + File.separator + "13_EvolutionaryWeighting.xml",
            "06_Visualisation" + File.separator + "07_DataSetAndWeightsVisualisation.xml",
            "01_IO" + File.separator + "21_PreprocessingModelWriter.xml",
            "01_IO" + File.separator + "22_PreprocessingModelLoader.xml",
            "07_Meta" + File.separator + "01_ParameterOptimization.xml",
            "07_Meta" + File.separator + "06_OperatorEnabler.xml",
            "05_Features" + File.separator + "17_WeightingThreshold.xml",
            "04_Validation" + File.separator + "13_SignificanceTest.xml",
            "07_Meta" + File.separator + "08_MacroDefinition.xml"
    };

	private int state = 0;

	private MainFrame mainFrame;

	private JEditorPane description;

	private JScrollPane descriptionScrollPane;

	private JButton prevButton, nextButton;

	public Tutorial(MainFrame mainFrame) {
		super(mainFrame, "RapidMiner Tutorial", false);
		this.mainFrame = mainFrame;

		JPanel rootPanel = new JPanel(new BorderLayout());
		rootPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		GridBagLayout layout = new GridBagLayout();
		JPanel mainPanel = new JPanel(layout);
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.ipadx = 10;
		c.ipady = 10;
		c.weightx = 1.0d;
		c.weighty = 1.0d;

		description = new JEditorPane("text/html", SwingTools.text2DisplayHtml(START_TEXT));
		description.setEditable(false);
		description.setBackground(this.getBackground());
		descriptionScrollPane = new ExtendedJScrollPane(description);
		c.gridwidth = GridBagConstraints.REMAINDER;
		layout.setConstraints(descriptionScrollPane, c);
		mainPanel.add(descriptionScrollPane);
		c.weighty = 0.0d;

		rootPanel.add(mainPanel, BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		prevButton = new JButton("Previous");
		prevButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				previous();
			}
		});
		prevButton.setEnabled(false);
		buttonPanel.add(prevButton);
		nextButton = new JButton("Next");
		nextButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				next();
			}
		});
		buttonPanel.add(nextButton);
		JButton closeButton = new JButton("Close");
		closeButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				close();
			}
		});
		buttonPanel.add(closeButton);
		rootPanel.add(buttonPanel, BorderLayout.SOUTH);

		getContentPane().add(rootPanel);

		pack();
		setSize(400, 600);
		setLocationRelativeTo(mainFrame);
		mainFrame.setTutorialMode(true);
	}

	private void setProcess(String process) {
		File expFile = ParameterService.getUserSampleFile(process);
		if ((expFile == null) || (!expFile.exists()))
			expFile = ParameterService.getSampleFile(process);
		mainFrame.open(expFile, false);
		description.setText(SwingTools.text2DisplayHtml(RapidMinerGUI.getMainFrame().getProcess().getRootOperator().getUserDescription()));
		description.getCaret().setDot(0);
		descriptionScrollPane.getVerticalScrollBar().setValue(0);
	}

	private void previous() {
		nextButton.setEnabled(true);
		if (state > 0)
			state--;
		if (state == 0) {
			prevButton.setEnabled(false);
			description.setText(SwingTools.text2DisplayHtml(START_TEXT));
		} else {
			setProcess(PROCESSES[state - 1]);
		}
	}

	private void next() {
		prevButton.setEnabled(true);
		if (state < PROCESSES.length + 1)
			state++;
		if (state == PROCESSES.length + 1) {
			nextButton.setEnabled(false);
			description.setText(SwingTools.text2DisplayHtml(END_TEXT));
		} else {
			setProcess(PROCESSES[state - 1]);
		}
	}

	private void close() {
		mainFrame.setProcess(new Process(), true);
		mainFrame.setTutorialMode(false);
		dispose();
	}
}
