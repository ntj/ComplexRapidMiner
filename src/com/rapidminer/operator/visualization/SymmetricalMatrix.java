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
package com.rapidminer.operator.visualization;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;

import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTable;

import Jama.Matrix;

import com.rapidminer.datatable.DataTable;
import com.rapidminer.datatable.DataTableSymmetricalMatrixAdapter;
import com.rapidminer.datatable.DataTablePairwiseMatrixExtractionAdapter;
import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.gui.plotter.PlotterPanel;
import com.rapidminer.gui.tools.SwingTools;
import com.rapidminer.gui.viewer.DataTableViewer;
import com.rapidminer.gui.viewer.DataTableViewerTable;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.ResultObjectAdapter;
import com.rapidminer.tools.Tools;


/**
 * A simple symmetrical matrix which can be used for correlation or covariance matrices. 
 * A special constructor for the attributes of an example set is provided.
 * 
 * @author Ingo Mierswa
 * @version $Id: SymmetricalMatrix.java,v 1.2 2008/05/09 19:23:14 ingomierswa Exp $
 */
public class SymmetricalMatrix extends ResultObjectAdapter {

	private static final long serialVersionUID = -5498982791125720765L;

	private static final int MAX_NUMBER_OF_RESULT_STRING_ATTRIBUTES = 20;
	
	private static final String RESULT_ICON_NAME = "table.png";
	
	private static Icon resultIcon = null;
	
	static {
		resultIcon = SwingTools.createIcon("16/" + RESULT_ICON_NAME);
	}
	
	private Matrix matrix;

	private String[] columnNames;

	private NumberFormat formatter;

	private String name;
	
	public SymmetricalMatrix(String name, String[] columnNames) {
		this(name, columnNames, new Matrix(columnNames.length, columnNames.length));
	}
	
	public SymmetricalMatrix(String name, String[] columnNames, Matrix matrix) {
		this.name = name;
		formatter = NumberFormat.getInstance();
		formatter.setMaximumFractionDigits(3);
		formatter.setMinimumFractionDigits(3);
		this.columnNames = columnNames;
		this.matrix = matrix;
	}

	public SymmetricalMatrix(String name, ExampleSet exampleSet) {
		this(name, getColumnNames(exampleSet));
	}

	private static String[] getColumnNames(ExampleSet exampleSet) {
		String[] attributeNames = new String[exampleSet.getAttributes().size()];
		int counter = 0;
		for (Attribute attribute : exampleSet.getAttributes()) {
			attributeNames[counter++] = attribute.getName();
		}
		return attributeNames;
	}

	public void setValue(int i, int j, double value) {
		this.matrix.set(i, j, value);
		this.matrix.set(j, i, value);
	}

	public double getValue(int i, int j) {
		return matrix.get(i, j);
	}

	public int getNumberOfColumns() {
		return this.columnNames.length;
	}
	
	public String getName() {
		return name + " Matrix";
	}

	public String toString() {
		StringBuffer result = new StringBuffer(name + " Matrix:" + Tools.getLineSeparator());
		for (int i = 0; i < columnNames.length; i++) {
			if (i < MAX_NUMBER_OF_RESULT_STRING_ATTRIBUTES) {
				result.append("\t" + columnNames[i]);
			} else {
				result.append("...");
				break;
			}
		}

		for (int i = 0; i < matrix.getRowDimension(); i++) {
			if (i < MAX_NUMBER_OF_RESULT_STRING_ATTRIBUTES) {
				result.append(Tools.getLineSeparator() + columnNames[i]);
				for (int j = 0; j < matrix.getColumnDimension(); j++) {
					if (j < MAX_NUMBER_OF_RESULT_STRING_ATTRIBUTES) {
						result.append("\t" + formatter.format(matrix.get(i, j)));
					} else {
						result.append("...");
						break;
					}
				}
			} else {
				result.append(Tools.getLineSeparator() + "...");
				break;
			}
		}
		return result.toString();
	}

	public DataTable createMatrixDataTable() {
		return new DataTableSymmetricalMatrixAdapter(this, this.columnNames);
	}

    public DataTable createPairwiseDataTable() {
        return new DataTablePairwiseMatrixExtractionAdapter(this, this.columnNames, new String[] { "First Attribute", "Second Attribute", name } );
    }
    
    public String getExtension() { return "cor"; }
    
    public String getFileDescription() { return name.toLowerCase() + " matrix"; }
    
	/**
	 * Returns a label that displays the {@link #toResultString()} result
	 * encoded as html.
	 */
	public java.awt.Component getVisualizationComponent(IOContainer container) { 
        final JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        
        // matrix viewer
        DataTable matrixDataTable = createMatrixDataTable();
        final DataTableViewer matrixTableViewer = new DataTableViewer(matrixDataTable, false);
        matrixTableViewer.getTable().setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        matrixTableViewer.getTable().setRendererType(DataTableViewerTable.ABS_SCALED);  

        // pairwise viewer
        DataTable pairwiseDataTable = createPairwiseDataTable();
        final DataTableViewer pairwiseTableViewer = new DataTableViewer(pairwiseDataTable, false);
        pairwiseTableViewer.getTable().setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);

        // pairwise plotter component
        final PlotterPanel plotterComponent = new PlotterPanel(pairwiseDataTable);

        // toggle radio button for views
        final JRadioButton matrixButton = new JRadioButton("Matrix View", true);
        matrixButton.setToolTipText("Changes to a table showing the " + name.toLowerCase() + ".");
        matrixButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (matrixButton.isSelected()) {
                    mainPanel.remove(1);
                    mainPanel.add(matrixTableViewer, BorderLayout.CENTER);
                    mainPanel.repaint();
                }
            }
        });

        final JRadioButton pairwiseButton = new JRadioButton("Pairwise " + name, true);
        pairwiseButton.setToolTipText("Changes to a table showing the " + name.toLowerCase() + " for all pairs.");
        pairwiseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (pairwiseButton.isSelected()) {
                    mainPanel.remove(1);
                    mainPanel.add(pairwiseTableViewer, BorderLayout.CENTER);
                    mainPanel.repaint();
                }
            }
        });
        
        final JRadioButton plotButton = new JRadioButton("Plot View", false);
        plotButton.setToolTipText("Changes to a plot view of the " + name.toLowerCase());
        plotButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (plotButton.isSelected()) {
                    mainPanel.remove(1);
                    mainPanel.add(plotterComponent, BorderLayout.CENTER);
                    mainPanel.repaint();
                }
            }
        });
        
        ButtonGroup group = new ButtonGroup();
        group.add(matrixButton);
        group.add(pairwiseButton);
        group.add(plotButton);
        JPanel togglePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        togglePanel.add(matrixButton);
        togglePanel.add(pairwiseButton);
        togglePanel.add(plotButton);

        mainPanel.add(togglePanel, BorderLayout.NORTH);
        mainPanel.add(matrixTableViewer, BorderLayout.CENTER);
        return mainPanel;
	}
	
	public Icon getResultIcon() {
		return resultIcon;
	}
}
