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
package com.rapidminer.example.table;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Iterator;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.ConstructionDescription;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.Statistics;


/** 
 * This class simply delegates all method invokations to the given attribute.
 * Subclasses might want to override some of the methods.
 * 
 * @author Ingo Mierswa
 * @version $Id: DelegateAttribute.java,v 1.3 2007/07/10 18:02:03 ingomierswa Exp $
 */
public class DelegateAttribute implements Attribute {

	private static final long serialVersionUID = 8692382802551175998L;
	
	private Attribute delegate;
	
	public DelegateAttribute(Attribute delegate) {
		this.delegate = delegate;
	}
	
	public Attribute getDelegate() { 
		return this.delegate;
	}

	public Object clone() {
		return delegate.clone();
	}
	
	public Iterator<Statistics> getAllStatistics() {
		return this.delegate.getAllStatistics();
	}

    public void registerStatistics(Statistics statistics) {
        this.delegate.registerStatistics(statistics);
    }
    
    /** Returns the desired statistics.
     *   
     *  @deprecated Please use the method {@link ExampleSet#getStatistics(Attribute, String)} instead. */
    @Deprecated
    public double getStatistics(String statisticsName) {
        return this.delegate.getStatistics(statisticsName);
    }

    /** Returns the desired statistics.
     *   
     *  @deprecated Please use the method {@link ExampleSet#getStatistics(Attribute, String, String)} instead. */
    @Deprecated
    public double getStatistics(String statisticsName, String parameterName) {
        return this.delegate.getStatistics(statisticsName, parameterName);
    }
    
	public int getBlockType() {
		return this.delegate.getBlockType();
	}

	public ConstructionDescription getConstruction() {
		return this.delegate.getConstruction();
	}

	public double getDefault() {
		return this.delegate.getDefault();
	}

	public String getName() {
		return this.delegate.getName();
	}

	public int getTableIndex() {
		return this.delegate.getTableIndex();
	}

	public double getValue(DataRow row) {
		return this.delegate.getValue(row);
	}

	public int getValueType() {
		return this.delegate.getValueType();
	}

	public boolean isNominal() {
		return this.delegate.isNominal();
	}

	public void readAttributeData(DataInput in) throws IOException {
		this.delegate.readAttributeData(in);
	}

	public void setBlockType(int b) {
		this.delegate.setBlockType(b);
	}

	public void setDefault(double value) {
		this.delegate.setDefault(value);
	}

	public void setName(String name) {
		this.delegate.setName(name);
	}

	public void setTableIndex(int index) {
		this.delegate.setTableIndex(index);
	}

	public void setValue(DataRow row, double value) {
		this.delegate.setValue(row, value);
	}

	public void writeAttributeData(DataOutput out) throws IOException {
		this.delegate.writeAttributeData(out);
	}

	public NominalMapping getMapping() {
		return this.delegate.getMapping();
	}
	
	public void setMapping(NominalMapping newMapping) {
		this.delegate.setMapping(newMapping);
	}
	
	public String getAsString(double value, int numberOfDigits, boolean quoteWhitespace) {
		return this.delegate.getAsString(value, numberOfDigits, quoteWhitespace);
	}
}
