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
package com.rapidminer.operator.learner.clustering.clusterer;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Vector;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.learner.clustering.CentroidBasedClusterModel;
import com.rapidminer.operator.learner.clustering.Cluster;
import com.rapidminer.operator.learner.clustering.FlatCrispClusterModel;
import com.rapidminer.tools.Tools;


/**
 * A cluster model used for the k-means clustering.
 * 
 * @author Michael Wurst, Ingo Mierswa
 * @version $Id: KMeansClusterModel.java,v 1.3 2007/06/30 23:24:35 ingomierswa Exp $
 */
public class FuzzyMembershipModel extends FlatCrispClusterModel implements CentroidBasedClusterModel {

	private static final long serialVersionUID = 3162433985759604081L;

	private double[][] centroids;
	private int[] clustersizes;
	private ExampleSet es;
	private FlatCrispClusterModel cm;
	private int m;
	private float exp;
	private DecimalFormat df = new DecimalFormat("0.00");

	public FuzzyMembershipModel(FlatCrispClusterModel cm ,double[][] centroids,int[] clustersizes, ExampleSet es, int m) {
		// Call copy constructor
		super();
		// Assign centroids
		this.centroids = centroids;
		this.clustersizes = clustersizes;
		this.es = es;
		this.cm = cm;
		this.m = m;
		this.exp = 2/(this.m-1);
		
		
		
	}

	public Vector<Double> getMembership(Example e){
		
			Vector<Double> result = new Vector<Double>(centroids.length);	
			LinkedList<Double> dists = new LinkedList<Double>();
			
			for (int j = 0; j < centroids.length; j++) {
				dists.add(getDistanceFromCentroid(j, e));
			}
			
					double sum = 0;
					for (int l = 0; l < centroids.length; l++) {
						sum=0;
						for (int k = 0; k < centroids.length; k++) {
							sum += Math.pow(dists.get(l) / dists.get(k),exp);
						}
					double mem = 1/sum;
					result.add(Math.round(mem*100.)/100.);
					}
			return result;		
	}

	public double getCentroidDistance(int index1, int index2) {
		double sum = 0.0;
		for (int i = 0; i < centroids[0].length; i++)
			sum = sum + (centroids[index1][i] - centroids[index2][i]) * (centroids[index1][i] - centroids[index2][i]);
		return Math.sqrt(sum);
	}

	public double getDistanceFromCentroid(int index, Example e) {
		double sum = 0.0;
		int i = 0;
		for (Attribute att : e.getAttributes()) {
			sum = sum + (centroids[index][i] - e.getValue(att)) * (centroids[index][i] - e.getValue(att));
			i++;
		}
		return Math.sqrt(sum);
	}

	public void setCentroid(int index, double[] values) {
		centroids[index] = values;
	}

	public double[] getCentroid(int index) {
		return centroids[index];
	}

	public String toString() {
		StringBuffer result = new StringBuffer(super.toString() + Tools.getLineSeparator());
		result.append("Cluster centroids:" + Tools.getLineSeparator());
		for (int i = 0; i < cm.getNumberOfClusters(); i++) {
			Cluster cl = cm.getClusterAt(i);
			result.append("Cluster " + cl.getId() + ":\t" + centroidToString(i) + Tools.getLineSeparator());
		}
		result.append(Tools.getLineSeparator());
		for (int i = 0; i < this.centroids.length; i++) {
			result.append("Centroid " + i + ":\t" + centroidToString(i) + "\t"+this.clustersizes[i]+Tools.getLineSeparator());
		}
		
		result.append(Tools.getLineSeparators(2));
		StringBuffer head = new StringBuffer();
		head.append("id\t[");
		for (int i = 0; i < centroids.length; i++) {
			head.append("CL"+i);
			if (i!=centroids.length-1) {
				head.append(", ");
			} else	{
				head.append("]\n");
			}
		}
		result.append(head);
		for (int i = 0; i < es.size(); i++) {
			result.append(i+"\t"+getMembership(es.getExample(i))+"\n");
		}
		
		
		return result.toString();
	}

	public String toResultString() {
		return toString();
	}

	private String centroidToString(int index) {
		StringBuffer s = new StringBuffer();
		int i = 0;
		for (Attribute att : es.getAttributes()) {
			s.append(att.getName() + " = " + Tools.formatNumber(centroids[index][i++]) + " ");
		}
		return s.toString();
	}

	public String[] getDimensionNames() {
		// TODO Auto-generated method stub
		return null;
	}
}
