

package de.tud.inf.operator.mm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.tools.Ontology;

import de.tud.inf.operator.mm.util.ClusteringInfo;
import de.tud.inf.operator.mm.util.MetaConfig;
import de.tud.inf.operator.mm.util.SortingIndex;


/**
 * This class implements the Quality strategy.
 * 
 * {@link http://www.siam.org/proceedings/datamining/2008/dm08_71_fern.pdf}
 * 
 * @version $Revision$
 * @author Andre Jaehnig
 */
public class QualitySelector extends AbstractSelector {

   /************************************************************************************************
    * FIELDS
    ***********************************************************************************************/

   /** Column name with the selected flag. */
   private static final String QUALITY_COLUMN_NAME_SELECTED = "quality_selected";

   /** Column name with the order of the selection. */
   private static final String QUALITY_COLUMN_NAME_ORDER = "quality_order";

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
   public QualitySelector(OperatorDescription description) {
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
      
      int sampleSize = this.getParameterAsInt(PARAMETER_SAMPLE_SIZE);
      if (sampleSize < 1 || sampleSize > exampleSetSize) {
         throw new UserError(this, 116, new Object[] { PARAMETER_SAMPLE_SIZE, sampleSize });
      }
      this.logNote("Requested clustering sample size: " + sampleSize);

      // create attributes for the selection flag and for a general order of selection
      Attribute qualityOrderAttr = AttributeFactory.createAttribute(QUALITY_COLUMN_NAME_ORDER, Ontology.INTEGER);
      Attribute qualitySelectedAttr = AttributeFactory.createAttribute(QUALITY_COLUMN_NAME_SELECTED, Ontology.NOMINAL);
      exampleSet.getExampleTable().addAttribute(qualityOrderAttr);
      exampleSet.getExampleTable().addAttribute(qualitySelectedAttr);

      // add attribute to view
      exampleSet.getAttributes().setSpecialAttribute(qualityOrderAttr, QUALITY_COLUMN_NAME_ORDER);
      exampleSet.getAttributes().setSpecialAttribute(qualitySelectedAttr, QUALITY_COLUMN_NAME_SELECTED);

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

      // fill selection attributes
      counter = 0;
      Iterator<SortingIndex> sortedIt = sortedIndex.iterator();
      int index, orderIndex;
      while (sortedIt.hasNext()) {
         index = sortedIt.next().getIndex();
         orderIndex = exampleSetSize - counter - 1;
         example = exampleSet.getExample(index);
         
         // set the order attribute
         example.setValue(qualityOrderAttr, orderIndex);
         
         // set the selection attribute
         if (orderIndex < sampleSize) {
            example.setValue(qualitySelectedAttr, "true");
         }
         else {
            example.setValue(qualitySelectedAttr, "false");
         }
         
         counter++;
      }
      
      // write meta config
      mc.setSelectorFileName(selectorFileName);
      ClusteringInfo ci = new ClusteringInfo();
      ci.setInfoColumnName(QUALITY_COLUMN_NAME_ORDER);
      ci.setSelectedColumnName(QUALITY_COLUMN_NAME_SELECTED);
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
