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
package com.rapidminer.gui.viewer;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.AttributeRole;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.Statistics;
import com.rapidminer.example.Tools;
import com.rapidminer.tools.Ontology;


/** The model for the {@link com.rapidminer.gui.viewer.MetaDataViewerTable}. 
 * 
 *  @author Ingo Mierswa
 *  @version $Id: MetaDataViewerTableModel.java,v 1.8 2008/05/09 19:22:59 ingomierswa Exp $
 */
public class MetaDataViewerTableModel extends AbstractTableModel {

    private static final long serialVersionUID = -1598719681189990076L;


    public static final int TYPE       = 0;
    public static final int INDEX      = 1;
    public static final int NAME       = 2;
    public static final int SOURCE     = 3;
    public static final int VALUE_TYPE = 4;
    public static final int BLOCK_TYPE = 5;
    public static final int STATISTICS_AVERAGE = 6;
    public static final int STATISTICS_RANGE   = 7;
    public static final int STATISTICS_SUM     = 8;
    public static final int STATISTICS_UNKNOWN = 9;
    
    public static final String[] COLUMN_NAMES = new String[] {
        "Type", "Index", "Name", "Construction", "Value Type", "Block Type", "Statistics", "Range", "Sum", "Unknown"
    };

    private static final String[] COLUMN_TOOL_TIPS = new String[] {
        "The type of the attribute (regular or one of the special types).",
        "The index of the attribute in the example table backing up this example set (view).",
        "The name of the attribute.",
        "The construction source of the attribute, i.e. how it was generated.",
        "The value type of this attribute, e.g. if the attribute is nominal or numerical.",
        "The block type of this attribute, e.g. if the attribute is a single attribute or part of a series.",
        "Basic statistics about the data set values with respect to this attribute.",
        "The range about the data set values with respect to this attribute (only numerical attributes).",
        "The sum of all values in the data set for this attribute.",
        "The number of unknown values in the data set for this attribute"
    };

    public static final Class[] COLUMN_CLASSES = new Class[] {
    	String.class, Double.class, String.class, String.class, String.class, String.class, String.class, String.class, Double.class, Double.class
    };
    
    private int[] currentMapping = {
        TYPE, NAME, VALUE_TYPE, STATISTICS_AVERAGE, STATISTICS_RANGE, STATISTICS_UNKNOWN
    };
    
    private ExampleSet exampleSet;

    private Attribute[] regularAttributes = new Attribute[0];
    
    private Attribute[] specialAttributes = new Attribute[0];
    
    private String[] specialAttributeNames = new String[0];
    
    public MetaDataViewerTableModel(ExampleSet exampleSet) {
        this.exampleSet = exampleSet;
        if (this.exampleSet != null) {
        	this.regularAttributes = Tools.createRegularAttributeArray(exampleSet);
        	this.specialAttributes = new Attribute[exampleSet.getAttributes().specialSize()];
        	this.specialAttributeNames = new String[exampleSet.getAttributes().specialSize()];
        	Iterator<AttributeRole> i = exampleSet.getAttributes().specialAttributes();
        	int counter = 0;
        	while (i.hasNext()) {
        		AttributeRole role = i.next();
        		this.specialAttributeNames[counter] = role.getSpecialName();
        		this.specialAttributes[counter] = role.getAttribute(); 
        		counter++;
        	}
        }
    }

    public void setShowColumn(int index, boolean show) {
        List<Integer> result = new LinkedList<Integer>();
        for (int i = 0; i < COLUMN_NAMES.length; i++) {
            if (i == index) {
                if (show) result.add(i);
            } else {
                if (getShowColumn(i)) result.add(i);
            }
        }
        this.currentMapping = new int[result.size()];
        Iterator<Integer> i = result.iterator();
        int counter = 0;
        while (i.hasNext()) 
            this.currentMapping[counter++] = i.next();
        fireTableStructureChanged();
        
    }
    
    public boolean getShowColumn(int index) {
        for (int i = 0; i < currentMapping.length; i++) {
            if (currentMapping[i] == index)
                return true;
        }
        return false;
    }
    
    public int getRowCount() {
    	if (this.exampleSet != null) {
    		return exampleSet.getAttributes().specialSize() + exampleSet.getAttributes().size();
    	} else {
    		return 0;
    	}
    }

