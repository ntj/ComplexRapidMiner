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
package com.rapidminer.example;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.table.ExampleTable;
import com.rapidminer.generator.ConstantGenerator;
import com.rapidminer.generator.FeatureGenerator;
import com.rapidminer.generator.GenerationException;
import com.rapidminer.tools.Ontology;


/**
 * Parses a file containing construction descriptions and adds the new
 * attributes to the example set.
 * 
 * @author Simon Fischer, Ingo Mierswa
 * @version $Id: AttributeParser.java,v 1.3 2007/06/23 01:12:06 ingomierswa Exp $
 */
public class AttributeParser {

	/**
	 * Maps construction descriptions of all generated attributes (including
	 * intermediate attributes) to the attributes.
	 */
	private Map<String, Attribute> allAttributes = new HashMap<String, Attribute>();

	/** A list of the newly generated attributes specified in the file. */
	private List<Attribute> newAttributes = new LinkedList<Attribute>();

	/** The example table to which the attributes should be added. */
	private ExampleTable exampleTable;

	public AttributeParser(ExampleTable et) {
		this.exampleTable = et;
		for (int i = 0; i < exampleTable.getNumberOfAttributes(); i++) {
			Attribute a = exampleTable.getAttribute(i);
			if (a != null)
				addAttribute(a);
		}
	}

	/** Returns a list of all parsed attributes. */
	public List getNewAttributes() {
		return newAttributes;
	}
    
	/** Parses all lines. */
	public void parseAll(InputStream in) throws IOException, GenerationException {
        Document document = null;
        try {
            document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in);
        } catch (SAXException e1) {
            throw new IOException(e1.getMessage());
        } catch (ParserConfigurationException e1) {
            throw new IOException(e1.getMessage());
        }

        Element constructionsElement = document.getDocumentElement();
        if (!constructionsElement.getTagName().equals("constructions")) {
            throw new IOException("Outer tag of attribute constructions file must be <constructions>");
        }
        
