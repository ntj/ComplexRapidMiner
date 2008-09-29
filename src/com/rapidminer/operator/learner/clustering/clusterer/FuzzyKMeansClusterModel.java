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
import java.util.LinkedList;
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
public class FuzzyKMeansClusterModel extends FlatCrispClusterModel implements CentroidBasedClusterModel {

	private static final long serialVersionUID = 3162433985759604081L;

	private double[][] centroids;

	private ExampleSet es;
	
	private int m;

	public FuzzyKMeansClusterModel(double[][] centroids, ExampleSet es, int m) {
		// Call copy constructor
		super();
		// Assign centroids
		this.centroids = centroids;
		this.es = es;
		this.m = m;
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
		for (int i = 0; i < getNumberOfClusters(); i++) {
			Cluster cl = getClusterAt(i);
			result.append("Cluster " + cl.getId() + ":\t" + centroidToString(i) + Tools.getLineSeparator());
		}
		
		result.append(Tools.getLineSeparator());
		result.append(getMemberships());
		
		return result.toString();
	}

	public String toResultString() {
		return toString();
	}

	
	public StringBuffer getMemberships(){
		
		StringBuffer res = new StringBuffer();
		
		res.append("Membership Values:"+Tools.getLineSeparator());
		
		int numCl = getNumberOfClusters();
		double minCdist = getMinD();
		double[] avgC = getAvgC();
		
		
		
		StringBuffer head = new StringBuffer();
		head.append("Id\t");
		for (int i = 0; i < numCl; i++) {
			Cluster cl = getClusterAt(i);
			head.append("Cluster "+cl.getId()+"\t");
		}
		
		head.append("PC\t\tXB\t\tFS\t");
		head.append("\n");
		res.append(head+Tools.getLineSeparator());
		
		float exp = 2/(this.m-1);
		DecimalFormat df = new DecimalFormat("0.00");
		
		double globalSumPC = 0;
		double rowSumPC = 0;
		double globalSumXB = 0;
		double rowSumXB = 0;
		double globalSumFS = 0;
		double rowSumFS = 0;
		
		for (int i = 0; i < es.size(); i++) {
			Example e = es.getExample(i);
			StringBuffer entry = new StringBuffer();
			entry.append(e.getId()+"\t");
			
			rowSumPC = 0;
			rowSumXB = 0;
			rowSumFS = 0;
			LinkedList<Double> dists = new LinkedList<Double>();
			
			for (int j = 0; j < getNumberOfClusters(); j++) {
				dists.add(getDistanceFromCentroid(j, e));
			}
			
			
			
			for (int k = 0; k < getNumberOfClusters(); k++) {
				
				double sum = 0;
				
				for (int l = 0; l < getNumberOfClusters(); l++) {
					sum+=Math.pow(dists.get(k)/dists.get(l),exp);
				}
				
				double mem = 1/sum;
				
				entry.append(df.format(mem)+"\t\t");
				
				rowSumPC+=Math.pow(mem,2);
				rowSumXB+=Math.pow(mem,2)*Math.pow(dists.get(k),2);
				rowSumFS+=Math.pow(mem, this.m)*(Math.pow(dists.get(k),2)-Math.pow(getCentroidToAvgDistance(k, avgC), 2));
			}
			globalSumPC+=rowSumPC;
			globalSumXB+=rowSumXB;
			globalSumFS+=rowSumFS;
			entry.append(df.format(rowSumPC)+"\t\t");
			entry.append(df.format(rowSumXB/Math.pow(minCdist, 2))+"\t\t");
			entry.append(df.format(rowSumFS)+"\t\t");
			entry.append("\n");
			res.append(entry);
			
			
		}
		res.append("\n\n"+Tools.getLineSeparator());
		res.append("Global Partition Coefficient: "+df.format(globalSumPC/es.size())+Tools.getLineSeparator());
		res.append("Global Sum Xie Beni: "+globalSumXB+Tools.getLineSeparator());
		res.append("Global Xie Beni: "+df.format(globalSumXB/(es.size()*Math.pow(minCdist, 2)))+Tools.getLineSeparator());
		res.append("Global Fukuyama Sugeno: "+df.format(globalSumFS)+Tools.getLineSeparator());
		res.append("min centroid distance: "+df.format(minCdist)+Tools.getLineSeparator());
		res.append("avgCentroid: "+avgToString(avgC)+Tools.getLineSeparator());
		res.append("|Examples|: "+es.size()+Tools.getLineSeparator());
		return res;
		
		
		
		
	}
	
	
	
	private double[] getAvgC() {
		
		double[] avg = new double[this.centroids[0].length];
		
			for (int i = 0; i < getNumberOfClusters(); i++) {
				
				double[] cent = this.centroids[i];
				
				for (int j = 0; j < cent.length; j++) {
					avg[j]+=cent[j];
				}
			}
		
			for (int i = 0; i < avg.length; i++) {
				avg[i]=avg[i]/getNumberOfClusters();
			}
		
		return avg;
	}

	private double getMinD() {
	
		double min = Integer.MAX_VALUE;
		for (int i = 0; i < getNumberOfClusters(); i++) {
			for (int j = 0; j < getNumberOfClusters(); j++) {
				double tmp = getCentroidDistance(i, j);
				if (tmp<min&&tmp!=0) {
					min=tmp;
				}
			}
		}
		return min;
	}
	
	public double getCentroidToAvgDistance(int index1, double[] avg ) {
		double sum = 0.0;
		for (int i = 0; i < centroids[0].length; i++)
			sum = sum + (centroids[index1][i] - avg[i]) * (centroids[index1][i] - avg[i]);
		return Math.sqrt(sum);
	}
	
	private String avgToString(double[] avg) {
		StringBuffer s = new StringBuffer();
		for (int j = 0; j < avg.length; j++) {
			s.append(Tools.formatNumber(avg[j]) + " ");
		} 
		return s.toString();
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
