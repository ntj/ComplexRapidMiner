package de.tud.inf.operator.learner.clustering.clusterer;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.MissingIOObjectException;
import com.rapidminer.operator.Model;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.learner.clustering.Cluster;
import com.rapidminer.operator.learner.clustering.ClusterModel;
import com.rapidminer.operator.learner.clustering.DefaultCluster;
import com.rapidminer.operator.learner.clustering.FlatClusterModel;
import com.rapidminer.operator.learner.clustering.FlatCrispClusterModel;
import com.rapidminer.operator.learner.clustering.IdUtils;
import com.rapidminer.operator.learner.clustering.clusterer.AbstractFlatClusterer;
import com.rapidminer.operator.learner.clustering.clusterer.FuzzyMembershipModel;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.UndefinedParameterError;

/**
 * 
 * 
 *
 */

public class SignificanceAwareMultiCaseClustering extends AbstractFlatClusterer{
	
	public static final Class<? extends IOObject> inputModelsClass = FuzzyMembershipModel.class;
	
	public static final String PARAMETER_T = "t";
	
	public static final String PARAMETER_ALPHA = "alpha";
	
	public static final String PARAMETER_UNDECIDABLE_CASE_MAPPING = "a? mapping";
	
	public static final String[] UNDECIDABLE_CASE_MAPPINGS = {"a+","a-"};
	
	public static final int UNDECIDABLE_CASE_TO_PLUS = 0;
	
	public static final int UNDECIDABLE_CASE_TO_MINUS = 1;
	
	private ArrayList<FuzzyMembershipModel> fuzzyInputModels;
	
	private double treshold;
	
	private double alpha;
	
	private int undecidableCaseMapping;
	
	private FlatCrispClusterModel clusterModel;

	public SignificanceAwareMultiCaseClustering(OperatorDescription description) {
		
		super(description);
	}

	@Override
	public ClusterModel createClusterModel(ExampleSet exampleSet)
			throws OperatorException {
		
		int[] globalAssignment = new int[3];

		double normalizedCase;

		Vector<Double> vi, vj;
		
		String viId, vjId;

		FuzzyMembershipModel model;
		
		DefaultCluster cluster;
		
		int clusterID = 0;
		
		addInputModels();
		
		setParameters();
		
		clusterModel = new FlatCrispClusterModel();

		for (int i = 0; i < exampleSet.size(); i++) {
			
			viId = IdUtils.getIdFromExample(exampleSet.getExample(i));
			
			/* get the Cluster for the first example */
			
			cluster = getClusterForId(viId);
			
			/* if it is not part of a cluster assign it to a new one */
			
			if(cluster == null) {
				
				cluster = new DefaultCluster(String.valueOf(++clusterID));
				
				cluster.addObject(viId);
						
				clusterModel.addCluster(cluster);
			}

			for (int j = i + 1; j < exampleSet.size(); j++) {

				//aPlusCount = aMinusCount = aQuestionCount = 0;
				
				globalAssignment[0] = globalAssignment[1] = globalAssignment[2] = 0;
				
				vjId = IdUtils.getIdFromExample(exampleSet.getExample(j));

				/* calculate the global assignment cases */

				for (int k = 0; k < fuzzyInputModels.size(); k++) {

					model = fuzzyInputModels.get(k);

					vi = model.getMembership(exampleSet.getExample(i));

					vj = model.getMembership(exampleSet.getExample(j));

					normalizedCase = filter(normalizedCasePlus(vi, vj));

					if (normalizedCase > 0)
					
						globalAssignment[0]++;
					else {

						if (normalizedCase < 0)
						
							globalAssignment[2]++;
						else
							
							globalAssignment[1]++;
					}

				}

				/* The second point is added to the same cluster if
				 * 		the global case is a+
				 * 		or the global case is a? and it is mapped to a+
				 * 		or the global vector is balanced and it is mapped to a+ 
				 */
				if(((balanced(globalAssignment) ||
						(maxIndex(globalAssignment) == 1)) && undecidableCaseMapping == UNDECIDABLE_CASE_TO_PLUS) ||
						maxIndex(globalAssignment) == 0) {
					
					/* Is the point part of a cluster */
					DefaultCluster clusterNew = getClusterForId(vjId);
					
					if (clusterNew != null) {
						
						/* if example belongs to another cluster -> merge> */
						
						if(!clusterNew.getId().equals(cluster.getId())) {
							
							cluster.addAll(clusterNew);
							clusterModel.removeCluster(clusterNew);
						}
					} else

						cluster.addObject(vjId);
				}	
			}
		}

		return clusterModel;
	}

	@Override
	public List<ParameterType> getParameterTypes() {
		
		List<ParameterType> parameterTypes = super.getParameterTypes();
		
		ParameterType type;
		
		type = new ParameterTypeDouble(PARAMETER_T,"The treshold value",0.0,1.0,0.1);
		type.setExpert(false);
		parameterTypes.add(type);
		
		type = new ParameterTypeDouble(PARAMETER_ALPHA,"The slope for the tanh-function",1.0,Double.MAX_VALUE,1.0);
		type.setExpert(false);
		parameterTypes.add(type);
		
		type = new ParameterTypeCategory(PARAMETER_UNDECIDABLE_CASE_MAPPING,"The mapping of the a? case of the global clustering",UNDECIDABLE_CASE_MAPPINGS,0);
		type.setExpert(false);
		parameterTypes.add(type);
		
		return parameterTypes;
	}

