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

import com.rapidminer.tools.IterationArrayList;

/**
 * This class visualizes matrices by two combo boxes that allow to specify 
 * a certain entry in the matrix.
 * 
 * @author Michael Wurst, Ingo Mierswa
 * @version $Id: MatrixComboBoxVisualizer.java,v 1.2 2008/05/09 19:23:23 ingomierswa Exp $
 * 
 */
public class MatrixComboBoxVisualizer<Ex, Ey> extends JPanel implements ItemListener {

    private static final long serialVersionUID = 1890397148444056696L;

    private List<Ex> xLabels;

    private List<Ey> yLabels;

    private JComboBox chooseBox1;

    private JComboBox chooseBox2;

    private JLabel label;

    private Matrix<Ex, Ey> matrix;

    public MatrixComboBoxVisualizer(Matrix<Ex, Ey> matrix) {
        this.matrix = matrix;
        this.xLabels = new IterationArrayList<Ex>(matrix.getXLabels());
        this.yLabels = new IterationArrayList<Ey>(matrix.getYLabels());

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

