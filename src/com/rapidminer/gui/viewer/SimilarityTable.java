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
package com.rapidminer.gui.viewer;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import com.rapidminer.gui.tools.ExtendedJScrollPane;
import com.rapidminer.gui.tools.ExtendedJTable;
import com.rapidminer.operator.similarity.SimilarityMeasure;

/**
 * The table for the similarity viewer.
 *
 * @author Ingo Mierswa
 * @version $Id: SimilarityTable.java,v 1.1 2007/06/22 14:12:59 ingomierswa Exp $
 */
public class SimilarityTable extends JPanel {

	private static final long serialVersionUID = 8251521865453407142L;

	public SimilarityTable(SimilarityMeasure similarity) {
		GridBagLayout layout = new GridBagLayout();
		setLayout(layout);
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1;
		c.weighty = 1;
		c.gridwidth = GridBagConstraints.REMAINDER;
		
		JTable similarityTable = new ExtendedJTable();
		SimilarityTableModel model = new SimilarityTableModel(similarity);
		similarityTable.setModel(model);
		
		JScrollPane tablePane = new ExtendedJScrollPane(similarityTable);
		layout.setConstraints(tablePane, c);
		add(tablePane);
	}
}
