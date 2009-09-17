

package de.tud.inf.operator.mm;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.MissingIOObjectException;
import com.rapidminer.operator.ModelApplier;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorCreationException;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.features.selection.RandomSelection;
import com.rapidminer.operator.features.transformation.PCA;
import com.rapidminer.operator.learner.clustering.clusterer.KMeans;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.parameter.ParameterTypeString;
import com.rapidminer.tools.Ontology;
import com.rapidminer.tools.OperatorService;

import de.tud.inf.operator.mm.util.MetaConfig;


/**
 * This operator creates a library of different clusterings of the same data-source. For now there are implemented 3
 * strategies that will be executed one after another and each of them produces 1/3 of the library.
 * 
 * @version $Revision$
 * @author Andre Jaehnig
 */
public class ClusterEnsembleCreator extends Operator {

   /************************************************************************************************
    * FIELDS
    ***********************************************************************************************/

   /** Number of clusterings each strategy should produce. */
   public static final String PARAMETER_CLUSTERING_COUNT_PER_STRATEGY = "count_per_strategy";

   /** Minimum number of clusters that should appear within a clustering. */
   public static final String PARAMETER_K_MIN = "k_min";

   /** Maximum number of clusters that should appear within a clustering. */
   public static final String PARAMETER_K_MAX = "k_max";

   /** Number of classes within the data. */
   public static final String PARAMETER_CLASS_COUNT = "class_count";

   /** Prefix for the new cluster columns. */
   public static final String PARAMETER_CLUSTER_COLUMN_PREFIX = "cluster_prefix";

   /** List of attributes that are only classifying and should not be used for clustering. */
   public static final String PARAMETER_CLASSIFYING_ATTRIBUTES = "classifying_attributes";

   /** Filename of the meta configuration file. */
   public static final String PARAMETER_META_FILENAME = "meta_filename";

   /** Filename of the ensemble file. */
   public static final String PARAMETER_ENSEMBLE_FILENAME = "ensemble_filename";
   
   /** Filename of the raw data. */
   public static final String PARAMETER_DATA_FILENAME = "data_filename";

   /** Operator for K-means clustering. */
   private Operator kMeans = null;

   /** Operator for a random feature selection. */
   private Operator randomFeatureSelector = null;

   /** Operator for a PCA. */
   private Operator pca = null;

   /** Operator for applying a model. */
   private Operator modelApplier = null;

   /** Random number creator. */
   private Random random = null;

   /** The prefix for the cluster column. */
   private String clusterColumnPrefix = null;

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
   public List<ParameterType> getParameterTypes() {
      List<ParameterType> types = super.getParameterTypes();

      types.add(new ParameterTypeInt(PARAMETER_CLUSTERING_COUNT_PER_STRATEGY,
            "The number of clusterings each strategy should produce.", 1, Integer.MAX_VALUE, 200));
      types.add(new ParameterTypeInt(PARAMETER_K_MIN,
            "Minimum number of clusters that should appear within a clustering.", 2, Integer.MAX_VALUE, true));
      types.add(new ParameterTypeInt(PARAMETER_K_MAX,
            "Maximum number of clusters that should appear within a clustering.", 2, Integer.MAX_VALUE, true));
      types.add(new ParameterTypeInt(PARAMETER_CLASS_COUNT, "Number of classes within the data.", 1, Integer.MAX_VALUE,
            true));
      types.add(new ParameterTypeString(PARAMETER_CLASSIFYING_ATTRIBUTES,
            "List of attributes that are only classifying and should not be used for clustering.", ""));
      types.add(new ParameterTypeString(PARAMETER_CLUSTER_COLUMN_PREFIX, "Prefix for the new cluster columns.", "cr"));
      types.add(new ParameterTypeString(PARAMETER_META_FILENAME, "Filename of the meta configuration file."));
      types.add(new ParameterTypeString(PARAMETER_ENSEMBLE_FILENAME, "Filename of the ensemble file."));
      types.add(new ParameterTypeString(PARAMETER_DATA_FILENAME, "Filename of the raw data file."));

      return types;
   }

   /************************************************************************************************
    * CONSTRUCTOR
    ***********************************************************************************************/

