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
import com.rapidminer.operator.learner.clustering.DefaultCluster;
import com.rapidminer.operator.learner.clustering.FlatClusterModel;
import com.rapidminer.operator.learner.clustering.FlatCrispClusterModel;
import com.rapidminer.operator.learner.clustering.IdUtils;
import com.rapidminer.operator.learner.clustering.clusterer.AbstractDensityBasedClusterer;
import com.rapidminer.operator.learner.clustering.clusterer.ClusteringAggregation;
import com.rapidminer.operator.learner.clustering.clusterer.DBScanClustering;
import com.rapidminer.operator.similarity.DistanceSimilarityConverter;
import com.rapidminer.operator.similarity.SimilarityMeasure;
import com.rapidminer.operator.similarity.attributebased.FuzzyObjectSimilarity;
import com.rapidminer.operator.similarity.attributebased.Matrix;
import com.rapidminer.operator.similarity.SimilarityUtil;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.ParameterTypeInt;

import com.rapidminer.operator.similarity.attributebased.AbstractValueBasedSimilarity;
import com.rapidminer.operator.similarity.attributebased.uncertain.AbstractProbabilityDensityFunction;
import com.rapidminer.operator.similarity.attributebased.uncertain.SimpleProbabilityDensityFunction;
import com.rapidminer.operator.uncertain.AbstractSampleStrategy;
import com.rapidminer.operator.uncertain.SimpleSampling;


/**
 * Implements the FDBSCAN algorithm.
 * 
 * @author Michael Huber, Peter B Volk
 * @see com.rapidminer.operator.learner.clustering.clusterer.DBScanClustering
 * @see com.rapidminer.operator.learner.clustering.clusterer.uncertain.DBScanEAClustering
 * @see com.rapidminer.operator.learner.clustering.clusterer.ClusteringAggregation
 */
public class FDBScanClustering extends AbstractDensityBasedClusterer {
	
	private ExampleSet es;

	private double maxDistance = 0.2;
	
	private static final String ABSOLUTE_ERROR = "Absolute error";

	private static final String MAX_DISTANCE_NAME = "max_distance";

	//NOTE: While not having a pdf for each element, this global fuzziness is used.
	private double globalFuzziness = 0.0;

	private static final String GLOBAL_FUZZINESS = "global_fuzziness";

	//sampleRate defines the amount of samples per Object
	private int sampleRate = 5;

	private static final String SAMPLE_RATE = "sample_rate";
	
	private AbstractSampleStrategy sampleStrategy;

	//HashMap that assigns an array of samples to each element
	private Map<String, Double[][]> sampleCache;

	//HashMap that assigns a pdf to each element
	private Map<String, AbstractProbabilityDensityFunction> pdfCache;
	
	//HashMap that assigns a Minimum Bounding Rectangle (MBR) to each element
	//private Map<String, MinimumBoundingRectangle> boundingBoxes;

	private Map<String, Double> coreObjectList;
	

	public FDBScanClustering(OperatorDescription description) {
		super(description);
		pdfCache = new HashMap<String, AbstractProbabilityDensityFunction>();
		sampleCache = new HashMap<String, Double[][]>();
		//boundingBoxes = new HashMap<String, MinimumBoundingRectangle>();
		coreObjectList = new HashMap<String, Double>();
		sampleStrategy = new SimpleSampling();
	}

	/** 
     * Creates a <code>ClusterModel</code> using <code>doClustering</code>.
     * 
     * @param es dataset that is clustered
     * @return <code>ClusterModel</code> the clustering of the given dataset
     */
	public ClusterModel createClusterModel(ExampleSet es) throws OperatorException {
		this.es = es;
		maxDistance = getParameterAsDouble(MAX_DISTANCE_NAME);
		globalFuzziness = getParameterAsDouble(GLOBAL_FUZZINESS);
		FlatClusterModel result = doClustering(es);
		return result;
	}

	/** 
     * Calculates the epsilon-neighborhood of the given core object
     * and returns a list of IDs of all found objects.
     * 
     * @param es Dataset of Elements to be processed.
     * @param id ID of the core object.
     * @return All IDs of Elements in the epsilon-neighborhood.
     */
	protected List<String> getNeighbours(ExampleSet es, String id) {
		List<String> preselection = new LinkedList<String>();
		for (int i = 0; i < getIds().size(); i++) {
			String id2 = getIds().get(i);
			if (isReachable(id, id2))
				preselection.add(id2);
		}
		
		if(!isCoreObject(id, preselection)) {
			//gibt eine leere Liste zurück, da die Kernobjektbedingung nicht zutrifft
			return new LinkedList<String>();
		}

		// In der Matrixmethode wurden bereits die Core-Object-Wahrscheinlichkeiten berechnet.
		// Hier wird nur P^reach berechnet und dann damit multipliziert...
		List<String> result = new LinkedList<String>();
		double coreProbability = getCoreObjectProbability(id);
		for (int i = 0; i < preselection.size(); i++) {
			String id2 = preselection.get(i);
			//Es wird die Wahrscheinlichkeit ausgerechnet (P^reach = P^core * P^entfernung)
			if ((coreProbability * similarity(id, id2)) > 0.5)
				result.add(id2);
		}
		return result;
	}

