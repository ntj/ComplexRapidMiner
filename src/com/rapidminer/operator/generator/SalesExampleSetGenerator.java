/*
 *  RapidMiner
 *
 *  Copyright (C) 2001-2008 by Rapid-I and the contributors
 *
 *  Complete list of developers available at our web site:
 *
 *       http://rapid-i.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package com.rapidminer.operator.generator;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.table.DoubleArrayDataRow;
import com.rapidminer.example.table.MemoryExampleTable;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.tools.Ontology;
import com.rapidminer.tools.RandomGenerator;

/**
 * Generates a random example set for testing purposes. The data represents a sales example set.
 * 
 * @author Ingo Mierswa
 * @version $Id: SalesExampleSetGenerator.java,v 1.1 2008/07/25 15:30:32 ingomierswa Exp $
 */
public class SalesExampleSetGenerator extends Operator {

	/** The parameter name for &quot;The number of generated examples.&quot; */
    public static final String PARAMETER_NUMBER_EXAMPLES = "number_examples";

    /** The parameter name for &quot;Use the given random seed instead of global random numbers (-1: use global).&quot; */
    public static final String PARAMETER_LOCAL_RANDOM_SEED = "local_random_seed";

    private static final int MAX_STORES = 15;
    
    private static final int MAX_CUSTOMERS = 2000;
    
    private static final String[] ATTRIBUTE_NAMES = {
        "transaction_id", "store_id", "customer_id", "product_id", "product_category", "date", "amount", "single_price"
    };
    
    private static final int ATT_TRANSACTION_ID   = 0;
    private static final int ATT_STORE_ID         = 1;
    private static final int ATT_CUSTOMER_ID      = 2;
    private static final int ATT_PRODUCT_ID       = 3;
    private static final int ATT_PRODUCT_CATEGORY = 4;
    private static final int ATT_DATE             = 5;
    private static final int ATT_AMOUNT           = 6;
    private static final int ATT_SINGLE_PRICE     = 7;
    
    private static final String[] PRODUCT_CATEGORIES = new String[] {
    	"Books", "Movies", "Electronics", "Home/Garden", "Health", "Toys", "Sports", "Clothing"
    };
    
    
    public SalesExampleSetGenerator(OperatorDescription description) {
		super(description);
	}
    
	public IOObject[] apply() throws OperatorException {
        // init
        int numberOfExamples = getParameterAsInt(PARAMETER_NUMBER_EXAMPLES);

        List<Attribute> attributes = new ArrayList<Attribute>();
        
        // transaction id
        Attribute transactionId = AttributeFactory.createAttribute(ATTRIBUTE_NAMES[ATT_TRANSACTION_ID], Ontology.INTEGER);
        attributes.add(transactionId);
        
        // store id
        Attribute storeId = AttributeFactory.createAttribute(ATTRIBUTE_NAMES[ATT_STORE_ID], Ontology.NOMINAL);
        for (int s = 1; s <= MAX_STORES; s++) {
        	storeId.getMapping().mapString("Store " + getFullStoreNumber(s));
        }
        attributes.add(storeId);
        
        // customer id
        Attribute customerId = AttributeFactory.createAttribute(ATTRIBUTE_NAMES[ATT_CUSTOMER_ID], Ontology.NOMINAL);
        for (int s = 1; s <= MAX_CUSTOMERS; s++) {
        	customerId.getMapping().mapString("Customer " + s);
        }
        attributes.add(customerId);
        
        // product id
        attributes.add(AttributeFactory.createAttribute(ATTRIBUTE_NAMES[ATT_PRODUCT_ID], Ontology.INTEGER));
        
        // product category
        Attribute productCategory = AttributeFactory.createAttribute(ATTRIBUTE_NAMES[ATT_PRODUCT_CATEGORY], Ontology.NOMINAL);
        for (int s = 0; s < PRODUCT_CATEGORIES.length; s++) {
        	productCategory.getMapping().mapString(PRODUCT_CATEGORIES[s]);
        }
        attributes.add(productCategory);
        
        // date
        attributes.add(AttributeFactory.createAttribute(ATTRIBUTE_NAMES[ATT_DATE], Ontology.DATE));
        
        // amount
        attributes.add(AttributeFactory.createAttribute(ATTRIBUTE_NAMES[ATT_AMOUNT], Ontology.INTEGER));

        // single price
        attributes.add(AttributeFactory.createAttribute(ATTRIBUTE_NAMES[ATT_SINGLE_PRICE], Ontology.REAL));
        
        
        // create table
        MemoryExampleTable table = new MemoryExampleTable(attributes);

        
        // create data
        RandomGenerator random = RandomGenerator.getRandomGenerator(getParameterAsInt(PARAMETER_LOCAL_RANDOM_SEED));
        for (int n = 0; n < numberOfExamples; n++) {
            double[] values = new double[ATTRIBUTE_NAMES.length];
            //"transaction_id", "store_id", "customer_id", "product_id", "product_category", "date", "amount", "single_price"
            values[0] = n + 1;
            values[1] = attributes.get(1).getMapping().mapString("Store " + getFullStoreNumber(random.nextIntInRange(1, MAX_STORES + 1)));
            values[2] = attributes.get(2).getMapping().mapString("Customer " + random.nextIntInRange(1, MAX_CUSTOMERS + 1));
            values[3] = random.nextIntInRange(10000, 100000);
            values[4] = random.nextInt(PRODUCT_CATEGORIES.length);
            values[5] = random.nextDateInRange(new GregorianCalendar(2005, 1, 1).getTime(), new GregorianCalendar(2008, 10, 30).getTime()).getTime();
            values[6] = random.nextIntInRange(1, 10);
            values[7] = random.nextDoubleInRange(10, 100);
            	
            table.addDataRow(new DoubleArrayDataRow(values));
        }

        // create example set and return it
        return new IOObject[] { table.createExampleSet(null, null, transactionId) };
	}
	
	private String getFullStoreNumber(int number) {
		if (number < 10) {
			return "0" + number;
		} else {
			return number + "";
		}
	}

	public Class<?>[] getInputClasses() {
		return new Class[0];
	}

	public Class<?>[] getOutputClasses() {
		return new Class[] { ExampleSet.class };
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
        ParameterType type = new ParameterTypeInt(PARAMETER_NUMBER_EXAMPLES, "The number of generated examples.", 1, Integer.MAX_VALUE, 100);
        type.setExpert(false);
        types.add(type);
        types.add(new ParameterTypeInt(PARAMETER_LOCAL_RANDOM_SEED, "Use the given random seed instead of global random numbers (-1: use global).", -1, Integer.MAX_VALUE, -1));
		return types;
	}
}
