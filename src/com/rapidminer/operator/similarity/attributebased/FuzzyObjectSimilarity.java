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
package com.rapidminer.operator.similarity.attributebased;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
//import com.rapidminer.example.Tools;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.learner.clustering.IdUtils;
import com.rapidminer.operator.similarity.SimilarityMeasure;

//import com.rapidminer.tools.math.MathFunctions;

/**
 * Similarity based on the correlation of two fuzzy objects.
 * This special similarity measure is used by FDBSCAN.
 * 
 * @author Michael Huber
 * @version $Id: CorrelationSimilarity.java,v 1.1 2007/05/27 21:59:45 ingomierswa Exp $
 */
public class FuzzyObjectSimilarity extends AbstractRealValueBasedSimilarity {

	private static final long serialVersionUID = 7106870911590574668L;
	
	private SimilarityMeasure nestedSim;
	
	private int sampleRate;
	
	private Map<String, double[][]> sampleCache;

	//private ExampleSet es;
	
	//private AbstractProbabilityDensityFunction pdf;
	

	public FuzzyObjectSimilarity(Map<String, double[][]> sampleCache, SimilarityMeasure similarityMeasure, 
			int sampleRate) {
		super();
		this.sampleRate = sampleRate;
		this.nestedSim = similarityMeasure;
		this.sampleCache = sampleCache;
	}

	public double similarity(double[] e1, double[] e2) {
		//TODO: Die eigentliche Similarity-Funktion implementieren, die einen Wert von 0 bis 1 zurückgibt,
		//die aber auch die Wahrscheinlichkeit, dass e1 ein Core-Object ist mit einbringen muss!
		
		return 0;//MathFunctions.correlation(e1, e2);
	}

/*	public double similarity(double e1, double e2) {
		//TODO: Hier wird ganz traditionell die Entfernung gemessen. (mit der eingebetteten Distanzfunktion)
		//Leider kann die similarity() Methode nur Strings annehmen...
		//XXX: Hard-coding der Euklidischen Distanz:
		double d = 0.0;
		if ((Double.isNaN(e1)) || (Double.isNaN(e2))) {
			return Double.NaN;
		}
		d = (e1 - e2) * (e1 - e2);
		d = Math.sqrt(d);
		return d;
	}
*/
	public double similarity(String id1, String id2) {
		if (!isSimilarityDefined(id1, id2))
			return java.lang.Double.NaN;
//		double[] e1 = getValuesFromId(x);
//		double[] e2 = getValuesFromId(y);
//		return similarity(e1, e2);
		
		double [][] e1 = sampleCache.get(id1);
		double [][] e2 = sampleCache.get(id2);
		int max_dimensions = e1.length;
		double[] a = new double[max_dimensions];
		double[] b = new double[max_dimensions];
		
		for(int i=0; i<sampleRate; i++) {		//Sample-Index für Element														//Element-Nummer durchblubbern???
			for(int j=0; j<sampleRate; j++) {	//Sample-Index für Preselection-Elemente																//Element-Nummer durchblubbern???
				//folgendes Statement ist nur zum umschreiben der Information
				for(int d=0; d<e1.length; d++) {	//geht einfach alle Dimensionen durch														//Sample-Dimension durchblubbern???
					a[d] = e1[d][i];
					b[d] = e2[d][j];
				}
/*				if(distance(a, b) <= maxDistance) {					//NOTE: Einfache euklidische Distanz wurde verwendet
					m.inc(i, j);
				}
*/			}
		}
		return Double.NaN;
	}
	
/*	protected double[][] getSamplesById(String id) {
		Example e = IdUtils.getExampleFromId(es, id);
		return getValues(e);
	}
*/
	public boolean isDistance() {
		return false;
	}
}

