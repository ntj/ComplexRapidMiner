
package de.tud.inf.operator.mm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.table.DataRow;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.tools.Ontology;

import de.tud.inf.operator.mm.util.ClusteringInfo;
import de.tud.inf.operator.mm.util.MetaConfig;
import de.tud.inf.operator.mm.util.SortingIndex;


/**
 * This class implements the Joint-Criterion strategy.
 * 
 * {@link http://www.siam.org/proceedings/datamining/2008/dm08_71_fern.pdf}
 * 
 * @version $Revision$
 * @author Andre Jaehnig
 */
public class JointCriterionSelector extends AbstractSelector {

   /************************************************************************************************
    * FIELDS
    ***********************************************************************************************/

   /** Column name with the indices of the selected order. */
   private static final String JOINT_CRITERION_COLUMN_NAME_ORDER = "jc_order";

   /** Column name with the selected flag. */
   private static final String JOINT_CRITERION_COLUMN_NAME_SELECTED = "jc_selected";

   /** Weighting-factor for the calculation. */
   private static final double ALPHA = 0.5d;

   /************************************************************************************************
    * GETTER & SETTER
    ***********************************************************************************************/

   /*
    * none
    */

   /************************************************************************************************
    * CONSTRUCTOR
    ***********************************************************************************************/

