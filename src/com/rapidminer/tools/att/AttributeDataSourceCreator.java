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
package com.rapidminer.tools.att;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.table.RapidMinerLineReader;
import com.rapidminer.tools.Ontology;


/** This class can be used as a simple attribute data source creation factory for many types of 
 *  table like data. 
 *  
 *  @author Ingo Mierswa
 *  @version $Id: AttributeDataSourceCreator.java,v 1.1 2007/05/27 22:03:43 ingomierswa Exp $ 
 */
public class AttributeDataSourceCreator {

    /** The list of the abstract attribute informations. */
    private ArrayList<AttributeDataSource> sources = new ArrayList<AttributeDataSource>();
        
    public AttributeDataSourceCreator() {}
    
    public List<AttributeDataSource> getAttributeDataSources() {
        return sources;
    }
    
    public void loadData(File file, char[] commentChars, String columnSeparators, boolean useQuotes, boolean firstLineAsNames, int maxCounter) throws IOException {
        this.sources.clear();        
        String[] columnNames = null;        
        int maxColumns = -1;
        int[] valueTypes = null;

        RapidMinerLineReader lineReader = new RapidMinerLineReader(columnSeparators, commentChars, useQuotes);
        BufferedReader in = new BufferedReader(new FileReader(file));

        int counter = 0;
        boolean first = true;
        while ((maxCounter < 0) || (counter <= maxCounter)) {            
        	String[] columns = lineReader.readLine(in, -1);
        	if (columns == null) // break loop if last line was read
        		break;
        	
            if ((maxColumns != -1) && (maxColumns != columns.length))
                throw new IOException("Number of columns in line " + counter + " was unexpected, was: " + columns.length + ", expected: " + maxColumns);
            
            if (first) {
                maxColumns = columns.length;
                valueTypes = new int[maxColumns];
                for (int i = 0; i < valueTypes.length; i++) {
                    valueTypes[i] = Ontology.INTEGER;
                }
                if (firstLineAsNames) {
                    columnNames = columns;
                } else {
                    guessValueTypes(columns, valueTypes);
                }
                first = false;
            } else {
                guessValueTypes(columns, valueTypes);
            }
            
            counter++;
        }
        in.close();


        if (columnNames == null) {
            String defaultName = file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf(File.separator) + 1);
            columnNames = new String[maxColumns];
            for (int i = 0; i < columnNames.length; i++)
                columnNames[i] = defaultName + " (" + (i+1) + ")";
        } else if (columnNames.length < maxColumns) {
            String defaultName = file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf(File.separator) + 1);
            String[] newColumnNames = new String[maxColumns];
            System.arraycopy(columnNames, 0, newColumnNames, 0, columnNames.length);
            for (int i = columnNames.length; i < newColumnNames.length; i++) {
                newColumnNames[i] = defaultName + " (" + (i+1) + ")";
            }
        }
        for (int i = 0; i < maxColumns; i++) {
            this.sources.add(new AttributeDataSource(AttributeFactory.createAttribute(columnNames[i], valueTypes[i]), file, i, "attribute"));
        }
    }
    
    public static void guessValueTypes(String[] data, int[] valueTypes) {
        for (int c = 0; c < valueTypes.length; c++) {
            String value = data[c];
            if ((value != null) && (!value.equals("?"))) {
                try {
                    double d = Double.parseDouble(value);
                    if ((valueTypes[c] == Ontology.INTEGER) && ((int) d != d)) {
                        valueTypes[c] = Ontology.REAL;
                    }
                } catch (NumberFormatException e) {
                    valueTypes[c] = Ontology.NOMINAL;
                }
            }
        }
    }
}
