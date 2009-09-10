package de.tud.inf.operator.fingerprints;



import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ValueDouble;

import com.rapidminer.operator.learner.clustering.Cluster;
import com.rapidminer.operator.learner.clustering.ClusterIterator;
import com.rapidminer.operator.learner.clustering.ClusterModel;


/**
 * This operator computes a distance measure of two ClusterModels as described by Gionis
 * @author Antje Gruner
 * @version $Id$
 */
public class ClusterModelDistanceGionis extends Operator{
	
	Integer distance; 
	
	public ClusterModelDistanceGionis(OperatorDescription description) {
		super(description);
		
		addValue(new ValueDouble("distance", "") {
			public double getDoubleValue() {
				return distance;
			}
		});
	}
	
	@Override
	public IOObject[] apply() throws OperatorException {
		ClusterModel cm1 = getInput(ClusterModel.class);
		ClusterModel cm2 = getInput(ClusterModel.class);
		distance = calculateDistance(cm1,cm2) + calculateDistance(cm2,cm1);
		 
		return new IOObject[]{};
	}
	
	private int calculateDistance(ClusterModel cm1, ClusterModel cm2){
		int dist = 0;
		ClusterIterator cm1Iter = new ClusterIterator(cm1);
		ClusterIterator cm2Iter = new ClusterIterator(cm2);
		Cluster c1, c2;
		List<ObjectPairs> objPairsList;
		ObjectPairs objPairs;
		while(cm1Iter.hasMoreClusters()){
			c1 = cm1Iter.nextCluster();
			objPairsList = computeObjectPairs(c1);
			for(int index = 0;index<objPairsList.size();index++){
				//fetch one key and all its pairObjects
				objPairs = objPairsList.get(index);
				//iterate over clusters of ClusterModel 2, if one cluster contains object key
				cm2Iter = new ClusterIterator(cm2);
				while(cm2Iter.hasNext()){
					c2 = cm2Iter.next();
					if(c2.contains(objPairs.key)){		
						//count numbers of pair object, which cluster c2 does NOT contain
						for(int o = 0;o<objPairs.values.size();o++)
							if(c2.contains(objPairs.values.get(o)));
							else dist++;
					}
				}
			}	
		}			
		return dist;
	}
	private List<ObjectPairs> computeObjectPairs(Cluster cm){
		Iterator<String> iter1 = cm.getObjects();
		Iterator<String> iter2;
		int count1 = 0;
		int count2 = 0;
		List<ObjectPairs> pairsList = new ArrayList<ObjectPairs>();
		String key;
		List<String> value = new ArrayList<String>();
		ObjectPairs objPairs;
		while(iter1.hasNext()){
			count1++;
			key = iter1.next();
			value  = new ArrayList<String>();
			count2 =0;
			iter2 = cm.getObjects();
			while(iter2.hasNext() && (count2<count1)) {
				iter2.next();
				count2++;
			}
			while(iter2.hasNext()){
				value.add(iter2.next());
			}
			if(value.size()>0){
				objPairs = new ObjectPairs(key,value);
				pairsList.add(objPairs);
			}
		}
		return pairsList;
	}
 	
	private class ObjectPairs{
		public String key;
		public List<String> values;
		
		public ObjectPairs(String key, List<String> values) {
			this.key = key;
			this.values = values;
		}
	}
	
	@Override
	public Class<?>[] getInputClasses() {
		return new Class[] {ClusterModel.class, ClusterModel.class};
	}

	@Override
	public Class<?>[] getOutputClasses() {
		return new Class[] {};
	}

}