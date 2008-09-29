package com.rapidminer.operator.learner.clustering.clusterer;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.OperatorCreationException;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.learner.clustering.ClusterModel;
import com.rapidminer.operator.learner.clustering.DefaultCluster;
import com.rapidminer.operator.learner.clustering.FlatClusterModel;
import com.rapidminer.operator.learner.clustering.FlatCrispClusterModel;
import com.rapidminer.operator.learner.clustering.IdUtils;
import com.rapidminer.operator.uncertain.AbstractPDFSampler;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.tools.OperatorService;

/**
 * Aggregates several clusterings into one. The strategy of this operator is
 * based on correlation clustering.
 * 
 * @author Peter B. Volk
 * @see com.rapidminer.operator.learner.clustering.clusterer.DBScanClustering
 * @see com.rapidminer.operator.learner.clustering.clusterer.uncertain.DBScanEAClustering
 * @see com.rapidminer.operator.learner.clustering.clusterer.uncertain.FDBScanClustering
 */
public class ClusteringAggregationWithUnvertainSampledElements extends
		AbstractFlatClusterer {

	private Vector<FlatCrispClusterModel> clusteringModels;
	private AbstractPDFSampler sampler;
	private IOContainer results;
	private AbstractClustering clust;
	private static final String NUM_THREADS = "Numer of cuncurrent threads";

	@Override
	public List<ParameterType> getParameterTypes() {
		if (sampler == null) {
			loadInternalOperators();
		}
		List<ParameterType> params = super.getParameterTypes();
		params.addAll(clust.getParameterTypes());
		params.addAll(sampler.getParameterTypes());
		ParameterType param = new ParameterTypeInt(NUM_THREADS,
				"Specifies how many threads may run concurrently.", 1, 300, 1);
		params.add(param);
		return params;
	}

	public ClusteringAggregationWithUnvertainSampledElements(
			OperatorDescription description) {
		super(description);

	}

	private void loadInternalOperators() {
		try {
			sampler = (AbstractPDFSampler) OperatorService
					.createOperator("PDFSampling");
			clust = (AbstractClustering) OperatorService
				.createOperator("DBScanClustering");
	
		} catch (OperatorCreationException e) {
			e.printStackTrace();
		}
	}

	public ClusterModel createClusterModel(ExampleSet es)
			throws OperatorException {
		if (es == null) {
			throw new OperatorException("Example set may not be null"
					+ Thread.currentThread().getName());
		}
		// initialize all components
		sampler.setParameters(getParameters());
		System.err.println("Starting sampling");
		IOContainer io = sampler.apply(new IOContainer(es));
		System.err.println("Completed sampling");
		results = new IOContainer();
		LinkedList<ClusteringThread> ll = new LinkedList<ClusteringThread>();
		IOObject[] obj =io.getIOObjects();
		int numExampleSetsPerThread =obj.length/getParameterAsInt(NUM_THREADS); 
		int j = 0;
		System.err.println("Distributing "+obj.length+" of es to "+getParameterAsInt(NUM_THREADS)+" threads");
		for (int i = 0; i < getParameterAsInt(NUM_THREADS); i++) {
			// create the threads
			AbstractClustering clusti;
			
				//create the workload for the threads
				ExampleSet[] objNew = null;
				if(i!=getParameterAsInt(NUM_THREADS)-1){
					objNew = new ExampleSet[numExampleSetsPerThread];
				}else{
					objNew = new ExampleSet[obj.length-numExampleSetsPerThread*i];
				}

				for(int k=0;k<objNew.length;j++){
					System.err.println("Thread "+i+" getting "+j);
					objNew[k] = (ExampleSet)obj[j];
					k++;
				}
				//create the threads
				if(objNew.length>0){
					clusti = new DBScanClustering(OperatorService.getOperatorDescriptions(DBScanClustering.class)[0]);
					clusti.setParameters(getParameters());
					ll.add(new ClusteringThread(objNew,(DBScanClustering) clusti));
				}
			 
		}
		System.err.println("Starting all threads");
		for (ClusteringThread ct : ll) {
			ct.start();
		}
		System.err.println("all Clusteringthreads started.");
		// block until everyone is done
		for (ClusteringThread ct : ll) {
			try {
				ct.join();
				System.err.println("Joining");
				results = results.append(ct.getResult().getIOObjects());
			} catch (InterruptedException e) {
				logError("Error: "+e.getMessage());
			}
		}
		System.err.println("completed clustering");
		clusteringModels = new Vector<FlatCrispClusterModel>();
				

		FlatCrispClusterModel result = new FlatCrispClusterModel();
		double edge;
		int uClusterId, vClusterId;
		int clusterCount = 0;
		int clustermodelSize =  clusteringModels.size();
		// Durchlaufen aller Paar-Kombinationen der Objekte u,v
		for (Example u : es) {
			for (Example v : es) {
				String uid = IdUtils.getIdFromExample(u);
				String vid = IdUtils.getIdFromExample(v);

				// Berechnung der Wahrscheinlichkeit, zusammengeclustert zu
				// werden
				edge = 0;
				for (int i = 0; i < clusteringModels.size(); i++) {
					uClusterId = getClusterId(uid, clusteringModels.get(i));
					vClusterId = getClusterId(vid, clusteringModels.get(i));
					if (uClusterId == vClusterId) { // hier wird Noise auch als
						// Cluster gesehen
						edge++;
					}
				}
				edge = edge / clustermodelSize;
				// logNote("(" + uid + ";" + vid + ") = " + edge);
				boolean uidIsInCluster = containsId(uid, result);
				boolean vidIsInCluster = containsId(vid, result);
				if (edge > 0.5) {
					if (!uidIsInCluster && !vidIsInCluster) {
						// beide in neues Cluster einfügen
						result.addCluster(new DefaultCluster(String	.valueOf(clusterCount)));
						
						((DefaultCluster) result.getClusterAt(clusterCount)).addObject(uid);
						((DefaultCluster) result.getClusterAt(clusterCount)).addObject(vid);
						clusterCount++;
						
					} else if ((uidIsInCluster && !vidIsInCluster)
								|| (!uidIsInCluster && vidIsInCluster)) {
						// ein Objekt ist bereits im ClusterModel result, das
						// andere nicht
						String a = uid;
						String b = vid;
						if (vidIsInCluster) {
							a = vid;
							b = uid;
						}
						// das nicht enthaltene Objekt zum Cluster des anderen
						// hinzufügen
						((DefaultCluster) result.getClusterAt(getClusterId(a,result))).addObject(b);
					} else if (uidIsInCluster&& vidIsInCluster) {// also wenn beide
						// bereits drin sind
						// UND sie sich in
						// verschiedenen
						// Clustern befinden
						int clusterIdUID = getClusterId(uid, result);
						int clusterIdVID = getClusterId(vid, result);
						if (clusterIdUID != clusterIdVID) {
							// beide Cluster mergen!
							DefaultCluster uCluster = (DefaultCluster) result.getClusterAt(clusterIdUID);
							DefaultCluster vCluster = (DefaultCluster) result.getClusterAt(clusterIdVID);
							uCluster.addAll(vCluster);
							result.removeCluster(vCluster);
							//resetClusterIds(result);
							clusterCount--; // weil es jetzt eines weniger
							
						}
						// TODO: Bei Löschen eines Clusters Bezeichnungen
						// erneuern
						// (die darin geführte Nummererierung stimmt nicht mehr)
					}
				}
			}
		}
		return result;
	}
	
	

	private void resetClusterIds(FlatCrispClusterModel cm) {
		for (int i = 0; i < cm.getNumberOfClusters(); i++) 
		{
			// TODO: resetClusterIds(FlatClusterModel cm)
			// Die Klasse Cluster bietet keine Möglichkeit im Nachhinein die
			// Bezeichner zu ändern.
		}
	}

	private int getClusterId(String id, FlatCrispClusterModel cm) {
		try{
			return Integer.valueOf(cm.getClusterById(id).getId());
		}catch(Exception e){
			return 0;
		}
	}

	private boolean containsId(String id, FlatClusterModel cm) {
		
		for (int i = 0; i < cm.getNumberOfClusters(); i++) {
			if (cm.getClusterAt(i).contains(id))
				return true;
		}
		return false;
	}

	public Class[] getInputClasses() {
		return new Class[] { ExampleSet.class };
	}

}
