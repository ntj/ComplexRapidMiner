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
package com.rapidminer.gui.viewer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.gui.tools.ExtendedJScrollPane;
import com.rapidminer.gui.tools.SwingTools;
import com.rapidminer.tools.Tableable;


/**
 * Can be used to display (parts of) the meta data by means of a JTable.
 * 
 * @author Ingo Mierswa
 * @version $Id: MetaDataViewer.java,v 1.7 2008/05/09 19:23:00 ingomierswa Exp $
 */
public class MetaDataViewer extends JPanel implements Tableable{

	private static final String MENU_ICON_NAME = "24/navigate_close.png";
	
	private static Icon menuIcon = null;
	
	static {
		menuIcon = SwingTools.createIcon(MENU_ICON_NAME);
	}
	
    private static class ToggleShowColumnItem extends JCheckBoxMenuItem implements ActionListener {
    	
        private static final long serialVersionUID = 570766967933245379L;
        
        private int index;
        
        private MetaDataViewerTable metaDataTable;
        
        ToggleShowColumnItem(String name, int index, boolean state, MetaDataViewerTable metaDataTable) {
            super("Show column '" + name + "'", state);
            setToolTipText("Toggles if the column with name '"+name+"' should be displayed");
            addActionListener(this);
            this.index = index;
            this.metaDataTable = metaDataTable;
        }
        
        public void actionPerformed(ActionEvent e) {
            metaDataTable.getMetaDataModel().setShowColumn(index, isSelected());
        }
    }
    
    private static final long serialVersionUID = 5466205420267797125L;

    private JLabel generalInfo = new JLabel();
    
    private MetaDataViewerTable metaDataTable = new MetaDataViewerTable();
    
    public MetaDataViewer(ExampleSet exampleSet, boolean showOptions) {
        super(new BorderLayout());
        JPanel infoPanel = new JPanel();
        GridBagLayout layout = new GridBagLayout();
        infoPanel.setLayout(layout);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(5,5,5,5);
        c.weightx = 1.0;
        c.weighty = 0.0;
        c.gridwidth = GridBagConstraints.RELATIVE;
        layout.setConstraints(generalInfo, c);
        infoPanel.add(generalInfo);
    
        if (showOptions) {
        	// try to use menu icon and use text button if icon is null
        	final JButton optionMenuButton = menuIcon != null ? new JButton("", menuIcon) {
        		private static final long serialVersionUID = 5070388716887885349L;
        		public Dimension getPreferredSize() {
        			Dimension dim = super.getPreferredSize();
        			dim.width = 24;
        			return dim;
        		}
        	} : new JButton("Menu");

        	optionMenuButton.setToolTipText("Shows additional options for this view.");
        	optionMenuButton.addMouseListener(new MouseListener() {
        		public void mouseEntered(MouseEvent e) {}
        		public void mouseExited(MouseEvent e) {}
        		public void mouseClicked(MouseEvent e) {}
        		public void mousePressed(MouseEvent e) {}
        		public void mouseReleased(MouseEvent e) {                
        			JPopupMenu menu = new JPopupMenu();
        			for (int i = 0; i < MetaDataViewerTableModel.COLUMN_NAMES.length; i++) {
        				menu.add(new ToggleShowColumnItem(MetaDataViewerTableModel.COLUMN_NAMES[i], i, metaDataTable.getMetaDataModel().getShowColumn(i), metaDataTable));
        			}
        			menu.show(optionMenuButton, e.getX(), e.getY());
        		}
        	});
        	JPanel optionButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        	optionButtonPanel.add(optionMenuButton);
        	c.weightx = 0.0;
        	c.gridwidth = GridBagConstraints.REMAINDER;
        	layout.setConstraints(optionButtonPanel, c);
        	infoPanel.add(optionButtonPanel);
        }
        
        add(infoPanel, BorderLayout.NORTH);
             
        JScrollPane tableScrollPane = new ExtendedJScrollPane(metaDataTable);
        add(tableScrollPane, BorderLayout.CENTER);
        
        setExampleSet(exampleSet);
    }

    public void setExampleSet(ExampleSet exampleSet) {
    	if (exampleSet != null) {
    		exampleSet.recalculateAllAttributeStatistics();
    		StringBuffer infoText = new StringBuffer("ExampleSet (");
    		int noExamples = exampleSet.size();
    		infoText.append(noExamples);
    		infoText.append(noExamples == 1 ? " example, " : " examples, ");
    		int noSpecial = exampleSet.getAttributes().specialSize();
    		infoText.append(noSpecial);
    		infoText.append(noSpecial == 1 ? " special attribute, " : " special attributes, ");
    		int noRegular = exampleSet.getAttributes().size();
    		infoText.append(noRegular);
    		infoText.append(noRegular == 1 ? " regular attribute)" : " regular attributes)");
    		generalInfo.setText(infoText.toString());
    		metaDataTable.setExampleSet(exampleSet);
    	} else {
    		generalInfo.setText("no examples");
    		metaDataTable.setExampleSet(null);
    	}
    }

	public String getCell(int row, int column) {
		return metaDataTable.getCell(row, column);
	}

	public int getColumnNumber() {
		return metaDataTable.getColumnNumber();
	}

	public int getRowNumber() {
		return metaDataTable.getRowNumber();
	}
}
