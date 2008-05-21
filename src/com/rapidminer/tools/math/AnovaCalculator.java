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
package com.rapidminer.tools.math;

import java.awt.Color;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JEditorPane;
import javax.swing.JLabel;

import com.rapidminer.gui.tools.ExtendedJScrollPane;
import com.rapidminer.gui.tools.SwingTools;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.tools.Tools;


/**
 * Determines if the null hypothesis (all actual mean values are the same) holds
 * for the given values. This class uses an ANalysis Of VAriances approach to
 * determine probability that the null hypothesis is wrong.
 * 
 * @author Ingo Mierswa
 * @version $Id: AnovaCalculator.java,v 1.6 2008/05/09 19:23:03 ingomierswa Exp $
 */
public class AnovaCalculator {

	public static class AnovaSignificanceTestResult extends SignificanceTestResult {

		private static final long serialVersionUID = 9007616378489018565L;

		private double sumSquaresBetween = 0.0d;

		private double sumSquaresResiduals = 0.0d;

		private double meanSquaresBetween = 0.0d;

		private double meanSquaresResiduals = 0.0d;

		private int df1 = 0;

		private int df2 = 0;
		
		private double alpha = 0.05;;

		private double fValue = 0.0d;

		private double prob = 0.0d;

		public AnovaSignificanceTestResult(double sumSquaresBetween, double sumSquaresResiduals, int df1, int df2, double alpha) {
			this.sumSquaresBetween = sumSquaresBetween;
			this.sumSquaresResiduals = sumSquaresResiduals;
			this.df1 = df1;
			this.df2 = df2;
			this.alpha = alpha;
			this.meanSquaresBetween = sumSquaresBetween / df1;
			this.meanSquaresResiduals = sumSquaresResiduals / df2;
			this.fValue = meanSquaresBetween / meanSquaresResiduals;
			FDistribution fDist = new FDistribution(df1, df2);
			this.prob = fDist.getProbabilityForValue(this.fValue);
			if (this.prob < 0)
				this.prob = 1.0d;
			else
				this.prob = 1.0d - this.prob;
		}

		public String getName() {
			return "Anova Test";
		}

		public String toString() {
			return "ANOVA result (f=" + Tools.formatNumber(fValue) + ", prob=" + Tools.formatNumber(prob) + ", alpha=" + Tools.formatNumber(alpha) + ")";
		}

		public double getProbability() {
			return prob;
		}
		
		/**
		 * Returns a label that displays the {@link #toResultString()} result
		 * encoded as html.
		 */
		public java.awt.Component getVisualizationComponent(IOContainer container) {
			StringBuffer buffer = new StringBuffer();
            Color bgColor = SwingTools.LIGHTEST_YELLOW;
            String bgColorString = "#" + Integer.toHexString(bgColor.getRed()) + Integer.toHexString(bgColor.getGreen()) + Integer.toHexString(bgColor.getBlue());
            Color headerColor = SwingTools.LIGHTEST_BLUE;
            String headerColorString = "#" + Integer.toHexString(headerColor.getRed()) + Integer.toHexString(headerColor.getGreen()) + Integer.toHexString(headerColor.getBlue());
			buffer.append("<table bgcolor=\""+bgColorString+"\" border=\"1\">");
			buffer.append("<tr bgcolor=\""+headerColorString+"\"><th>Source</th><th>Square Sums</th><th>DF</th><th>Mean Squares</th><th>F</th><th>Prob</th></tr>");
			buffer.append("<tr><td>Between</td><td>" + Tools.formatNumber(sumSquaresBetween) + "</td><td>" + df1 + "</td><td>" + Tools.formatNumber(meanSquaresBetween) + "</td><td>" + Tools.formatNumber(fValue) + "</td><td>" + Tools.formatNumber(prob) + "</td></tr>");
			buffer.append("<tr><td>Residuals</td><td>" + Tools.formatNumber(sumSquaresResiduals) + "</td><td>" + df2 + "</td><td>" + Tools.formatNumber(meanSquaresResiduals) + "</td><td></td><td></td></tr>");
			buffer.append("<tr><td>Total</td><td>" + Tools.formatNumber(sumSquaresBetween + sumSquaresResiduals) + "</td><td>" + (df1 + df2) + "</td><td></td><td></td><td></td></tr>");
			buffer.append("</table>");
			buffer.append("<br>Probability for random values with the same result: " + Tools.formatNumber(prob) + "<br>");
			if (prob < alpha)
				buffer.append("Difference between actual mean values is probably significant, since " + Tools.formatNumber(prob) + " &lt; alpha = " + Tools.formatNumber(alpha) + "!");
			else
				buffer.append("Difference between actual mean values is probably not significant, since " + Tools.formatNumber(prob) + " &gt; alpha = " + Tools.formatNumber(alpha) + "!");

			JEditorPane textPane = new JEditorPane("text/html", "<html><h1>" + getName() + "</h1>" + buffer.toString() + "</html>");
            textPane.setBackground((new JLabel()).getBackground());
			textPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(11, 11, 11, 11));
            return new ExtendedJScrollPane(textPane);
		}
	}
	

	private double alpha = 0.05;

	private List<TestGroup> groups = new LinkedList<TestGroup>();

	public void setAlpha(double alpha) {
		this.alpha = alpha;
	}

	public void addGroup(TestGroup group) {
		groups.add(group);
	}
	
	public void addGroup(double numberOfValues, double mean, double variance) {
		addGroup(new TestGroup(numberOfValues, mean, variance));
	}

	public void clearGroups() {
		groups.clear();
	}
	
	public SignificanceTestResult performSignificanceTest() throws SignificanceCalculationException {
		if (groups.size() < 2) {
			throw new SignificanceCalculationException("Cannot calculate ANOVA: not enough groups added (current number of groups: " + groups.size() + ", must be at least 2");
		}

		double meanOfMeans = 0.0d;
		Iterator<TestGroup> i = groups.iterator();
		while (i.hasNext()) {
			TestGroup group = i.next();
			meanOfMeans += group.getMean();
		}
		meanOfMeans /= groups.size();

		double sumSquaresBetween = 0.0d;
		i = groups.iterator();
		while (i.hasNext()) {
			TestGroup group = i.next();
			double diff = group.getMean() - meanOfMeans;
			sumSquaresBetween += group.getNumber() * (diff * diff);
		}

		double sumSquaresResiduals = 0.0d;
		int counterSum = 0;
		i = groups.iterator();
		while (i.hasNext()) {
			TestGroup group = i.next();
			sumSquaresResiduals += (group.getNumber() - 1) * group.getVariance();
			counterSum += group.getNumber();
		}

		return new AnovaSignificanceTestResult(sumSquaresBetween, sumSquaresResiduals, groups.size() - 1, counterSum - groups.size(), alpha);
	}
}
