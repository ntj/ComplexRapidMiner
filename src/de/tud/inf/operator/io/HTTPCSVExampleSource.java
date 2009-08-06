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
package de.tud.inf.operator.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import com.rapidminer.Process;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ProcessRootOperator;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.io.SimpleExampleSource;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeFile;
import com.rapidminer.parameter.ParameterTypeString;


/**
 * <p>This operator can read csv files. All values must be separated by
 * &quot;,&quot;, by &quot;;&quot;, or by white space like tabs. 
 * The first line is used for attribute names as default.</p> 
 * 
 * <p>For other file formats or column separators you can
 * use in almost all cases the operator {@link SimpleExampleSource}
 * or, if this is not sufficient, the operator {@link ExampleSource}.</p>
 * 
 * @rapidminer.index HTTP-CSV
 * @author Ingo Mierswa, Peter Benjamin Volk
 * @version $Id: CSVExampleSource.java,v 1.5 2008/07/07 07:06:38 ingomierswa Exp $
 */
public class HTTPCSVExampleSource extends SimpleExampleSource {

	public static final String URL = "connectString";
	
	public HTTPCSVExampleSource(OperatorDescription description) {
		super(description);
	}
    
	public Class<?>[] getInputClasses() {
		return new Class[0];
	}

	public Class<?>[] getOutputClasses() {
		return new Class[] { ExampleSet.class };
	}
	
	public IOObject[] apply() throws OperatorException{
		//open HTTP connection and read data from proxy

		Random r = new Random();
		File tmpFile;
		try {
			tmpFile = File.createTempFile("httpcvssource_"+r.nextInt(), null);
			URL page;
			page = new URL(getParameterAsString(URL));
			 // Process the URL far enough to find the right handler
	    	URLConnection urlc = page.openConnection();
		    urlc.setUseCaches(false); // Don't look at possibly cached data
		    BufferedReader br = new BufferedReader(new InputStreamReader(urlc.getInputStream()));
		    String tmp = null;
		    FileWriter fw = new FileWriter(tmpFile);
		    while ((tmp = br.readLine() ) != null) {
		            fw.write(tmp+"\n");
		    }
		    // Close file writer
		    fw.close();
		    setParameter(super.PARAMETER_FILENAME, tmpFile.getCanonicalPath());
		} catch (MalformedURLException e) {
			throw new UserError(this, e, 302, new Object[] { e.getMessage() });
		} catch (IOException e1) {
			throw new UserError(this, e1, 302, new Object[] { e1.getMessage() });
		}
		
		
		IOObject[] retValues =  super.apply();
		
		return retValues;
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = new LinkedList<ParameterType>();
		types.add(new ParameterTypeString(URL, "Defines the URL that the csv file is read from", false));
		types.addAll(super.getParameterTypes());
		Iterator<ParameterType> p = types.iterator();
		while (p.hasNext()) {
			ParameterType type = p.next();
			if (type.getKey().equals(PARAMETER_READ_ATTRIBUTE_NAMES)) {
				type.setDefaultValue(true);
			} else if (type.getKey().equals(PARAMETER_FILENAME)) {
				((ParameterTypeFile)type).setExtension("csv");
				type.setDefaultValue("c:/temp");
				((ParameterTypeFile)type).setOptional(true);
				type.setHidden(true);	
			} else if (type.getKey().equals(PARAMETER_USE_QUOTES)) {
				type.setDefaultValue(true);
			} else if (type.getKey().equals(PARAMETER_COLUMN_SEPARATORS)) {
				type.setDefaultValue(",\\s*|;\\s*");
			}
		}
		
		return types;
	}
}
