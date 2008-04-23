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
package com.rapidminer.gui.processeditor;

import java.awt.BorderLayout;
import java.util.Properties;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.rapidminer.gui.MainFrame;
import com.rapidminer.gui.RapidMinerGUI;
import com.rapidminer.gui.operatortree.OperatorTree;
import com.rapidminer.gui.properties.OperatorPropertyTable;
import com.rapidminer.gui.properties.PropertyTable;
import com.rapidminer.gui.properties.SettingsChangeListener;
import com.rapidminer.gui.tools.ExtendedJScrollPane;
import com.rapidminer.operator.Operator;
import com.rapidminer.tools.Tools;


/**
 * Displays the {@link OperatorTree} and a {@link PropertyTable} in a SplitPane.
 * This tree view is the main process editor of the RapidMiner GUI. The left side
 * shows the current operator tree. On the right side the parameters for the
 * currently selected operators are shown.
 * 
 * @author Ingo Mierswa
 * @version $Id: MainProcessEditor.java,v 1.4 2007/06/20 22:13:13 ingomierswa Exp $
 */
public class MainProcessEditor extends JSplitPane implements ProcessEditor, ChangeListener, SettingsChangeListener {
    
	private static final long serialVersionUID = -2503689886544116347L;
	
	public static final int PARAMETERS   = 0;
	public static final int XML          = 1;
	public static final int DESCRIPTION  = 2;
	public static final int NEW_OPERATOR = 3;
	
	private OperatorTree operatorTree;

    private JTabbedPane operatorTreeTabs = new JTabbedPane();
    
	private JTabbedPane editorTabs = new JTabbedPane();

	private OperatorPropertyTable propertyTable;

	private XMLEditor xmlEditor;
	
	private DescriptionEditor descriptionEditor;
	
	private NewOperatorEditor newOperatorEditor;
	
	private JLabel propertyMessageLabel = new JLabel();
	
	private int lastIndex = 0;
	
	private MainFrame mainFrame;
    
	
	public MainProcessEditor(MainFrame mainFrame) {
		super(HORIZONTAL_SPLIT);
		// will cause the tree half to keep fixed size during resizing
        setResizeWeight(0.0);
		setBorder(null);
        
		this.mainFrame = mainFrame;
		this.operatorTree = new OperatorTree(mainFrame);
		this.operatorTree.setBorder(null);
		this.propertyTable = new OperatorPropertyTable(mainFrame, this.propertyMessageLabel);
		this.xmlEditor = new XMLEditor(mainFrame);
		this.descriptionEditor = new DescriptionEditor();
		this.newOperatorEditor = new NewOperatorEditor();
		
		JScrollPane treeScrollPane = new ExtendedJScrollPane(operatorTree);
		treeScrollPane.setBorder(null);
		operatorTreeTabs.add(treeScrollPane, "Operator Tree");
		add(operatorTreeTabs);
        
		// create tabs
		int counter = 0;
		JPanel propertyPanel = new JPanel(new BorderLayout());
        this.propertyTable.setOpaque(false);
		propertyPanel.add(this.propertyTable, BorderLayout.CENTER);
		propertyPanel.add(this.propertyMessageLabel, BorderLayout.SOUTH);
		JScrollPane propertyPane = new ExtendedJScrollPane(propertyPanel);
		editorTabs.add(propertyPane, "Parameters");
		editorTabs.setToolTipTextAt(counter++, "Shows the parameters of the currently selected operator.");
        
		editorTabs.add(this.xmlEditor, "XML");
		editorTabs.setToolTipTextAt(counter++, "Shows the XML definition of the current process setup.");
		editorTabs.add(this.descriptionEditor, "Comment");
		editorTabs.setToolTipTextAt(counter++, "Shows a comment editor for the currently selected operator.");
		editorTabs.add(newOperatorEditor, "New Operator");
		editorTabs.setToolTipTextAt(counter++, "Shows a grouped view of all available operators for dragging them into the operator tree.");
        add(editorTabs);
		
		// important that this method is invoked as last
		editorTabs.addChangeListener(this);
	}
	    
	/** Currently, the only process editor beside the operator tree is the XML editor. Therefore,
	 *  this editor is returnes if the index matches XML and null is returned otherwise. */
	private ProcessEditor getProcessEditor(int index) {
		switch (index) {
		case XML:
			return xmlEditor;
		case DESCRIPTION:
			return descriptionEditor;
		default:
			return null;	
		}
	}
	
	public void setGroupSelectionDivider(int pos) {
		this.newOperatorEditor.setDividerLocation(pos);
	}
	
	public int getGroupSelectionDivider() {
		return this.newOperatorEditor.getDividerLocation();
	}
	
	public void processChanged(Operator operator) {
		this.operatorTree.setOperator(operator);
		this.propertyTable.setOperator(null);
		this.xmlEditor.processChanged(operator);
		this.descriptionEditor.setCurrentOperator(null);
	}
	
	public void setCurrentOperator(Operator current) {
		this.propertyTable.setOperator(current);
		this.xmlEditor.setCurrentOperator(current);
		this.descriptionEditor.setCurrentOperator(current);
	}
	
	public void validateProcess() {}
	

	public OperatorTree getOperatorTree() {
		return this.operatorTree;
	}

	public OperatorPropertyTable getPropertyTable() {
		return this.propertyTable;
	}

	public XMLEditor getXMLEditor() {
		return this.xmlEditor;
	}
	
	public boolean isXMLViewActive() {
		return (editorTabs.getSelectedIndex() == XML);
	}

	public boolean isDescriptionViewActive() {
		return (editorTabs.getSelectedIndex() == DESCRIPTION);
	}
	
	public void changeToXMLEditor() {
		this.editorTabs.setSelectedIndex(XML);
	}

    public void changeFromNewOperator2ParameterEditor() {
        if (this.editorTabs.getSelectedIndex() == NEW_OPERATOR)
            this.editorTabs.setSelectedIndex(PARAMETERS);
    }
    
	public void stateChanged(ChangeEvent e) {
        // important in order to save the last user editing:
		getPropertyTable().stopCurrentEditing();
		
		int currentIndex = editorTabs.getSelectedIndex();
		
		if (lastIndex == currentIndex) {
			return;
		}
		
		try {
			if (currentIndex >= 0) {
				ProcessEditor lastEditor = getProcessEditor(lastIndex);
				if (lastEditor != null) {
					lastEditor.validateProcess();
				}
			}
			ProcessEditor newEditor = getProcessEditor(currentIndex);
			if (newEditor != null) {
				newEditor.processChanged(RapidMinerGUI.getMainFrame().getProcess().getRootOperator());
			} 
			lastIndex = currentIndex;
		} catch (Exception ex) {
			switch (JOptionPane.showConfirmDialog(this, ex.toString() + Tools.getLineSeparator() + "Cancel to ignore changes, Ok to go on editing.", "Error", JOptionPane.OK_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE)) {
				case JOptionPane.OK_OPTION:
					editorTabs.setSelectedIndex(lastIndex);
					break;
				default:
				case JOptionPane.CANCEL_OPTION:
					ProcessEditor lastEditor = getProcessEditor(lastIndex);
					if (lastEditor != null) {
						lastEditor.processChanged(RapidMinerGUI.getMainFrame().getProcess().getRootOperator());
					}
					break;
			}
		}
		this.mainFrame.enableActions();
	}

    public void settingsChanged(Properties properties) {}
}
