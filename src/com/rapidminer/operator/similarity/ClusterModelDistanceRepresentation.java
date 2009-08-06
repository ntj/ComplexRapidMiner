package com.rapidminer.operator.similarity;

import com.rapidminer.tools.math.Averagable;

public class ClusterModelDistanceRepresentation extends Averagable{

	
	public ClusterModelDistanceRepresentation(String name, int distance) {
		super();
		this.name = name;
		this.distance = distance;
	}

	private String name;
	private int distance;
	
	@Override
	protected void buildSingleAverage(Averagable averagable) {	
		ClusterModelDistanceRepresentation other = (ClusterModelDistanceRepresentation) averagable;
		this.distance += other.distance;
	}

	@Override
	public double getMikroAverage() {
		return distance;
	}

	@Override
	public double getMikroVariance() {
		return Double.NaN;
	}

	@Override
	public String getName() {
		return this.name;
	}


}
