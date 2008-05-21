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
package com.rapidminer.example;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

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
import com.rapidminer.tools.LoggingHandler;
import com.rapidminer.tools.Ontology;


/**
 * Parses a file containing construction descriptions and adds the new
 * attributes to the example set.
 * 
 * @author Simon Fischer, Ingo Mierswa
 * @version $Id: AttributeParser.java,v 1.7 2008/05/09 19:22:43 ingomierswa Exp $
 */
public class AttributeParser {

	/** The example table to which the attributes should be added. */
	private ExampleTable exampleTable;
	
	/** The attributes which should be constructed during this construction parsing process (including intermediate atts). */
	private List<Attribute> attributes2Construct = new LinkedList<Attribute>();

	
	public AttributeParser(ExampleTable et) {
		this.exampleTable = et;
	}
    
	/** Parses all lines. */
	public void generateAll(LoggingHandler logging, ExampleSet exampleSet, InputStream in) throws IOException, GenerationException {
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
                Attribute att = generateAttribute(logging, attributeString);
                if (att != null) {
                	if (attributeName != null) {
                		att.setName(attributeName);
                	}
                	exampleSet.getAttributes().addRegular(att);
                }
            }
        }
	}

	public Attribute generateAttribute(LoggingHandler logging, String constructionDescription) throws GenerationException {
		attributes2Construct.clear();
		parseAttributes(constructionDescription);
        return generate(logging, attributes2Construct);
	}
	
	
	// ===========================================================================

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

	/** Recursively parses the string starting at the current position. */
	private List<Attribute> parseAttributes(String construction) throws GenerationException {
		List<Attribute> attributes = new LinkedList<Attribute>();
		
		int start = 0;
		while (start < construction.length()) {
			int leftBr = construction.indexOf("(", start);
			int comma = construction.indexOf(",", start);
			if ((comma == -1) && (leftBr == -1)) { // no comma and left bracket
				int end = construction.length();
				String name = construction.substring(start, end).trim();
				if (name.startsWith(ConstantGenerator.FUNCTION_NAME)) {
					throw new GenerationException("The function name '" + ConstantGenerator.FUNCTION_NAME + "' must be used with empty arguments!");
				} else {
					Attribute attribute = AttributeFactory.createAttribute(name, Ontology.NUMERICAL);
					attributes.add(attribute);
					start = construction.length();
				}
			} else if ((leftBr == -1) || ((comma < leftBr) && (comma != -1))) {
				int end = comma;
				String name = construction.substring(start, end).trim();
				if (name.startsWith(ConstantGenerator.FUNCTION_NAME)) {
					throw new GenerationException("The function name '" + ConstantGenerator.FUNCTION_NAME + "' must be used with empty arguments!");
				} else {
					Attribute attribute = AttributeFactory.createAttribute(name, Ontology.NUMERICAL);
					attributes.add(attribute);
					start = end + 1;
				}
			} else {
				int rightBr = getClosingBracketIndex(construction, leftBr);
				String functionName = construction.substring(start, leftBr).trim();
				
				List<Attribute> argumentList = parseAttributes(construction.substring(leftBr + 1, rightBr).trim());
				ConstructionDescription[] argumentDescriptions = new ConstructionDescription[argumentList.size()];
				for (int i = 0; i < argumentDescriptions.length; i++) {
					argumentDescriptions[i] = argumentList.get(i).getConstruction();
				}

				Attribute generated = AttributeFactory.createAttribute(functionName, argumentDescriptions);
				attributes.add(generated);
				attributes2Construct.add(generated);

				start = construction.indexOf(",", rightBr) + 1;
				if (start <= 0)
					start = construction.length();
			}
		}
		return attributes;
	}

	private Attribute getAttributeInTable(ExampleTable table, Attribute attribute) {
		for (int i = 0; i < table.getNumberOfAttributes(); i++) {
			Attribute a = table.getAttribute(i);
			if ((a != null) && (a.getName().equals(attribute.getName())))
				return a;
		}
		return null;
	}
	
	private Attribute getConstructedAttributeInTable(ExampleTable table, Attribute attribute) {
		for (int i = 0; i < table.getNumberOfAttributes(); i++) {
			Attribute a = table.getAttribute(i);
			if ((a != null) && (a.getConstruction().equals(attribute.getConstruction())))
				return a;
		}
		return null;
	}
	
	private FeatureGenerator getGenerator(Attribute a) throws GenerationException {
		FeatureGenerator fg = FeatureGenerator.createGeneratorForFunction(a.getConstruction().getFunction());
		if (fg != null) {
			Attribute[] args = new Attribute[a.getConstruction().getArguments().length];
			for (int c = 0; c < args.length; c++) {
				args[c] = a.getConstruction().getArguments()[c].getAttribute();
			}
			if (args != null) {
				for (int n = 0; n < args.length; n++) {
					Attribute actualAttribute = getAttributeInTable(exampleTable, args[n]);
					if (actualAttribute == null) {
						actualAttribute = getConstructedAttributeInTable(exampleTable, args[n]);
					}
					if (args[n] == null) {
						throw new GenerationException("No attribute with name " + args[n].getName() + " was found.");	
					}
					args[n] = actualAttribute;
				}
				if (fg.getInputAttributes().length != args.length) {
					throw new GenerationException(fg + " has arity " + fg.getInputAttributes().length + "!");
				} else {
					fg.setArguments(args);
				}
			}
			return fg;
		} else {
			throw new GenerationException("No generator found for function " + a.getConstruction().getFunction());
		}
	}

	/** Generates new attributes as long as it is possible. */
	private Attribute generate(LoggingHandler logging, List<Attribute> attributes2Construct) throws GenerationException {
		List<Attribute> allGeneratedAttributes = new LinkedList<Attribute>();
		
		Attribute targetAttribute = null;
		for (Attribute constructionAttribute : attributes2Construct) {
			if (getAttributeInTable(exampleTable, constructionAttribute) == null) {
				FeatureGenerator fg = getGenerator(constructionAttribute);
				
				List<FeatureGenerator> generatorList = new LinkedList<FeatureGenerator>();
				generatorList.add(fg);
				List<Attribute> lastGeneratedAtts = FeatureGenerator.generateAll(exampleTable, generatorList);
				targetAttribute = lastGeneratedAtts.get(lastGeneratedAtts.size() - 1);
				allGeneratedAttributes.addAll(lastGeneratedAtts);	
			}
		}
		allGeneratedAttributes.clear();
		return targetAttribute;
	}
}
