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
package com.rapidminer.operator.visualization;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTable;

import com.rapidminer.datatable.DataTable;
import com.rapidminer.datatable.DataTableCorrelationMatrixAdapter;
import com.rapidminer.datatable.DataTablePairwiseCorrelationAdapter;
import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.gui.plotter.PlotterPanel;
import com.rapidminer.gui.viewer.DataTableViewer;
import com.rapidminer.gui.viewer.DataTableViewerTable;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.ResultObjectAdapter;
import com.rapidminer.tools.Tools;


/**
 * A simple correlation matrix for a set of value columns. A special constructor
 * for the attributes of an example set is provided.
 * 
 * @author Ingo Mierswa
 * @version $Id: CorrelationMatrix.java,v 2.6 2006/03/21 15:35:39 ingomierswa
 *          Exp $
 */
public class CorrelationMatrix extends ResultObjectAdapter {

	private static final long serialVersionUID = -5498982791125720765L;

	private static final int MAX_NUMBER_OF_RESULT_STRING_ATTRIBUTES = 20;
	
	private double[][] matrix;

	private String[] columnNames;

	private NumberFormat formatter;

	public CorrelationMatrix(String[] columnNames) {
		formatter = NumberFormat.getInstance();
		formatter.setMaximumFractionDigits(3);
		formatter.setMinimumFractionDigits(3);
		this.columnNames = columnNames;
		matrix = new double[columnNames.length][columnNames.length];
	}

	public CorrelationMatrix(ExampleSet exampleSet) {
		this(getColumnNames(exampleSet));
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
		matrix[i][j] = value;
		matrix[j][i] = value;
	}

	public double getValue(int i, int j) {
		return matrix[i][j];
	}

	public int getNumberOfColumns() {
		return this.columnNames.length;
	}
	
	public String getName() {
		return "Correlation Matrix";
	}

	public String toString() {
		StringBuffer result = new StringBuffer("correlation matrix:" + Tools.getLineSeparator());
		for (int i = 0; i < columnNames.length; i++) {
			if (i < MAX_NUMBER_OF_RESULT_STRING_ATTRIBUTES) {
				result.append("\t" + columnNames[i]);
			} else {
				result.append("...");
				break;
			}
		}

		for (int i = 0; i < matrix.length; i++) {
			if (i < MAX_NUMBER_OF_RESULT_STRING_ATTRIBUTES) {
				result.append(Tools.getLineSeparator() + columnNames[i]);
				for (int j = 0; j < matrix[i].length; j++) {
					if (j < MAX_NUMBER_OF_RESULT_STRING_ATTRIBUTES) {
						result.append("\t" + formatter.format(matrix[i][j]));
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

	private DataTable createMatrixDataTable() {
		return new DataTableCorrelationMatrixAdapter(this, this.columnNames);
	}

    private DataTable createPairwiseCorrelationDataTable() {
        return new DataTablePairwiseCorrelationAdapter(this, this.columnNames);
    }
    
    public String getExtension() { return "cor"; }
    
    public String getFileDescription() { return "correlation matrix"; }
    
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

        // pairwise correlation viewer
        DataTable pairwiseDataTable = createPairwiseCorrelationDataTable();
        final DataTableViewer pairwiseTableViewer = new DataTableViewer(pairwiseDataTable, false);
        pairwiseTableViewer.getTable().setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);

        // pairwise correlation plotter component
        final PlotterPanel plotterComponent = new PlotterPanel(pairwiseDataTable);

        // toggle radio button for views
        final JRadioButton matrixButton = new JRadioButton("Matrix View", true);
        matrixButton.setToolTipText("Changes to a table showing information about all correlations.");
        matrixButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (matrixButton.isSelected()) {
                    mainPanel.remove(1);
                    mainPanel.add(matrixTableViewer, BorderLayout.CENTER);
                    mainPanel.repaint();
                }
            }
        });

        final JRadioButton pairwiseButton = new JRadioButton("Pairwise Correlations", true);
        pairwiseButton.setToolTipText("Changes to a table showing the correlations for all pairs.");
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
        plotButton.setToolTipText("Changes to a plot view of the correlation data.");
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
}
