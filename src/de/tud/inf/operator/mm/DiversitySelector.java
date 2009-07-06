

package de.tud.inf.operator.mm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
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
 * This class implements the Diversity strategy.
 * 
 * {@link http://www.siam.org/proceedings/datamining/2008/dm08_71_fern.pdf}
 * 
 * @version $Revision$
 * @author Andre Jaehnig
 */
public class DiversitySelector extends AbstractSelector {

   /************************************************************************************************
    * FIELDS
    ***********************************************************************************************/

   /** Column name with the selected flag. */
   private static final String DIVERSITY_COLUMN_NAME_SELECTED = "diversity_selected";

   /** Column name with the order of the selection. */
   private static final String DIVERSITY_COLUMN_NAME_ORDER = "diversity_order";

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
   public DiversitySelector(OperatorDescription description) {
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
      Attribute diversityOrderAttr = AttributeFactory.createAttribute(DIVERSITY_COLUMN_NAME_ORDER, Ontology.INTEGER);
      Attribute diversitySelectedAttr = AttributeFactory.createAttribute(DIVERSITY_COLUMN_NAME_SELECTED,
            Ontology.NOMINAL);
      exampleSet.getExampleTable().addAttribute(diversityOrderAttr);
      exampleSet.getExampleTable().addAttribute(diversitySelectedAttr);

      // add attributes to view
      exampleSet.getAttributes().setSpecialAttribute(diversityOrderAttr, DIVERSITY_COLUMN_NAME_ORDER);
      exampleSet.getAttributes().setSpecialAttribute(diversitySelectedAttr, DIVERSITY_COLUMN_NAME_SELECTED);

      // add dummy attribute-column (only to the table)
      Attribute workingAttr = AttributeFactory.createAttribute(WORKING_COLUMN_NAME, Ontology.REAL);
      exampleSet.getExampleTable().addAttribute(workingAttr);

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

      // fill selection attribute
      int order = 0;
      int workingColumnPos = 0;
      Example selectedExample = null;
      DataRow curWorkingRow = null;
      double oldValue, addValue;
      for (int i = 0; i < exampleSetSize; i++) {
         selectedExample = exampleSet.getExample(selectedIndex);

         // set ordering-index
         selectedExample.setValue(diversityOrderAttr, order);

         // set selected flag
         if (order < sampleSize) {
            selectedExample.setValue(diversitySelectedAttr, "true");
         }
         else {
            selectedExample.setValue(diversitySelectedAttr, "false");
         }

         /*
          * copy each nmi-value of this row to the working column (the 1st value of this row to the
          * 1st value of the working-column, the 2nd value of this row to the 2nd value of the
          * working-column, ...)
          */
         workingColumnPos = 0;
         for (Attribute attr : exampleSet.getAttributes()) {
            if (!attr.getName().startsWith(clusterColumnPrefix)) {
               // not relevant
               continue;
            }

            curWorkingRow = exampleSet.getExampleTable().getDataRow(workingColumnPos);
            if (workingColumnPos == selectedIndex) {
               // this value is not relevant anymore
               curWorkingRow.set(workingAttr, Double.POSITIVE_INFINITY);
            }
            else {
               // add nmi-value to working column
               oldValue = curWorkingRow.get(workingAttr);
               addValue = selectedExample.getNumericalValue(attr);
               curWorkingRow.set(workingAttr, oldValue + addValue);
            }

            workingColumnPos++;
         }

         // find minimum of the working column .. 
         sortedIndex = new ArrayList<SortingIndex>(exampleSetSize);
         counter = 0;
         it = exampleSet.iterator();
         while (it.hasNext()) {
            example = it.next();
            sortedIndex.add(new SortingIndex(Double.valueOf(example.getNumericalValue(workingAttr)), counter));
            counter++;
         }
         Collections.sort(sortedIndex);

         // .. and set new selected index for the next iteration
         selectedIndex = sortedIndex.get(0).getIndex();
         order++;
      }

      // remove working attribute
      exampleSet.getExampleTable().removeAttribute(workingAttr);
      
      // write meta config
      mc.setSelectorFileName(selectorFileName);
      ClusteringInfo ci = new ClusteringInfo();
      ci.setInfoColumnName(DIVERSITY_COLUMN_NAME_ORDER);
      ci.setSelectedColumnName(DIVERSITY_COLUMN_NAME_SELECTED);
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