	@Override
	public Class<?>[] getInputClasses() {
		
		return new Class<?>[] { ExampleSet.class, FuzzyMembershipModel.class};
	}
	
	public void addInputModels() {
		
		IOContainer inputContainer = this.getInput();
		
		fuzzyInputModels = new ArrayList<FuzzyMembershipModel>();
		
		while(inputContainer.contains(inputModelsClass)) {
			
			try {
				fuzzyInputModels.add(inputContainer.remove(FuzzyMembershipModel.class));
				
			} catch (MissingIOObjectException e) {
				
				log("This Exception should never occur. Must be something wrong with the IOContainer implementation");
			}
		}		
	}
	
	public int c(Vector<Double> vi, Vector<Double> vj) throws OperatorException {
		
		if(vi.size() != vj.size())
			throw new OperatorException("Assingnment Vectors differ");
		
		int ret = -1;
		
		double maxTempValueI = 0.0;
		double maxTempValueJ = 0.0;
		
		for(int i = 0;i < vi.size(); i++) {
			
			if(vi.get(i) >= maxTempValueI && vj.get(i) >= maxTempValueJ) {
				
				ret = 1;
				maxTempValueI = vi.get(i);
				maxTempValueJ = vj.get(i);
			} else {
				
				if(vi.get(i) >= maxTempValueI) {
					
					ret = -1;
					maxTempValueI = vi.get(i);
				
				} else {
					
					if(vj.get(i) >= maxTempValueJ) {
						
						ret = -1;
						maxTempValueJ = vj.get(i);
					}
				}
			}
		}
		return ret;
	}
	
	public boolean balanced(Vector<Double> v) {
		
		double max = 0.0;
		
		int maxCount = 0;
		
		for(int i = 0;i < v.size(); i++) {
			
			if(v.get(i) > max) {
				
				/*new maximum*/
				maxCount = 1;
				
				max = v.get(i);
				
			} else {
				
				/* one component more has the maximum */
				if(v.get(i) == max) {
					
					maxCount++;
				}
			}
		}
		
		/* v is balanced if two or more components are maximum*/
		return (maxCount > 1);
	}
	
	public int maxIndex(int[] v) {
		
		int max = -1;
		
		int maxIndex = -1;
		
		for(int i = 0;i<v.length;i++) {
			
			if(v[i] > max) {
				
				max = v[i];
				
				maxIndex = i;
			}
		}
		
		return maxIndex;
	}
	
	public boolean balanced(int[] v) {
		
		int max = 0;
		
		int maxCount = 0;
		
		for(int i = 0;i < v.length; i++) {
			
			if(v[i] > max) {
				
				/*new maximum*/
				maxCount = 1;
				
				max = v[i];
				
			} else {
				
				/* one component more has the maximum */
				if(v[i] == max) {
					
					maxCount++;
				}
			}
		}
		
		/* v is balanced if two or more components are maximum*/
		return (maxCount > 1);
	}
	
	public int paCase(Vector<Double> vi, Vector<Double> vj) throws OperatorException {

		int cValue = c(vi, vj);

		if (cValue == 1 && (!(balanced(vi) || balanced(vj))))
			return 1;

		if (cValue == -1)
			return -1;

		return 0;
	}
	
	public double significance(Vector<Double> vi, Vector<Double> vj) {
		
		int kl = vi.size();
		
		double significance = 0.0;
		
		double klMinusOne = 1.0/(double)kl;
		
		for(int i = 0; i < kl; i++) {
			
			significance += Math.abs(vi.get(i) - klMinusOne) * Math.abs(vj.get(i) - klMinusOne);
		}
		
		return significance;
	}
	
	public double casePlus(Vector<Double> vi, Vector<Double> vj, int paCase) throws OperatorException {
		
		return (paCase * significance(vi, vj));
	}
	
	public double norm(double numberOfCluster, int caseOutcome) {
		
		switch(caseOutcome) {
		
		case 1:
			return (1.0 - (double)(1/numberOfCluster));
			
		case -1:
			return ((4.0 / (numberOfCluster * numberOfCluster)) + (3.0 / numberOfCluster));
			
		case 0:
			return 1;
			
		default:
			return 0;
		
		}
	}
	
	public double normalizedCasePlus(Vector<Double> vi, Vector<Double> vj) throws OperatorException {
		
		int paCase = paCase(vi, vj);
		
		return (casePlus(vi, vj, paCase) / norm(vi.size(),paCase));
	
	}
	
	public double filter(double normalizedCaseOutcome) {
		
		normalizedCaseOutcome = Math.tanh(alpha * normalizedCaseOutcome);
		
		if(Math.abs(normalizedCaseOutcome) <= this.treshold )
			return 0;
		
		return normalizedCaseOutcome;
	}
	
	public DefaultCluster getClusterForId(String exampleId) {
		
		Cluster cluster;
		
		for(int i = 0;i < clusterModel.getNumberOfClusters();i++) {
			
			cluster = clusterModel.getClusterAt(i);
			if(cluster.contains(exampleId))
				return (DefaultCluster)cluster;
		}
		
		return null;
	}
	
	private void setParameters() throws OperatorException{
		
		treshold = getParameterAsDouble(PARAMETER_T);
		
		alpha = getParameterAsDouble(PARAMETER_ALPHA);
		
		undecidableCaseMapping = getParameterAsInt(PARAMETER_UNDECIDABLE_CASE_MAPPING);
	}
}
