package de.tud.inf.operator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.learner.clustering.Cluster;
import com.rapidminer.operator.learner.clustering.ClusterIterator;
import com.rapidminer.operator.learner.clustering.ClusterModel;
import com.rapidminer.operator.learner.clustering.IdUtils;
import com.rapidminer.operator.similarity.attributebased.uncertain.ProbabilityDensityFunction;
import com.rapidminer.tools.Ontology;
import com.rapidminer.tools.ParameterService;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import com.rapidminer.tools.math.similarity.numerical.EuclideanDistance;

import de.tud.inf.example.set.ComplexExampleSet;
import de.tud.inf.example.table.UncertainAttribute;


/**
 * implementation of the uncertain validity index (UCI)
 * assumes that all attributes are numeric, euclidean distance as distance measure
 * @author Antje Gruner
 * 
 */
public class UCIvalidityIndex extends Operator {

	private DistanceMeasure dist  = new EuclideanDistance();
	
	private boolean ignoreOutlierCluster = true;

	public UCIvalidityIndex(OperatorDescription description) {
		super(description);
		// TODO Auto-generated constructor stub
	}

	@Override
	public IOObject[] apply() throws OperatorException {
		System.out.println("[UCI.apply()]");
		ClusterModel cm1 = getInput(ClusterModel.class);
		ComplexExampleSet es = getInput(ComplexExampleSet.class);

		ClusterIterator cm1Iter = new ClusterIterator(cm1);
		double value = 0;
		int clCount =0;
		while (cm1Iter.hasMoreClusters()) {
			value += calculateClusterValue(cm1Iter.nextCluster(), es);
			clCount++;
		}
		if(ignoreOutlierCluster) clCount--;
		
		if(clCount!=0) value = value/clCount;
		else value = 0;
		try {
				printStatistics(this.getApplyCount(),Integer.toString(clCount) + "   " + Double.toString(value));
			} catch (IOException e) {
				e.printStackTrace();
			}

//		return new IOObject[] { new ClusterModelDistanceRepresentation(
	//				"index value",value ) };
			//TODO define class return value
			return null;
	}

	public void printStatistics(int id, String value) throws IOException{
		 
		 String path = ParameterService.getUserWorkspace().getAbsolutePath() +  "/performance/UCI/";
	     
	     String fName = this.getName() + ".stat";
	     	     
	     File pathToFiles = new File(path);
	     if(!pathToFiles.exists()) throw new IOException("path " + pathToFiles.getAbsolutePath() + "does not exist");
	     File file =  new File(path + fName);
	     BufferedWriter fw = null;
	     if (!file.exists() || id==1){
	    	 file.createNewFile();
	    	 fw =  new BufferedWriter(new FileWriter(file,false));
	    	 fw.write("it nrc  uci\n");
	     }
	     else
	    	 fw =  new BufferedWriter(new FileWriter(file,true));
	     fw.write(id + "  " + value  + "\n");
	     fw.close();
	     
	}
	
	
	/**
	 * calculates the robustness of convex hull of a cluster in clusterModel
	 * 
	 * @param c
	 * @return
	 */
	public double calculateClusterValue(Cluster c, ComplexExampleSet es) {
		if(c.getNumberOfObjects() != 0){
			Iterator<String> objects = c.getObjects();
			String obj;
			ProbabilityDensityFunction pdf;
			Example e;
			//initialize needed arrays
			double[] center = calculateClusterCenter(c, es);
			double[] value = new double[center.length];
			double[] bBox = new double[center.length];
			double oDist,bBDist;
			double cIndex =0;
			int dimIt; //the dimension counter (iterates over the dimension of an object (wrapped attributes + atomar attributes))
			while (objects.hasNext()) {
				dimIt =0;
				obj = objects.next();
				e = IdUtils.getExampleFromId(es, obj);
				//fill needed arrays
				for (Attribute a : es.getAttributes()) {
					if (Ontology.ATTRIBUTE_VALUE_TYPE.isA(a.getValueType(),	Ontology.UNCERTAIN)) {
						pdf = e.getUncertainValue(a);
						double[] pValue = e.getComplexValueAsArray(a);
						//fill value array with partial values
						for(int i=0;i<pValue.length;i++)
							value[i+dimIt] = pValue[i];
						//fill ucRegion bbox with values
						pdf = (ProbabilityDensityFunction)e.getComplexValue((UncertainAttribute)a);
						for(int i =0;i<pValue.length;i++)
							bBox[i+dimIt] = pdf.getMaxValue(i)- pdf.getMinValue(i);
						dimIt+=pValue.length;
					}
					else{
						if(!a.isNominal()){
							value[dimIt] = e.getValue(a);
							bBox[dimIt] =0;
							dimIt++;
						}
					}
				}
				//now compute index for object
				oDist = dist.calculateDistance(value,center);
				bBDist = dist.calculateDistance(bBox, new double[bBox.length]);
				//System.out.println("Distance:  " + oDist + "  BoundingBox Extent:  " + bBDist);
				cIndex += oDist*bBDist;
			}
			
			//normalize
			//System.out.println("Cluster value " + cIndex/c.getNumberOfObjects());
			return cIndex/c.getNumberOfObjects();
			
		}
		else return 0;
	}

	/**
	 * computes the average value of all representative points of uncertain data
	 * regions in one cluster, assumes that a representative point of a UC-region
	 * consists of all values of atomar attributes + inner attribute values of
	 * uncertain attributes
	 * 
	 * @param c
	 * @param es
	 * @return
	 */
	public double[] calculateClusterCenter(Cluster c, ComplexExampleSet es) {
		if(c.getNumberOfObjects() != 0){
			//determine dimension of center point (nr of certain attributes + inner attributes of uncertain attributes)
			double[] center = new double[es.getNrAtomarAttributes() + es.getNrWrappedAttributes()];
			Iterator<String> objects = c.getObjects();
			Example e;
			int it;
			//iterate over objects in cluster
			while (objects.hasNext()) {
				it =0;
				e = IdUtils.getExampleFromId(es, objects.next());
				//iterate over dimensions of one object
				for (Attribute a : es.getAttributes()) {
					//fetch exact dimension certain attributes + inner attributes of uncertain attributes and sum up values
					if (Ontology.ATTRIBUTE_VALUE_TYPE.isA(a.getValueType(),	Ontology.UNCERTAIN)) {
						double[] values = e.getComplexValueAsArray(a);
						for(int i =0;i<values.length;i++)
							center[it + i] += values[i];
						it += values.length;
					} else {
							// test whether attribute is special?(label attribute)
							if(!a.isNominal()){
								center[it] += e.getValue(a);
								it++;
							}
					}
				}
			}
			//calculate average of sum vector
			System.out.print("center: ");
			for(int i=0;i<center.length;i++){
				center[i] = center[i]/c.getNumberOfObjects();
				System.out.print(center[i] + "  ");
			}
			System.out.println("");
			return center;
		}
		return null;
	}
	
	@Override
	public Class<?>[] getInputClasses() {
		return new Class[] { ClusterModel.class };

	}
	
	@Override
	public Class<?>[] getOutputClasses() {
		//return new Class[] { ClusterModelDistanceRepresentation.class };
		return null;
	}

}
