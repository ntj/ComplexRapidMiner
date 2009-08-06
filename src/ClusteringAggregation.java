

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorCreationException;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.io.CSVExampleSource;
import com.rapidminer.operator.io.SimpleExampleSource;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeString;
import com.rapidminer.tools.Ontology;
import com.rapidminer.tools.OperatorService;

import de.tud.inf.operator.mm.util.MetaConfig;
import de.tud.inf.operator.mm.util.SortingIndex;


/**
 * 
 * 
 * @version $Revision$
 * @author Andre Jaehnig
 */
public class ClusteringAggregation extends Operator {

   /************************************************************************************************
    * FIELDS
    ***********************************************************************************************/

   /** Filename of the meta configuration file. */
   public static final String PARAMETER_META_FILENAME = "meta_filename";
   
   /** Filename of the aggregation file. */
   public static final String PARAMETER_AGGREGATION_FILENAME = "aggregation_filename";

   /** Name of the class that should be used for the selection. */
   public static final String PARAMETER_SELECTOR = "selector";

   /** Name of the column containing the aggregation information. */
   public static final String PARAMETER_AGGREGATION_COLUMN_NAME = "aggregation_column";

   /** Must be the same like {@link SimpleXMLExampleSource.PARAMETER_FILENAME} */
   public static final String PARAMETER_FILENAME = "filename";

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
      return new Class[0];
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
   public List<ParameterType> getParameterTypes() {
      List<ParameterType> types = super.getParameterTypes();

      types.add(new ParameterTypeString(PARAMETER_META_FILENAME, "Filename of the meta configuration file."));
      types.add(new ParameterTypeString(PARAMETER_AGGREGATION_FILENAME, "Filename of the aggregation file."));
      
      types.add(new ParameterTypeString(PARAMETER_SELECTOR,
            "Name of the class that should be used for the selection."));
      types.add(new ParameterTypeString(PARAMETER_AGGREGATION_COLUMN_NAME,
            "Name of the column containing the aggregation information.", "crAggr"));

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
   public ClusteringAggregation(OperatorDescription description) {
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
      // get parameters
      String metaFileName = this.getParameterAsString(PARAMETER_META_FILENAME);
      String aggregationFileName = this.getParameterAsString(PARAMETER_AGGREGATION_FILENAME);
      String selector = this.getParameterAsString(PARAMETER_SELECTOR);
      String aggregationColumnName = this.getParameterAsString(PARAMETER_AGGREGATION_COLUMN_NAME);
      
      MetaConfig mc = MetaConfig.load(metaFileName);
      String clusterColumnPrefix = mc.getClusteringColumnPrefix();
      String idColumnName = mc.getIdColumnName();
      String csvFileNameClustering = mc.getEnsembleFileName();
      String csvFileNameSelection = mc.getSelectorFileName();
      String selectionColumnName = mc.getClusteringInfo().get(selector).getSelectedColumnName();

      // read csv files and get example sets
      Operator csvReader = null;
      try {
         csvReader = OperatorService.createOperator(CSVExampleSource.class);
      }
      catch (OperatorCreationException oce) {
         throw new Error(oce.getMessage());
      }

      // first the clustering csv
      csvReader.setParameter(PARAMETER_FILENAME, csvFileNameClustering);
      csvReader.setParameter(CSVExampleSource.PARAMETER_ID_NAME, idColumnName);
      csvReader.setParameter(CSVExampleSource.PARAMETER_USE_COMMENT_CHARACTERS, "false");
      IOObject[] ioo = csvReader.apply();
      ExampleSet clusteringExampleSet = (ExampleSet) ioo[0];
      int elementCount = clusteringExampleSet.size();

      
      // and now the selection csv
      csvReader.setParameter(PARAMETER_FILENAME, csvFileNameSelection);
      csvReader.setParameter(CSVExampleSource.PARAMETER_ID_NAME, "");
      ioo = csvReader.apply();
      ExampleSet selectionExampleSet = (ExampleSet) ioo[0];
      int clusteringCount = selectionExampleSet.size();
          

      this.logNote("Input is: " + elementCount + " elements and " + clusteringCount + " different clusterings.");

      // read out which clusterings are important
      List<Integer> selectedClusteringIds = new ArrayList<Integer>();
      Attribute selectionAttr = selectionExampleSet.getAttributes().get(selectionColumnName);
      if (selectionAttr == null) {
         throw new Error("At the selection CSV is no such column ('" + selectionColumnName + "')");
      }
      Iterator<Example> it = selectionExampleSet.iterator();
      Example example = null;
      int counter = 0;
      while (it.hasNext()) {
         example = it.next();
         if (example.getNominalValue(selectionAttr).equalsIgnoreCase("true")) {
            selectedClusteringIds.add(counter);
         }
         counter++;
      }
      List<Attribute> selectedClusteringAttributes = new LinkedList<Attribute>();
      counter = 0;
      StringBuffer sb = new StringBuffer("Following clusterings are selected: ");
      for (Attribute attr : clusteringExampleSet.getAttributes()) {
         if (!attr.getName().startsWith(clusterColumnPrefix) && !attr.getName().equals(aggregationColumnName)) {
            // not relevant
            continue;
         }
         if (selectedClusteringIds.contains(counter)) {
            selectedClusteringAttributes.add(attr);
            sb.append(attr.getName() + " ");
         }
         counter++;
      }
      int selectedClusteringCount = selectedClusteringAttributes.size();
      this.logNote(sb.toString());

      // check if we have all selected attributes
      if (selectedClusteringIds.size() != selectedClusteringCount) {
         throw new Error("Error while retrieving all selected clustering attributes.");
      }
      this.logNote(selectedClusteringCount + " clusterings are selected.");

      // add new attribute to the clustering example set for the aggregation
      Attribute aggregationAttr = AttributeFactory.createAttribute(aggregationColumnName, Ontology.INTEGER);
      clusteringExampleSet.getExampleTable().addAttribute(aggregationAttr);
      clusteringExampleSet.getAttributes().setSpecialAttribute(aggregationAttr, aggregationColumnName);

      // set dummy value for this new attribute
      it = clusteringExampleSet.iterator();
      while (it.hasNext()) {
         it.next().setValue(aggregationAttr, Integer.MIN_VALUE);
      }

      // for each combination of two elements
      it = clusteringExampleSet.iterator();
      Iterator<Example> innerIt = null;
      Example innerExample = null;
      double baseWeight = 1.0d / (double) selectedClusteringCount;
      double weight;
      int curClusterCount = 0;
      Iterator<Example> reclusterIt = null;
      Example reclusterExample = null;
      while (it.hasNext()) { // u
         example = it.next();
         innerIt = clusteringExampleSet.iterator();
         while (innerIt.hasNext()) { // v
            innerExample = innerIt.next();
            if (innerExample.getId() >= example.getId()) {
               // do avoid check of v vs. u (because we already have u vs. v)
               break;
            }

            // calculate fraction of clusterings that places u and v in different clusters
            weight = 0.0d;

            // for each selected clustering
            for (Attribute attr : selectedClusteringAttributes) {
               // if in different cluster -> edge = edge + 1
               if ((int) example.getNumericalValue(attr) != (int) innerExample.getNumericalValue(attr)) {
                  weight++;
               }
            }
            weight *= baseWeight;

            // check if the edge between u and v is of interest
            if (weight <= 0.5) {
               Integer clusterId = (int) example.getNumericalValue(aggregationAttr);
               Integer innerClusterId = (int) innerExample.getNumericalValue(aggregationAttr);

               // if both are not in a cluster -> add a new one for this both
               if (clusterId == Integer.MIN_VALUE && innerClusterId == Integer.MIN_VALUE) {
                  example.setValue(aggregationAttr, curClusterCount);
                  innerExample.setValue(aggregationAttr, curClusterCount);
                  this.logNote("Added a new cluster (#" + curClusterCount + ")");
                  curClusterCount++;
               }
               // else if both are already in different clusters -> merge them
               else if (clusterId != Integer.MIN_VALUE && innerClusterId != Integer.MIN_VALUE) {
                  if (clusterId != innerClusterId) {
                     this.logNote("Merge cluster #" + innerClusterId + " into cluster #" + clusterId);
                     reclusterIt = clusteringExampleSet.iterator();
                     while (reclusterIt.hasNext()) {
                        reclusterExample = reclusterIt.next();
                        if ((int) reclusterExample.getNumericalValue(aggregationAttr) == innerClusterId) {
                           reclusterExample.setValue(aggregationAttr, clusterId);
                        }
                     }
                  }
               }
               // else if one of them is in a cluster -> add the other one to the same
               else {
                  if (clusterId == Integer.MIN_VALUE) {
                     example.setValue(aggregationAttr, innerClusterId);
                  }
                  else {
                     innerExample.setValue(aggregationAttr, clusterId);
                  }
               }
            }
         }
      }

      // rename cluster ids
      List<SortingIndex> sortedIndex = new ArrayList<SortingIndex>(elementCount);
      counter = 0;
      it = clusteringExampleSet.iterator();
      while (it.hasNext()) {
         example = it.next();
         sortedIndex.add(new SortingIndex(Double.valueOf(example.getNumericalValue(aggregationAttr)), counter));
         counter++;
      }
      Collections.sort(sortedIndex);

      // fill selection attributes
      counter = 0;
      Iterator<SortingIndex> sortedIt = sortedIndex.iterator();
      int clusterId = 0;
      int curClusterId;
      int prevClusterId = -1;
      while (sortedIt.hasNext()) {
         example = clusteringExampleSet.getExample(sortedIt.next().getIndex());
         curClusterId = (int) example.getNumericalValue(aggregationAttr);
         if (curClusterId == clusterId) {
            prevClusterId = curClusterId;
            continue;
         }
         else if (curClusterId == prevClusterId) {
            example.setValue(aggregationAttr, clusterId);
         }
         else {
            clusterId++;
            prevClusterId = curClusterId;
            example.setValue(aggregationAttr, clusterId);
         }
      }

      this.logNote("Aggregation contains " + (clusterId + 1) + " cluster.");
      
      // write meta config
      mc.setAggregationFileName(aggregationFileName);
      mc.setAggregationColumnName(aggregationColumnName);
      mc.setAggregationClusterCount(clusterId + 1);
      mc.setSelectorUsedForAggregation(selector);      
      mc.save(metaFileName);

      return new IOObject[] { clusteringExampleSet };
   }

   /************************************************************************************************
    * PRIVATE METHODS
    ***********************************************************************************************/

   /*
    * none
    */

}
