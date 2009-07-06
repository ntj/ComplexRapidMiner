

package de.tud.inf.operator.mm;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import Jama.EigenvalueDecomposition;
import Jama.Matrix;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.table.DataRowFactory;
import com.rapidminer.example.table.MemoryExampleTable;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorCreationException;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.learner.clustering.clusterer.KMeans;
import com.rapidminer.operator.preprocessing.IdTagging;
import com.rapidminer.tools.Ontology;
import com.rapidminer.tools.OperatorService;
import com.rapidminer.tools.att.AttributeSet;

import de.tud.inf.operator.mm.util.ClusteringInfo;
import de.tud.inf.operator.mm.util.MetaConfig;


/**
 * This class implements the Cluster-and-Select strategy.
 * 
 * {@link http://www.siam.org/proceedings/datamining/2008/dm08_71_fern.pdf}
 * 
 * TO AVOID INSTABILITY BECAUSE OF THE MANTISSE AND IEEE 754 THE DECIMAL PLACES ARE SET TO A FIXED VALUE
 * 
 * {@link http://en.wikipedia.org/wiki/IEEE_754-1985}
 * 
 * @version $Revision$
 * @author Andre Jaehnig
 */
public class CASSelector extends AbstractSelector {

   /************************************************************************************************
    * FIELDS
    ***********************************************************************************************/

   /** decimal places */
   private static final int DECIMAL_PLACES = 10;

   /** Column name for the cluster of this selector. */
   private static final String CAS_COLUMN_NAME_CLUSTER = "cas_cluster";

   /** Column name for the indicator which clustering is selected. */
   private static final String CAS_COLUMN_NAME_SELECTED = "cas_selected";

