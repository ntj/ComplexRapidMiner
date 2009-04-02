package com.rapidminer.operator.learner.clustering.clusterer;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import jxl.demo.Escher;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.table.DataRow;
import com.rapidminer.example.table.DataRowFactory;
import com.rapidminer.example.table.ListDataRowReader;
import com.rapidminer.example.table.MemoryExampleTable;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.generator.TargetFunction;
import com.rapidminer.operator.learner.clustering.AbstractClusterModel;
import com.rapidminer.operator.learner.clustering.ClusterModel;
import com.rapidminer.operator.learner.clustering.FlatCrispClusterModel;
import com.rapidminer.operator.learner.clustering.IdUtils;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.tools.Ontology;
import com.rapidminer.tools.RandomGenerator;
import com.rapidminer.tools.att.AttributeSet;

public class FuzzyMembershipEvaluator extends AbstractFlatClusterer {

	/** The parameter name for &quot;the fuzzifier&quot; */
	public static final String PARAMETER_M = "m";
	
	/** The parameter name for &quot;should the membership values be added to the ExampleSet as well&quot; */
	public static final String PARAMETER_ADD_LABEL = "add_label";
	
	private int m;
	private int numAttributes;
	private int numClusters;
	private double[][] centroids;
	private int[] clustersizes;
	private FuzzyMembershipModel fmm;
	
	public FuzzyMembershipEvaluator(OperatorDescription description) {
		super(description);
	}

	@Override
	public ClusterModel createClusterModel(ExampleSet es)
			throws OperatorException {
		
		m = getParameterAsInt(PARAMETER_M);
		FlatCrispClusterModel fccm = this.getInput(FlatCrispClusterModel.class);
		numClusters = fccm.getNumberOfClusters();
		this.centroids = computeCentroids(es);
		this.clustersizes = computeClusterSizes(fccm);
		fmm = new FuzzyMembershipModel(fccm,centroids,clustersizes,es,m);
		
		if (getParameterAsBoolean(PARAMETER_ADD_LABEL)) {
			addLabels(es);
		}
		
		return fmm;
	}

		
	
	private void addLabels(ExampleSet es) {
		
		Vector<Attribute> ats = new Vector<Attribute>(centroids.length);
		
		for (int i = 0; i < centroids.length; i++) {
			Attribute cluster = AttributeFactory.createAttribute("Cluster"+i, Ontology.REAL);
			ats.add(cluster);
			es.getExampleTable().addAttribute(cluster);
			es.getAttributes().setSpecialAttribute(cluster, "Cluster"+i);
		}
		
		for (int i = 0; i < es.size(); i++) {
			
			Vector<Double> temp = fmm.getMembership(es.getExample(i));
			
			for (int j = 0; j < temp.size(); j++) {
				es.getExample(i).setValue(ats.get(j), temp.get(j));
			}
			
			
		}
		
	}

	protected double[][] computeCentroids(ExampleSet es) throws OperatorException {
		numAttributes = es.getAttributes().size();
		this.centroids = new double[numClusters][numAttributes];
		this.clustersizes = new int[numClusters];
		for (int j = 0; j < numClusters; j++) {
			int memcount = 0;
			double[] temp = new double[numAttributes]; 
			for (int i = 0; i < es.size(); i++) {
				Example ex = es.getExample(i);
					if (ex.getValue(ex.getAttributes().getCluster())==j) {
						memcount++;
						int m = 0;
						for (Attribute att : ex.getAttributes()) {
							if (!ex.getAttributes().getRole(att).isSpecial()) {
								temp[m] = temp[m]+ex.getValue(att);
								m++;
							}
						}
					}
			}
			for (int k = 0; k < temp.length; k++) {
				centroids[j][k] = temp[k]/memcount;
				clustersizes[j] = memcount;
			}
			
		}
		return centroids;
	}
	
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		ParameterType type = new ParameterTypeInt(PARAMETER_M, "Fuzzifier", 2, Integer.MAX_VALUE, 2);
		type.setExpert(false);
		types.add(type);
		types.add(new ParameterTypeBoolean(PARAMETER_ADD_LABEL, "should the cluster values be added as label as well", false));
		return types;
	}
	
	protected int[] computeClusterSizes(FlatCrispClusterModel fccm) {
		
		int[] ret = new int[numClusters];
		
		for(int i = 0; i < numClusters; i++) {
			
			ret[i] = fccm.getClusterAt(i).getNumberOfObjects();
		}
		
		return ret;
	}

	@Override
	public Class<?>[] getOutputClasses() {
	
		return new Class[] {FuzzyMembershipModel.class};
	}
}


