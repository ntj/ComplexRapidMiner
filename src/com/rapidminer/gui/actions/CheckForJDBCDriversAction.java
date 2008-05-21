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
package com.rapidminer.gui.actions;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.rapidminer.gui.MainFrame;
import com.rapidminer.gui.tools.IconSize;
import com.rapidminer.gui.tools.JDBCDriverTable;
import com.rapidminer.gui.tools.SwingTools;
import com.rapidminer.tools.jdbc.DatabaseService;
import com.rapidminer.tools.jdbc.DriverInfo;


/**
 * Start the corresponding action.
 * 
 * @author Ingo Mierswa
 * @version $Id: CheckForJDBCDriversAction.java,v 1.5 2008/05/09 19:22:46 ingomierswa Exp $
 */
public class CheckForJDBCDriversAction extends AbstractAction {

    private static final long serialVersionUID = -3497263063489866721L;

	private static final String ICON_NAME = "data_connection.png";
	
	private static final Icon[] ICONS = new Icon[IconSize.values().length];
	
	static {
		int counter = 0;
		for (IconSize size : IconSize.values()) {
			ICONS[counter++] = SwingTools.createIcon(size.getSize() + "/" + ICON_NAME);
		}
	}
		
    private MainFrame mainFrame;
    
    public CheckForJDBCDriversAction(MainFrame mainFrame, IconSize size) {
        super("Show Database Drivers...", ICONS[size.ordinal()]);
        putValue(SHORT_DESCRIPTION, "List all available JDBC database drivers which are currently available to RapidMiner");
        putValue(MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_J));
        this.mainFrame = mainFrame;
    }

    public void actionPerformed(ActionEvent e) {
    	final JDialog dialog = new JDialog(mainFrame, "Available Database Drivers", true);
    	dialog.setLayout(new BorderLayout());
    	
    	JTextArea driverInfoText = new JTextArea("The currently available JDBC drivers are listed below. Please make sure to copy missing drivers into the directory lib/jdbc and restart RapidMiner in order to make additional drivers available.");
    	driverInfoText.setLineWrap(true);
    	driverInfoText.setWrapStyleWord(true);
    	driverInfoText.setBackground(dialog.getBackground());
    	dialog.add(driverInfoText, BorderLayout.NORTH);
    	
        DriverInfo[] drivers = DatabaseService.getAllDriverInfos();
        JDBCDriverTable driverTable = new JDBCDriverTable(drivers);
        dialog.add(new JScrollPane(driverTable), BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dialog.dispose();
			}
        });
        closeButton.setMargin(new Insets(4,4,4,4));
        buttonPanel.add(closeButton);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.pack();
        dialog.setLocationRelativeTo(mainFrame);
        dialog.setVisible(true);
    }
}    
