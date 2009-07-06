
package de.tud.inf.operator.mm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import Jama.Matrix;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.table.DataRowFactory;
import com.rapidminer.example.table.MemoryExampleTable;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeString;
import com.rapidminer.tools.Ontology;
import com.rapidminer.tools.att.AttributeSet;

import de.tud.inf.operator.mm.util.MetaConfig;


/**
 * This class computes a NMI matrix of the given clusterings.
 * 
 * @version $Revision$
 * @author Andre Jaehnig
 */
public class MutualInformationCalculator extends Operator {

   /************************************************************************************************
    * FIELDS
    ***********************************************************************************************/

   /** Indicates if the mutual information values should be normalized. */
   public static final String PARAMETER_NORMALIZE = "normalize";

   /** Indicates if an additional column with the sum of the row should be added. */
   public static final String PARAMETER_ADD_SNMI = "add_snmi";

   /** Column name for the sum of normalized mutual informations. */
   public static final String SNMI_COLUMN_NAME = "snmi";
   
   /** Filename of the meta configuration file. */
   public static final String PARAMETER_META_FILENAME = "meta_filename";
   
   /** Filename of the nmi file. */
   public static final String PARAMETER_NMI_FILENAME = "nmi_filename";

   /************************************************************************************************
    * GETTER & SETTER
    ***********************************************************************************************/
   /*
    * (non-Javadoc)
    * 
    * @see com.rapidminer.operator.Operator#getInputClasses()
    */
   @Override
   public Class<?>[] getInputClasses() {
      return new Class[] { ExampleSet.class };
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.rapidminer.operator.Operator#getOutputClasses()
    */
   @Override
   public Class<?>[] getOutputClasses() {
      return new Class[] { ExampleSet.class };
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.rapidminer.operator.Operator#getParameterTypes()
    */
   @Override
   public List<ParameterType> getParameterTypes() {
      List<ParameterType> types = super.getParameterTypes();
      types.add(new ParameterTypeBoolean(PARAMETER_NORMALIZE,
            "Indicates if the mutual information values should be normalized.", true));
      types
            .add(new ParameterTypeBoolean(
                  PARAMETER_ADD_SNMI,
                  "Indicates if an additional column with the sum of the row should be added. (If this value is true all values will be normalized regardless of the normalized-value.)",
                  true));
      types.add(new ParameterTypeString(PARAMETER_META_FILENAME, "Filename of the meta configuration file."));
      types.add(new ParameterTypeString(PARAMETER_NMI_FILENAME, "Filename of the nmi file."));

      
      return types;
   }

   /************************************************************************************************
    * CONSTRUCTOR
    ***********************************************************************************************/

   /**
    * Constructor.
    * 
    * @param description
    */
   public MutualInformationCalculator(OperatorDescription description) {
      super(description);
   }

   /************************************************************************************************
    * PUBLIC METHODS
    ***********************************************************************************************/

   /*
    * (non-Javadoc)
    * 
    * @see com.rapidminer.operator.Operator#apply()
    */
   @Override
   public IOObject[] apply() throws OperatorException {
      // get input example set
      ExampleSet clusterExampleSet = this.getInput(ExampleSet.class);
      int elementCount = clusterExampleSet.size();
      this.logNote("Input example-set has " + elementCount + " elements.");

      // read out parameters
      boolean normalizeValues = this.getParameterAsBoolean(PARAMETER_NORMALIZE);
      boolean addSnmiColumn = this.getParameterAsBoolean(PARAMETER_ADD_SNMI);
      String metaFileName = this.getParameterAsString(PARAMETER_META_FILENAME);
      String nmiFileName = this.getParameterAsString(PARAMETER_NMI_FILENAME);
      MetaConfig mc = MetaConfig.load(metaFileName);
      String clusterColumnPrefix = mc.getClusteringColumnPrefix();

      // create attributes for each clustering
      AttributeSet attributeSet = new AttributeSet();
      for (Attribute attribute : clusterExampleSet.getAttributes()) {
         String attrName = attribute.getName();
         if (attrName.startsWith(clusterColumnPrefix)) {
            attributeSet.addAttribute(AttributeFactory.createAttribute(attrName, Ontology.REAL));
         }
      }

      // create attribute for the sum of NMI (if desired)
      if (addSnmiColumn) {
         Attribute sumNMIAttr = AttributeFactory.createAttribute(SNMI_COLUMN_NAME, Ontology.REAL);
         attributeSet.addAttribute(sumNMIAttr);

         // adding this column implicates that all values will be normalized
         normalizeValues = true;
      }

      // create table for the output
      MemoryExampleTable table = new MemoryExampleTable(attributeSet.getAllAttributes());
      DataRowFactory drf = new DataRowFactory(DataRowFactory.TYPE_DOUBLE_ARRAY, '.');

      // create a list of cluster-id-assignments for each clustering
      this.logNote("Creating a map with cluster-id-assignments for each clustering.");
      Map<String, Map<Integer, List<Integer>>> clusterAssign = new LinkedHashMap<String, Map<Integer, List<Integer>>>();
      Iterator<Example> it = clusterExampleSet.iterator();
      Attribute idAttr = clusterExampleSet.getAttributes().getId();
      // iterate through all elements
      while (it.hasNext()) {
         Example example = it.next();

         // get element id
         int id = (int) example.getValue(idAttr);

         // iterate through all clustering attributes
         for (Attribute attribute : clusterExampleSet.getAttributes()) {
            String attrName = attribute.getName();
            if (!attrName.startsWith("cr")) {
               // not relevant
               continue;
            }

            // get map of cluster-id-assignments for the current clustering
            Map<Integer, List<Integer>> clusters = clusterAssign.get(attrName);
            if (clusters == null) {
               // add new map for this clustering
               clusters = new HashMap<Integer, List<Integer>>();
               clusterAssign.put(attrName, clusters);
            }

            // get id list for the current cluster number
            int clusterId = (int) example.getValue(attribute);
            List<Integer> idList = clusters.get(clusterId);
            if (idList == null) {
               // add new list for this cluster
               idList = new ArrayList<Integer>();
               clusters.put(clusterId, idList);
            }

            // add id to the id-list of the cluster
            idList.add(id);
         }
      }

      // calculate entropy for each cluster result
      this.logNote("Calculating the entropy for each clustering.");
      Map<String, Double> entropies = new HashMap<String, Double>();
      double entropy;
      double idListSize;
      for (Entry<String, Map<Integer, List<Integer>>> entry : clusterAssign.entrySet()) {
         entropy = 0.0d;
         // iterate through clusters
         for (List<Integer> idList : entry.getValue().values()) {
            idListSize = (double) idList.size();
            entropy += idListSize * Math.log(idListSize / (double) elementCount);
         }
         entropies.put(entry.getKey(), entropy);
      }
      int clusteringCount = entropies.size();

      /*
       * calculate mutual information for each combination of 2 clusterings
       * 
       * because of the instability of the java mathematics I decided to store the values at a
       * matrix first (to easily build a symmetric matrix) and afterwards put them into the example
       * set
       */
      this.logNote("Calculating the mutual information for each combination of 2 clusterings.");
      Matrix temporaryMIMatrix = new Matrix(clusteringCount, clusteringCount);
      int sizeA, sizeB, commonIds;
      Integer firstA, lastA, firstB, lastB;
      double mutualInformation;
      int clusteringNumA = 0;
      int clusteringNumB = 0;
      for (String keyA : clusterAssign.keySet()) {
         clusteringNumB = 0;
         for (String keyB : clusterAssign.keySet()) {
            if (clusteringNumB > clusteringNumA) {
               break;
            }
            // iterate through the cluster-combinations of A and B
            mutualInformation = 0.0d;
            for (List<Integer> idListA : clusterAssign.get(keyA).values()) {
               sizeA = idListA.size();
               firstA = idListA.get(0);
               lastA = idListA.get(sizeA - 1);
               for (List<Integer> idListB : clusterAssign.get(keyB).values()) {
                  // get number of elements that both clusters have in common (ids are ordered)

                  // first check if they even have common elements
                  sizeB = idListB.size();
                  firstB = idListB.get(0);
                  lastB = idListB.get(sizeB - 1);

                  if (lastA < firstB || firstA > lastB) {
                     // no they don't
                     commonIds = 0;
                  }
                  else {
                     // they have
                     Set<Integer> dummySet = new HashSet<Integer>(idListA);
                     dummySet.addAll(idListB);
                     commonIds = sizeA + sizeB - dummySet.size();
                  }

                  if (commonIds == 0) {
                     // mutual information = 0.0
                     continue;
                  }

                  mutualInformation += (double) commonIds
                                      * Math.log((double) (elementCount * commonIds) / (double) (sizeA * sizeB));
               }
            }

            if (normalizeValues) {
               mutualInformation = mutualInformation / Math.sqrt(entropies.get(keyA) * entropies.get(keyB));
            }

            // store value at matrix (symmetric matrix)
            temporaryMIMatrix.set(clusteringNumA, clusteringNumB, mutualInformation);
            temporaryMIMatrix.set(clusteringNumB, clusteringNumA, mutualInformation);

            clusteringNumB++;
         }

         clusteringNumA++;
      }

      // copy matrix values into example set
      this.logNote("Copy MI-values into output example set.");
      Double[] values = new Double[attributeSet.getAllAttributes().size()];
      double mutualInformationSum;
      int counter;
      for (int row = 0; row < clusteringCount; row++) {
         mutualInformationSum = 0.0d;
         counter = 0;
         for (int col = 0; col < clusteringCount; col++) {
            double value = temporaryMIMatrix.get(row, col);
            values[counter++] = value;
            if (row != col) {
               mutualInformationSum += value;
            }
         }

         if (addSnmiColumn) {
            values[counter++] = mutualInformationSum;
         }

         // add values to the table
         table.addDataRow(drf.create(values, table.getAttributes()));
      }

      // create output example set
      ExampleSet newExampleSet = table.createExampleSet(attributeSet);
      
      mc.setNmiFileName(nmiFileName);
      mc.setNmiNormalized(normalizeValues);
      mc.setSnmiAdded(addSnmiColumn);
      mc.setSnmiColumnName(SNMI_COLUMN_NAME);
      mc.save(metaFileName);

      return new IOObject[] { newExampleSet };
   }

   /************************************************************************************************
    * PRIVATE METHODS
    ***********************************************************************************************/

   /*
    * none
    */

}
