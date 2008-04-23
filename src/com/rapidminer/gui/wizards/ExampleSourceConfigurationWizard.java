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
package com.rapidminer.gui.wizards;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.gui.RapidMinerGUI;
import com.rapidminer.gui.tools.ExtendedJScrollPane;
import com.rapidminer.gui.tools.SwingTools;
import com.rapidminer.operator.io.ExampleSource;
import com.rapidminer.parameter.Parameters;
import com.rapidminer.tools.Ontology;
import com.rapidminer.tools.Tools;
import com.rapidminer.tools.att.AttributeDataSource;


/**
 * This class is the creator for wizard dialogs defining the configuration for 
 * {@link ExampleSource} operators.
 * 
 * @author Ingo Mierswa
 * @version $Id: ExampleSourceConfigurationWizard.java,v 1.3 2007/06/02 11:35:44 ingomierswa Exp $
 */
public class ExampleSourceConfigurationWizard extends AbstractConfigurationWizard {
    
    private static final long serialVersionUID = 1261772342282270078L;

    private static final int MAX_NUMBER_OF_LINES = 20;
    
	private static final int TITLE_STEP          = 0;
    private static final int DATA_LOADING_STEP   = 1;
    private static final int COLUMN_STEP         = 2;
    private static final int NAME_STEP           = 3;
    private static final int VALUE_TYPE_STEP     = 4;
    private static final int ATTRIBUTE_TYPE_STEP = 5;
    private static final int RESULT_FILE_STEP    = 6;

    /** The list of the abstract attribute informations. */
	private ArrayList<AttributeDataSource> sources = new ArrayList<AttributeDataSource>();
	
	/** The current data sample. */
	private List<String[]> data = new ArrayList<String[]>();
	
    /** The text field with the name of the data file. */
    private JTextField fileTextField = new JTextField(40);

    /** The text field defining the comment characters. */
    private JTextField commentCharField = new JTextField("#");
    
    /** Indicates that the columns are separated by a semicolon. */
    private JRadioButton semicolonButton = new JRadioButton("separated by ;");
    
    /** Indicates that the columns are separated by a comma. */
    private JRadioButton commaButton = new JRadioButton("separated by ,");
    
    /** Indicates that the columns are separated by tabs. */
    private JRadioButton tabButton = new JRadioButton("separated by tabulars [\\t]");
    
    /** Indicates that the columns are separated by any white space. */
    private JRadioButton whiteSpaceButton = new JRadioButton("separated by any white space [\\s+]");
    
    /** Indicates that the columns are separated by the column separator defined by a regular expression. */
    private JRadioButton regExpButton = new JRadioButton("separation defined by a regular expression (default)");
    
    /** The text field with the column separator. */
    private JTextField columnSeparatorTextField = new JTextField(",\\s*|;\\s*|\\s+");
    
    /** Indicates if the first row should be used for column names. */
    private JCheckBox firstRowAsNames = new JCheckBox("Use first row for column names", false);
    
    /** The view on the data with the current settings. */
    private ExampleSourceConfigurationWizardDataTable dataView = new ExampleSourceConfigurationWizardDataTable(sources, data);
    
    /** The scroll pane for the data view. */
    private JScrollPane dataViewPane = new ExtendedJScrollPane(dataView);
    
    /** The view of the attribute value types. */
    private ExampleSourceConfigurationWizardValueTypeTable valueTypeView = new ExampleSourceConfigurationWizardValueTypeTable(sources);

    /** The view of the attribute types (regular or special). */
    private ExampleSourceConfigurationWizardAttributeTypeTable attributeTypeView = new ExampleSourceConfigurationWizardAttributeTypeTable(sources);
    
    /** The result attribute description file name. */
    private JTextField resultFileField = new JTextField(40);
    
    /** Creates a new wizard. */
    public ExampleSourceConfigurationWizard(ConfigurationListener listener) {
        super("Example Source Wizard", listener);

        dataViewPane.setVisible(false);
         
        // add all steps
        addTitleStep();
        addDataLoadingStep();
        addColumnSeparatorStep();
        addNameDefinitionStep();
        addValueTypeDefinitionStep();
        addAttributeTypeDefinitionStep();
        addResultFileDefinitionStep();
        
        addBottomComponent(dataViewPane);
    }
    
