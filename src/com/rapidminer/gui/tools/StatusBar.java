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
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;

import com.rapidminer.BreakpointListener;
import com.rapidminer.ProcessListener;
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
 * @version $Id: StatusBar.java,v 1.9 2008/07/29 09:29:25 ingomierswa Exp $
 */
public class StatusBar extends JPanel implements BreakpointListener, ProcessListener {

	private static class OperatorEntry {
		
		private Collection<OperatorEntry> children = new LinkedList<OperatorEntry>();
		private Operator operator;

		public OperatorEntry(Operator operator) {
			this.operator = operator;
		}

		public void addOperator(Operator operator) {
			synchronized (children) {
				if (this.operator == operator.getParent())
					children.add(new OperatorEntry(operator));
				else {
					for (OperatorEntry childEntry : children)
						childEntry.addOperator(operator);
				}
			}
		}

		public void removeOperator(Operator operator) {
			synchronized (children) {
				Iterator<OperatorEntry> iterator = children.iterator();
				while (iterator.hasNext()) {
					OperatorEntry childEntry = iterator.next();
					if (childEntry.getOperator() == operator)
						iterator.remove();
					else
						childEntry.removeOperator(operator);
				}
			}
		}

		public String toString(OperatorEntry entry, long time) {
			synchronized (children) {
				StringBuffer buffer = new StringBuffer();
				Operator currentOperator = entry.getOperator();
				buffer.append("[" + currentOperator.getApplyCount() + "] "
						+ currentOperator.getName() + "  "
						+ ((time - currentOperator.getStartTime()) / 1000)
						+ " s");
				Iterator<OperatorEntry> iterator = children.iterator();
				if (iterator.hasNext())
					buffer.append("  >  ");
				while (iterator.hasNext()) {
					OperatorEntry childEntry = iterator.next();
					if (children.size() > 1)
						buffer.append("  ( ");
					buffer.append(childEntry.toString(childEntry, time));
					if (children.size() > 1)
						buffer.append(" )  ");
					if (iterator.hasNext())
						buffer.append("  |  ");
				}
				return buffer.toString();
			}
		}

		public Operator getOperator() {
			return operator;
		}
	}
	
	
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

	private JLabel clockLabel = createLabel(getTime(), true);

	private JLabel operatorLabel = createLabel("                         ", false);

	//private transient Collection<Operator> currentOperators = new LinkedList<Operator>();
	private OperatorEntry rootOperator = null;
	
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
		layout.setConstraints(operatorLabel, constraints);
		add(operatorLabel);

		clockLabel.setToolTipText("The current system time.");
		constraints.weightx = 0;
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		layout.setConstraints(clockLabel, constraints);
		add(clockLabel);
	}

	public void startClockThread() {
		new Thread("StatusBar-Thread") {
			public void run() {
				setPriority(MIN_PRIORITY);
				while (true) {
					try {
						clockLabel.setText(getTime());
						if (specialText != null) {
							setText(specialText);
						} else {
							setText();
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
		operatorLabel.setText("[" + op.getApplyCount() + "] " + op.getName() + ": breakpoint reached " + BreakpointListener.BREAKPOINT_POS_NAME[breakpoint] + " operator, press resume...");
		trafficLightLabel.setIcon(stoppedIcon);
	}

	public void resume() {
		breakpoint = -1;
		if (rootOperator != null)
			setText();
		else
			operatorLabel.setText(" ");
		trafficLightLabel.setIcon(runningIcon);
	}

	public void processStarts() {
		rootOperator = null;
		operatorLabel.setText("");
		specialText = null;
		trafficLightLabel.setIcon(runningIcon);
	}
	
	public void processStartedOperator(Operator op) {
		if (rootOperator == null)
			rootOperator = new OperatorEntry(op);
		else
			rootOperator.addOperator(op);
		//setStartText(op);
	}
	
	public void processFinishedOperator(Operator op) {
		if (rootOperator != null)
			rootOperator.removeOperator(op);
		//setEndText(op);
	}

	public void processEnded() {
		operatorLabel.setText("");
		rootOperator = null;
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
		operatorLabel.setText(text);
	}

	private void setText() {
		if (rootOperator != null)
			operatorLabel.setText(rootOperator.toString(rootOperator, System.currentTimeMillis()));
		else
			operatorLabel.setText("");
	}
	
	/*
	private void setStartText(Operator op) {
		setText("started: [" + op.getApplyCount() + "] " + op.getName());
	}
	
	private void setEndText(Operator op) {
		setText("ended: [" + op.getApplyCount() + "] " + op.getName());
	}
	*/
}
