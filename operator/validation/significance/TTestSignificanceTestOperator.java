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
package com.rapidminer.operator.validation.significance;

import java.awt.Color;

import javax.swing.JEditorPane;
import javax.swing.JLabel;

import com.rapidminer.gui.tools.ExtendedJScrollPane;
import com.rapidminer.gui.tools.SwingTools;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.performance.PerformanceCriterion;
import com.rapidminer.operator.performance.PerformanceVector;
import com.rapidminer.tools.Tools;
import com.rapidminer.tools.math.FDistribution;
import com.rapidminer.tools.math.SignificanceTestResult;


/**
 * Determines if the null hypothesis (all actual mean values are the same) holds
 * for the input performance vectors. This operator uses a simple (pairwise)
 * t-test to determine the probability that the null hypothesis is wrong. Since
 * a t-test can only be applied on two performance vectors this test will be
 * applied to all possible pairs. The result is a significance matrix. However,
 * pairwise t-test may introduce a larger type I error. It is recommended to
 * apply an additional ANOVA test to determine if the null hypothesis is wrong
 * at all.
 * 
 * @author Ingo Mierswa
 * @version $Id: TTestSignificanceTestOperator.java,v 1.5 2006/03/21 15:35:53
 *          ingomierswa Exp $
 */
public class TTestSignificanceTestOperator extends SignificanceTestOperator {

	/** The result for a paired t-test. */
	public static class TTestSignificanceTestResult extends SignificanceTestResult {

		private static final long serialVersionUID = -5412090499056975997L;

		private PerformanceVector[] allVectors;

		private double[][] probMatrix;

		private double alpha = 0.05d;

		public TTestSignificanceTestResult(PerformanceVector[] allVectors, double[][] probMatrix, double alpha) {
			this.allVectors = allVectors;
			this.probMatrix = probMatrix;
			this.alpha = alpha;
		}

		public String getName() {
			return "Pairwise t-Test";
		}

		/** Returns NaN since no single probability will be delivered. */
		public double getProbability() {
			return Double.NaN;
		}
		
		public String toString() {
			StringBuffer result = new StringBuffer();
			result.append("Probabilities for random values with the same result:" + Tools.getLineSeparator());
			for (int i = 0; i < allVectors.length; i++) {
				for (int j = 0; j < allVectors.length; j++) {
					if (!Double.isNaN(probMatrix[i][j]))
						result.append(Tools.formatNumber(probMatrix[i][j]) + "\t");
					else
						result.append("-----\t");
				}
				result.append(Tools.getLineSeparator());
			}
			result.append("Values smaller than alpha=" + Tools.formatNumber(alpha) + " indicate a probably significant difference between the mean values!" + Tools.getLineSeparator());
			result.append("List of performance values:" + Tools.getLineSeparator());
			for (int i = 0; i < allVectors.length; i++) {
				result.append(i + ": " + Tools.formatNumber(allVectors[i].getMainCriterion().getAverage()) + " +/- " + Tools.formatNumber(Math.sqrt(allVectors[i].getMainCriterion().getVariance())) + Tools.getLineSeparator());
			}
			return result.toString();
		}

		public java.awt.Component getVisualizationComponent(IOContainer container) {
			StringBuffer buffer = new StringBuffer();
            Color bgColor = SwingTools.LIGHTEST_YELLOW;
            String bgColorString = Integer.toHexString(bgColor.getRed()) + Integer.toHexString(bgColor.getGreen()) + Integer.toHexString(bgColor.getBlue());
            
			buffer.append("<table bgcolor=\""+bgColorString+"\" border=\"1\">");
			buffer.append("<tr><td></td>");
			for (int i = 0; i < allVectors.length; i++) {
				buffer.append("<td>" + Tools.formatNumber(allVectors[i].getMainCriterion().getAverage()) + " +/- " + Tools.formatNumber(Math.sqrt(allVectors[i].getMainCriterion().getVariance())) + "</td>");
			}
			buffer.append("</tr>");
			for (int i = 0; i < allVectors.length; i++) {
				buffer.append("<tr><td>" + Tools.formatNumber(allVectors[i].getMainCriterion().getAverage()) + " +/- " + Tools.formatNumber(Math.sqrt(allVectors[i].getMainCriterion().getVariance())) + "</td>");
				for (int j = 0; j < allVectors.length; j++) {
					buffer.append("<td>");
					if (!Double.isNaN(probMatrix[i][j])) {
						double prob = probMatrix[i][j];
						if (prob < alpha) {
							buffer.append("<b>");
						}
						buffer.append(Tools.formatNumber(prob));
						if (prob < alpha) {
							buffer.append("</b>");
						}
					}
					buffer.append("</td>");
				}
				buffer.append("</tr>");
			}
			buffer.append("</table>");
			buffer.append("<br>Probabilities for random values with the same result.<br>Bold values are smaller than alpha=" + Tools.formatNumber(alpha) + " which indicates a probably significant difference between the actual mean values!");

            JEditorPane textPane = new JEditorPane("text/html", "<html><h1>" + getName() + "</h1>" + buffer.toString() + "</html>");
            textPane.setBackground((new JLabel()).getBackground());
            textPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(11, 11, 11, 11));
            return new ExtendedJScrollPane(textPane);
		}
	}

	public TTestSignificanceTestOperator(OperatorDescription description) {
		super(description);
	}

	public SignificanceTestResult performSignificanceTest(PerformanceVector[] allVectors, double alpha) {
		double[][] resultMatrix = new double[allVectors.length][allVectors.length];
		for (int i = 0; i < allVectors.length; i++) {
			for (int j = 0; j < (i + 1); j++)
				resultMatrix[i][j] = Double.NaN; // fill lower triangle with
													// NaN --> empty in result
													// string
			for (int j = i + 1; j < allVectors.length; j++) {
				resultMatrix[i][j] = getProbability(allVectors[i].getMainCriterion(), allVectors[j].getMainCriterion());
			}
		}
		return new TTestSignificanceTestResult(allVectors, resultMatrix, alpha);
	}

	private double getProbability(PerformanceCriterion pc1, PerformanceCriterion pc2) {
		double totalDeviation = ((pc1.getAverageCount() - 1) * pc1.getVariance() + (pc2.getAverageCount() - 1) * pc2.getVariance()) / (pc1.getAverageCount() + pc2.getAverageCount() - 2);
		double factor = 1.0d / (1.0d / pc1.getAverageCount() + 1.0d / pc2.getAverageCount());
		double diff = pc1.getAverage() - pc2.getAverage();
		double t = factor * diff * diff / totalDeviation;
		FDistribution fDist = new FDistribution(1, pc1.getAverageCount() + pc2.getAverageCount() - 2);
		double prob = fDist.getProbabilityForValue(t);
		prob = prob < 0 ? 1.0d : 1.0d - prob;
		return prob;
	}

	public int getMinSize() {
		return 2;
	}

	public int getMaxSize() {
		return Integer.MAX_VALUE;
	}
}
