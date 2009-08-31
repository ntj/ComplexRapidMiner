package de.tud.inf.operator.fingerprints.gsc;

import java.util.ArrayList;
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

public class GscCosineSimilarity extends SimilarityAdapter implements ExampleBasedSimilarityMeasure{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5697316802104876027L;
	
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
		
		double sum = 0.0;
		double sum1 = 0.0;
		double sum2 = 0.0;
		for(int i = 0;i<x.getValues().length;i++) {
			for(int j = 0;j<x.getValues()[i].length;i++) {
				
				double v1 = x.getValues()[i][j];
				double v2 = y.getValues()[i][j];
				
				sum = sum + v1*v2;
				sum1 = sum1 + v1*v1;
				sum2 = sum2 + v2*v2;
			}
		}
		
		if(sum1 > 0 && sum2 > 0)
			return sum / (Math.sqrt(sum1)*Math.sqrt(sum2));
		return Double.NaN;
	}
}