    /** Returns up to 9 for the following eight columns (depending on the current column selection): <br>
     * 0: type<br>
     * 1: index<br>
     * 2: name<br>
     * 3: construction<br>
     * 4: value type<br>
     * 5: block type<br>
     * 6: basic statistics<br>
     * 7: range statistics<br>
     * 8: sum statistics<br>
     * 9: unknown statistics<br>
     */
    public int getColumnCount() {
    	if (this.exampleSet != null) {
    		return currentMapping.length;
    	} else {
    		return 0;
    	}
    }

    public Object getValueAt(int row, int col) {
        Attribute attribute = null;
        String type = "regular";
        if (row < specialAttributes.length) {
            attribute = specialAttributes[row];
            type = specialAttributeNames[row];
        } else {
            attribute = this.regularAttributes[row - specialAttributes.length];
        }
        int actualColumn = currentMapping[col];
        switch (actualColumn) {
            case TYPE: return type;
            case INDEX: return attribute.getTableIndex();
            case NAME: return attribute.getName();
            case SOURCE: return attribute.getConstruction();
            case VALUE_TYPE: return Ontology.ATTRIBUTE_VALUE_TYPE.mapIndex(attribute.getValueType());
            case BLOCK_TYPE: return Ontology.ATTRIBUTE_BLOCK_TYPE.mapIndex(attribute.getBlockType());
            case STATISTICS_AVERAGE:
                if (attribute.isNominal()) {
                    int modeIndex = (int)exampleSet.getStatistics(attribute, Statistics.MODE);
                    String mode = null;
                    if (modeIndex != -1) 
                        mode = attribute.getMapping().mapIndex(modeIndex);
                    if (mode != null) {  
                        return "mode = " + mode + " (" + com.rapidminer.tools.Tools.formatIntegerIfPossible(exampleSet.getStatistics(attribute, Statistics.COUNT, mode)) + ")";
                    } else {
                        return "mode = unknown";
                    }
                } else {
                    double average  = exampleSet.getStatistics(attribute, Statistics.AVERAGE);
                    double variance = Math.sqrt(exampleSet.getStatistics(attribute, Statistics.VARIANCE));
                    return 
                        "avg = " + com.rapidminer.tools.Tools.formatIntegerIfPossible(average) + " +/- " + 
                        com.rapidminer.tools.Tools.formatIntegerIfPossible(variance);
                }
            case STATISTICS_RANGE:
                if (attribute.isNominal()) {
                    StringBuffer str = new StringBuffer();
                    Iterator<String> i = attribute.getMapping().getValues().iterator();
                    int n = 0;
                    while (i.hasNext()) {
                        if (n > 0)
                            str.append(", ");
                        n++;
                        String value = i.next();
                        str.append(value);
                        str.append(" (" + com.rapidminer.tools.Tools.formatIntegerIfPossible(exampleSet.getStatistics(attribute, Statistics.COUNT, value)) + ")");

                    }
                    return str.toString();
                } else {
                    return 
                        "[" + com.rapidminer.tools.Tools.formatNumber(exampleSet.getStatistics(attribute, Statistics.MINIMUM)) + 
                        " ; " + com.rapidminer.tools.Tools.formatNumber(exampleSet.getStatistics(attribute, Statistics.MAXIMUM)) + "]";
                }                
            case STATISTICS_SUM:
            	return exampleSet.getStatistics(attribute, Statistics.SUM);
            case STATISTICS_UNKNOWN:
              return exampleSet.getStatistics(attribute, Statistics.UNKNOWN);
            default: return "unknown";
        }
    }
    
    /** Returns one of the following nine column names:<br>
     * 0: type<br>
     * 1: index<br>
     * 2: name<br>
     * 3: construction<br>
     * 4: type<br>
     * 5: block type<br>
     * 6: basic statistics<br>
     * 7: range statistics<br>
     * 8: sum statistics<br>
     * 9: unknown statistics<br>
     */
    public String getColumnName(int col) {
        return COLUMN_NAMES[currentMapping[col]];
    }

    /** Returns the classes of the columns. */
    public Class<?> getColumnClass(int col) {
    	return COLUMN_CLASSES[currentMapping[col]];
    }
    
    
    /** Returns the tool tip text for the specified column. */
    public String getColumnToolTip(int column) {
        if ((column < 0) || (column >= currentMapping.length)) return "";
        else return COLUMN_TOOL_TIPS[currentMapping[column]];
    }
}