    private void addTitleStep() {
        JPanel panel = SwingTools.createTextPanel("Welcome to the Example Source Wizard", "This wizard will guide you through the process of data loading and meta data definition. Using this wizard will involve the following steps:" + 
                "<ul>" + 
                "<li>Selection of a data file</li>" + 
                "<li>Definition of the column separators</li>" + 
                "<li>Definition of the attribute names</li>" + 
                "<li>Definition of the attribute value types</li>" +
                "<li>Definition of special attributes like labels or IDs</li>" +
                "<li>Saving the data and meta data into files used by the operator</li>" +
                "</ul>");

        addStep(panel);
    }
    
    private void addDataLoadingStep() {
        JPanel panel = SwingTools.createTextPanel("Please specify a data file...", "Please specify the location of a data file. The format can almost be arbitrary as long as it is in any way a attribute value format with columns for attribute values and rows for examples.");

        GridBagLayout layout = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0;
        c.weighty = 0;
        c.insets = new Insets(7,7,7,7);
        JPanel content = new JPanel(layout);
        
        JLabel label = new JLabel("File: ");
        layout.setConstraints(label, c);
        content.add(label);
        
        c.weightx = 1;
        c.gridwidth = GridBagConstraints.RELATIVE;
        layout.setConstraints(this.fileTextField, c);
        content.add(this.fileTextField);
        
        JButton chooseFileButton = new JButton("Choose...");
        chooseFileButton.addActionListener(new ActionListener() {
           public void actionPerformed(ActionEvent e) {
               File file = SwingTools.chooseFile(ExampleSourceConfigurationWizard.this, null, true, null, null);
               fileTextField.setText(file.getAbsolutePath());
           }
        });
        c.weightx = 0;
        c.gridwidth = GridBagConstraints.REMAINDER;
        layout.setConstraints(chooseFileButton, c);
        content.add(chooseFileButton);

        label = new JLabel("Comment characters (optionally): ");
        c.weightx = 0;
        c.gridwidth = 1;
        layout.setConstraints(label, c);
        content.add(label);

        c.weightx = 1;
        c.gridwidth = GridBagConstraints.RELATIVE;
        layout.setConstraints(this.commentCharField, c);
        content.add(this.commentCharField);
        
        JPanel fillPanel = new JPanel();
        c.weightx = 0;
        c.gridwidth = GridBagConstraints.REMAINDER;
        layout.setConstraints(fillPanel, c);
        content.add(fillPanel);        
        
        JPanel yFillPanel = new JPanel();
        c.weightx = 0;
        c.weighty = 1;
        c.gridwidth = GridBagConstraints.REMAINDER;
        layout.setConstraints(yFillPanel, c);
        content.add(yFillPanel);    
        
        panel.add(content, BorderLayout.CENTER);
        addStep(panel);
    }
    
