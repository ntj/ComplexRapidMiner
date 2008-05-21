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
package com.rapidminer.gui.viewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.rapidminer.gui.plotter.PlotterAdapter;
import com.rapidminer.gui.tools.SwingTools;
import com.rapidminer.operator.similarity.SimilarityMatrix;
import com.rapidminer.operator.similarity.SimilarityMeasure;
import com.rapidminer.tools.LogService;


/**
 * A k-distance visualization for similarities.
 * 
 * @author Peter B. Volk, Michael Wurst, Ingo Mierswa
 * @version $Id: SimilarityKDistanceVisualization.java,v 1.6 2008/05/09 19:23:01 ingomierswa Exp $
 */
public class SimilarityKDistanceVisualization extends PlotterAdapter implements ActionListener {

	private static final long serialVersionUID = -3774235976141625821L;

	private static final int DEFAULT_K_NUMBER = 3;

	private static final int LABEL_MARGIN_X = 50;

	private static final int LABEL_MARGIN_Y = 15;

	private static final Font SCALED_LABEL_FONT = LABEL_FONT.deriveFont(AffineTransform.getScaleInstance(1, -1));

	private double minX, maxX, minY, maxY, xTicSize, yTicSize;

	private SimilarityMatrix simMatrix = null;

	private SimilarityMeasure sim = null;

	private JTextField k_distance_jtext;

	private int updatePanelHeight;

	private LinkedList<Double> kDistanceValues;

	private int k = DEFAULT_K_NUMBER;

	private SimilarityVisualization simVisualiser = null;

	public SimilarityKDistanceVisualization(SimilarityMeasure sim) {
		super();
		this.sim = sim;
		setBackground(Color.white);
		
		setLayout(new BorderLayout());
		JLabel label = null;
		label = new JLabel("k : ");

		JButton updateButton = new JButton("Update");
		updateButton.addActionListener(this);

		k_distance_jtext = new JTextField();
		k_distance_jtext.setText(Integer.toString(this.k));
		k_distance_jtext.setColumns(5);

		JPanel updatePanel = new JPanel(new FlowLayout());
		updatePanel.add(label);
		updatePanel.add(k_distance_jtext);
		updatePanel.add(updateButton);

		JPanel updatePanelAligned = new JPanel(new BorderLayout());
		updatePanelAligned.add(updatePanel, BorderLayout.WEST);

		add(updatePanelAligned, BorderLayout.NORTH);
		this.updatePanelHeight = updatePanelAligned.getHeight();
	}

	public SimilarityKDistanceVisualization(SimilarityMeasure sim, SimilarityVisualization visualization) {
		this(sim);
		this.simVisualiser = visualization;
	}

	public JComponent getOptionsComponent(int index) {
		JLabel label = new JLabel("K:");
		label.setToolTipText("Set the k which should be displayed.");
		return label;

	}

	/** Indicates how many bins should be used for the distribution plot. */
	public void setK(int k) {
		this.k = k;
		repaint();
	}

	protected void prepareData() {
		this.minX = Double.POSITIVE_INFINITY;
		this.maxX = Double.NEGATIVE_INFINITY;
		this.minY = Double.POSITIVE_INFINITY;
		this.maxY = Double.NEGATIVE_INFINITY;
		// expection handling. The k is larger than the number of points in the map

		if (this.simMatrix == null)
			this.simMatrix = new SimilarityMatrix(this.sim);

		if (this.k >= this.simMatrix.getNumXLabels()) {
			LogService.getGlobal().log("KDistanceVisualization: k is larger than the number of labels", LogService.WARNING);
			k = this.simMatrix.getNumXLabels();
		}

		this.minX = 0;
		this.maxX = this.simMatrix.getNumYLabels();

		Iterator itX = this.simMatrix.getXLabels(), itY = null;
		String tempX = null, tempY = null;
		double tempDistance = 0;
		LinkedList<Double> sortList = null;

		this.kDistanceValues = new LinkedList<Double>();
		while (itX.hasNext()) {
			sortList = new LinkedList<Double>();
			tempX = (String) itX.next();
			itY = this.simMatrix.getYEntries(tempX);
			while (itY.hasNext()) {
				tempY = (String) itY.next();
				tempDistance = this.simMatrix.getEntry(tempX, tempY);
				tempY = null;
				sortList.add(tempDistance);
			}
			tempX = null;
			// sort the list

			Collections.sort(sortList);
			double currentValue = sortList.get(this.k - 1);
			this.minY = Math.min(minY, currentValue);
			this.maxY = Math.max(maxY, currentValue);
			this.kDistanceValues.add(currentValue);
			sortList = null;

		}

		Collections.sort(this.kDistanceValues);
		Collections.reverse(this.kDistanceValues);


		xTicSize = getNumericalTicSize(minX, maxX);
		yTicSize = getNumericalTicSize(minY, maxY);
		minX = Math.floor(minX / xTicSize) * xTicSize;
		maxX = Math.ceil(maxX / xTicSize) * xTicSize;
		minY = Math.floor(minY / yTicSize) * yTicSize;
		maxY = Math.ceil(maxY / yTicSize) * yTicSize;
	}

