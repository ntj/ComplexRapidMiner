package de.tud.inf.operator.learner.clustering.clusterer;

import java.util.ArrayList;
import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.learner.clustering.IdUtils;
import com.rapidminer.operator.learner.clustering.clusterer.uncertain.DBScanEAClustering;
import com.rapidminer.operator.similarity.attributebased.uncertain.ProbabilityDensityFunction;
import com.rapidminer.tools.Ontology;


/**
 * extends DBSCAN^EA, takes attribute specific uncertainty regions
 * @author Antje Gruner
 *
 */
public class DBScanEAClusteringWithPdf extends DBScanEAClustering{

	public DBScanEAClusteringWithPdf(OperatorDescription description) {
		super(description);
	}

	
	@Override
	protected Double[][] getSamples(String id) {
		if(!sampleCache.containsKey(id)) {
			Example ex = IdUtils.getExampleFromId(es, id);
			//instead of using globalFuzziness, sample uncertain [correlated] values with corresponding pdf
			
			ProbabilityDensityFunction pdf;
			List<Double[][]> resList = new ArrayList<Double[][]>();
			
			for (Attribute att : ex.getAttributes())
				if(att.isComplex() && Ontology.ATTRIBUTE_VALUE_TYPE.isA(att.getValueType(),Ontology.UNCERTAIN)){
						pdf = ex.getUncertainValue(att);
						sampleStrategy.setPdf(pdf);
						sampleStrategy.setValue(pdf.getValue());
						resList.add(sampleStrategy.getSamples());
					}
				else{
				   //certain value ->  return sampleStrategy.sampleRate times the same point
				   //since attribute is not complex -> dimension = 1
				   int dim = 1;
				   Double[][] pDim = new Double[sampleStrategy.getSampleRate()][dim];
				   for(int i =0;i<pDim.length;i++)
					   pDim[i][0] = ex.getValue(att);
				   resList.add(pDim);
				}
			
			//finally put subdimensional sample points together
			Double[][] res = concatSubDimPoints(resList);
			sampleCache.put(id, res);
			return res;
		}
		return sampleCache.get(id);
	}
	

	/**
	 * concatenates a list of subdimensional sample points to full dimensional sample point
	 * @param list
	 * @return
	 */
	private Double[][] concatSubDimPoints(List<Double[][]> list) {
			//test whether sampleRate>=1
		   int dimPoint =0;
		   //fetch dimension
		   for(int i=0;i<list.size();i++)
			   //get dim. from first sample point (should be all the same)
			   if(list.get(i).length>0){
				   dimPoint += list.get(i)[0].length;
			   }
		   	
		   int sampleRate = list.get(0).length;
		   Double[][] res = new Double[sampleRate][dimPoint];
		
		   int count =0;
		   for(int i=0;i<list.size();i++){ //nr of list entries
			   for(int j=0;j<list.get(i).length;j++){ //sample rate
				   Double[] subPoint = list.get(i)[j];
				   System.arraycopy(subPoint, 0, res[j], count, subPoint.length);			   
			   }
			   count += list.get(i)[0].length;
		   }
		   return res;
		}
	
	
}
