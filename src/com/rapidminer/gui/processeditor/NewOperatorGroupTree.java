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
import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.tree.TreePath;

import com.rapidminer.gui.processeditor.actions.ClearFilterAction;
import com.rapidminer.gui.tools.ExtendedJScrollPane;
import com.rapidminer.gui.tools.ExtendedJToolBar;
import com.rapidminer.tools.GroupTree;
import com.rapidminer.tools.OperatorService;

/**
 * This tree displays all groups and can be used to change the selected operators. 
 * 
 * @author Ingo Mierswa
 * @version $Id: NewOperatorGroupTree.java,v 1.9 2008/05/09 19:23:16 ingomierswa Exp $
 */
public class NewOperatorGroupTree extends JPanel {
    
    private static final long serialVersionUID = 133086849304885475L;

    private static final String DEFAULT_FILTER_TEXT = "[Filter]";
	
    private JTextField filterField = new JTextField(12);
    
    private transient NewOperatorGroupTreeModel model = new NewOperatorGroupTreeModel(OperatorService.getGroups());
    
    private JTree operatorGroupTree = new JTree(model);
    
    private NewOperatorEditor editor;
    

    public NewOperatorGroupTree(NewOperatorEditor editor) {
        this.editor = editor;
        setLayout(new BorderLayout());
        
        operatorGroupTree.setShowsRootHandles(true);
		ToolTipManager.sharedInstance().registerComponent(operatorGroupTree);
        operatorGroupTree.setToggleClickCount(5); 
        operatorGroupTree.setCellRenderer(new NewOperatorGroupTreeRenderer());
        operatorGroupTree.expandRow(0);
        
        add(new ExtendedJScrollPane(operatorGroupTree), BorderLayout.CENTER);
        
        filterField.setToolTipText("Insert a search string in order to filter the new operator tree above.");
        filterField.setForeground(Color.LIGHT_GRAY);   
        filterField.setText(DEFAULT_FILTER_TEXT);
        filterField.addKeyListener(new KeyListener() {
            public void keyPressed(KeyEvent e) {
               filterField.setForeground(Color.BLACK);
            }
            public void keyTyped(KeyEvent e) {
                //updateFilter(e);
            }             
            public void keyReleased(KeyEvent e) {
                updateFilter(e);
            }
        });
        filterField.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
                filterField.selectAll();
            }
        });
        filterField.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent e) {}
            public void focusLost(FocusEvent e) {
                updateFilter(null);
            }
        });
        
        JToolBar clearBar = new ExtendedJToolBar();
        clearBar.add(filterField);
        clearBar.add(new ClearFilterAction(this));
        clearBar.setBorder(null);
        
        add(clearBar, BorderLayout.SOUTH);
	}
    
    public void clearFilter() {
        filterField.setForeground(Color.LIGHT_GRAY);
        filterField.setText(DEFAULT_FILTER_TEXT);
        updateFilter(null);
    }
    
    private void updateFilter(KeyEvent e) {
        String filterText = filterField.getText();
        if ((filterText == null) || (filterText.trim().length() == 0)) {
            if ((e == null) || 
                ((e.getKeyCode() != KeyEvent.VK_BACK_SPACE) && 
                 (e.getKeyCode() != KeyEvent.VK_DELETE) &&
                 (e.getKeyCode() != KeyEvent.VK_SHIFT) &&
                 (e.getKeyCode() != KeyEvent.VK_ALT) &&
                 (e.getKeyCode() != KeyEvent.VK_ALT_GRAPH) &&
                 (e.getKeyCode() != KeyEvent.VK_CONTROL) &&
                 (e.getKeyCode() != KeyEvent.VK_META) &&
                 (!e.isActionKey()))) {
                    filterField.setForeground(Color.LIGHT_GRAY);
                    filterField.setText(DEFAULT_FILTER_TEXT);
            }
        }
        if (DEFAULT_FILTER_TEXT.equals(filterText))
            filterText = null;
        model.applyFilter(filterText);
        GroupTree root = (GroupTree)this.operatorGroupTree.getModel().getRoot();
        TreePath path = new TreePath(root);
        showNodes(root, path);
    }
    
    private void showNodes(GroupTree tree, TreePath path) {
        if (tree.getSubGroups().size() == 0) {
            int row = this.operatorGroupTree.getRowForPath(path);
            this.operatorGroupTree.expandRow(row);
            this.editor.setOperatorList(tree);
        } else if ((tree.getSubGroups().size() == 1) && (tree.getOperatorDescriptions().size() == 0)) {
            int row = this.operatorGroupTree.getRowForPath(path);
            this.operatorGroupTree.expandRow(row);
            GroupTree child = tree.getSubGroup(0);
            path = path.pathByAddingChild(child);
            showNodes(child, path);
        } else {
            int row = this.operatorGroupTree.getRowForPath(path);
            this.operatorGroupTree.expandRow(row);
            this.editor.setOperatorList(null);
        }        
    }
    
    public JTree getTree() {
        return this.operatorGroupTree;
    }
}
