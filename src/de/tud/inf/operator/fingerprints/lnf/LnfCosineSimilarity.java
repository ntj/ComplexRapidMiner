package de.tud.inf.operator.fingerprints.lnf;



import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.learner.clustering.IdUtils;
import com.rapidminer.operator.similarity.SimilarityAdapter;
import com.rapidminer.operator.similarity.attributebased.ExampleBasedSimilarityMeasure;

public class LnfCosineSimilarity extends SimilarityAdapter implements ExampleBasedSimilarityMeasure {

	private static final long serialVersionUID = 2353396887502782366L;
	private ExampleSet es = null;
	private Attribute simAttribute;
	private Set<String> ids;
	
	public void init(ExampleSet exampleSet) throws OperatorException {
		this.es = exampleSet;
		this.es.remapIds();
		
		simAttribute = es.getAttributes().get("Fingerprint");
		
		ids = new HashSet<String>();
		Iterator<Example> er = es.iterator();
		while (er.hasNext())
			ids.add(IdUtils.getIdFromExample(er.next()));	
	}

	public double similarity(Example x, Example y) {
		return distance(x.getNominalValue(simAttribute), y.getNominalValue(simAttribute));
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
		if (ids.contains(x) && ids.contains(y))
			return true;
		else
			return false;
	}

	public double similarity(String x, String y) {
		if (!isSimilarityDefined(x, y))
			return java.lang.Double.NaN;
		Example ex = IdUtils.getExampleFromId(es, x);
		Example ey = IdUtils.getExampleFromId(es, y);

		return distance(ex.getNominalValue(simAttribute), ey.getNominalValue(simAttribute));
	}
	
	public double distance(String e1, String e2)
	{
		StringTokenizer string1 = new StringTokenizer(e1, "#");
		StringTokenizer string2 = new StringTokenizer(e2, "#");
			
		String[] s1 = string1.nextToken().split("!");;
		String[] s2 = string2.nextToken().split("!");
		
		double sum = 0;
		double sumS1 = 0;
		double sumS2 = 0;
		
		boolean s1Token = true;
		boolean s2Token = true;
		while (string1.hasMoreTokens() || string2.hasMoreTokens() || (s1Token == true) || (s2Token == true)) {
			if ((string1.hasMoreTokens() == false) && (s1Token == false)) {
				sumS2 += Math.pow(Double.valueOf(s2[1]),2);
				if (string2.hasMoreTokens())
					s2 = string2.nextToken().split("!");
				else
					s2Token = false;
			}
			else if ((string2.hasMoreTokens() == false) && (s2Token == false)) {
				sumS1 += Math.pow(Double.valueOf(s1[1]),2);
				if (string1.hasMoreTokens())
					s1 = string1.nextToken().split("!");
				else
					s1Token = false;
			}
			// equal
			else if (s1[0].equals(s2[0])) {
				sum += Double.valueOf(s1[1]) * Double.valueOf(s2[1]);
				sumS1 += Math.pow(Double.valueOf(s1[1]),2);
				sumS2 += Math.pow(Double.valueOf(s2[1]),2);
				if (string1.hasMoreTokens())
					s1 = string1.nextToken().split("!");
				else
					s1Token = false;
				if (string2.hasMoreTokens())
					s2 = string2.nextToken().split("!");
				else
					s2Token = false;
			}
			// s1 is smaller
			else if (s1[0].compareTo(s2[0]) < 0) {
				sumS1 += Math.pow(Double.valueOf(s1[1]),2);
				if (string1.hasMoreTokens())
					s1 = string1.nextToken().split("!");
				else
					s1Token = false;
			}
			// s1 is greater
			else if (s1[0].compareTo(s2[0]) > 0) {
				sumS2 += Math.pow(Double.valueOf(s2[1]),2);
				if (string2.hasMoreTokens())
					s2 = string2.nextToken().split("!");
				else
					s2Token = false;
			}
		}
		return sum / Math.sqrt(sumS1 * sumS2);
	}

}