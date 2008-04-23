/*
 *  RapidMiner
 *
 *  Copyright (C) 2001-2007 by Rapid-I and the contributors
 *
 *  Complete list of developers available at our web site:
 *
 *       http://rapid-i.com
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License as 
 *  published by the Free Software Foundation; either version 2 of the
 *  License, or (at your option) any later version. 
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 *  USA.
 */
package com.rapidminer.operator.visualization;

import java.util.LinkedList;
import java.util.List;

import com.rapidminer.datatable.DataTable;
import com.rapidminer.datatable.DataTableMerger;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.SplittedExampleSet;
import com.rapidminer.gui.plotter.SimplePlotterDialog;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Model;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorChain;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.condition.AllInnerOperatorCondition;
import com.rapidminer.operator.condition.InnerOperatorCondition;
import com.rapidminer.operator.learner.PredictionModel;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.tools.math.ROCDataGenerator;


/**
 * This operator uses its inner operators (each of those must produce a model) and
 * calculates the ROC curve for each of them. All ROC curves together are 
 * plotted in the same plotter. This operator uses an internal split
 * into a test and a training set from the given data set.
 * 
 * Please note that a predicted label of the given example set will be removed during 
 * the application of this operator.
 * 
 * @author Ingo Mierswa
 * @version $Id: ROCBasedComparisonOperator.java,v 1.2 2007/06/15 16:58:39 ingomierswa Exp $
 */
public class ROCBasedComparisonOperator extends OperatorChain {


	/** The parameter name for &quot;Relative size of the training set&quot; */
	public static final String PARAMETER_SPLIT_RATIO = "split_ratio";

	/** The parameter name for &quot;Defines the sampling type of the cross validation (linear = consecutive subsets, shuffled = random subsets, stratified = random subsets with class distribution kept constant)&quot; */
	public static final String PARAMETER_SAMPLING_TYPE = "sampling_type";

	/** The parameter name for &quot;Use the given random seed instead of global random numbers (-1: use global)&quot; */
	public static final String PARAMETER_LOCAL_RANDOM_SEED = "local_random_seed";
    public ROCBasedComparisonOperator(OperatorDescription description) {
        super(description);
    }

    public IOObject[] apply() throws OperatorException {
        ExampleSet exampleSet = getInput(ExampleSet.class);
        if (exampleSet.getAttributes().getLabel() == null) {
            throw new UserError(this, 105);
        }
        if (!exampleSet.getAttributes().getLabel().isNominal()) {
            throw new UserError(this, 101, "ROC Comparison", exampleSet.getAttributes().getLabel());
        }
        if (exampleSet.getAttributes().getLabel().getMapping().getValues().size() != 2) {
            throw new UserError(this, 114, "ROC Comparison", exampleSet.getAttributes().getLabel());
        }
                
        double splitRatio = getParameterAsDouble(PARAMETER_SPLIT_RATIO);
        SplittedExampleSet eSet = new SplittedExampleSet((ExampleSet)exampleSet.clone(), splitRatio, getParameterAsInt(PARAMETER_SAMPLING_TYPE), getParameterAsInt(PARAMETER_LOCAL_RANDOM_SEED));
        PredictionModel.removePredictedLabel(eSet);
        
        List<DataTable> dataTables = new LinkedList<DataTable>();
        for (int i = 0; i < getNumberOfOperators(); i++) {
            // learn model on training set
            eSet.selectSingleSubset(0);
            Operator innerOperator = getOperator(i);
            IOContainer result = innerOperator.apply(new IOContainer(eSet));
            Model model = result.remove(Model.class);
            
            // apply model on test set
            eSet.selectSingleSubset(1);
            model.apply(eSet);
            if (eSet.getAttributes().getPredictedLabel() == null) {
                throw new UserError(this, 107);
            }
            
            // calculate ROC values
            ROCDataGenerator rocDataGenerator = new ROCDataGenerator(1.0d, 1.0d);
            List<double[]> rocPoints = rocDataGenerator.createROCDataList(eSet);
            DataTable dataTable = rocDataGenerator.createDataTable(rocPoints, false, false);
            dataTable.setName(innerOperator.getName());
            dataTables.add(dataTable);

            // remove predicted label
            PredictionModel.removePredictedLabel(eSet);    
        }
        
        // merge data tables
        DataTableMerger merger = new DataTableMerger();
        DataTable dataTable = merger.getMergedTables(dataTables, "FP/N", 0, 1);
        
        // create plotter
        SimplePlotterDialog plotter = new SimplePlotterDialog(dataTable);
        plotter.setXAxis(0);
        for (int i = 1; i <= dataTables.size(); i++)
            plotter.plotColumn(i, true);
        plotter.setDrawRange(0.0d, 1.0d, 0.0d, 1.0d);
        plotter.setDrawPoints(false);
        plotter.setDrawLabel(false);
        plotter.setSize(500, 500);
        plotter.setLocationRelativeTo(plotter.getOwner());
        plotter.setVisible(true);
        
        return new IOObject[] { exampleSet };
    }
    
    public Class[] getInputClasses() {
        return new Class[] { ExampleSet.class };
    }

    public Class[] getOutputClasses() {
        return new Class[] { ExampleSet.class };
    }
   
    public InnerOperatorCondition getInnerOperatorCondition() {
        return new AllInnerOperatorCondition(new Class[] { ExampleSet.class }, 
                                             new Class[] { Model.class });
    }

    public int getMinNumberOfInnerOperators() {
        return 1;
    }

    public int getMaxNumberOfInnerOperators() {
        return Integer.MAX_VALUE;
    }
    
    public List<ParameterType> getParameterTypes() {
        List<ParameterType> types = super.getParameterTypes();
        ParameterType type = new ParameterTypeDouble(PARAMETER_SPLIT_RATIO, "Relative size of the training set", 0.0d, 1.0d, 0.7d);
        type.setExpert(false);
        types.add(type);
        types.add(new ParameterTypeCategory(PARAMETER_SAMPLING_TYPE, "Defines the sampling type of the cross validation (linear = consecutive subsets, shuffled = random subsets, stratified = random subsets with class distribution kept constant)", SplittedExampleSet.SAMPLING_NAMES, SplittedExampleSet.STRATIFIED_SAMPLING));
        types.add(new ParameterTypeInt(PARAMETER_LOCAL_RANDOM_SEED, "Use the given random seed instead of global random numbers (-1: use global)", -1, Integer.MAX_VALUE, -1));
        return types;
    }
}
