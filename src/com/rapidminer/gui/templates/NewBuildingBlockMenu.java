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
package com.rapidminer.gui.templates;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.StringReader;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.rapidminer.gui.RapidMinerGUI;
import com.rapidminer.gui.tools.SwingTools;
import com.rapidminer.operator.Operator;
import com.rapidminer.tools.BuildingBlockService;
import com.rapidminer.tools.XMLException;


/**
 * This menu contains all building blocks, the predefined and the user defined.
 * 
 * @author Ingo Mierswa
 * @version $Id: NewBuildingBlockMenu.java,v 1.6 2008/05/09 19:22:52 ingomierswa Exp $
 */
public class NewBuildingBlockMenu extends JMenu {

	private static final long serialVersionUID = 316102134905132452L;

	private static final String NEW_BUILDING_BLOCK_ICON_NAME = "24/box_new.png";
	
	private static Icon newBuildingBlockIcon = null;
	
	static {
		// init icon
		newBuildingBlockIcon = SwingTools.createIcon(NEW_BUILDING_BLOCK_ICON_NAME);
	}
	
	public NewBuildingBlockMenu() {
		super("New Building Block");
		setIcon(newBuildingBlockIcon);
	}

    public void addAllMenuItems() {
        setMenuItems(BuildingBlockService.getBuildingBlocks());
    }
    
	public void setMenuItems(Collection<BuildingBlock> buildingBlocks) {
        removeAll();
		Iterator<BuildingBlock> i = buildingBlocks.iterator();
		while (i.hasNext()) {
			final BuildingBlock buildingBlock = i.next();
			JMenuItem item = null;
			final String name = buildingBlock.getName();
			String iconPath = buildingBlock.getIconPath();
			if (iconPath == null) {
			    item = new JMenuItem(name);
			} else {
			    ImageIcon icon = SwingTools.createIcon(iconPath);
			    item = new JMenuItem(name, icon);
			}
			item.setToolTipText(buildingBlock.getDescription());
			item.addActionListener(new ActionListener() {
			    public void actionPerformed(ActionEvent e) {
			        String xmlDescription = buildingBlock.getXML();
			        try {
			            InputSource source = new InputSource(new StringReader(xmlDescription));
			            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(source);
			            Element element = document.getDocumentElement();
                        Operator operator = Operator.createFromXML(element);
			            RapidMinerGUI.getMainFrame().getOperatorTree().insert(operator);
			        } catch (Exception ex) {
			            SwingTools.showSimpleErrorMessage("Cannot instantiate building block '" + name + "'.", ex);
			        }
			    }
			});
			// disable building block which cannot be created, e.g. cause they consist of operators
			// part of a non-loaded plugin
			item.setEnabled(checkBuildingBlock(buildingBlock));
			add(item);
		}
	}
    
    /** Returns true if the building block does not contain errors and can be properly loaded. */
    public static boolean checkBuildingBlock(BuildingBlock buildingBlock) {
        try {
            String xmlDescription = buildingBlock.getXML();
            InputSource source = new InputSource(new StringReader(xmlDescription));
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(source);
            Element element = document.getDocumentElement();
            Operator operator = Operator.createFromXML(element);
            operator.remove();
            return true;
        } catch (IOException ex) {
            return false;
        } catch (SAXException e) {
        	return false;
		} catch (ParserConfigurationException e) {
			return false;
		} catch (XMLException e) {
			return false;
		}
    }
}