   /** Dummy column name. */
   private static final String WORKING_COLUMN_NAME = "working";

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
   public CASSelector(OperatorDescription description) {
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
      double decimalMultiplier = Math.pow(10.0d, DECIMAL_PLACES);
      this.logNote("Number of used decimal places: " + DECIMAL_PLACES);

      // get example set with the clusterings
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

      // create attributes for the selection flag and for the cluster-assignment
      Attribute casClusterAttr = AttributeFactory.createAttribute(CAS_COLUMN_NAME_CLUSTER, Ontology.NOMINAL);
      Attribute casSelectedAttr = AttributeFactory.createAttribute(CAS_COLUMN_NAME_SELECTED, Ontology.NOMINAL);
      exampleSet.getExampleTable().addAttribute(casClusterAttr);
      exampleSet.getExampleTable().addAttribute(casSelectedAttr);

      // add attribute to view
      exampleSet.getAttributes().setSpecialAttribute(casClusterAttr, CAS_COLUMN_NAME_CLUSTER);
      exampleSet.getAttributes().setSpecialAttribute(casSelectedAttr, CAS_COLUMN_NAME_SELECTED);

      // add dummy attribute-column (only to the table)
      Attribute workingSNMIAttr = AttributeFactory.createAttribute(WORKING_COLUMN_NAME + "1", Ontology.REAL);
      Attribute workingNMIAttr = AttributeFactory.createAttribute(WORKING_COLUMN_NAME + "2", Ontology.REAL);
      Attribute workingSumAttr = AttributeFactory.createAttribute(WORKING_COLUMN_NAME + "3", Ontology.REAL);
      exampleSet.getExampleTable().addAttribute(workingSNMIAttr);
      exampleSet.getExampleTable().addAttribute(workingNMIAttr);
      exampleSet.getExampleTable().addAttribute(workingSumAttr);

      // get SNMI attribute
      Attribute snmiAttr = exampleSet.getAttributes().get(snmiColumnName);
      if (snmiAttr == null) {
         throw new UserError(this, 111, snmiColumnName);
      }

      // build the affinity and the diagonal matrix based on the NMI
      this.logNote("Build the affinity matrix based on the NMI-values.");
      Matrix affinityMatrix = new Matrix(exampleSetSize, exampleSetSize);
      Matrix diagonalMatrix = new Matrix(exampleSetSize, exampleSetSize);
      Iterator<Example> it = exampleSet.iterator();
      Example example = null;
      Attributes exampleSetAttributes = exampleSet.getAttributes();
      int i = 0;
      int j;
      while (it.hasNext()) {
         example = it.next();
         j = 0;
         for (Attribute attribute : exampleSetAttributes) {
            if (!attribute.getName().startsWith(clusterColumnPrefix)) {
               // not relevant
               continue;
            }
            if (i == j) {
               affinityMatrix.set(i, j, 0.0d);
               diagonalMatrix.set(i, j, example.getValue(snmiAttr));
            }
            else {
               affinityMatrix.set(i, j, example.getValue(attribute));
            }
            j++;
         }
         i++;
      }

      /*
       * calculating L = D^(-1/2) A D^(-1/2) with A = affinityMatrix and D = diagonalMatrix
       * 
       * L = (D^(1/2)^-1) A (D^(1/2)^-1) L = (sqrt(D) ^ -1) A (sqrt(D) ^ -1)
       * 
       * because D is a diagonal matrix the elements x of D can easily calculate by x = 1 / sqrt(x)
       */
      this.logNote("Calculate matrix D and L (see paper for details).");
      for (int n = 0; n < exampleSetSize; n++) {
         diagonalMatrix.set(n, n, 1.0d / Math.sqrt(diagonalMatrix.get(n, n)));
      }

      Matrix l = diagonalMatrix.times(affinityMatrix).times(diagonalMatrix);

      // round the values of L
      int rows = l.getRowDimension();
      int cols = l.getColumnDimension();
      for (int r = 0; r < rows; r++) {
         for (int c = 0; c < cols; c++) {
            l.set(r, c, Math.round(l.get(r, c) * decimalMultiplier) / decimalMultiplier);
         }
      }

      // get the eigenvalues and -vectors
      this.log("Execute an eigenvalue-decomposition on the matrix L.");
      EigenvalueDecomposition evd = l.eig();
      Matrix eigenvectorMatrix = evd.getV();
      double[] eigenvalues = evd.getRealEigenvalues();

      /*
       * select the k highest eigenvectors (skip those with a duplicate eigenvalue) and form the matrix X by stacking
       * the eigenvectors in columns
       */
      Matrix x = new Matrix(exampleSetSize, sampleSize);
      i = 0;
      double prevEigenvalue = Double.NaN;
      double eigenvalue;
      for (j = eigenvalues.length - 1; j >= 0 && i < sampleSize; j--) {
         eigenvalue = eigenvalues[j];
         if (eigenvalue != prevEigenvalue) {
            // selected this one
            Matrix curVector = eigenvectorMatrix.getMatrix(0, exampleSetSize - 1, j, j);
            x.setMatrix(0, exampleSetSize - 1, i, i, curVector);
            prevEigenvalue = eigenvalue;
            i++;
         }
      }

      if (i < sampleSize - 1) {
         throw new Error("Couldn't find enough eigenvectors.");
      }

      // form the matrix Y from X by re-normalizing each of X's row
      Matrix y = new Matrix(exampleSetSize, sampleSize);
      double length;
      for (int r = 0; r < exampleSetSize; r++) {
         // get length of the row vector
         length = 0.0d;
         for (int c = 0; c < sampleSize; c++) {
            length += Math.pow(x.get(r, c), 2);
         }
         length = Math.sqrt(length);

         // normalize the row values of X and put them into Y
         for (int c = 0; c < sampleSize; c++) {
            y.set(r, c, x.get(r, c) / length);
         }
      }

      /*
       * treating each row in Y as an point and cluster them via k-means
       */

      // create 'sampleSize' attributes
      AttributeSet attributeSet = new AttributeSet();
      for (i = 0; i < sampleSize; i++) {
         attributeSet.addAttribute(AttributeFactory.createAttribute("selected" + i, Ontology.REAL));
      }

      // create table and example set for the kmeans clustering
      this.logNote("Execute a k-Means clustering on the eigenvectors (k = " + sampleSize + ").");
      MemoryExampleTable table = new MemoryExampleTable(attributeSet.getAllAttributes());
      DataRowFactory drf = new DataRowFactory(DataRowFactory.TYPE_DOUBLE_ARRAY, '.');
      Double[] values = new Double[attributeSet.getAllAttributes().size()];

      rows = y.getRowDimension();
      cols = y.getColumnDimension();
      int counter;
      for (int r = 0; r < rows; r++) {
         counter = 0;
         for (int c = 0; c < cols; c++) {
            values[counter++] = y.get(r, c);
         }

         // add values to the table
         table.addDataRow(drf.create(values, table.getAttributes()));
      }

      ExampleSet kmeansExampleSet = table.createExampleSet(attributeSet);

      // run kmeans
      Operator kMeans = null;
      Operator idTagging = null;
      try {
         idTagging = OperatorService.createOperator(IdTagging.class);
         kMeans = OperatorService.createOperator(KMeans.class);
      }
      catch (OperatorCreationException oce) {
         throw new Error(oce.getMessage());
      }
      idTagging.apply(new IOContainer(kmeansExampleSet));
      kMeans.setParameter(KMeans.PARAMETER_K, String.valueOf(sampleSize));
      kMeans.setParameter(KMeans.PARAMETER_ADD_CLUSTER_ATTRIBUTE, "true");
      kMeans.apply(new IOContainer(kmeansExampleSet));

      // copy cluster column to output example set
      it = kmeansExampleSet.iterator();
      Attribute kmeansClusterAttr = kmeansExampleSet.getAttributes().getSpecial(Attributes.CLUSTER_NAME);
      counter = 0;
      Map<String, Integer> selectedIndices = new HashMap<String, Integer>();
      String clusterValue = null;
      Example outputExample = null;
      Integer oldIdx;
      while (it.hasNext()) {
         example = it.next();
         clusterValue = example.getNominalValue(kmeansClusterAttr);
         outputExample = exampleSet.getExample(counter);
         outputExample.setValue(casClusterAttr, clusterValue);

         // get old selected index for this cluster
         oldIdx = selectedIndices.get(clusterValue);
         if (oldIdx == null) {
            // there is none
            selectedIndices.put(clusterValue, counter);
         }
         else {
            // compare SNMI of both
            if (outputExample.getValue(snmiAttr) > exampleSet.getExample(oldIdx).getValue(snmiAttr)) {
               // set the new index
               selectedIndices.put(clusterValue, counter);
            }
         }

         counter++;
      }

      // mark the selected clusterings at the the output example set
      it = exampleSet.iterator();
      counter = 0;
      while (it.hasNext()) {
         example = it.next();
         if (selectedIndices.get(example.getNominalValue(casClusterAttr)) == counter) {
            // selected
            example.setValue(casSelectedAttr, "true");
         }
         else {
            // not selected
            example.setValue(casSelectedAttr, "false");
         }

         counter++;
      }

      // write meta config
      mc.setSelectorFileName(selectorFileName);
      ClusteringInfo ci = new ClusteringInfo();
      ci.setInfoColumnName(CAS_COLUMN_NAME_CLUSTER);
      ci.setSelectedColumnName(CAS_COLUMN_NAME_SELECTED);
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
