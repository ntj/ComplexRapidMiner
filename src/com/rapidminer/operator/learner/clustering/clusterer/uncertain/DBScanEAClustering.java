package com.rapidminer.operator.learner.clustering.clusterer.uncertain;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.InputDescription;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.learner.clustering.ClusterModel;
import com.rapidminer.operator.learner.clustering.FlatClusterModel;
import com.rapidminer.operator.learner.clustering.IdUtils;
import com.rapidminer.operator.learner.clustering.clusterer.AbstractDensityBasedClusterer;
import com.rapidminer.operator.similarity.SimilarityMeasure;
import com.rapidminer.operator.similarity.attributebased.uncertain.SimpleProbabilityDensityFunction;
import com.rapidminer.operator.uncertain.AbstractSampleStrategy;
import com.rapidminer.operator.uncertain.SimpleSampling;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.ParameterTypeInt;

/**
 * Implements the DBSCAN^EA algorithm.
 * 
 * @author Michael Huber, Peter B Volk
 * @see com.rapidminer.operator.learner.clustering.clusterer.DBScanClustering
 * @see com.rapidminer.operator.learner.clustering.clusterer.uncertain.FDBScanClustering
 * @see com.rapidminer.operator.learner.clustering.clusterer.ClusteringAggregation
 */
public class DBScanEAClustering extends AbstractDensityBasedClusterer {

	private ExampleSet es;

	private double maxDistance = 0.2;

	private static final String MAX_DISTANCE_NAME = "max_distance";

	//NOTE: While not having a pdf for each element, this global fuzziness is used.
	private double globalFuzziness = 0.0;

	private static final String GLOBAL_UNCERTAINTY = "global_uncertainty";

	private double lambda = 0.5;
	
	private static final String LAMBDA = "lambda";
	
	//NOTE: sampleRate defines the amount of samples per Object
	private int sampleRate = 5;

	private static final String SAMPLE_RATE = "sample_rate";
	
	private static final String ABSOLUTE_ERROR = "Absolute error";
	
	private AbstractSampleStrategy sampleStrategy;
	
	private Map<String, Double[][]> sampleCache;
	
	
	public DBScanEAClustering(OperatorDescription description) {
		super(description);
	}

	/** 
     * Creates a <code>ClusterModel</code> using <code>doClustering</code>.
     * 
     * @param es dataset that is clustered
     * @return <code>ClusterModel</code> the clustering of the given dataset
     */
	public ClusterModel createClusterModel(ExampleSet es) throws OperatorException {
		this.es = es;
		lambda = getParameterAsDouble(LAMBDA);
		globalFuzziness = getParameterAsDouble(GLOBAL_UNCERTAINTY);
		maxDistance = getParameterAsDouble(MAX_DISTANCE_NAME);
		sampleStrategy = new SimpleSampling();
		sampleCache = new HashMap<String, Double[][]>();
		FlatClusterModel result = doClustering(es);
		return result;
	}

	/** 
     * Calculates the neighborhood of the given core object
     * and returns a list of IDs of all found objects.
     * 
     * @param es Dataset of Elements to be processed.
     * @param id ID of the core object.
     * @return All IDs of Elements in the epsilon-neighborhood.
     */
	protected List<String> getNeighbours(ExampleSet es, String id) {
		List<String> result = new LinkedList<String>();
		for (int i = 0; i < getIds().size(); i++) {
			String id2 = getIds().get(i);
			double v = lambdaDistance(id, id2, this.lambda);
			if (v <= maxDistance)
				result.add(id2);
		}
		return result;
	}

	public double lambdaDistance(String id1, String id2, double lambda) {
		double diff = maxDistance(id1, id2) - minDistance(id1, id2);
		double lDist = diff * (1 - lambda) + minDistance(id1, id2);
		return lDist;
	}
	
	public double minDistance(String id1, String id2) {
		double dist = Double.MAX_VALUE;
		Double [][] e1 = getSamples(id1);
		Double [][] e2 = getSamples(id2);
		int max_dimensions = e1.length;
		double[] a = new double[max_dimensions];
		double[] b = new double[max_dimensions];
		
		for(int i=0; i<sampleRate; i++) {
			for(int j=0; j<sampleRate; j++) {
				for(int d=0; d<max_dimensions; d++) {
					a[d] = e1[i][d];
					b[d] = e2[j][d];
				}
				if(distance(a, b) < dist) {
					dist = distance(a, b);
				}
			}
		}
		return dist;
	}
	
