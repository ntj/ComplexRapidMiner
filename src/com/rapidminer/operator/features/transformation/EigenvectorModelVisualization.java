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
package com.rapidminer.operator.features.transformation;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import com.rapidminer.datatable.DataTable;
import com.rapidminer.datatable.SimpleDataTable;
import com.rapidminer.datatable.SimpleDataTableRow;
import com.rapidminer.gui.plotter.Plotter;
import com.rapidminer.gui.plotter.ScatterPlotter;
import com.rapidminer.gui.tools.ExtendedJScrollPane;
import com.rapidminer.gui.tools.ExtendedJTable;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.tools.Tools;


/**
 * This class can be used for all eigenvector based model visualizations. 
 * 
 * @author Ingo Mierswa
 * @version $Id: EigenvectorModelVisualization.java,v 1.7 2008/05/09 19:22:51 ingomierswa Exp $
 */
public class EigenvectorModelVisualization implements ActionListener {

    private static class EigenvectorModel extends AbstractTableModel {

        private static final long serialVersionUID = -9026248524043239399L;

        private String[] attributeNames;
        
        private List<? extends ComponentVector> eigenVectors;
        
        private int numberOfComponents;
        
        public EigenvectorModel(List<? extends ComponentVector> eigenVectors, String[] attributeNames, int numberOfComponents) {
        	this.eigenVectors = eigenVectors;
        	this.attributeNames = attributeNames;
        	this.numberOfComponents = numberOfComponents;
        }
        
        public int getColumnCount() {
            return eigenVectors.size() + 1;
        }

        public int getRowCount() {
            return numberOfComponents;
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            if (columnIndex == 0) {
                return attributeNames[rowIndex];
            } else {
                return Tools.formatNumber(eigenVectors.get(columnIndex - 1).getVector()[rowIndex]);
            }
        }
        
        public String getColumnName(int column) {
            if (column == 0) {
                return "Attribute";
            } else {
                return "PC " + column;
            }
        }
    };

    
    private static class EigenvalueModel extends AbstractTableModel {

        private static final long serialVersionUID = -9026248524043239399L;

        private double varianceSum;

        private double[] cumulativeVariance;
        
        private List<? extends ComponentVector> eigenVectors;

        public EigenvalueModel(List<? extends ComponentVector> eigenVectors, double[] cumulativeVariance, double varianceSum) {
        	this.eigenVectors = eigenVectors;
        	this.cumulativeVariance = cumulativeVariance;
            this.varianceSum = varianceSum;
        }
        
        public int getColumnCount() {
            return 4;
        }

        public int getRowCount() {
            return eigenVectors.size();
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            switch (columnIndex) {
                case 0: return "PC " + (rowIndex + 1);
                case 1: return Tools.formatNumber(Math.sqrt(eigenVectors.get(rowIndex).getEigenvalue()));
                case 2: return Tools.formatNumber(eigenVectors.get(rowIndex).getEigenvalue() / this.varianceSum);
                case 3: return Tools.formatNumber(cumulativeVariance[rowIndex]);
                default: return "unknown";
            }
        }
        
        public String getColumnName(int column) {
            switch (column) {
                case 0: return "Component";
                case 1: return "Standard Deviation";
                case 2: return "Proportion of Variance";
                case 3: return "Cumulative Variance";
                default: return "unknown";
            }
        }
    };

    
    
    private Component[] visualizationComponent;

    private JPanel visualizationPanel;

    private JRadioButton eigenValueTableButton;
    private JRadioButton eigenVectorTableButton;
    private JRadioButton cumulativeVariancePlotButton;

    private int lastSelectedIndex;

    
    private String[] attributeNames;
    
    private double[] cumulativeVariance;
    
    private List<? extends ComponentVector> eigenVectors;
    
    private boolean manualNumber;
    
    private int numberOfComponents;
    
    private String name;
    
    private double varianceThreshold;
    
    public EigenvectorModelVisualization(String name,
                                         String[] attributeNames,
                                         double[] cumulativeVariances,
                                         List<? extends ComponentVector> eigenVectors,
                                         boolean manualNumber,
                                         int numberOfComponents,
                                         double varianceThreshold) {
        this.name = name;
        this.attributeNames = attributeNames;
        this.cumulativeVariance = cumulativeVariances;
        this.eigenVectors = eigenVectors;
        this.manualNumber = manualNumber;
        this.numberOfComponents = numberOfComponents;
        this.varianceThreshold = varianceThreshold;
    }
    
