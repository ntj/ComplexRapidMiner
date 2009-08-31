package de.tud.inf.operator.fingerprints.gsc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.learner.clustering.IdUtils;
import com.rapidminer.operator.similarity.SimilarityAdapter;
import com.rapidminer.operator.similarity.attributebased.ExampleBasedSimilarityMeasure;

import de.tud.inf.example.set.attributevalues.ConstantArrayValue;
import de.tud.inf.example.table.ConstantArrayAttribute;

public class GscCosineSimilarityWithRot extends SimilarityAdapter implements ExampleBasedSimilarityMeasure{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2510264977810624068L;
	
	private Attribute fingerPrintAttr;
	private Set<String> ids;
	private ExampleSet es;

	
	public void init(ExampleSet exampleSet) throws OperatorException {
		ids = new HashSet<String>(exampleSet.size(),1.0f);
		this.es = exampleSet;
		fingerPrintAttr = exampleSet.getAttributes().get("fingerprint");
		if(!(fingerPrintAttr instanceof ConstantArrayAttribute))
			throw new OperatorException("SimilarityMeasure only definded for ConstantArrayAttribute");
		for(Example e : exampleSet)
			ids.add(IdUtils.getIdFromExample(e));
		
	}

	
	public double similarity(Example x, Example y) {
		return similarity((ConstantArrayValue)x.getComplexValue(fingerPrintAttr), (ConstantArrayValue)y.getComplexValue(fingerPrintAttr));
	}

	
	public Iterator<String> getIds() {
		return ids.iterator();
	}

	
	public int getNumberOfIds() {
		// TODO Auto-generated method stub
		return ids.size();
	}

	
	public boolean isDistance() {
		
		return false;
	}

	
	public boolean isSimilarityDefined(String x, String y) {
		return (ids.contains(x) && ids.contains(y));
	}

	
	public double similarity(String x, String y) {

		if(!isSimilarityDefined(x, y))
			return Double.NaN;
		Example ex = IdUtils.getExampleFromId(es, x);
		Example ey = IdUtils.getExampleFromId(es, y);
		
		return similarity((ConstantArrayValue)ex.getComplexValue(fingerPrintAttr), (ConstantArrayValue)ey.getComplexValue(fingerPrintAttr));
	}
	
	public double similarity(ConstantArrayValue x, ConstantArrayValue y) {
		
		double[][] beam1 = x.getValues();
		double[][] beam2 = y.getValues();
		
		int num = beam1.length;
		List<Double> distList = new ArrayList<Double>(num);
		
		for(int i = 0;i<num;i++) {
			double sum = 0.0;
			double sum1 = 0.0;
			double sum2 = 0.0;
			
			for(int j = 0;j<beam1.length;j++) {
				for(int k = 0;k<beam1[j].length;k++) {
					sum += beam1[j][k]*beam2[j][k];
					sum1 += beam1[j][k]*beam1[j][k];
					sum2 += beam2[j][k]*beam2[j][k];
				}
			}
			
			if(sum1 > 0 && sum2 >0)
				distList.add(sum / (Math.sqrt(sum1) * Math.sqrt(sum2)));
			else
				distList.add(Double.NaN);
			
			double[] temp = beam1[0];
			for(int j=1;j<num;j++)
				beam1[j-1] = beam1[j];
			beam1[num-1] = temp;
		}
		
		Collections.sort(distList);
		
		return distList.get(distList.size()-1);
	}

}
