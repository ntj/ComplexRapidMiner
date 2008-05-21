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

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.HeadlessException;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * A (modal) progress monitor dialog which is also able to show state texts and
 * also provides an interemediate mode.
 *
 * @author Santhosh Kumar, Ingo Mierswa
 * @version $Id: ProgressDialog.java,v 1.4 2008/05/09 19:22:59 ingomierswa Exp $
 */
public class ProgressDialog extends JDialog implements ChangeListener {
	
	private static final long serialVersionUID = -8792339176006884719L;
	
	private JLabel statusLabel = new JLabel();
	private JProgressBar progressBar;
	private transient ProgressMonitor monitor;

	public ProgressDialog(Frame owner, ProgressMonitor monitor, boolean modal) throws HeadlessException {
		super(owner, "Progress", true);
		init(monitor);
	}

	public ProgressDialog(Dialog owner, ProgressMonitor monitor, boolean modal) throws HeadlessException {
		super(owner);
		init(monitor);
	}

	private void init(ProgressMonitor monitor) {
		this.monitor = monitor;

		progressBar = new JProgressBar(0, monitor.getTotal());
		if (monitor.isIndeterminate()) {
			progressBar.setIndeterminate(true);
		} else {
			progressBar.setValue(monitor.getCurrent());
		}
		statusLabel.setText(monitor.getStatus());

		JPanel contents = (JPanel)getContentPane();
		contents.setBorder(BorderFactory.createEmptyBorder(11, 11, 11, 11));
		contents.add(statusLabel, BorderLayout.NORTH);
		contents.add(progressBar);

		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		monitor.addChangeListener(this);
	}

	public void stateChanged(final ChangeEvent ce) {
		// ensure EDT thread 
		if (!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					stateChanged(ce);
				}
			});
			return;
		}

		if (monitor.getCurrent() != monitor.getTotal()) {
			statusLabel.setText(monitor.getStatus());
			if (!monitor.isIndeterminate())
				progressBar.setValue(monitor.getCurrent());
		} else {
			dispose();
		}
	}
}