    private Component getEigenvalueTable() {
        double varianceSum = 0.0d;
        for (ComponentVector ev : this.eigenVectors) {
            varianceSum += ev.getEigenvalue();
        }
        
        JTable eigenvalueTable = new ExtendedJTable();
        eigenvalueTable.setModel(new EigenvalueModel(eigenVectors, cumulativeVariance, varianceSum));


        StringBuffer result = new StringBuffer("<html><h1>" + name + " - Eigenvalues" + "</h1>");
        if (manualNumber) {
            result.append("Number of Components: " + numberOfComponents + "<br><br>");
        } else {
            result.append("Variance Threshold: " + varianceThreshold + "<br></html>");
        }
        
        JLabel headerLabel = new JLabel(result.toString());
        
        JPanel panel = new JPanel();
        GridBagLayout layout = new GridBagLayout();
        panel.setLayout(layout);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0d;
        c.weighty = 0.0d;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.insets = new Insets(11,11,11,11);
        c.gridwidth = GridBagConstraints.REMAINDER;
        
        layout.setConstraints(headerLabel, c);
        panel.add(headerLabel);
        
        JScrollPane tablePane = new ExtendedJScrollPane(eigenvalueTable);
        c.weighty = 1.0d;
        layout.setConstraints(tablePane, c);
        panel.add(tablePane);
        
        return panel;
    }

    private Component getEigenvectorTable() {        
        JTable eigenvectorTable = new ExtendedJTable();
        eigenvectorTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        eigenvectorTable.setModel(new EigenvectorModel(eigenVectors, attributeNames, numberOfComponents));


        StringBuffer result = new StringBuffer("<html><h1>" + name + " - Eigenvectors" + "</h1>");
        if (manualNumber) {
            result.append("Number of Components: " + numberOfComponents + "<br><br>");
        } else {
            result.append("Variance Threshold: " + varianceThreshold + "<br></html>");
        }
        
        JLabel headerLabel = new JLabel(result.toString());
        
        JPanel panel = new JPanel();
        GridBagLayout layout = new GridBagLayout();
        panel.setLayout(layout);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0d;
        c.weighty = 0.0d;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.insets = new Insets(11,11,11,11);
        c.gridwidth = GridBagConstraints.REMAINDER;
        
        layout.setConstraints(headerLabel, c);
        panel.add(headerLabel);
        
        JScrollPane tablePane = new ExtendedJScrollPane(eigenvectorTable);
        c.weighty = 1.0d;
        layout.setConstraints(tablePane, c);
        panel.add(tablePane);
        
        return panel;
    }

    private JComponent getVariancePlot() {
        DataTable dataTable = new SimpleDataTable("Cumulative Proportion of Variance", new String[] { "Principal Components", "Cumulative Proportion of Variance" });
        dataTable.add(new SimpleDataTableRow(new double[] { 0.0d, 0.0d }));
        for (int i = 0; i < this.cumulativeVariance.length; i++) {
            dataTable.add(new SimpleDataTableRow(new double[] { i + 1, cumulativeVariance[i] }));
        }

        Plotter plotter = new ScatterPlotter(dataTable);
        plotter.setAxis(ScatterPlotter.X_AXIS, 0);
        plotter.setPlotColumn(1, true);

        return plotter.getPlotter();
    }

    public Component getVisualizationComponent(IOContainer container) {
        visualizationPanel = new JPanel();
        visualizationPanel.setLayout(new BorderLayout());

        eigenValueTableButton = new JRadioButton("Eigenvalue View");
        eigenVectorTableButton = new JRadioButton("Eigenvector View");
        cumulativeVariancePlotButton = new JRadioButton("Variance Plot View");

        eigenValueTableButton.addActionListener(this);
        eigenVectorTableButton.addActionListener(this);
        cumulativeVariancePlotButton.addActionListener(this);

        ButtonGroup group = new ButtonGroup();
        group.add(eigenValueTableButton);
        group.add(eigenVectorTableButton);
        group.add(cumulativeVariancePlotButton);
        eigenValueTableButton.setSelected(true);

        JPanel buttons = new JPanel();
        buttons.setLayout(new GridLayout(1, 3));
        buttons.add(eigenValueTableButton);
        buttons.add(eigenVectorTableButton);
        buttons.add(cumulativeVariancePlotButton);

        JPanel toppanel = new JPanel();
        toppanel.setLayout(new BorderLayout());
        toppanel.add(buttons, BorderLayout.WEST);
        visualizationPanel.add(toppanel, BorderLayout.NORTH);

        visualizationComponent = new Component[3];
        visualizationComponent[0] = this.getEigenvalueTable();
        visualizationComponent[1] = this.getEigenvectorTable();
        visualizationComponent[2] = this.getVariancePlot();
        visualizationPanel.add(visualizationComponent[0], BorderLayout.CENTER);
        this.lastSelectedIndex = 0;

        return visualizationPanel;
    }

    public void actionPerformed(ActionEvent arg0) {
        visualizationPanel.remove(visualizationComponent[lastSelectedIndex]);
        if (eigenValueTableButton.isSelected()) {
            visualizationPanel.add(visualizationComponent[0], BorderLayout.CENTER);
            lastSelectedIndex = 0;
        } else if (eigenVectorTableButton.isSelected()) {
            visualizationPanel.add(visualizationComponent[1], BorderLayout.CENTER);
            lastSelectedIndex = 1;
        } else if (cumulativeVariancePlotButton.isSelected()) {
            visualizationPanel.add(visualizationComponent[2], BorderLayout.CENTER);
            lastSelectedIndex = 2;
        }
        visualizationPanel.repaint();
    }
}
