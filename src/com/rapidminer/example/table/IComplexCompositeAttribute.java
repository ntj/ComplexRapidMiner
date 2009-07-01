package com.rapidminer.example.table;

import java.util.List;

import com.rapidminer.example.Attribute;

/***
 * defines functions which are useful for C
 * @author Antje Gruner
 *
 */
public interface IComplexCompositeAttribute {

	public int getInnerAttributeCount();
	
	public List<Attribute> getInnerAttributes();
	
	public Attribute getInnerAttribute(String attributeName);
}
