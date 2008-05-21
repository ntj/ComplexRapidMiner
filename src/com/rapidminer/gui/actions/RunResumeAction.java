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

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.Icon;
import javax.swing.KeyStroke;

import com.rapidminer.Process;
import com.rapidminer.gui.ConditionalAction;
import com.rapidminer.gui.MainFrame;
import com.rapidminer.gui.tools.IconSize;
import com.rapidminer.gui.tools.SwingTools;


/**
 * Start the corresponding action.
 * 
 * @author Ingo Mierswa
 * @version $Id: RunResumeAction.java,v 1.5 2008/05/09 19:22:46 ingomierswa Exp $
 */
public class RunResumeAction extends ConditionalAction {

	private static final long serialVersionUID = -9179608146142997027L;

	private static final String RUN_ICON_NAME    = "media_play.png";
	private static final String RESUME_ICON_NAME = "media_pause.png";
	
	private static final Icon[] RUN_ICONS    = new Icon[IconSize.values().length];
	private static final Icon[] RESUME_ICONS = new Icon[IconSize.values().length];
	
	static {
		int counter = 0;
		for (IconSize size : IconSize.values()) {
			RUN_ICONS[counter++] = SwingTools.createIcon(size.getSize() + "/" + RUN_ICON_NAME);
		}
		
		counter = 0;
		for (IconSize size : IconSize.values()) {
			RESUME_ICONS[counter++] = SwingTools.createIcon(size.getSize() + "/" + RESUME_ICON_NAME);
		}
	}
		
	private IconSize iconSize;
    
    private MainFrame mainFrame;
    
    public RunResumeAction(MainFrame mainFrame, IconSize size) {
        super("Run");
        this.iconSize = size;
        putValue(SHORT_DESCRIPTION, "Run / Resume the current process");
        putValue(MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_R));
        putValue(SMALL_ICON, RUN_ICONS[size.ordinal()]);
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
        setCondition(PROCESS_RUNNING, DISALLOWED);
        this.mainFrame = mainFrame;
    }

    public void actionPerformed(ActionEvent e) {
    	mainFrame.runProcess();
    }
    
    public void updateState() {
        if (mainFrame.getProcessState() == Process.PROCESS_STATE_STOPPED) {
            putValue(SMALL_ICON, RUN_ICONS[iconSize.ordinal()]);
            setCondition(PROCESS_RUNNING, DISALLOWED);
            setCondition(PROCESS_PAUSED, DONT_CARE);
        } else {
            putValue(SMALL_ICON, RESUME_ICONS[iconSize.ordinal()]);
            setCondition(PROCESS_RUNNING, DONT_CARE);
            setCondition(PROCESS_PAUSED, MANDATORY);
        }
    }
}
