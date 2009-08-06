package de.tud.inf.operator.clustering.similarity;

import com.rapidminer.operator.similarity.ClusterModelDistanceRepresentation;
import com.rapidminer.tools.math.Averagable;

public class KDistance extends Averagable{	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3628207430264617596L;
	private int kdistance;

	
	public KDistance(int kdistance) {
		super();
		this.kdistance = kdistance;
	}
	
	@Override
	protected void buildSingleAverage(Averagable averagable) {
		KDistance other = (KDistance) averagable;
		this.kdistance += other.kdistance;
	}

	@Override
	public double getMikroAverage() {
		return kdistance;
	}

	@Override
	public double getMikroVariance() {
		return Double.NaN;
	}

	@Override
	public String getName() {
		
		return "KDistance measure";
	}

}