        NodeList constructions = constructionsElement.getChildNodes();
        for (int i = 0; i < constructions.getLength(); i++) {
            Node node = constructions.item(i);
            if (node instanceof Element) {
                Element constructionTag = (Element)node;
                String tagName = constructionTag.getTagName();
                if (!tagName.equals("attribute"))
                    throw new IOException("Only tags <attribute> are allowed, was " + tagName);
                String attributeName = constructionTag.getAttribute("name");
                String attributeString = constructionTag.getAttribute("construction");
                Attribute att = parseAttribute(attributeString);
                if (attributeName != null)
                    att.setName(attributeName);
            }
        }
	}

	/**
	 * Adds a new attribute to the map. If the map already contains an attribute
	 * with the same construction description, this attribute is returned.
	 */
	private Attribute addAttribute(Attribute a) {
		Attribute oldAtt = allAttributes.get(a.getConstruction().getDescription());
		if (oldAtt != null) {
			return oldAtt;
		} else {
			allAttributes.put(a.getConstruction().getDescription(), a);
			return a;
		}
	}

	private static int getClosingBracketIndex(String string, int startIndex) throws GenerationException {
		int openCount = 1;
		while (true) {
			int nextOpen = string.indexOf("(", startIndex + 1);
			int nextClosing = string.indexOf(")", startIndex + 1);
			if (nextClosing == -1)
				throw new GenerationException("Malformed attribute description: mismatched parantheses");
			if ((nextOpen != -1) && (nextOpen < nextClosing)) {
				openCount++;
				startIndex = nextOpen;
			} else {
				openCount--;
				startIndex = nextClosing;
			}
			if (openCount == 0) {
				return nextClosing;
			}
		}
	}

	public Attribute parseAttribute(String constructionDescription) throws GenerationException {
		Attribute[] attributes = parseAttributes(constructionDescription);
		if (attributes.length != 1)
			throw new GenerationException("Malformed function description: too many attributes");
        newAttributes.add(attributes[0]);
		return attributes[0];
	}

	/** Recursively parses the string starting at the current position. */
	private Attribute[] parseAttributes(String construction) throws GenerationException {
		List<Attribute> attributes = new LinkedList<Attribute>();

		int start = 0;

		while (start < construction.length()) {
			int leftBr = construction.indexOf("(", start);
			int comma = construction.indexOf(",", start);
			if ((comma == -1) && (leftBr == -1)) {
				int end = construction.length();
				String name = construction.substring(start, end).trim();
				if (name.startsWith(ConstantGenerator.FUNCTION_NAME)) {
					throw new GenerationException("The function name '" + ConstantGenerator.FUNCTION_NAME + "' must be used with empty arguments!");
				} else {
					Attribute attribute = AttributeFactory.createAttribute(name, Ontology.NUMERICAL);
					attributes.add(addAttribute(attribute));
					start = construction.length();
				}
			} else if ((leftBr == -1) || ((comma < leftBr) && (comma != -1))) {
				int end = comma;
				String name = construction.substring(start, end).trim();
				if (name.startsWith(ConstantGenerator.FUNCTION_NAME)) {
					throw new GenerationException("The function name '" + ConstantGenerator.FUNCTION_NAME + "' must be used with empty arguments!");
				} else {
					Attribute attribute = AttributeFactory.createAttribute(name, Ontology.NUMERICAL);
					attributes.add(addAttribute(attribute));
					start = end + 1;
				}
			} else {
				int rightBr = getClosingBracketIndex(construction, leftBr);
				String functionName = construction.substring(start, leftBr).trim();
				Attribute[] arguments = parseAttributes(construction.substring(leftBr + 1, rightBr).trim());
				ConstructionDescription[] argumentDescriptions = new ConstructionDescription[arguments.length];
				for (int i = 0; i < argumentDescriptions.length; i++) {
					argumentDescriptions[i] = arguments[i].getConstruction();
				}
				attributes.add(addAttribute(AttributeFactory.createAttribute(functionName, argumentDescriptions)));
				start = construction.indexOf(",", rightBr) + 1;
				if (start <= 0)
					start = construction.length();
			}
		}

		Attribute[] attributeArray = new Attribute[attributes.size()];
		attributes.toArray(attributeArray);
		return attributeArray;
	}

	/**
	 * Returns a collection of feature generators that can generate the
	 * attributes that are not yet generated. The attributes are then removed
	 * from the map.
	 */
	private Collection<FeatureGenerator> applicableGenerators() throws GenerationException {
		Set<FeatureGenerator> generators = new HashSet<FeatureGenerator>();
		Iterator i = allAttributes.values().iterator();
		while (i.hasNext()) {
			Attribute a = (Attribute) i.next();
			if (FeatureGenerator.getAttributeInTable(exampleTable, a) == null) {
				if (argumentsAlreadyGenerated(a)) {
					FeatureGenerator fg = FeatureGenerator.createGeneratorForFunction(a.getConstruction().getFunction());
					if (fg != null) {
						Attribute[] args = new Attribute[a.getConstruction().getArguments().length];
						for (int c = 0; c < args.length; c++) {
							args[c] = a.getConstruction().getArguments()[c].getAttribute();
						}
						if (args != null) {
							for (int n = 0; n < args.length; n++) {
								args[n] = FeatureGenerator.getAttributeInTable(exampleTable, args[n]);
							}
							if (fg.getInputAttributes().length != args.length) {
								throw new GenerationException(fg + " has arity " + fg.getInputAttributes().length + "!");
							} else {
								fg.setArguments(args);
							}
						}
						generators.add(fg);
						i.remove();
					}
				}
			} else {
				i.remove();
			}
		}
		return generators;
	}

	/** Returns true if the example set already contains <tt>a</tt>. */
	private boolean argumentsAlreadyGenerated(Attribute a) {
		ConstructionDescription[] arguments = a.getConstruction().getArguments();
		if (arguments == null)
			return true;
		for (int i = 0; i < arguments.length; i++) {
			if (FeatureGenerator.getAttributeInTable(exampleTable, arguments[i].getAttribute()) == null)
				return false;
		}
		return true;
	}

	/** Generates new attributes as long as it is possible. */
	public void generateAll(ExampleSet exampleSet) throws GenerationException {
		List<Attribute> allGeneratedAttributes = new LinkedList<Attribute>();
		Collection<FeatureGenerator> generators;
		while ((generators = applicableGenerators()).size() > 0) {
			// as a side effect, generated attributes are removed from
			// allAttributes
			List<Attribute> generatedAtts = FeatureGenerator.generateAll(exampleTable, generators);
			allGeneratedAttributes.addAll(generatedAtts);
		}

		Iterator<Attribute> i = allAttributes.values().iterator();
		while (i.hasNext()) {
			exampleSet.getLog().logError("Could not generate attribute " + i.next());
		}
		if (allAttributes.size() > 0)
			throw new GenerationException("Couldn't generate all attributes! Failing: " + allAttributes);

		// replace generated attribute names by user specified names and add the
		// new
		// attributes to the example set
		// The reason: newAttributes is only a dummy list which only keeps the
		// construction description and names.
		// The actually created attributes are stored in allGeneratedAttributes
		// (only these are part of an ET!)
		i = newAttributes.iterator();
		while (i.hasNext()) {
			Attribute attribute = i.next();
			Attribute generatedAttribute = null;
			Iterator<Attribute> k = allGeneratedAttributes.iterator();
			// try to find the actual attribute in the newly constructed
			while (k.hasNext()) {
				Attribute currentAttribute = k.next();
				if (currentAttribute.getConstruction().equals(attribute.getConstruction())) {
					generatedAttribute = currentAttribute;
					break;
				}
			}
			// not found? try the original attributes in example table (maybe construction was identity)
			if (generatedAttribute == null) {
				for (int a = 0; a < exampleSet.getExampleTable().getAttributeCount(); a++) {
					Attribute currentAttribute = exampleSet.getExampleTable().getAttribute(a);
					if (currentAttribute.getConstruction().equals(attribute.getConstruction())) {
						generatedAttribute = currentAttribute;
						break;
					}
				}
			}
			if (generatedAttribute != null) {
				generatedAttribute.setName(attribute.getName());
				if (!exampleSet.getAttributes().contains(generatedAttribute))
					exampleSet.getAttributes().addRegular(generatedAttribute);
			}
		}

		// delete intermediate attributes
        Iterator<Attribute> a = allGeneratedAttributes.iterator();
        while (a.hasNext()) {
            Attribute removeCandidate = a.next();
            Iterator<Attribute> n = newAttributes.iterator();
            while (n.hasNext()) {
                Attribute newAttribute = n.next();
                if (!removeCandidate.getConstruction().equals(newAttribute.getConstruction())) {
                    a.remove();
                    break;
                }
            }
        }
		allGeneratedAttributes.removeAll(newAttributes);
		exampleSet.getLog().log("Removing " + allGeneratedAttributes.size() + " intermediate attributes.");
		i = allGeneratedAttributes.iterator();
		while (i.hasNext()) {
			exampleTable.removeAttribute(i.next());
		}
	}
}
