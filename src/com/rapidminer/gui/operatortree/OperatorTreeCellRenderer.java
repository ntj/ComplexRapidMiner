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
package com.rapidminer.gui.operatortree;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Polygon;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.tree.DefaultTreeCellRenderer;

import com.rapidminer.BreakpointListener;
import com.rapidminer.gui.tools.SwingTools;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.tools.Tools;


/**
 * A renderer for operator tree cells that displays the operator's icon, name,
 * class, breakpoints, droplines and error hints.
 * 
 * @author Ingo Mierswa, Helge Homburg
 * @version $Id: OperatorTreeCellRenderer.java,v 2.17 2006/03/21 15:35:40
 *          ingomierswa Exp $
 */
public class OperatorTreeCellRenderer extends DefaultTreeCellRenderer {

	/** The panel which will be used for the actual rendering. */
	private static class OperatorPanel extends JPanel {

		private static final String BREAKPOINT_BEFORE = "24/breakpoint_up.png";
		
		private static final String BREAKPOINT_AFTER = "24/breakpoint_down.png";
		
		private static final String BREAKPOINT_WITHIN = "24/breakpoint.png";
		
		private static final String BREAKPOINTS = "24/breakpoints.png";
		
		private static final String WARNINGS = "24/warning.png";
		
		
		private static final long serialVersionUID = -7680223153786362865L;

		private static final Color SELECTED_COLOR = UIManager.getColor("Tree.selectionBackground");

		private static final Color BORDER_SELECTED_COLOR = UIManager.getColor("Tree.selectionBorderColor");

		private static final Color TEXT_SELECTED_COLOR = UIManager.getColor("Tree.selectionForeground");

		private static final Color TEXT_NON_SELECTED_COLOR = UIManager.getColor("Tree.textForeground");
		
		private static Icon breakpointBeforeIcon = null;
		
		private static Icon breakpointAfterIcon = null;

		private static Icon breakpointWithinIcon = null;
		
		private static Icon breakpointsIcon = null;

		private static Icon warningsIcon = null;
		
		static {
			// init breakpoint icons
			breakpointBeforeIcon = SwingTools.createIcon(BREAKPOINT_BEFORE);
			breakpointAfterIcon  = SwingTools.createIcon(BREAKPOINT_AFTER);
			breakpointWithinIcon = SwingTools.createIcon(BREAKPOINT_WITHIN);
			breakpointsIcon = SwingTools.createIcon(BREAKPOINTS);
			
			// init warnings icon
			warningsIcon = SwingTools.createIcon(WARNINGS);
		}
		
		private JLabel iconLabel = new JLabel("");

		private JLabel nameLabel = new JLabel("");

		private JLabel classLabel = new JLabel("");

		private JLabel breakpoint = new JLabel("");

		private JLabel error = new JLabel("");

		private boolean isSelected = false;

		private boolean hasFocus = false;

		private int dndMarker;

		private int[] downArrowXPoints = { 
			4, 4, 6, 3, 0, 2, 2
		};

		private int[] downArrowYPoints = { 
		    0, 4, 4, 7, 4, 4, 0
		};

		private int[] upArrowXPoints = { 
			3, 6, 4, 4, 2, 2, 0 
		};
		
		private int[] upArrowYPoints = { 
			0, 3, 3, 7, 7, 3, 3
		};
		
		private Polygon upArrow = new Polygon(upArrowXPoints, upArrowYPoints, 7);

		private Polygon downArrow = new Polygon(downArrowXPoints, downArrowYPoints, 7);