   /**
    * Constructor.
    * 
    * @param description Description of the operator.
    */
   public ClusterEnsembleCreator(OperatorDescription description) {
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
      // get input example set and some details
      ExampleSet inputExampleSet = getInput(ExampleSet.class);
      int elementCount = inputExampleSet.size();
      int dimensionCount = inputExampleSet.getAttributes().size();
      this.logNote("Input example-set has " + elementCount + " elements and " + dimensionCount + " dimensions.");

      // create a copy for the ouput
      ExampleSet outputExampleSet = (ExampleSet) inputExampleSet.clone();

      // read out parameters
      int countPerStrategy = this.getParameterAsInt(PARAMETER_CLUSTERING_COUNT_PER_STRATEGY);
      String classifyingAttributesStr = this.getParameterAsString(PARAMETER_CLASSIFYING_ATTRIBUTES);
      List<String> classifyingAttributes = null;
      if (classifyingAttributesStr != null && classifyingAttributesStr.length() > 0) {
         classifyingAttributes = Arrays.asList(classifyingAttributesStr.split(","));
      }
      else {
         classifyingAttributes = new LinkedList<String>();
      }
      this.clusterColumnPrefix = this.getParameterAsString(PARAMETER_CLUSTER_COLUMN_PREFIX);
      String metaFileName = this.getParameterAsString(PARAMETER_META_FILENAME);
      String ensembleFileName = this.getParameterAsString(PARAMETER_ENSEMBLE_FILENAME);
      String rawDataFileName = this.getParameterAsString(PARAMETER_DATA_FILENAME);

      int kMin, kMax;
      if (this.isParameterSet(PARAMETER_K_MIN)) {
         kMin = this.getParameterAsInt(PARAMETER_K_MIN);
      }
      else {
         // kMin is not given
         kMin = 2;
      }
      if (this.isParameterSet(PARAMETER_K_MAX)) {
         kMax = this.getParameterAsInt(PARAMETER_K_MAX);
      }
      else {
         // kMax is not given
         kMax = (int) Math.sqrt(elementCount);
      }
      if (this.isParameterSet(PARAMETER_CLASS_COUNT)) {
         if (this.isParameterSet(PARAMETER_K_MAX)) {
            // ignore class count
            this.logWarning("Parameter " + PARAMETER_CLASS_COUNT + " will be ignored.");
         }
         else {
            this.logNote("Setting Parameter " + PARAMETER_K_MAX + " to 2 * " + PARAMETER_CLASS_COUNT + ".");
            int classCount = this.getParameterAsInt(PARAMETER_CLASS_COUNT);
            kMax = 2 * classCount;
         }
      }

      // check kMin/kMax-values
      if (kMin < 2) {
         throw new Error("Parameter " + PARAMETER_K_MIN + " has to be at least '2'.");
      }
      if (kMax > elementCount) {
         throw new Error("Parameter " + PARAMETER_K_MAX
                         + " can not be larger than the number of elements of this data-set.");
      }
      else if (kMax < kMin) {
         throw new Error("Parameter " + PARAMETER_K_MAX + " can not be smaller than parameter " + PARAMETER_K_MIN + ".");
      }
      this.logNote("Global settings for the clustering ensemble:");
      this.logNote("kMin = " + kMin + "\tkMax = " + kMax);
      this.logNote("Clustering-count per strategy: " + countPerStrategy);
      this.logNote("Classifying attributes: " + classifyingAttributesStr);

      // create the necessary operators
      try {
         // K-means operator
         this.kMeans = OperatorService.createOperator(KMeans.class);

         // random feature selector operator
         this.randomFeatureSelector = OperatorService.createOperator(RandomSelection.class);

         // pca
         this.pca = OperatorService.createOperator(PCA.class);

         // model applier
         this.modelApplier = OperatorService.createOperator(ModelApplier.class);
      }
      catch (OperatorCreationException oce) {
         throw new Error(oce.getMessage());
      }

      // mark classifying attributes as special ones
      Attribute attr = null;
      for (String attrName : classifyingAttributes) {
         // get the attribute
         attr = inputExampleSet.getAttributes().get(attrName);

         // set special
         inputExampleSet.getAttributes().setSpecialAttribute(attr, attrName);
      }

      // write meta file to disk
      MetaConfig mc = new MetaConfig();
      mc.setClassifyingAttributeNames(classifyingAttributes);
      mc.setClusteringColumnPrefix(this.clusterColumnPrefix);
      mc.setClusteringCount(3 * countPerStrategy);
      mc.setEnsembleFileName(ensembleFileName);
      mc.setIdColumnName(inputExampleSet.getAttributes().getId().getName());
      mc.setDataFileName(rawDataFileName);
      mc.save(metaFileName);

      // initialize some strategy wide variables
      int clusteringCounter = 0;
      this.random = new Random();
      IOContainer operatorOutput = null;

      // ############################################################################################
      // STRATEGY 1
      // kMeans with different random initializations.
      // ############################################################################################
      this.logNote("Running strategy 1:");
      this.logNote("The different clustering solutions are obtained by applying K-means to the "
                   + "same data with different random intialization.");
      this.logNote("===================");

      for (int i = 0; i < countPerStrategy; i++) {
         this.logNote("Pass #" + clusteringCounter);

         // execute k-means
         operatorOutput = this.runKMeans(inputExampleSet, kMin, kMax, true);

         // copy cluster-attribute to output example set
         outputExampleSet = this.copyClusterColumnToOutput(outputExampleSet, operatorOutput, clusteringCounter);

         clusteringCounter++;
         this.logNote("----------");
      }

      // ############################################################################################
      // STRATEGY 2
      // kMeans with different random feature subsets.
      // ############################################################################################
      this.logNote("Running strategy 2:");
      this.logNote("The different clustering solutions are obtained by using different random " + "feature subsets.");
      this.logNote("===================");

      // get min/max values for the feature count
      int featureCountMin = 2;
      int featureCountMax = (int) Math.round(dimensionCount / 2.0d);
      if (featureCountMax < featureCountMin) {
         featureCountMax = featureCountMin;
      }
      this.logNote("Local settings for strategy 2:");
      this.logNote("min-feature-count = " + featureCountMin + "\tmax-feature-count = " + featureCountMax);

      ExampleSet reducedExampleSet = null;
      for (int i = 0; i < countPerStrategy; i++) {
         this.logNote("Pass #" + clusteringCounter);

         // execute feature reduction
         operatorOutput = this.runFeatureReduction(inputExampleSet, featureCountMin, featureCountMax);
         reducedExampleSet = operatorOutput.get(ExampleSet.class);

         // execute k-means
         operatorOutput = this.runKMeans(reducedExampleSet, kMin, kMax, false);

         // copy cluster-attribute to output example set
         outputExampleSet = this.copyClusterColumnToOutput(outputExampleSet, operatorOutput, clusteringCounter);

         clusteringCounter++;
         this.logNote("----------");
      }

      // ############################################################################################
      // STRATEGY 3
      // kMeans with different PCs
      // ############################################################################################
      this.logNote("Running strategy 3:");
      this.logNote("Use of different random linear projections of the features to create different "
                   + "clustering solutions.");
      this.logNote("===================");

      // get min/max values for the feature count
      featureCountMin = 2;
      featureCountMax = (int) Math.round((dimensionCount - classifyingAttributes.size()) / 2.0d);
      if (featureCountMax < featureCountMin) {
         featureCountMax = featureCountMin;
      }
      this.logNote("Local settings for strategy 3:");
      this.logNote("min-feature-count = " + featureCountMin + "\tmax-feature-count = " + featureCountMax);

      // execute pca
      this.logNote("Starting a full PCA on the input example set.");
      this.pca.setParameter(PCA.PARAMETER_REDUCTION_TYPE, String.valueOf(PCA.REDUCTION_NONE));
      operatorOutput = this.pca.apply(new IOContainer(inputExampleSet));
      // apply pca model
      operatorOutput = this.modelApplier.apply(operatorOutput);
      ExampleSet pcaExampleSet = operatorOutput.get(ExampleSet.class);
      this.logNote("PCA finished!");

      for (int i = 0; i < countPerStrategy; i++) {
         this.logNote("Pass #" + clusteringCounter);

         // execute feature reduction
         operatorOutput = this.runFeatureReduction(pcaExampleSet, featureCountMin, featureCountMax);
         reducedExampleSet = operatorOutput.get(ExampleSet.class);

         // execute k-means
         operatorOutput = this.runKMeans(reducedExampleSet, kMin, kMax, false);

         // copy cluster-attribute to output example set
         outputExampleSet = this.copyClusterColumnToOutput(outputExampleSet, operatorOutput, clusteringCounter);

         clusteringCounter++;
         this.logNote("----------");
      }

      return new IOObject[] { outputExampleSet };
   }