	public double maxDistance(String id1, String id2) {
		double dist = Double.MIN_VALUE;
		Double [][] e1 = getSamples(id1);
		Double [][] e2 = getSamples(id2);
		int max_dimensions = e1.length;
		double[] a = new double[max_dimensions];
		double[] b = new double[max_dimensions];
		
		for(int i=0; i<sampleRate; i++) {
			for(int j=0; j<sampleRate; j++) {
				for(int d=0; d < max_dimensions; d++) {
					a[d] = e1[i][d];
					b[d] = e2[j][d];
				}
				if(distance(a, b) > dist) {
					dist = distance(a, b);
				}
			}
		}
		return dist;
	}

	/**
	 * Calculates the distance using the euclidean distance measurement.
	 * 
	 * @param e1 measure starting point
	 * @param e2 measure ending point
	 */
	public double distance(double e1, double e2) {
		if ((Double.isNaN(e1)) || (Double.isNaN(e2))) {
			return Double.NaN;
		}
		return Math.sqrt((e1 - e2) * (e1 - e2));
	}
	
	/**
	 * Calculates the distance of n-dimensional vectors using the
	 * euclidean distance measurement.
	 * 
	 * @param e1 n-dimensional starting vector
	 * @param e2 n-dimensional ending vector
	 */
	public double distance(double[] e1, double[] e2) {
		double sum = 0.0;
		int counter = 0;
		for (int i = 0; i < e1.length; i++) {
			if ((!Double.isNaN(e1[i])) && (!Double.isNaN(e2[i]))) {
				sum = sum + (e1[i] - e2[i]) * (e1[i] - e2[i]);
				counter++;
			}
		}
		double d = Math.sqrt(sum);
		if (counter > 0)
			return d;
		else
			return Double.NaN;
	}
	
	protected Double[][] getSamples(String id) {
		if(!sampleCache.containsKey(id)) {
			Example ex = IdUtils.getExampleFromId(es, id);
			sampleStrategy.setElement(getValues(ex));
			sampleStrategy.setPdf(new SimpleProbabilityDensityFunction(globalFuzziness,getParameterAsBoolean(ABSOLUTE_ERROR)));
			Double[][] res = sampleStrategy.getSamples();
			sampleCache.put(id, res);
			return res;
		}
		return sampleCache.get(id);
	}
	
	private double[] getValues(Example e) {
		if (e == null)
			return null;
		double[] values = new double[e.getAttributes().size()];
		int index = 0;
		for (Attribute attribute : e.getAttributes())
			values[index++] = e.getValue(attribute);
		return values;
	}

	public InputDescription getInputDescription(Class cls) {
		if (SimilarityMeasure.class.isAssignableFrom(cls)) {
			return new InputDescription(cls, false, true);
		} else {
			return super.getInputDescription(cls);
		}
	}
	
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		ParameterType p1;
		p1 = new ParameterTypeDouble(MAX_DISTANCE_NAME, "maximal distance", 0.0, Double.POSITIVE_INFINITY, 0.8);
		p1.setExpert(false);
		types.add(p1);
		ParameterType p2;
		p2 = new ParameterTypeDouble(GLOBAL_UNCERTAINTY, "global fuzzyness", 0.0, Double.POSITIVE_INFINITY, 0.0);
		p2.setDescription("Global fuzziness describes by which amount the values from the example set " +
				"could fluctuate: i.e. plus/minus the given value. ");
		p2.setExpert(false);
		types.add(p2);
		ParameterType p3;
		p3 = new ParameterTypeInt(SAMPLE_RATE, "sample rate", 0, Integer.MAX_VALUE, 5);
		p3.setDescription("Sample Rate sets the number of samples that are taken from each element.");
		p3.setExpert(false);
		types.add(p3);
		ParameterType p4;
		p2 = new ParameterTypeBoolean(ABSOLUTE_ERROR, "Specifies if the error is an absolute error",true);
		p2.setExpert(false);
		types.add(p2);
		
		p4 = new ParameterTypeDouble(LAMBDA, "lambda", 0, 1, 0.5);
		p4.setDescription("The range of this parameter spans from an extremly optimistic (1) " +
				"to an extemly pessimistic (0) cluster strategy.");
		p4.setExpert(false);
		types.add(p4);
		//types.add(SimilarityUtil.generateSimilarityParameter());
		return types;
	}
}
