

package de.tud.inf.operator.mm;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.table.DataRow;
import com.rapidminer.example.table.DataRowFactory;
import com.rapidminer.example.table.MemoryExampleTable;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorCreationException;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.tools.Ontology;
import com.rapidminer.tools.OperatorService;
import com.rapidminer.tools.att.AttributeSet;

import de.tud.inf.operator.mm.util.ClusteringInfo;
import de.tud.inf.operator.mm.util.MetaConfig;


/**
 * This class implements the Convex-Hull strategy.
 * 
 * {@link http://www.siam.org/proceedings/datamining/2008/dm08_71_fern.pdf}
 * 
 * @version $Revision$
 * @author Andre Jaehnig
 */
public class ConvexHullSelector extends AbstractSelector {

   /************************************************************************************************
    * FIELDS
    ***********************************************************************************************/

   /** Column name for the indicator which clustering is selected. */
   private static final String CH_COLUMN_NAME_SELECTED = "ch_selected";

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
   public ConvexHullSelector(OperatorDescription description) {
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

      // create attribute for the order of clusterings
      Attribute chSelectedAttr = AttributeFactory.createAttribute(CH_COLUMN_NAME_SELECTED, Ontology.NOMINAL);
      exampleSet.getExampleTable().addAttribute(chSelectedAttr);

      // add attribute to view
      exampleSet.getAttributes().setSpecialAttribute(chSelectedAttr, CH_COLUMN_NAME_SELECTED);

      /*
       * create new example set with the points for the convex hull computation
       */
      // create attributes for each clustering
      AttributeSet attributeSet = new AttributeSet();
      Attribute attrCi = AttributeFactory.createAttribute("Ci", Ontology.STRING);
      Attribute attrCj = AttributeFactory.createAttribute("Cj", Ontology.STRING);
      Attribute attrNMI = AttributeFactory.createAttribute("NMI", Ontology.REAL);
      Attribute attrAvgSNMI = AttributeFactory.createAttribute("AvgSNMI", Ontology.REAL);
      attributeSet.setSpecialAttribute("Ci", attrCi);
      attributeSet.setSpecialAttribute("Cj", attrCj);
      attributeSet.addAttribute(attrNMI);
      attributeSet.addAttribute(attrAvgSNMI);

      // get snmi-column-attribute of the nmi-csv-file
      Attribute snmiAttr = exampleSet.getAttributes().get(snmiColumnName);
      if (snmiAttr == null) {
         throw new UserError(this, 111, snmiColumnName);
      }

      // create table for the output
      MemoryExampleTable table = new MemoryExampleTable(attributeSet.getAllAttributes());
      DataRowFactory drf = new DataRowFactory(DataRowFactory.TYPE_DOUBLE_ARRAY, '.');

      // create convex hull calculator example set
      ExampleSet convexHullExampleSet = table.createExampleSet(attributeSet);

      Object[] values = new Object[4];
      int counter;
      int rowNum = 0;
      Example exampleI;
      Example exampleJ;
      Example example;
      double snmiI, snmiJ, nmi;
      DataRow row = null;
      for (int i = 0; i < exampleSetSize; i++) {
         for (int j = 0; j < exampleSetSize; j++) {
            if (j >= i) {
               break;
            }
            counter = 0;

            // get NMI and SNMI values
            exampleI = exampleSet.getExample(i);
            exampleJ = exampleSet.getExample(j);
            snmiI = exampleI.getValue(snmiAttr);
            snmiJ = exampleJ.getValue(snmiAttr);
            nmi = exampleI.getValue(exampleSet.getAttributes().get(clusterColumnPrefix + j));

            values[counter++] = nmi;
            values[counter++] = (snmiI + snmiJ) / 2.0d;

            // add values to the table
            row = drf.create(values, table.getAttributes());
            table.addDataRow(row);

            // add special values
            example = convexHullExampleSet.getExample(rowNum++);
            example.setValue(attrCi, clusterColumnPrefix + i);
            example.setValue(attrCj, clusterColumnPrefix + j);
         }
      }

      // run convex hull calculator
      Operator chCalculator = null;
      try {
         chCalculator = OperatorService.createOperator(ConvexHullCalculator.class);
      }
      catch (OperatorCreationException e) {
         throw new UserError(this, 109);
      }
      chCalculator.apply(new IOContainer(convexHullExampleSet));

      // extract those clustering names/ids that are part of the convex hull
      Attribute selectedAttr = convexHullExampleSet.getAttributes().getSpecial(
            ConvexHullCalculator.CONVEX_HULL_MEMBER_COLUMN_NAME);
      Iterator<Example> it = convexHullExampleSet.iterator();
      Set<Integer> selectedClusteringIds = new HashSet<Integer>();
      String clusteringName1, clusteringName2;
      while (it.hasNext()) {
         example = it.next();
         if (example.getNominalValue(selectedAttr).equalsIgnoreCase("true")) {
            clusteringName1 = example.getNominalValue(attrCi);
            clusteringName2 = example.getNominalValue(attrCj);
            selectedClusteringIds.add(Integer.valueOf(clusteringName1.substring(clusterColumnPrefix.length())));
            selectedClusteringIds.add(Integer.valueOf(clusteringName2.substring(clusterColumnPrefix.length())));
         }
      }
      this.logNote("Sample size will be: " + selectedClusteringIds.size());

      // mark the selected clusterings at the the output example set
      it = exampleSet.iterator();
      counter = 0;
      while (it.hasNext()) {
         example = it.next();
         if (selectedClusteringIds.contains(counter)) {
            // selected
            example.setValue(chSelectedAttr, "true");
         }
         else {
            // not selected
            example.setValue(chSelectedAttr, "false");
         }

         counter++;
      }
      
      // write meta config
      mc.setSelectorFileName(selectorFileName);
      ClusteringInfo ci = new ClusteringInfo();
      ci.setSelectedColumnName(CH_COLUMN_NAME_SELECTED);
      ci.setSampleSize(selectedClusteringIds.size());
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