    private void addColumnSeparatorStep() {
        JPanel panel = SwingTools.createTextPanel("Please specify a column separator...", 
        		"Please specify a column separator. The default separator will separate columns " +
        		"after , or ; followed by an arbitrary number of white spaces or by white space alone. " +
        		"Please note that only the first few lines of the data file will be shown.");
        
        regExpButton.setSelected(true);
        Box buttonBox = new Box(BoxLayout.Y_AXIS);
        semicolonButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                reloadData();
                updateViews();
            }
        });
        buttonBox.add(semicolonButton);
        commaButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                reloadData();
                updateViews();
            }
        });
        buttonBox.add(commaButton);
        tabButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                reloadData();
                updateViews();
            }
        });
        buttonBox.add(tabButton);
        whiteSpaceButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                reloadData();
                updateViews();
            }
        });
        buttonBox.add(whiteSpaceButton);
        regExpButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                reloadData();
                updateViews();
            }
        });
        buttonBox.add(regExpButton);
        
        ButtonGroup group = new ButtonGroup();
        group.add(semicolonButton);
        group.add(commaButton);
        group.add(tabButton);
        group.add(whiteSpaceButton);
        group.add(regExpButton);
        
        GridBagLayout layout = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.NORTH;
        c.insets = new Insets(7,7,7,7);
        JPanel content = new JPanel(layout);
        
        c.weightx = 0;
        c.weighty = 0;
        c.gridwidth = GridBagConstraints.RELATIVE;
        c.gridheight = 2;
        layout.setConstraints(buttonBox, c);
        content.add(buttonBox);
        
        JLabel label = new JLabel("Regular expression: ");
        c.weightx = 0;
        c.weighty = 0;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.gridheight = 1;
        layout.setConstraints(label, c);
        content.add(label);
                
        GridBagLayout regExpLayout = new GridBagLayout();
        GridBagConstraints regExpC = new GridBagConstraints();
        JPanel regExpPanel = new JPanel(regExpLayout);
        regExpC.fill = GridBagConstraints.HORIZONTAL;
        regExpC.insets = new Insets(7,7,7,7);
        
        regExpC.gridwidth = GridBagConstraints.RELATIVE;
        regExpC.weightx = 1;
        regExpLayout.setConstraints(this.columnSeparatorTextField, regExpC);
        regExpPanel.add(this.columnSeparatorTextField); 
        
        JButton updateButton = new JButton("Update");
        updateButton.addActionListener(new ActionListener() {
           public void actionPerformed(ActionEvent e) {
               reloadData();
               updateViews();
           }
        });
        regExpC.gridwidth = GridBagConstraints.REMAINDER;
        regExpC.weightx = 0;
        regExpLayout.setConstraints(updateButton, regExpC);
        regExpPanel.add(updateButton); 
        
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.weightx = 1;
        layout.setConstraints(regExpPanel, c);
        content.add(regExpPanel);
        
        panel.add(content, BorderLayout.CENTER);
        addStep(panel);
    }
    
    private void addNameDefinitionStep() {
        JPanel panel = SwingTools.createTextPanel("Please specify the column names...", "Please specify if the names can be taken from the first line of the data file.");

        GridBagLayout layout = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0;
        c.weighty = 0;
        c.insets = new Insets(7,7,7,7);
        JPanel content = new JPanel(layout);        
        
        firstRowAsNames.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	reloadData();
                updateViews();
            }
        });
        c.gridwidth = GridBagConstraints.REMAINDER;
        layout.setConstraints(firstRowAsNames, c);
        content.add(firstRowAsNames);
        
        panel.add(content, BorderLayout.CENTER);
        addStep(panel);
    }
    
    private void addValueTypeDefinitionStep() {
        JPanel content = new JPanel(new BorderLayout());
        JPanel textPanel = SwingTools.createTextPanel("Please specify the attribute value types...", "Please specify the attribute value types. RapidMiner tries to guess the value types based on the the complete data file (which might take some time) but some adjustments might still be necessary.");
        content.add(textPanel, BorderLayout.NORTH);  
        JScrollPane valueTypeViewPane = new ExtendedJScrollPane(valueTypeView);
        valueTypeViewPane.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(7,7,7,7), BorderFactory.createTitledBorder("Attribute Value Types")));
        content.add(valueTypeViewPane, BorderLayout.CENTER);   
        addStep(content);
    }
    
    private void addAttributeTypeDefinitionStep() {
        JPanel content = new JPanel(new BorderLayout());
        JPanel textPanel = SwingTools.createTextPanel("Please specify special attributes...", "Please specify special attribues if there are any. You can specify arbitrary special attributes in the Attribute Editor but in this Wizard only the most important types (label, id...) are supported.");
        content.add(textPanel, BorderLayout.NORTH);  
        JScrollPane typeViewPane = new ExtendedJScrollPane(attributeTypeView);
        typeViewPane.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(7,7,7,7), BorderFactory.createTitledBorder("Attribute Types")));
        content.add(typeViewPane, BorderLayout.CENTER);   
        addStep(content);
    }
    
    private void addResultFileDefinitionStep() {
        JPanel panel = SwingTools.createTextPanel("Please specify a file name...", 
        		"Please specify a file name which is used for the created attribute description " +
        		"file (.aml) based on the settings before. A corresponding data file will automatically be saved " + 
        		"with the extension \".dat\". Please note that existing files with these names will be overwritten. " +
                "Both files are necessary parameters for the ExampleSource operator " +
        		"and will - like all other important parameters - automatically be defined for this operator after this " +
        		"wizard was finished.");

        GridBagLayout layout = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0;
        c.weighty = 0;
        c.insets = new Insets(7,7,7,7);
        JPanel content = new JPanel(layout);
                
        JLabel label = new JLabel("Result file name: ");
        layout.setConstraints(label, c);
        content.add(label);
        
        c.weightx = 1;
        c.gridwidth = GridBagConstraints.RELATIVE;
        layout.setConstraints(this.resultFileField, c);
        content.add(this.resultFileField);
        
        JButton chooseFileButton = new JButton("Choose...");
        chooseFileButton.addActionListener(new ActionListener() {
           public void actionPerformed(ActionEvent e) {
               File file = SwingTools.chooseFile(ExampleSourceConfigurationWizard.this, null, false, "aml", "attribute description file");
               resultFileField.setText(file.getAbsolutePath());
           }
        });
        c.weightx = 0;
        c.gridwidth = GridBagConstraints.REMAINDER;
        layout.setConstraints(chooseFileButton, c);
        content.add(chooseFileButton);
        
        JPanel yFillPanel = new JPanel();
        c.weightx = 0;
        c.weighty = 1;
        c.gridwidth = GridBagConstraints.REMAINDER;
        layout.setConstraints(yFillPanel, c);
        content.add(yFillPanel);   
        
        panel.add(content, BorderLayout.CENTER);
        addStep(panel);
    }

    private void reloadData() {
        this.sources.clear();
        this.data.clear();
    
        if (fileTextField.getText().trim().length() == 0)
        	return;
        
    	File file = new File(fileTextField.getText());
    	String commentString = commentCharField.getText();
    	String columnSeparators = getColumnSeparators();
    	boolean firstLineAsNames = firstRowAsNames.isSelected();
    	
        String[] columnNames = null;        
        int maxColumns = 0;
        try {
        	BufferedReader in = new BufferedReader(new FileReader(file));
        	String line = null;
        	int counter = 0;
        	boolean first = true;
        	while (((line = in.readLine()) != null) && (counter <= MAX_NUMBER_OF_LINES)) {
        		if ((commentString != null) && (commentString.trim().length() > 0) && (line.startsWith(commentString)))
        			continue;
        		String[] columns = line.trim().split(columnSeparators);
        		maxColumns = Math.max(maxColumns, columns.length);
        		if (first) {
        			if (firstLineAsNames) {
        				columnNames = columns;
        			} else {
        				data.add(columns);
        			}
        			first = false;
        		} else {
        			data.add(columns);
        		}
        		counter++;
        	}
        	in.close();
        } catch (IOException e) {
            SwingTools.showSimpleErrorMessage("Cannot load data: " + e.getMessage(), e);
        }

        if (columnNames == null) {
        	String defaultName = file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf(File.separator) + 1);
        	columnNames = new String[maxColumns];
        	for (int i = 0; i < columnNames.length; i++)
        		columnNames[i] = defaultName + " (" + (i+1) + ")";
        } else if (columnNames.length < maxColumns) {
        	String defaultName = file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf(File.separator) + 1);
        	String[] newColumnNames = new String[maxColumns];
        	System.arraycopy(columnNames, 0, newColumnNames, 0, columnNames.length);
        	for (int i = columnNames.length; i < newColumnNames.length; i++) {
        		newColumnNames[i] = defaultName + " (" + (i+1) + ")";
        	}
        }
        for (int i = 0; i < maxColumns; i++) {
        	this.sources.add(new AttributeDataSource(AttributeFactory.createAttribute(columnNames[i], Ontology.NOMINAL), file, i, "attribute"));
        }
        
		//valueTypeView.guessValueTypes(new File(fileTextField.getText()), commentString, columnSeparators, firstLineAsNames);
    }

    private String getColumnSeparators() {
        String columnSeparators = columnSeparatorTextField.getText();
        if (semicolonButton.isSelected()) {
            columnSeparators = ";";
        } else if (commaButton.isSelected()) {
            columnSeparators = ",";
        } else if (tabButton.isSelected()) {
            columnSeparators = "\\t";
        } else if (whiteSpaceButton.isSelected()) {
            columnSeparators = "\\s+";
        }
        return columnSeparators;
    }
    
    private void updateViews() {
    	dataView.update();
        dataViewPane.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(7,7,7,7), BorderFactory.createTitledBorder("Data Example (" + data.size() + " rows, " + sources.size() + " columns)")));
    	valueTypeView.update();
    	attributeTypeView.update();
    }
    
 
    protected void performStepAction(int currentStep, int oldStep) {
        if ((currentStep > 1) && (currentStep < getNumberOfSteps() - 1))
            dataViewPane.setVisible(true);
        else 
            dataViewPane.setVisible(false);
        
        switch (currentStep) {
        case TITLE_STEP: break;
        case DATA_LOADING_STEP: break;
        case COLUMN_STEP: 
        	reloadData();
        	break;
        case NAME_STEP: break;
        case VALUE_TYPE_STEP:
            String commentString = commentCharField.getText();
            String columnSeparators = getColumnSeparators();
            boolean firstLineAsNames = firstRowAsNames.isSelected();
            valueTypeView.guessValueTypes(new File(fileTextField.getText()), commentString, columnSeparators, firstLineAsNames);
            break;
        case ATTRIBUTE_TYPE_STEP: break;
        case RESULT_FILE_STEP:
            for (int i = 1; i < Attributes.KNOWN_ATTRIBUTE_TYPES.length; i++)
                ensureAttributeTypeIsUnique(Attributes.KNOWN_ATTRIBUTE_TYPES[i]);
            break;
        }
        
        updateViews();
    }

    protected void finish(ConfigurationListener listener) {        
        String resultFileName = resultFileField.getText().trim();
        // sanity checks
        if ((sources.size() == 0) || ((data.size() == 0))) {
            SwingTools.showVerySimpleErrorMessage("You must specify a data file and proper settings - the operator will not work without this." + Tools.getLineSeparator() + "Please select \"Cancel\" if you want to abort this wizard.");
        } else if (resultFileName.length() == 0) {
            SwingTools.showVerySimpleErrorMessage("You must specify a file name for the attribute description file - " + Tools.getLineSeparator() + "the operator will not work without this. Please select \"Cancel\" if you want" + Tools.getLineSeparator() + "to abort this wizard.");
        } else {
            // everything is OK --> write files and dispose
            File attributeFile = new File(resultFileName);
            File dataFile = new File(resultFileName.substring(0, resultFileName.lastIndexOf(".") + 1) + "dat");
            try {
                writeData(dataFile);
                writeAttributeDescriptions(attributeFile);
            } catch (IOException e) {
                SwingTools.showSimpleErrorMessage("ExampleSource Configuation Wizard was not able to write a file.", e);
            }
            Parameters parameters = listener.getParameters();
            parameters.setParameter("attributes", attributeFile.getAbsolutePath());
            parameters.setParameter("column_separators", "\\t");
            parameters.setParameter("comment_chars", commentCharField.getText());
            listener.setParameters(parameters);
            dispose();
            RapidMinerGUI.getMainFrame().getPropertyTable().refresh();
        }
    }

    /** Writes the complete data set into a new file. This method should not be called during the 
     *  wizard configuration but only during finishing due to performance reasons. */
    private void writeData(File file) throws IOException {
        // set data file for attribute sources
        for (int i = 0; i < sources.size(); i++) {
            AttributeDataSource source = sources.get(i);
            source.setSource(file, i);
        }
          
        PrintWriter out = new PrintWriter(new FileWriter(file));
        
        File originalDataFile = new File(fileTextField.getText());
        String commentString = commentCharField.getText();
        String columnSeparators = getColumnSeparators();
        boolean firstLineAsNames = firstRowAsNames.isSelected();
                
        try {
            BufferedReader in = new BufferedReader(new FileReader(originalDataFile));
            String line = null;
            boolean first = true;
            while ((line = in.readLine()) != null) {
                if ((commentString != null) && (commentString.trim().length() > 0) && (line.startsWith(commentString)))
                    continue;
                String[] columns = line.trim().split(columnSeparators);
                if (first) {
                    if (!firstLineAsNames) {
                        writeColumnData(out, columns);
                    }
                    first = false;
                } else {
                    writeColumnData(out, columns);
                }
            }
            in.close();
        } catch (IOException e) {
            SwingTools.showSimpleErrorMessage("Cannot re-write data: " + e.getMessage(), e);
        }
        out.close();
    }
    
    private void writeColumnData(PrintWriter out, String[] columnData) {
        for (int col = 0; col < columnData.length; col++) {
            if (col != 0)
                out.print("\t");
            out.print(columnData[col]);
        }
        out.println();    
    }
    
    private void writeAttributeDescriptions(File file) {
        if (file != null) {
            try {
                writeXML(file);
            } catch (java.io.IOException e) {
                JOptionPane.showMessageDialog(this, e.toString(), "Error saving attribute file " + file, JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /** Before this method will properly work the method {@link #writeData(File)} must have been called. */
    private void writeXML(File attFile) throws IOException {
        PrintWriter out = new PrintWriter(new FileWriter(attFile));
        File defaultSource = sources.get(0).getFile();
        String relativePath = Tools.getRelativePath(defaultSource, attFile);
        out.println("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
        out.println("<attributeset default_source=\"" + relativePath + "\">");
        Iterator i = sources.iterator();
        int c = 0;
        while (i.hasNext()) {
            AttributeDataSource ads = (AttributeDataSource) i.next();
            Attribute attribute = ads.getAttribute();
            if (attribute.isNominal()) {
                Iterator<String[]> d = data.iterator();
                while (d.hasNext()) {
                    String[] dataRow = d.next();
                    attribute.getMapping().mapString(dataRow[c]);
                }
            }
            ads.writeXML(out, defaultSource);
            c++;
        }
        out.println("</attributeset>");
        out.close();
    }

    private void ensureAttributeTypeIsUnique(String type) {
        List<AttributeDataSource> columns = new LinkedList<AttributeDataSource>();
        List<Integer> columnNumbers = new LinkedList<Integer>();
        Iterator<AttributeDataSource> i = sources.iterator();
        int j = 0;
        while (i.hasNext()) {
            AttributeDataSource source = i.next();
            if ((source.getType() != null) && source.getType().equals(type)) {
                columns.add(source);
                columnNumbers.add(j);
            }
            j++;
        }
        if (columns.size() > 1) {
            String[] names = new String[columns.size()];
            int counter = 0;
            for (AttributeDataSource ads : columns) {
                names[counter++] = ads.getAttribute().getName();
            }
            javax.swing.JTextArea message = new javax.swing.JTextArea("The special attribute " + type + " is multiply defined. Please select one of the data columns (others will be changed to regular attributes). Press \"Cancel\" to ignore.", 4, 40);
            message.setEditable(false);
            message.setLineWrap(true);
            message.setWrapStyleWord(true);
            message.setBackground(new javax.swing.JLabel("").getBackground());
            String selection = (String) JOptionPane.showInputDialog(this, message, type + " multiply defined", JOptionPane.WARNING_MESSAGE, null, names, names[0]);
            if (selection != null) {
                i = columns.iterator();
                while (i.hasNext()) {
                    AttributeDataSource source = i.next();
                    if (!source.getAttribute().getName().equals(selection)) {
                        source.setType(Attributes.ATTRIBUTE_NAME);
                        updateViews();
                    }
                }
            }
        }
    }
}