   /************************************************************************************************
    * PRIVATE METHODS
    ***********************************************************************************************/

   /**
    * This method runs an k-Means clustering on the input example set and returns the output example set and the
    * clustering model. The parameter k will be randomly drawn between kMin and kMax. Also there can be specified if the
    * initialization of the centroids should be randomized or not.
    * 
    * @param exampleSet The input example set that should clustered.
    * @param kMin Minimum number of clusters.
    * @param kMax Maximum number of clusters.
    * @param randomSeed Flag for a random initialization of the centroids.
    * @return Container with the output example set and the cluster model.
    */
   private IOContainer runKMeans(ExampleSet exampleSet, int kMin, int kMax, boolean randomSeed) {
      // get new seed
      int seed;
      if (randomSeed) {
         seed = this.random.nextInt(Integer.MAX_VALUE);
      }
      else {
         seed = -1;
      }

      // get new k
      int k = this.random.nextInt(kMax - kMin + 1) + kMin;

      // setting parameters
      this.logNote("K-means-settings:\tk = " + k + "\tseed = " + seed);
      kMeans.setParameter(KMeans.PARAMETER_LOCAL_RANDOM_SEED, String.valueOf(seed));
      kMeans.setParameter(KMeans.PARAMETER_K, String.valueOf(k));

      // fire!
      IOContainer output;
      try {
         output = this.kMeans.apply(new IOContainer(exampleSet));
      }
      catch (OperatorException e) {
    	  e.printStackTrace();
         throw new Error("Error while running K-means.",e);
      }

      return output;
   }

