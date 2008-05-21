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

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;

import com.rapidminer.BreakpointListener;
import com.rapidminer.ProcessListener;
import com.rapidminer.operator.ProcessRootOperator;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.Operator;


/**
 * The status bar shows the currently applied operator and the time it needed so
 * far. In addition, the number of times the operator was already applied is
 * also displayed. On the right side a clock is shown which shows the system
 * time. On the left side a colored bullet shows the current running state similar
 * to a traffic light. Please note that the clock thread must be manuall started by
 * invoking {@link #startClockThread()} after construction.
 * 
 * @author Ingo Mierswa, Simon Fischer
 * @version $Id: StatusBar.java,v 1.5 2008/05/09 19:22:59 ingomierswa Exp $
 */
public class StatusBar extends JPanel implements BreakpointListener, ProcessListener {

	private static final String INACTIVE_ICON_NAME = "24/bullet_ball_glass_grey.png";
	private static final String RUNNING_ICON_NAME  = "24/bullet_ball_glass_green.png";
	private static final String STOPPED_ICON_NAME  = "24/bullet_ball_glass_red.png";
	
	private static Icon inactiveIcon = null;
	private static Icon runningIcon  = null;
	private static Icon stoppedIcon  = null;
	
	static {
		inactiveIcon = SwingTools.createIcon(INACTIVE_ICON_NAME);
		runningIcon = SwingTools.createIcon(RUNNING_ICON_NAME);
		stoppedIcon = SwingTools.createIcon(STOPPED_ICON_NAME);
	}
	
	private static final long serialVersionUID = 1189363377612273467L;

	private JLabel clock = createLabel(getTime(), true);

	private JLabel operator = createLabel("                         ", false);

	private transient Operator currentOperator = null;

	private JLabel trafficLightLabel = new JLabel();
	
	private int breakpoint = -1;

	private String specialText = null;

	public StatusBar() {
		GridBagLayout layout = new GridBagLayout();
		setLayout(layout);
		GridBagConstraints constraints = new GridBagConstraints();
		
		constraints.fill = GridBagConstraints.HORIZONTAL;
		
		trafficLightLabel.setIcon(inactiveIcon);
		trafficLightLabel.setToolTipText("Indicates the current running state.");
		constraints.weightx = 0;
		layout.setConstraints(trafficLightLabel, constraints);
		add(trafficLightLabel);

		constraints.weightx = 1;
		constraints.gridwidth = GridBagConstraints.RELATIVE;
		layout.setConstraints(operator, constraints);
		add(operator);

		clock.setToolTipText("The current system time.");
		constraints.weightx = 0;
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		layout.setConstraints(clock, constraints);
		add(clock);
	}

	public void startClockThread() {
		new Thread() {
			public void run() {
				setPriority(MIN_PRIORITY);
				while (true) {
					try {
						clock.setText(getTime());
						if (specialText != null) {
							setText(specialText);
						} else if (currentOperator != null) {
							long execTime = System.currentTimeMillis() - currentOperator.getStartTime();
							if (execTime > 1000) {
								if (breakpoint < 0) {
									setText("[" + currentOperator.getApplyCount() + "] " + currentOperator.getName() + "  " + (execTime / 1000) + " s");
								}
							}
						} else {
							setText("");
						}
						sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
						return;
					}
				}
			}
		}.start();
	}
	
	private static Border createBorder() {
		return new Border() {

			public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
				Color highlight = c.getBackground().brighter().brighter();
				Color shadow = c.getBackground().darker().darker();
				Color oldColor = g.getColor();
				g.translate(x, y);
				g.setColor(shadow);
				g.drawLine(3, 0, 3, h - 2);
				g.drawLine(3, 0, w - 3, 0);
				g.setColor(highlight);
				g.drawLine(3, h - 2, w - 3, h - 2);
				g.drawLine(w - 3, 1, w - 3, h - 2);
				g.translate(-x, -y);
				g.setColor(oldColor);
			}

			public Insets getBorderInsets(Component c) {
				return new Insets(1, 4, 2, 3);
			}

			public boolean isBorderOpaque() {
				return false;
			}
		};
	}

	private static JLabel createLabel(String text, boolean border) {
		JLabel label = new JLabel(text);
		if (border) {
			label.setBorder(createBorder());
		}
		label.setFont(label.getFont().deriveFont(Font.PLAIN));
		return label;
	}

	private static String getTime() {
		return java.text.DateFormat.getTimeInstance().format(new java.util.Date());
	}

	public void breakpointReached(Operator op, IOContainer io, int location) {
		breakpoint = location;
		operator.setText("[" + op.getApplyCount() + "] " + op.getName() + ": breakpoint reached " + BreakpointListener.BREAKPOINT_POS_NAME[breakpoint] + " operator, press resume...");
		trafficLightLabel.setIcon(stoppedIcon);
	}

	public void resume() {
		breakpoint = -1;
		if (currentOperator != null)
			setText();
		else
			operator.setText(" ");
		trafficLightLabel.setIcon(runningIcon);
	}

	public void processStarts() {
		currentOperator = null;
		operator.setText("");
		specialText = null;
		trafficLightLabel.setIcon(runningIcon);
	}
	
	public void processStep(ProcessRootOperator op) {
		currentOperator = op.getProcess().getCurrentOperator();
		setText();
	}

	public void processEnded() {
		operator.setText("");
		currentOperator = null;
		specialText = null;
		trafficLightLabel.setIcon(inactiveIcon);
	}

	public void setSpecialText(String specialText) {
		this.specialText = specialText;
		setText(this.specialText);
	}

	public void clearSpecialText() {
		this.specialText = null;
		setText("");
	}

	private synchronized void setText(String text) {
		operator.setText(text);
	}

	private void setText() {
		setText("[" + currentOperator.getApplyCount() + "] " + currentOperator.getName());
	}
}
