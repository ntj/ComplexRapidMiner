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
package com.rapidminer.gui.tools;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Some utils for the creation of a modal progress monitor dialog.
 *
 * @author Santhosh Kumar, Ingo Mierswa
 * @version $Id: ProgressUtils.java,v 1.3 2008/05/09 19:22:59 ingomierswa Exp $
 */
public class ProgressUtils {
	
	static class MonitorListener implements ChangeListener, ActionListener {
		
		private ProgressMonitor monitor;
		private Window owner;
		private Timer timer;
		private boolean modal;
		
		public MonitorListener(Window owner, ProgressMonitor monitor, boolean modal) {
			this.owner = owner;
			this.monitor = monitor;
			this.modal = modal;
		}

		public void stateChanged(ChangeEvent ce) {
			ProgressMonitor monitor = (ProgressMonitor) ce.getSource();
			if (monitor.getCurrent() != monitor.getTotal()) {
				if (timer == null) {
					timer = new Timer(monitor.getWaitingTime(), this);
					timer.setRepeats(false);
					timer.start();
				}
			} else {
				if (timer != null && timer.isRunning())
					timer.stop();
				monitor.removeChangeListener(this);
			}
		}

		public void actionPerformed(ActionEvent e) {
			monitor.removeChangeListener(this);
			ProgressDialog dlg = owner instanceof Frame ? new ProgressDialog((Frame) owner, monitor, modal) : new ProgressDialog((Dialog) owner, monitor, modal);
			dlg.pack();
			dlg.setLocationRelativeTo(null);
			dlg.setVisible(true);
		}
	}

	/** Create a new (modal) progress monitor dialog. Please note the the value for total (the maximum
	 *  number of possible steps) is greater then 0 even for indeterminate progresses. The value
	 *  of waitingTime is used before the dialog is actually created and shown. */
	public static ProgressMonitor createModalProgressMonitor(Component owner, int total, boolean indeterminate, int waitingTimeBeforeDialogAppears, boolean modal) {
		ProgressMonitor monitor = new ProgressMonitor(total, indeterminate, waitingTimeBeforeDialogAppears);
		Window window = owner instanceof Window ? (Window) owner : SwingUtilities.getWindowAncestor(owner);
		monitor.addChangeListener(new MonitorListener(window, monitor, modal));
		return monitor;
	}
}
