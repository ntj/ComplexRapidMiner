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
package com.rapidminer.gui.dialog.boxviewer;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;

import org.freehep.util.export.ExportDialog;

import com.rapidminer.gui.RapidMinerGUI;
import com.rapidminer.gui.dialog.actions.BoxViewerExportAction;
import com.rapidminer.gui.dialog.actions.BoxViewerPrintAction;
import com.rapidminer.gui.tools.ComponentPrinter;
import com.rapidminer.gui.tools.ExtendedJScrollPane;
import com.rapidminer.gui.tools.ExtendedJToolBar;
import com.rapidminer.gui.tools.IconSize;
import com.rapidminer.gui.tools.SwingTools;
import com.rapidminer.operator.Operator;


/**
 * This dialog renderers the process (or any other given operator) in a box
 * style. It also provides a print and export function in a tool bar.
 * 
 * @author Ingo Mierswa
 * @version $Id: BoxViewerDialog.java,v 1.7 2008/05/09 19:23:02 ingomierswa Exp $
 */
public class BoxViewerDialog extends JDialog {

	private static final long serialVersionUID = -1090928037950227038L;
	
	public transient final Action PRINT_ACTION_24 = new BoxViewerPrintAction(this, IconSize.SMALL);
	public transient final Action PRINT_ACTION_32 = new BoxViewerPrintAction(this, IconSize.MIDDLE);

	public transient final Action EXPORT_ACTION_24 = new BoxViewerExportAction(this, IconSize.SMALL);
	public transient final Action EXPORT_ACTION_32 = new BoxViewerExportAction(this, IconSize.MIDDLE);
	
	private ProcessRenderer processRenderer;
	
	private transient PrinterJob printerJob = PrinterJob.getPrinterJob();
	
	public BoxViewerDialog(Operator operator) {
		super(RapidMinerGUI.getMainFrame(), "Box View", false);
		setLayout(new BorderLayout());
		
		// toolbar
		JToolBar toolBar = new ExtendedJToolBar();
		toolBar.add(PRINT_ACTION_32);
		toolBar.add(EXPORT_ACTION_32);
		add(toolBar, BorderLayout.NORTH);
		
		// process renderer
		processRenderer = new ProcessRenderer();
		processRenderer.setOperator(RapidMinerGUI.getMainFrame().getProcess().getRootOperator());
		JScrollPane scrollPane = new ExtendedJScrollPane(processRenderer);
		add(scrollPane, BorderLayout.CENTER);
		
		// button panel
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		JButton closeButton = new JButton("Close");
		closeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}	
		});
		buttonPanel.add(closeButton);
		add(buttonPanel, BorderLayout.SOUTH);
		
		setSize(400,600);
		setLocationRelativeTo(RapidMinerGUI.getMainFrame());
	}
	
	protected Object readResolve() {
		this.printerJob = PrinterJob.getPrinterJob();
		return this;
	}
	
	public void printProcess() {
		printerJob.setPrintable(new ComponentPrinter(processRenderer));
		if (printerJob.printDialog()) {
			try {
				printerJob.print();
			} catch (PrinterException pe) {
				SwingTools.showSimpleErrorMessage("Printer error", pe);
			}
		}
	}
	
	public void exportProcess() {
		ExportDialog exportDialog = new ExportDialog("RapidMiner");
		exportDialog.showExportDialog(RapidMinerGUI.getMainFrame(), "Export", processRenderer, RapidMinerGUI.getMainFrame().getBaseName());
	}
}
