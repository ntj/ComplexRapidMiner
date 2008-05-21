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
package com.rapidminer.tools;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Constructor;

import com.rapidminer.example.AttributeRole;
import com.rapidminer.example.NominalStatistics;
import com.rapidminer.example.NumericalStatistics;
import com.rapidminer.example.SimpleAttributes;
import com.rapidminer.example.UnknownStatistics;
import com.rapidminer.example.table.BinominalAttribute;
import com.rapidminer.example.table.BinominalMapping;
import com.rapidminer.example.table.NumericalAttribute;
import com.rapidminer.example.table.PolynominalAttribute;
import com.rapidminer.example.table.PolynominalMapping;
import com.rapidminer.operator.IOContainer;


/** 
 * This class handles all kinds in- and output write processes for all kinds of objects 
 * into and from XML. This class must use object streams since memory consumption is too
 * big otherwise. Hence, string based methods are no longer supported.
 * 
 * @author Ingo Mierswa
 * @version $Id: XMLSerialization.java,v 1.5 2008/05/09 19:22:55 ingomierswa Exp $
 */
public class XMLSerialization {

	private static XMLSerialization singleton;
	
	private com.thoughtworks.xstream.XStream xStream;
    
	public XMLSerialization(ClassLoader classLoader) {
		try {
            Class<?> xStreamClass = Class.forName("com.thoughtworks.xstream.XStream");
            Class generalDriverClass = Class.forName("com.thoughtworks.xstream.io.HierarchicalStreamDriver");
            Constructor constructor = xStreamClass.getConstructor(new Class[] { generalDriverClass });
            Class driverClass = Class.forName("com.thoughtworks.xstream.io.xml.XppDriver");
            xStream = (com.thoughtworks.xstream.XStream)constructor.newInstance(driverClass.newInstance());
			xStream.setMode(com.thoughtworks.xstream.XStream.ID_REFERENCES);  
        
			// define default aliases here
			addAlias("IOContainer", IOContainer.class);
			addAlias("PolynominalAttribute", PolynominalAttribute.class);
			addAlias("BinominalAttribute", BinominalAttribute.class);
			addAlias("NumericalAttribute", NumericalAttribute.class);
			
			addAlias("PolynominalMapping", PolynominalMapping.class);
			addAlias("BinominalMapping", BinominalMapping.class);
			
			addAlias("NumericalStatistics", NumericalStatistics.class);
			addAlias("NominalStatistics", NominalStatistics.class);
			addAlias("UnknownStatistics", UnknownStatistics.class);
			
			addAlias("SimpleAttributes", SimpleAttributes.class);
			addAlias("AttributeRole", AttributeRole.class);
			
			xStream.setClassLoader(classLoader);
		} catch (Throwable e) {
            LogService.getGlobal().log("Cannot initialize XML serialization. Probably the libraries 'xstream.jar' and 'xpp.jar' were not provided. XML serialization will not work!", LogService.ERROR);
		}
	}
	
	public static void init(ClassLoader classLoader) {
		singleton = new XMLSerialization(classLoader);
	}
	
	public void addAlias(String name, Class clazz) {
		if (xStream != null) {
			xStream.alias(name, clazz);
        }
	}
	
	public void writeXML(Object object, OutputStream out) throws IOException {
		if (xStream != null) {
			ObjectOutputStream xOut = xStream.createObjectOutputStream(new OutputStreamWriter(out));
			xOut.writeObject(object);
            // flush is necessary since close is only invoked on the underlying stream...  
			xOut.flush();
            // XXX: leak of resources here since no close is invoked?
		} else {
            LogService.getGlobal().log("Cannot write XML serialization. Probably the libraries 'xstream.jar' and 'xpp.jar' were not provided...", LogService.ERROR);
			throw new IOException("Cannot write object with XML serialization.");
		}
	}

	public Object fromXML(InputStream in) throws IOException {   
		if (xStream != null) {
			try {
				ObjectInputStream xIn = xStream.createObjectInputStream(new InputStreamReader(in));
				Object result = null;
				try {
					result = xIn.readObject();
				} catch (ClassNotFoundException e) {
					throw new IOException("Class not found: " + e.getMessage());
				}
                // XXX: leak of resources here since no close is invoked?
				return result;
			} catch (Throwable e) {
				throw new IOException("Cannot read from XML stream, wrong format: " + e.getMessage());
			}
		} else {
            LogService.getGlobal().log("Cannot read object from XML serialization. Probably the libraries 'xstream.jar' and 'xpp.jar' were not provided...", LogService.ERROR);
			throw new IOException("Cannot read object from XML serialization.");
		}
	}
	
	/** Returns the singleton instance. */
	public static XMLSerialization getXMLSerialization() {
		return singleton;
	}
}