		public OperatorPanel() {
			setBackground(new java.awt.Color(0, 0, 0, 0));
			setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

			// layout
			GridBagLayout layout = new GridBagLayout();
			setLayout(layout);
			GridBagConstraints c = new GridBagConstraints();
			c.fill = GridBagConstraints.BOTH;
			c.weightx = 0;

			layout.setConstraints(iconLabel, c);
			add(iconLabel);

			// name panel
			JPanel namePanel = new JPanel();
			namePanel.setBackground(new java.awt.Color(0, 0, 0, 0));
			GridBagLayout nameLayout = new GridBagLayout();
			GridBagConstraints nameC = new GridBagConstraints();
			nameC.fill = GridBagConstraints.BOTH;
			nameC.insets = new Insets(1, 1, 1, 1);
			namePanel.setLayout(nameLayout);

			nameLabel.setHorizontalAlignment(SwingConstants.LEFT);
			nameLabel.setFont(getFont().deriveFont(Font.PLAIN, 12));
			nameLabel.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));
			nameC.gridwidth = GridBagConstraints.REMAINDER;
			nameLayout.setConstraints(nameLabel, nameC);
			namePanel.add(nameLabel);

			classLabel.setHorizontalAlignment(SwingConstants.LEFT);
			classLabel.setFont(getFont().deriveFont(Font.PLAIN, 10));
			classLabel.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));
			nameLayout.setConstraints(classLabel, nameC);
			namePanel.add(classLabel);

			c.weightx = 1;
			layout.setConstraints(namePanel, c);
			add(namePanel);

			c.gridwidth = GridBagConstraints.RELATIVE;
			c.weightx = 0;

			layout.setConstraints(breakpoint, c);
			add(breakpoint);

			c.gridwidth = GridBagConstraints.REMAINDER;

			layout.setConstraints(error, c);
			add(error);

		}

		public void updateOperator(JTree tree, Operator operator, boolean selected, boolean focus) {
			this.isSelected = selected;
			this.hasFocus = focus;

			if (selected) {
				nameLabel.setForeground(TEXT_SELECTED_COLOR);
				classLabel.setForeground(TEXT_SELECTED_COLOR);
			} else {
				nameLabel.setForeground(TEXT_NON_SELECTED_COLOR);
				classLabel.setForeground(TEXT_NON_SELECTED_COLOR);
			}

			dndMarker = ((OperatorTree) tree).getAssociatedDnDSupport().getOperatorMarker(operator.getName());
			OperatorDescription descr = operator.getOperatorDescription();
			Icon icon = descr.getIcon();
			if (icon != null)
				iconLabel.setIcon(icon);
			else
				iconLabel.setIcon(null);
			iconLabel.setEnabled(operator.isEnabled());

			nameLabel.setText(operator.getName());
			nameLabel.setEnabled(operator.isEnabled());
			classLabel.setText(descr.getName());
			classLabel.setEnabled(operator.isEnabled());

			// ICONS
			// breakpoints
			if (operator.hasBreakpoint(BreakpointListener.BREAKPOINT_BEFORE)) {
				breakpoint.setIcon(breakpointBeforeIcon);
			} else if (operator.hasBreakpoint(BreakpointListener.BREAKPOINT_AFTER)) {
				breakpoint.setIcon(breakpointAfterIcon);
			} else if (operator.hasBreakpoint(BreakpointListener.BREAKPOINT_WITHIN)) {
				breakpoint.setIcon(breakpointWithinIcon);
			} else {
				breakpoint.setIcon(null);
			}

			if (((operator.hasBreakpoint(BreakpointListener.BREAKPOINT_BEFORE)) &&
				 (operator.hasBreakpoint(BreakpointListener.BREAKPOINT_AFTER))) || 
				((operator.hasBreakpoint(BreakpointListener.BREAKPOINT_BEFORE)) &&
				 (operator.hasBreakpoint(BreakpointListener.BREAKPOINT_WITHIN))) ||
				((operator.hasBreakpoint(BreakpointListener.BREAKPOINT_WITHIN)) &&
				 (operator.hasBreakpoint(BreakpointListener.BREAKPOINT_AFTER)))) {
				breakpoint.setIcon(breakpointsIcon);				
			}
			breakpoint.setEnabled(operator.isEnabled());

			// errors
			List errors = operator.getErrorList();
			if (errors.size() > 0) {
				error.setIcon(warningsIcon);
				setToolTipText(SwingTools.transformToolTipText("<b>Error: </b>" + (String) errors.get(0)));
			} else {
				error.setIcon(null);
				
				String descriptionText = descr.getLongDescriptionHTML();
				if (descriptionText == null) {
					descriptionText = descr.getShortDescription();
				}
				
				StringBuffer toolTipText = new StringBuffer("<b>Description: </b>" + descriptionText);
				if (operator != null) {
		        	toolTipText.append(Tools.getLineSeparator() + "<b>Input:</b> " + SwingTools.getStringFromClassArray(operator.getInputClasses()));
		        	toolTipText.append(Tools.getLineSeparator() + "<b>Output:</b> " + SwingTools.getStringFromClassArray(operator.getOutputClasses()));
		        }
				setToolTipText(SwingTools.transformToolTipText(toolTipText.toString()));
			}
			error.setEnabled(operator.isEnabled());

			setEnabled(operator.isEnabled());
		}

		private void paintUpperDropline(Graphics graphics) {
			Graphics g = graphics.create();
			g.setColor(SwingTools.LIGHT_BLUE);
			g.fillRect(0, 0, getWidth() - 1, 2);
			g.setColor(SwingTools.DARK_BLUE);
			g.drawRect(0, 0, getWidth() - 1, 2);
			
			g.translate(1, 3);
			g.setColor(SwingTools.LIGHT_BLUE);
			g.fillPolygon(upArrow);
			g.setColor(SwingTools.DARK_BLUE);
			g.drawPolygon(upArrow);
			
			g.translate(getWidth() - 10, 0);			
			g.setColor(SwingTools.LIGHT_BLUE);
			g.fillPolygon(upArrow);
			g.setColor(SwingTools.DARK_BLUE);
			g.drawPolygon(upArrow);
			
			g.dispose();
		}

		private void paintLowerDropline(Graphics graphics) {
			Graphics g = graphics.create();
			g.setColor(SwingTools.LIGHT_BLUE);
			g.fillRect(0, getHeight() - 3, getWidth() - 1, 2);
			g.setColor(SwingTools.DARK_BLUE);
			g.drawRect(0, getHeight() - 3, getWidth() - 1, 2);
			
			g.translate(1, getHeight() - 11);
			g.setColor(SwingTools.LIGHT_BLUE);
			g.fillPolygon(downArrow);
			g.setColor(SwingTools.DARK_BLUE);
			g.drawPolygon(downArrow);

			g.translate(getWidth() - 10, 0);
			g.setColor(SwingTools.LIGHT_BLUE);
			g.fillPolygon(downArrow);
			g.setColor(SwingTools.DARK_BLUE);
			g.drawPolygon(downArrow);
			
			g.dispose();
		}

		public void paint(Graphics g) {
			if (isSelected) {
				g.setColor(SELECTED_COLOR);
				g.fillRect(0, 0, getWidth(), getHeight());
			}
			
			if (hasFocus) {
				g.setColor(BORDER_SELECTED_COLOR);
				g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
			}
			
			if (dndMarker == DnDSupport.fullMarker) {
				g.setColor(BORDER_SELECTED_COLOR);
				g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);				
			}
			
			if (dndMarker == DnDSupport.upperMarker) {
				paintUpperDropline(g);
			}

			if (dndMarker == DnDSupport.lowerMarker) {
				paintLowerDropline(g);
			}
			
			super.paint(g);
		}
	}

	private static final long serialVersionUID = -8256080174651447518L;

	private OperatorPanel operatorPanel = new OperatorPanel();

	public OperatorTreeCellRenderer() {}

	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
		if (value instanceof Operator) {
			operatorPanel.updateOperator(tree, (Operator) value, selected, hasFocus);
			return operatorPanel;
		} else {
			JLabel label = new JLabel(value.toString());
			label.setEnabled(tree.isEnabled());
			return label;
		}
	}
}