	protected void drawPoints(Graphics2D g, double dx, double dy, double sx, double sy) {
		if (this.kDistanceValues != null && this.kDistanceValues.size() <= 2) {
			LogService.getGlobal().log("KDistanceVisualization: No values in value map", LogService.WARNING);
			return;
		}
		if (this.kDistanceValues != null) {
			Iterator it = this.kDistanceValues.iterator();
			double offset = 0;
			while (it.hasNext()) {
				drawPoint(g, offset + dx, ((Double) it.next() + dy) * sy, Color.RED, Color.BLACK);
				offset += sx;
			}
		}
	}

	private void drawGrid(Graphics2D g, double dx, double dy, double sx, double sy) {
		DecimalFormat format = new DecimalFormat("0.00E0");
		g.setFont(SCALED_LABEL_FONT);
		int numberOfXTics = (int) Math.ceil((maxX - minX) / xTicSize) + 1;
		for (int i = 0; i < numberOfXTics; i++) {
			drawVerticalTic(g, i, format, dx, dy, sx, sy);
		}

		int numberOfYTics = (int) Math.ceil((maxY - minY) / yTicSize) + 1;
		for (int i = 0; i < numberOfYTics; i++) {
			drawHorizontalTic(g, i, format, dx, dy, sx, sy);
		}
	}

	private void drawVerticalTic(Graphics2D g, int ticNumber, DecimalFormat format, double dx, double dy, double sx, double sy) {
		double x = ticNumber * xTicSize + minX;
		g.setColor(GRID_COLOR);
		g.draw(new Line2D.Double((x + dx) * sx, (minY + dy) * sy, (x + dx) * sx, (maxY + dy) * sy));
		g.setColor(Color.black);
	}

	private void drawHorizontalTic(Graphics2D g, int ticNumber, DecimalFormat format, double dx, double dy, double sx, double sy) {
		double y = ticNumber * yTicSize + minY;
		g.setColor(GRID_COLOR);
		g.draw(new Line2D.Double((minX + dx) * sx, (y + dy) * sy, (maxX + dx) * sx, (y + dy) * sy));
		g.setColor(Color.black);
		String label = format.format(y) + " ";
		Rectangle2D stringBounds = SCALED_LABEL_FONT.getStringBounds(label, g.getFontRenderContext());
		g.drawString(label, (float) ((minX + dx) * sx - stringBounds.getWidth()), (float) ((y + dy) * sy - stringBounds.getHeight() / 2 - stringBounds.getY()));
	}

	private void drawPoints(Graphics2D g, int pixWidth, int pixHeight) {
		double sx = 0.0d;
		double sy = 0.0d;
		sx = ((double) pixWidth - LABEL_MARGIN_X) / (maxX - minX);
		sy = ((double) pixHeight - LABEL_MARGIN_Y) / (maxY - minY);

		Graphics2D coordinateSpace = (Graphics2D) g.create();
		coordinateSpace.translate(LABEL_MARGIN_X, LABEL_MARGIN_Y);
		drawGrid(coordinateSpace, -minX, -minY, sx, sy);
		drawPoints(coordinateSpace, -minX, -minY, sx, sy);
		coordinateSpace.dispose();
	}

	public void paintComponent(Graphics graphics) {
		super.paintComponent(graphics);

		int pixWidth = getWidth() - 2 * MARGIN;
		int pixHeight = getHeight() - 2 * MARGIN - this.updatePanelHeight - 50;
		Graphics2D translated = (Graphics2D) graphics.create();
		translated.translate(MARGIN, MARGIN);
		paintGraph(translated, pixWidth, pixHeight);

	}

	public void paintGraph(Graphics graphics, int pixWidth, int pixHeight) {
		Graphics2D g = (Graphics2D) graphics;
		Graphics2D scaled = (Graphics2D) g.create();

		scaled.translate(0, pixHeight + 1 + this.updatePanelHeight + 50);
		// prepare data
		prepareData();

		scaled.scale(1, -1);
		g.setColor(Color.black);
		drawPoints(scaled, pixWidth, pixHeight);

		scaled.dispose();

		// x-axis label
		String xAxisLabel = "sorted k-distances";
		Rectangle2D stringBounds = SCALED_LABEL_FONT.getStringBounds(xAxisLabel, g.getFontRenderContext());
		g.drawString(xAxisLabel, MARGIN + (float)(pixWidth / 2.0d - stringBounds.getWidth() / 2.0d), MARGIN + (float)(pixHeight - 2.0d * stringBounds.getHeight()) + 3);

		// y-axis label
		String yAxisLabel = "k-distance value";
		stringBounds = LABEL_FONT.getStringBounds(yAxisLabel, g.getFontRenderContext());
		g.drawString(yAxisLabel, MARGIN, (int) (MARGIN + stringBounds.getHeight() + 6));
	}

	public void actionPerformed(ActionEvent arg0) {
		try {
			Integer.parseInt(k_distance_jtext.getText());
		} catch (NumberFormatException e) {
			SwingTools.showVerySimpleErrorMessage("Please enter a integer value for k");
			return;
		}

		this.k = Integer.parseInt(k_distance_jtext.getText());
		this.kDistanceValues = null;
		this.simVisualiser.repaint();
	}
}
