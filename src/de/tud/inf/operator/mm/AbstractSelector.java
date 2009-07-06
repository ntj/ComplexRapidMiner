

package de.tud.inf.operator.mm;

import java.util.List;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.parameter.ParameterTypeString;

/**
 * Abstract class for the clustering selectors.
 * 
 * @version $Revision$
 * @author Andre Jaehnig
 */
public abstract class AbstractSelector extends Operator {

   /************************************************************************************************
    * FIELDS
    ***********************************************************************************************/
   
   /** The number of clusterings that should be selected. */
   public static final String PARAMETER_SAMPLE_SIZE = "sample_size";
   
   /** Filename of the meta configuration file. */
   public static final String PARAMETER_META_FILENAME = "meta_filename";
   
   /** Filename of the ensemble file. */
   public static final String PARAMETER_SELECTOR_FILENAME = "selector_filename";

   
   /** Dummy column name. */
   protected static final String WORKING_COLUMN_NAME = "working";

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
      
      types.add(new ParameterTypeInt(PARAMETER_SAMPLE_SIZE,
            "The number of clusterings that should be selected. (min = 1; max = number of clusterings)", 1,
            Integer.MAX_VALUE));
      types.add(new ParameterTypeString(PARAMETER_META_FILENAME, "Filename of the meta configuration file."));
      types.add(new ParameterTypeString(PARAMETER_SELECTOR_FILENAME, "Filename of the selector file."));

      
      return types;
   }
   
   /************************************************************************************************
    * CONSTRUCTOR
    ***********************************************************************************************/

   /**
    * 
    * @param description
    */
   public AbstractSelector(OperatorDescription description) {
      super(description);
   }



   

}
