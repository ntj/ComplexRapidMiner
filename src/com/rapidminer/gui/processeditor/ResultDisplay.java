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
package com.rapidminer.gui.processeditor;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import com.rapidminer.datatable.DataTable;
import com.rapidminer.gui.tools.ExtendedJScrollPane;
import com.rapidminer.gui.tools.ExtendedJTabbedPane;
import com.rapidminer.gui.tools.SwingTools;
import com.rapidminer.gui.viewer.DataTableViewer;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.MissingIOObjectException;
import com.rapidminer.operator.ResultObject;


/**
 * The result display is the view of the RapidMiner GUI which refers to (intermediate)
 * results. It can display all IOObjects which are deliverd, each in a tab which
 * is displayed on the right side. If the process produces some statistics,
 * e.g. performance against generation, these are plotted online.
 * 
 * @author Ingo Mierswa
 * @version $Id: ResultDisplay.java,v 1.9 2008/05/09 19:23:16 ingomierswa Exp $
 */
public class ResultDisplay extends JPanel {

	private static final long serialVersionUID = 1970923271834221630L;

	private static final String OPERATOR_TREE_ICON_NAME = "table.png";
	private static final String DEFAULT_RESULT_ICON_NAME = "presentation_chart.png";
	
	private static Icon dataTableIcon = null;
	private static Icon defaultResultIcon = null;
	
	static {
		dataTableIcon = SwingTools.createIcon("16/" + OPERATOR_TREE_ICON_NAME);
		defaultResultIcon = SwingTools.createIcon("16/" + DEFAULT_RESULT_ICON_NAME);
	}
	
	private List<ResultObject> results = new LinkedList<ResultObject>();

	private JTabbedPane tabs = new ExtendedJTabbedPane();

	private JLabel label = new JLabel("Results");;

	private Collection<DataTable> dataTables = new LinkedList<DataTable>();

	public ResultDisplay() {
		super(new BorderLayout());
		add(tabs, BorderLayout.CENTER);
		add(label, BorderLayout.NORTH);
		setData(null, "Results");
	}

	public void clear() {
		this.tabs.removeAll();
		this.results.clear();
		this.dataTables.clear();
		label.setText("No results produced.");
		repaint();
	}

	public void setDataTables(Collection<DataTable> dataTables) {
		this.dataTables = dataTables;
		addDataTables();
	}

	private void addDataTables() {
		Iterator<DataTable> i = dataTables.iterator();
		while (i.hasNext()) {
			DataTable table = i.next();
			tabs.addTab(table.getName(), dataTableIcon, new DataTableViewer(table, true, DataTableViewer.PLOT_MODE), "The data table '"+table.getName()+"'.");
		}
	}

	public void setData(IOContainer resultContainer, String message) {
		int selectedIndex = tabs.getSelectedIndex();

		for (int i = tabs.getTabCount() - 1; i >= 0; i--) {
			Component c = tabs.getComponentAt(i);
			if (!(c instanceof DataTableViewer)) {
				tabs.removeTabAt(i);
			}
		}

		this.results = convertToList(resultContainer);

		// check for double names
		Set<String> doubleUsedNames = new TreeSet<String>();
		Set<String> usedResultNames = new TreeSet<String>();
		Iterator<ResultObject> r = results.iterator();
		while (r.hasNext()) {
			ResultObject resultObject = r.next();
			if (usedResultNames.contains(resultObject.getName())) {
				doubleUsedNames.add(resultObject.getName());
			}
			usedResultNames.add(resultObject.getName());
		}

		if (this.results.size() > 0) {
			label.setText(null);
			Iterator<ResultObject> i = this.results.iterator();
			while (i.hasNext()) {
				ResultObject result = i.next();

				// result panel
				JPanel resultPanel = new JPanel(new BorderLayout());
				Component visualisationComponent = result.getVisualizationComponent(resultContainer);
				if (visualisationComponent instanceof JLabel)
					visualisationComponent = new ExtendedJScrollPane(visualisationComponent);
				resultPanel.putClientProperty("main.component", visualisationComponent);
				resultPanel.add(visualisationComponent, BorderLayout.CENTER);

				// action buttons
				JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
				Iterator action = result.getActions().iterator();
				while (action.hasNext()) {
					buttonPanel.add(new JButton((Action) action.next()));
				}
				resultPanel.add(buttonPanel, BorderLayout.SOUTH);
				String tabName = result.getName();
				if (doubleUsedNames.contains(result.getName())) {
					tabName = result.getName() + " (" + result.getSource() + ")";
				}
				
				Icon resultIcon = result.getResultIcon();
				if (resultIcon == null) {
					resultIcon = defaultResultIcon;
				}
				tabs.addTab(tabName, resultIcon, resultPanel, "Show the result '" + result.getName() + "'.");
			}
		} else {
			label.setText("No results produced.");
		}

		if (selectedIndex < tabs.getTabCount()) {
			tabs.setSelectedIndex(selectedIndex);
		} else {
			if (tabs.getTabCount() > 0)
				tabs.setSelectedIndex(0);
		}
	}

	public Component getCurrentlyDisplayedComponent() {
		if (tabs.getTabCount() == 0) {
			return tabs;
		} else {
			return tabs.getSelectedComponent();
		}
	}
	
	private static List<ResultObject> convertToList(IOContainer container) {
		List<ResultObject> list = new LinkedList<ResultObject>();
		if (container != null) {
			ResultObject result = null;
			do {
				try {
					result = container.get(ResultObject.class, list.size());
					list.add(result);
				} catch (MissingIOObjectException e) {
					break;
				}
			} while (result != null);
		}
		return list;
	}

	public void showSomething() {
		if (tabs.getSelectedIndex() == -1)
			if (tabs.getTabCount() > 0)
				tabs.setSelectedIndex(0);
	}
}