   /**
    * Constructor.
    * 
    * @param description
    */
   public JointCriterionSelector(OperatorDescription description) {
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
      // get example set
      ExampleSet exampleSet = this.getInput(ExampleSet.class);
      int exampleSetSize = exampleSet.size();
      this.logNote("Input example-set has " + exampleSetSize + " elements.");

      // get parameters
      String metaFileName = this.getParameterAsString(PARAMETER_META_FILENAME);
      String selectorFileName = this.getParameterAsString(PARAMETER_SELECTOR_FILENAME);
      MetaConfig mc = MetaConfig.load(metaFileName);
      String snmiColumnName = mc.getSnmiColumnName();
      String clusterColumnPrefix = mc.getClusteringColumnPrefix();
      
      int sampleSize = this.getParameterAsInt(PARAMETER_SAMPLE_SIZE);
      if (sampleSize < 1 || sampleSize > exampleSetSize) {
         throw new UserError(this, 116, new Object[] { PARAMETER_SAMPLE_SIZE, sampleSize });
      }
      this.logNote("Requested clustering sample size: " + sampleSize);

      // create attributes for the selection flag and for a general order of selection
      Attribute jcOrderAttr = AttributeFactory.createAttribute(JOINT_CRITERION_COLUMN_NAME_ORDER, Ontology.INTEGER);
      Attribute jcSelectedAttr = AttributeFactory.createAttribute(JOINT_CRITERION_COLUMN_NAME_SELECTED,
            Ontology.NOMINAL);
      exampleSet.getExampleTable().addAttribute(jcOrderAttr);
      exampleSet.getExampleTable().addAttribute(jcSelectedAttr);

      // add attribute to view
      exampleSet.getAttributes().setSpecialAttribute(jcOrderAttr, JOINT_CRITERION_COLUMN_NAME_ORDER);
      exampleSet.getAttributes().setSpecialAttribute(jcSelectedAttr, JOINT_CRITERION_COLUMN_NAME_SELECTED);

      // add dummy attribute-column (only to the table)
      Attribute workingSNMIAttr = AttributeFactory.createAttribute(WORKING_COLUMN_NAME + "1", Ontology.REAL);
      Attribute workingNMIAttr = AttributeFactory.createAttribute(WORKING_COLUMN_NAME + "2", Ontology.REAL);
      Attribute workingSumAttr = AttributeFactory.createAttribute(WORKING_COLUMN_NAME + "3", Ontology.REAL);
      exampleSet.getExampleTable().addAttribute(workingSNMIAttr);
      exampleSet.getExampleTable().addAttribute(workingNMIAttr);
      exampleSet.getExampleTable().addAttribute(workingSumAttr);

      // get a sorted iterator over the snmi-column of the nmi-csv-file
      Attribute snmiAttr = exampleSet.getAttributes().get(snmiColumnName);
      if (snmiAttr == null) {
         throw new UserError(this, 111, snmiColumnName);
      }
      List<SortingIndex> sortedIndex = new ArrayList<SortingIndex>(exampleSetSize);
      int counter = 0;
      Iterator<Example> it = exampleSet.iterator();
      Example example = null;
      while (it.hasNext()) {
         example = it.next();
         sortedIndex.add(new SortingIndex(Double.valueOf(example.getNumericalValue(snmiAttr)), counter));
         counter++;
      }
      Collections.sort(sortedIndex);

      // setting first selected element-index (the one with the highest snmi)
      int selectedIndex = sortedIndex.get(exampleSetSize - 1).getIndex();

      // a list with all (so far) selected indices
      List<Integer> selectedIndices = new LinkedList<Integer>();

      // fill selection attributes
      int order = 0;
      double sumSNMIs = 0.0d;
      double sumNMIs = 0.0d;
      double curSNMI, curNMI, newSumSNMIs, newSumNMIs;
      DataRow selectedRow = null;
      DataRow curRow = null;
      for (int i = 0; i < exampleSetSize; i++) {
         for (int j = 0; j < exampleSetSize; j++) {
            if (selectedIndices.contains(j)) {
               // not relevant anymore
               continue;
            }

            curRow = exampleSet.getExampleTable().getDataRow(j);
            curSNMI = curRow.get(snmiAttr);

            curNMI = 0.0d;
            for (Attribute attr : exampleSet.getAttributes()) {
               if (!attr.getName().startsWith(clusterColumnPrefix)) {
                  // not relevant
                  continue;
               }
               if (selectedIndices.contains(Integer.valueOf(attr.getName().substring(clusterColumnPrefix.length())))) {
                  curNMI += 1.0d - curRow.get(attr);
               }
            }

            newSumSNMIs = sumSNMIs + curSNMI;
            newSumNMIs = sumNMIs + curNMI;
            curRow.set(workingSNMIAttr, newSumSNMIs);
            curRow.set(workingNMIAttr, newSumNMIs);
            curRow.set(workingSumAttr, ALPHA * newSumSNMIs + (1.0d - ALPHA) * newSumNMIs);
         }

         // find maximum
         sortedIndex = new ArrayList<SortingIndex>(exampleSetSize);
         counter = 0;
         it = exampleSet.iterator();
         while (it.hasNext()) {
            example = it.next();
            sortedIndex.add(new SortingIndex(Double.valueOf(example.getNumericalValue(workingSumAttr)), counter));
            counter++;
         }
         Collections.sort(sortedIndex);

         // set new selected index
         selectedIndex = sortedIndex.get(exampleSetSize - 1).getIndex();
         selectedIndices.add(selectedIndex);

         // get new basic values for the calculation
         selectedRow = exampleSet.getExampleTable().getDataRow(selectedIndex);
         sumSNMIs = selectedRow.get(workingSNMIAttr);
         sumNMIs = selectedRow.get(workingNMIAttr);

         // reset working values of this selected index so that they are no longer relevant
         selectedRow.set(workingSNMIAttr, Double.NEGATIVE_INFINITY);
         selectedRow.set(workingNMIAttr, Double.NEGATIVE_INFINITY);
         selectedRow.set(workingSumAttr, Double.NEGATIVE_INFINITY);

         // set ordering-index
         selectedRow.set(jcOrderAttr, order);

         // set selected flag
         if (order < sampleSize) {
            exampleSet.getExample(selectedIndex).setValue(jcSelectedAttr, "true");
         }
         else {
            exampleSet.getExample(selectedIndex).setValue(jcSelectedAttr, "false");
         }

         order++;
      }

      // remove working attributes
      exampleSet.getExampleTable().removeAttribute(workingSNMIAttr);
      exampleSet.getExampleTable().removeAttribute(workingNMIAttr);
      exampleSet.getExampleTable().removeAttribute(workingSumAttr);
      
      // write meta config
      mc.setSelectorFileName(selectorFileName);
      ClusteringInfo ci = new ClusteringInfo();
      ci.setInfoColumnName(JOINT_CRITERION_COLUMN_NAME_ORDER);
      ci.setSelectedColumnName(JOINT_CRITERION_COLUMN_NAME_SELECTED);
      ci.setSampleSize(sampleSize);
      mc.getClusteringInfo().put(this.getClass().getName(), ci);
      mc.save(metaFileName);

      return new IOObject[] { exampleSet };
   }

   /************************************************************************************************
    * PRIVATE METHODS
    ***********************************************************************************************/

   /*
    * none
    */

}
