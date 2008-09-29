package com.rapidminer.operator.learner.clustering.clusterer;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.MissingIOObjectException;
import com.rapidminer.operator.OperatorException;

class ClusteringThread extends Thread {

	private ExampleSet[] conti;
	private IOContainer results;
	private DBScanClustering clust;
	private int i = 0;

	public ClusteringThread(ExampleSet[] conti, DBScanClustering clust) {
		this.conti = conti;
		this.clust = clust;
		results = new IOContainer();
	}

	private void addClusterModel(IOContainer container) {
		results = results.append(container.getIOObjects());
	}

	private ExampleSet getNextExampleSet() throws MissingIOObjectException {
		if (i < conti.length) {
			ExampleSet es;
			es = conti[i];
			i++;
			return es;
		}
		return null;
	}

	public void run() {
		try {
			ExampleSet es = null;
			while ((es = getNextExampleSet()) != null) {
			
				if(clust == null){
					System.err.println("clust is null");
				}
				if(es == null){
					System.err.println("es is null");
				}
				System.err.println("applying to "+clust.getIds());
				IOContainer io = clust.apply(new IOContainer(es));
				addClusterModel(io);
			}
		} catch (OperatorException e) {
			e.printStackTrace();
		}
	}

	public IOContainer getResult() {
		return results;
	}
}