   /**
    * This methods selects a random subset of features from the input example set and returns the according reduced
    * example set. The number of features will be randomly drawn between the two given boundaries.
    * 
    * @param exampleSet The input example set.
    * @param minFeatures Minimum number of features.
    * @param maxFeatures Maximum number of features.
    * @return The reduced example set.
    */
   private IOContainer runFeatureReduction(ExampleSet exampleSet, int minFeatures, int maxFeatures) {
      // get new seed
      int seed = this.random.nextInt(Integer.MAX_VALUE);

      // get new feature count
      int featureCount = this.random.nextInt(maxFeatures - minFeatures + 1) + minFeatures;

      // setting parameters
      this.logNote("Feature-reduction-settings:\tfeature-count = " + featureCount + "\tseed = " + seed);
      this.randomFeatureSelector.setParameter(RandomSelection.PARAMETER_NUMBER_OF_FEATURES, String
            .valueOf(featureCount));
      this.randomFeatureSelector.setParameter(RandomSelection.PARAMETER_LOCAL_RANDOM_SEED, String.valueOf(seed));

      // fire!
      IOContainer output;
      try {
         output = this.randomFeatureSelector.apply(new IOContainer(exampleSet));
      }
      catch (OperatorException e) {
         throw new Error("Error while running feature reduction.");
      }

      return output;
   }

   /**
    * This method copies the cluster-assigments from the clustering output to the given example set as a new column.
    * 
    * @param exampleSet The input example set to which the new column should be added.
    * @param clusteringOutput The out of the clustering (clustering result).
    * @param num The current number of the clustering.
    * @return An example set with an extra column for the given clustering result.
    */
   private ExampleSet copyClusterColumnToOutput(ExampleSet exampleSet, IOContainer clusteringOutput, int num) {
      // get example set from the clustering
      ExampleSet clusteringExampleSet;
      try {
         clusteringExampleSet = clusteringOutput.get(ExampleSet.class);
      }
      catch (MissingIOObjectException e) {
         throw new Error("Something went wrong :/");
      }
      Attribute clusterAttribute = clusteringExampleSet.getAttributes().getSpecial(Attributes.CLUSTER_NAME);

      // add new attribute to the output example set
      Attribute newClusterAttribute = AttributeFactory
            .createAttribute(this.clusterColumnPrefix + num, Ontology.NOMINAL);
      exampleSet.getExampleTable().addAttribute(newClusterAttribute);
      exampleSet.getAttributes().setSpecialAttribute(newClusterAttribute, this.clusterColumnPrefix + num);

      // iterate through clustering example set and copy the cluster attribute values
      Iterator<Example> it1 = clusteringExampleSet.iterator();
      Iterator<Example> it2 = exampleSet.iterator();
      Example example1 = null;
      Example example2 = null;
      while (it1.hasNext() && it2.hasNext()) {
         example1 = it1.next();
         example2 = it2.next();
         example2.setValue(newClusterAttribute, example1.getNominalValue(clusterAttribute));
      }

      return exampleSet;
   }

}