	//Berechnet die Wahrscheinlichkeit, dass die zwei Objekte "epsilon-Nachbarn" sind.
	public double similarity(String id1, String id2) {
		double prob = 0;
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
				if(distance(a, b) <= maxDistance) {
					prob++;
				}
			}
		}
		prob = prob / (sampleRate*sampleRate);
		return prob;
	}
	
	//Prüft ob mindestens ein Sample innerhalb der Epsilon-Umgebung liegt.
	public boolean isReachable(String id1, String id2) {
		boolean reachable = false;
		//NOTE: Wenn diese Methode umgeschrieben wird, muss sampleCache delegiert werden!
		//TODO: Bounding Box hernehmen und checken, ob Element überhaupt in Frage kommt
		//Falls es die nicht gibt aus samples erstellen
		//Falls es die samples noch nicht gibt, die mit element und pdf erstellen
/*		for (int i = 0; i < sampleRate; i++) {
			MinimumBoundingRectangle mbr1 = getBoundingBox(id1);
			MinimumBoundingRectangle mbr2 = getBoundingBox(id2);
			if (minimalDistance(mbr1, mbr2) < maxDistance)
				reachable = true;
		}
*/
		reachable = true;
		return reachable;
	}
	
	public double getCoreObjectProbability(String id) {
		return coreObjectList.get(id);
	}
	
	public boolean isCoreObject(String id, List<String> preselection) {
		//Core-Object Prüfung. Soweit möglich werden "ge-cache-te" Daten/Infos verwedet...
		if(coreObjectList.containsKey(id)) {
			return true;
		}
		
		Double[][] sample = getSamples(id);
		int max_dimensions = sample.length;
		
		//Prüfung auf core object fand noch nicht statt
		//jetzt zu Cache hinzufügen (coreObjectList)
		Matrix m = new Matrix(sampleRate);
		m.reset(1); //1, weil das CoreObject mitgezählt wird
		double[] a = new double[max_dimensions];
		double[] b = new double[max_dimensions];
		
		//Hier wird die Matrix erstellt. Folien S. 72
		for(int k=0; k<preselection.size(); k++) {
			Double[][] tempSample = getSamples(preselection.get(k));
			for(int i=0; i<sampleRate; i++) {		//Sample-Index für Element
				for(int j=0; j<sampleRate; j++) {	//Sample-Index für Preselection-Elemente
					//folgendes Statement ist nur zum Umschreiben der Information
					for(int d=0; d<max_dimensions; d++) {
						a[d] = sample[i][d];
						b[d] = tempSample[i][d];
					}
					if(distance(a, b) <= maxDistance) {
						m.inc(i, j);
					}
				}
			}
		}
		
		//Check, ob core object oder nicht:
	
		double prob = 0;
		for(int i=0; i<sampleRate; i++) {
			for(int j=0; j<sampleRate; j++) {
				if(m.getValue(i, j) >= minPts) {
					prob++;
				}
			}
		}
		prob = prob / (sampleRate*sampleRate);
		
		if(prob > 0.5) {
			coreObjectList.put(id, prob);
			return true;
		}
		return false;
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
	
//	public MinimumBoundingRectangle getBoundingBox(String id) {
//		return boundingBoxes.get(id);
//	}
	
//	public double minimalDistance(MinimumBoundingRectangle mbr1, MinimumBoundingRectangle mbr2) {
//		//Finde jeweils ein Element in jeder BoundingBox, die sich am nähsten sind
//		double r;
//		double min1, max1, min2, max2;
//		
//		//NOTE: Dies sollte eigentlich schon durch isSimilarityDefined geprüft worden sein.
//		if(mbr1.getDimension() != mbr2.getDimension()) {
//			return Double.NaN;
//		}
//		//Prüfung jeder Dimension auf Minimum
//		for(int i=0; i<mbr1.getDimension(); i++) {
//			min1 = mbr1.getMinimumValue(i);
//			max1 = mbr1.getMaximumValue(i);
//			min2 = mbr2.getMinimumValue(i);
//			max2 = mbr2.getMaximumValue(i);
//			
//			//r = 
//		}
//		return 0;
//		
//		//Benutze die angegebene Distanz-Funktion um die Entfernung zu messen
//		//double dist = nestedSim...;
//		//return dist;
//	}

	//TODO: getSamples in eigene Klasse SampleCache delegieren.
	//Wenn isReachable() geändert wird, muss sie umgeschrieben werden.
	protected Double[][] getSamples(String id) {
		if(!sampleCache.containsKey(id)) {
			Example ex = IdUtils.getExampleFromId(es, id);
			
			sampleStrategy.setPdf(new SimpleProbabilityDensityFunction(globalFuzziness,getParameterAsBoolean(ABSOLUTE_ERROR)));
			sampleStrategy.setValue(getValues(ex));
			Double res[][] = sampleStrategy.getSamples();
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
		p2 = new ParameterTypeDouble(GLOBAL_FUZZINESS, "global fuzzyness", 0.0, Double.POSITIVE_INFINITY, 0.0);
		p2.setDescription("Global fuzziness describes by which amount the values from the example set " +
				"could fluctuate: i.e. plus/minus the given value. ");
		p2.setExpert(false);
		types.add(p2);
		
		p2 = new ParameterTypeBoolean(ABSOLUTE_ERROR, "Specifies if the error is an absolute error",true);
		p2.setExpert(false);
		types.add(p2);
		
		ParameterType p3;
		p3 = new ParameterTypeInt(SAMPLE_RATE, "sample rate", 0, Integer.MAX_VALUE, 5);
		p3.setDescription("Sample Rate sets the number of samples that are taken from each element.");
		p3.setExpert(false);
		types.add(p3);
		ParameterType pmeasure;
		pmeasure = SimilarityUtil.generateSimilarityParameter();
		pmeasure.setExpert(true);
		pmeasure.setDescription("nested distance measure");
		types.add(pmeasure);
		return types;
	}
}
