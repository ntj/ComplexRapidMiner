package com.rapidminer.operator.learner.clustering.clusterer;

import java.util.Iterator;
import java.util.Vector;

import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.learner.clustering.ClusterModel;
import com.rapidminer.operator.learner.clustering.DefaultCluster;
import com.rapidminer.operator.learner.clustering.FlatClusterModel;
import com.rapidminer.operator.learner.clustering.FlatCrispClusterModel;
import com.rapidminer.operator.learner.clustering.IdUtils;

/**
 * Aggregates several clusterings into one. The strategy of this
 * operator is based on correlation clustering.
 * 
 * @author Michael Huber
 * @see com.rapidminer.operator.learner.clustering.clusterer.DBScanClustering
 * @see com.rapidminer.operator.learner.clustering.clusterer.DBScanEAClustering
 * @see com.rapidminer.operator.learner.clustering.clusterer.FDBScanClustering
 */
public class ClusteringAggregation extends AbstractFlatClusterer {

	private Vector<FlatCrispClusterModel> clusteringModels;
	
	public ClusteringAggregation(OperatorDescription description) {
		super(description);
	}

	public ClusterModel createClusterModel(ExampleSet es)
			throws OperatorException {
		clusteringModels = new Vector<FlatCrispClusterModel>();
		IOContainer ioc = getInput();
		while(ioc.contains(ClusterModel.class)) {
			clusteringModels.add(ioc.get(FlatCrispClusterModel.class));
			ioc.remove(FlatCrispClusterModel.class);
		}
		
		FlatCrispClusterModel result = new FlatCrispClusterModel();
		double edge;
		int uClusterId, vClusterId;
		int clusterCount = 0;
		
		//Durchlaufen aller Paar-Kombinationen der Objekte u,v
		for (Example u: es) {
			for (Example v: es) {
				String uid = IdUtils.getIdFromExample(u);
				String vid = IdUtils.getIdFromExample(v);

				//Berechnung der Wahrscheinlichkeit, zusammengeclustert zu werden
				edge = 0;
				for(int i = 0; i < clusteringModels.size(); i++) {
					uClusterId = getClusterId(uid, clusteringModels.get(i));
					vClusterId = getClusterId(vid, clusteringModels.get(i));
					if(uClusterId == vClusterId) { //hier wird Noise auch als Cluster gesehen
						edge++;
					}
				}
				edge = edge / clusteringModels.size();
				//logNote("(" + uid + ";" + vid + ") = " + edge);
				if(edge > 0.5) {
					if(!containsId(uid, result) && !containsId(vid, result)) {
						//beide in neues Cluster einfügen
						result.addCluster(new DefaultCluster("" + clusterCount));
						((DefaultCluster) result.getClusterAt(clusterCount)).addObject(uid);
						((DefaultCluster) result.getClusterAt(clusterCount)).addObject(vid);
						clusterCount++;
					} else if((containsId(uid, result) && !containsId(vid, result)) ||
							(!containsId(uid, result) && containsId(vid, result))) {
						//ein Objekt ist bereits im ClusterModel result, das andere nicht
						String a = uid;
						String b = vid;
						if(containsId(vid, result)) {
							a = vid;
							b = uid;
						}
						//das nicht enthaltene Objekt zum Cluster des anderen hinzufügen
						((DefaultCluster) result.getClusterAt(getClusterId(a, result))).addObject(b);
					} else if (containsId(uid, result) && containsId(vid, result)) {//also wenn beide bereits drin sind UND sie sich in verschiedenen Clustern befinden
						if(getClusterId(uid, result) != getClusterId(vid, result)) {
							//beide Cluster mergen!
							DefaultCluster uCluster = (DefaultCluster)result.getClusterAt(getClusterId(uid, result));
							DefaultCluster vCluster = (DefaultCluster)result.getClusterAt(getClusterId(vid, result));
							String tempId;
							Iterator<String> it = vCluster.getObjects();
							while(it.hasNext()) {
								tempId = it.next();
								//vCluster.removeObject(tempId);
								uCluster.addObject(tempId);
							}
							result.removeCluster(vCluster);
							resetClusterIds(result);
							clusterCount--; //weil es jetzt eines weniger gibt...
						}
						//TODO: Bei Löschen eines Clusters Bezeichnungen erneuern
						//(die darin geführte Nummererierung stimmt nicht mehr)
					}
				}
			}
		}
		return result;
	}

	private void resetClusterIds(FlatClusterModel cm) {
		for(int i = 0; i < cm.getNumberOfClusters(); i++) {
			//TODO: resetClusterIds(FlatClusterModel cm)
			//Die Klasse Cluster bietet keine Möglichkeit im Nachhinein die Bezeichner zu ändern.
		}
	}
	
//	private int getNextUnusedClusterId(FlatClusterModel cm) {
//		return 0;
//	}

	
	private int getClusterId(String id, FlatClusterModel cm) {
		for(int i = 0; i < cm.getNumberOfClusters(); i++) {
			if(cm.getClusterAt(i).contains(id))
				return i;
		}
		return 0;
	}
	

//	private int getClusterId(String id, int clusterModelId) {
//		for(int i = 0; i < clusteringModels.get(clusterModelId).getNumberOfClusters(); i++) {
//			if(clusteringModels.get(clusterModelId).getClusterAt(i).contains(id))
//				return i;
//		}
//		return 0;
//	}


	private boolean containsId(String id, FlatClusterModel cm) {
		for(int i = 0; i < cm.getNumberOfClusters(); i++) {
			if(cm.getClusterAt(i).contains(id))
				return true;
		}
		return false;
	}
	
	public Class[] getInputClasses() {
		return new Class[] { ExampleSet.class, ClusterModel.class };
	}

}
