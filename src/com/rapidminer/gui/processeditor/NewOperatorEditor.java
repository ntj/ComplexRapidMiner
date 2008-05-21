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

import java.util.Collections;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JSplitPane;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

import com.rapidminer.gui.tools.ExtendedJScrollPane;
import com.rapidminer.gui.tools.OperatorList;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.tools.GroupTree;


/**
 * This container contains all available operators in a grouped view (tree). From here the
 * current group can be selected which displays the operators of the group in an operator
 * list on the left side. From here, new operators can be dragged into the operator tree.
 *  
 * @author Ingo Mierswa
 * @version $Id: NewOperatorEditor.java,v 1.4 2008/05/09 19:23:16 ingomierswa Exp $
 */
public class NewOperatorEditor extends JSplitPane implements TreeSelectionListener {
    
	private static final long serialVersionUID = -8910332473638172252L;

	private NewOperatorGroupTree newOperatorGroupTree;
	
	private OperatorList operatorList;
	
	public NewOperatorEditor() {
		super(HORIZONTAL_SPLIT);
		// will cause the tree half to keep fixed size during resizing
		setResizeWeight(0.0);
		setBorder(null);
		
		this.newOperatorGroupTree = new NewOperatorGroupTree(this); 
		this.newOperatorGroupTree.getTree().addTreeSelectionListener(this);
		add(newOperatorGroupTree);
        
        this.operatorList = new OperatorList();
        add(new ExtendedJScrollPane(operatorList));
    }

	public void valueChanged(TreeSelectionEvent e) {
		TreePath path = e.getNewLeadSelectionPath();
		if (path != null) {
			GroupTree tree = (GroupTree)path.getLastPathComponent();
			setOperatorList(tree);
		}
	}
    
    public void setOperatorList(GroupTree selectedTree) {
        Vector<OperatorDescription> descriptions = new Vector<OperatorDescription>();
        if (selectedTree != null) {
            Iterator<OperatorDescription> i = selectedTree.getOperatorDescriptions().iterator();
            while (i.hasNext()) {
                descriptions.add(i.next());
            }
            Collections.sort(descriptions);
        }
        this.operatorList.setOperatorDescriptions(descriptions);        
    }
}
