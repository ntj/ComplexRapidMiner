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
package com.rapidminer.tools.math.matrix;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;
import java.util.Vector;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * This class can be used to visualize matrices.
 * 
 * @author Michael Wurst, Ingo Mierswa
 *
 * @param <Ex>
 * @param <Ey>
 */
public class MatrixVisualizer<Ex, Ey> extends JPanel implements ItemListener {

    private static final long serialVersionUID = 1890397148444056696L;

    private List<Ex> xLabels;

    private List<Ey> yLabels;

    private JComboBox chooseBox1;

    private JComboBox chooseBox2;

    private JLabel label;

    private Matrix<Ex, Ey> matrix;

    public MatrixVisualizer(List<Ex> xLabels, List<Ey> yLabels, Matrix<Ex, Ey> matrix) {
        this.matrix = matrix;
        this.xLabels = xLabels;
        this.yLabels = yLabels;

        JPanel panel = new JPanel(new FlowLayout());

        chooseBox1 = new JComboBox(new Vector<Ex>(xLabels));
        chooseBox2 = new JComboBox(new Vector<Ey>(yLabels));
        label = new JLabel("value: 0.0");

        chooseBox1.addItemListener(this);
        chooseBox2.addItemListener(this);

        panel.add(new JLabel("x: "));
        panel.add(chooseBox1);
        panel.add(new JLabel(" y: "));
        panel.add(chooseBox2);
        panel.add(label);

        this.setLayout(new BorderLayout());
        this.add(panel, BorderLayout.CENTER);
    }

    public void itemStateChanged(ItemEvent e) {
        Ex o1 = xLabels.get(chooseBox1.getSelectedIndex());
        Ey o2 = yLabels.get(chooseBox2.getSelectedIndex());

        label.setText("value: " + matrix.getEntry(o1, o2));
    }
}

