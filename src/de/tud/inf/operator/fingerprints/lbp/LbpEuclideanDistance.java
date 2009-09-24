package de.tud.inf.operator.fingerprints.lbp;



import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.learner.clustering.IdUtils;
import com.rapidminer.operator.similarity.SimilarityAdapter;
import com.rapidminer.operator.similarity.attributebased.ExampleBasedSimilarityMeasure;

import de.tud.inf.example.set.attributevalues.DataMapValue;

public class LbpEuclideanDistance extends SimilarityAdapter implements ExampleBasedSimilarityMeasure {

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
		return euclideanDistance(x.getNominalValue(simAttribute), y.getNominalValue(simAttribute));
	}

	public Iterator<String> getIds() {
		return ids.iterator();
	}

	public int getNumberOfIds() {
		return ids.size();
	}

	public boolean isDistance() {
		return true;
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

		double dist;
		if (ex.getId() == ey.getId())
			dist = 0;
		else
			dist = euclideanDistance(ex.getDataMapValue(simAttribute), ey.getDataMapValue(simAttribute));
		//System.out.println(x + "-" + y + ": " + dist);
		return dist;
	}
	
	public double euclideanDistance(String e1, String e2)
	{
		StringTokenizer string1 = new StringTokenizer(e1, "#");
		StringTokenizer string2 = new StringTokenizer(e2, "#");
			
		String[] s1 = string1.nextToken().split("!");;
		String[] s2 = string2.nextToken().split("!");
		double dist = 0;
		boolean s1Token = true;
		boolean s2Token = true;
		int x, y;
		while (string1.hasMoreTokens() || string2.hasMoreTokens() || (s1Token == true) || (s2Token == true)) {
			x = Integer.valueOf(s1[0]);
			y = Integer.valueOf(s2[0]);
			
			if ((string1.hasMoreTokens() == false) && (s1Token == false)) {
				dist = dist + Math.pow(Double.valueOf(s2[1]), 2);
				if (string2.hasMoreTokens())
					s2 = string2.nextToken().split("!");
				else
					s2Token = false;
			}
			else if ((string2.hasMoreTokens() == false) && (s2Token == false)) {
				dist = dist + Math.pow(Double.valueOf(s1[1]), 2);
				if (string1.hasMoreTokens())
					s1 = string1.nextToken().split("!");
				else
					s1Token = false;
			}
			// equal
			else if (x == y) {
				dist = dist + Math.pow(Double.valueOf(s1[1]) - Double.valueOf(s2[1]), 2);
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
			else if (x < y) {
				dist = dist + Math.pow(Double.valueOf(s1[1]), 2);
				if (string1.hasMoreTokens())
					s1 = string1.nextToken().split("!");
				else
					s1Token = false;
			}
			// s1 is greater
			else if (x > y) {
				dist = dist + Math.pow(Double.valueOf(s2[1]), 2);
				if (string2.hasMoreTokens())
					s2 = string2.nextToken().split("!");
				else
					s2Token = false;
			}
		}
		return Math.sqrt(dist);
	}
	
	public double euclideanDistance(DataMapValue e1, DataMapValue e2)
	{	
		double dist = 0;
		//get maps
		Map<Integer,Double> map1 = e1.getMap();
		Map<Integer,Double> map2 = e2.getMap();
		Iterator<Integer> keys1 = map1.keySet().iterator();
		Iterator<Integer> keys2 = map2.keySet().iterator();
		//TODO: ensure that there is at least one tuple in map
		Integer key1 = keys1.next();
		Integer key2 = keys2.next();
		boolean s1Token = true;
		boolean s2Token = true;
		while (keys1.hasNext() || keys2.hasNext() || (s1Token == true) || (s2Token == true)) {
			if ((!keys1.hasNext()) && (s1Token == false)) {
				dist = dist + Math.pow(map2.get(key2), 2);
				if (keys2.hasNext())
					key2 = keys2.next();
				else
					s2Token = false;
			}
			else if ((!keys2.hasNext()) && (s2Token == false)) {
				dist = dist + Math.pow(map1.get(key1), 2);
				if (keys1.hasNext())
					key1 = keys1.next();
				else
					s1Token = false;
			}
			// equal
			else if (key1.equals(key2)) {
				dist = dist + Math.pow(map1.get(key1) - map2.get(key2), 2);
				if (keys1.hasNext())
					key1 = keys1.next();
				else
					s1Token = false;
				if (keys2.hasNext())
					key2 = keys2.next();
				else
					s2Token = false;
			}
			// s1 is smaller
			else if (key1.compareTo(key2) < 0) {
				dist = dist + Math.pow(map1.get(key1), 2);
				if (keys1.hasNext())
					key1 = keys1.next();
				else
					s1Token = false;
			}
			// s1 is greater
			else if (key1.compareTo(key2) > 0) {
				dist = dist + Math.pow(map2.get(key2), 2);
				if (keys2.hasNext())
					key2 = keys2.next();
				else
					s2Token = false;
			}
		}
		return Math.sqrt(dist);
	}

}
