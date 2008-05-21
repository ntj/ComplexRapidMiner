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
package com.rapidminer.gui.properties;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import com.rapidminer.gui.tools.SwingTools;
import com.rapidminer.tools.ParameterService;


/**
 * The settings dialog for user settings. These are stored in a
 * &quot;rapidminerrc&quot; file in the user directory &quot;.rapidminer&quot; and can
 * overwrite system wide settings. The settings are grouped in
 * {@link SettingsTabs} each of which contains a {@link SettingsPropertyTable}.
 * 
 * @author Ingo Mierswa
 * @version $Id: SettingsDialog.java,v 1.3 2008/05/09 19:22:45 ingomierswa Exp $
 */
public class SettingsDialog extends JDialog {

	private static final long serialVersionUID = 6665295638614289994L;

	private SettingsTabs tabs = new SettingsTabs();

    private List<SettingsChangeListener> listeners = new LinkedList<SettingsChangeListener>();
    
    
	public SettingsDialog(JFrame owner) {
		super(owner, "Settings", true);
		BorderLayout borderLayout = new BorderLayout();
		borderLayout.setHgap(5);
		borderLayout.setVgap(5);
		JPanel panel = new JPanel(borderLayout);

		JTextArea label = new JTextArea("You can edit the file '" + ParameterService.getConfigFile("rapidminerrc") + "' to make system wide settings. This dialog will save all changes to '" + ParameterService.getUserConfigFile("rapidminerrc" + "." + System.getProperty("os.name")) + "'.", 3, 60);
		label.setLineWrap(true);
		label.setWrapStyleWord(true);
		label.setBackground(this.getBackground());
		label.setEditable(false);
		panel.add(label, BorderLayout.NORTH);

		panel.add(tabs, BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JButton ok = new JButton("Apply");
		ok.setToolTipText("Apply settings for this session.");
		ok.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				tabs.applyProperties();
                fireSettingsChanged();
				dispose();
			}
		});
		buttonPanel.add(ok);
		JButton save = new JButton("Save");
		save.setToolTipText("Save settings for this and future sessions.");
		save.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				try {
					tabs.save();
                    fireSettingsChanged();
					dispose();
				} catch (IOException ioe) {
					SwingTools.showSimpleErrorMessage("Cannot save properties.", ioe);
				}
			}
		});
		buttonPanel.add(save);
		JButton cancel = new JButton("Cancel");
		cancel.setToolTipText("Do not change settings.");
		cancel.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		buttonPanel.add(cancel);
		panel.add(buttonPanel, BorderLayout.SOUTH);

		panel.setBorder(javax.swing.BorderFactory.createEmptyBorder(11, 11, 11, 11));
		getContentPane().add(panel);
		pack();
		setLocationRelativeTo(owner);
	}
    
    public void addSettingsChangedListener(SettingsChangeListener listener) {
        listeners.add(listener);
    }

    public void removeSettingsChangedListener(SettingsChangeListener listener) {
        listeners.remove(listener);
    }
    
    protected void fireSettingsChanged() {
        Iterator<SettingsChangeListener> i = listeners.iterator();
        while (i.hasNext()) {
            i.next().settingsChanged(System.getProperties());
        }
    }
}
