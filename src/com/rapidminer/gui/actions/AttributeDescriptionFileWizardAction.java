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
package com.rapidminer.gui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Icon;

import com.rapidminer.Process;
import com.rapidminer.gui.tools.IconSize;
import com.rapidminer.gui.tools.SwingTools;
import com.rapidminer.gui.wizards.ConfigurationListener;
import com.rapidminer.gui.wizards.ExampleSourceConfigurationWizard;
import com.rapidminer.parameter.Parameters;


/**
 * Start the corresponding action.
 * 
 * @author Ingo Mierswa
 * @version $Id: AttributeDescriptionFileWizardAction.java,v 1.2 2007/06/07 17:12:19 ingomierswa Exp $
 */
public class AttributeDescriptionFileWizardAction extends AbstractAction implements ConfigurationListener {

	private static final long serialVersionUID = 5591885109312707090L;

	private static final String ICON_NAME = "magic-wand.png";
	
	private static final Icon[] ICONS = new Icon[IconSize.values().length];
	
	static {
		int counter = 0;
		for (IconSize size : IconSize.values()) {
			ICONS[counter++] = SwingTools.createIcon("icons/" + size.getSize() + "/" + ICON_NAME);
		}
	}
		
	
	public AttributeDescriptionFileWizardAction(IconSize size) {
		super("Attribute Description File Wizard", ICONS[size.ordinal()]);
		putValue(SHORT_DESCRIPTION, "Displays a dialog allowing the definition of attribute description files (.aml) for almost arbitrary data files");
		putValue(MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_A));
	}

	public void actionPerformed(ActionEvent e) {
		ExampleSourceConfigurationWizard wizard = new ExampleSourceConfigurationWizard(this);
		wizard.setVisible(true);
	}

	/** Returns an empty parameters object. */
	public Parameters getParameters() {
		return new Parameters();
	}

	/** Does nothing. */
	public void setParameters(Parameters parameters) {}
	
	/** Returns null. */
	public Process getProcess() {
		return null;
	}
}
