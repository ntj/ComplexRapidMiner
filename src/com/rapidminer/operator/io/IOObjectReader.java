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
package com.rapidminer.operator.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.List;
import java.util.zip.GZIPInputStream;

import com.rapidminer.operator.AbstractIOObject;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeFile;


/**
 * Generic reader for all types of IOObjects. Reads an IOObject from a file.
 * 
 * @author Ingo Mierswa
 * @version $Id: IOObjectReader.java,v 1.2 2007/06/15 16:58:37 ingomierswa Exp $
 */
public class IOObjectReader extends Operator {
	

	/** The parameter name for &quot;Filename of the object file.&quot; */
	public static final String PARAMETER_OBJECT_FILE = "object_file";
	public IOObjectReader(OperatorDescription description) {
		super(description);
	}
	
	/** Writes the attribute set to a file. */
	public IOObject[] apply() throws OperatorException {
		File objectFile = getParameterAsFile(PARAMETER_OBJECT_FILE);
		IOObject object = null;
		try {
            // try if the model was written as a serializable model
			ObjectInputStream objectIn = new ObjectInputStream(new FileInputStream(objectFile));
			object = (IOObject)objectIn.readObject();
			objectIn.close();
		} catch (Exception e) {
			log("Cannot deserialize binary object, trying XML deserialization...");
            // if not serialized, then try the usual model serialization (xml)
			InputStream in = null;
	        try {
	            in = new GZIPInputStream(new FileInputStream(objectFile));
	        } catch (IOException e1) {
	            try {
	                // maybe already uncompressed?
	                in = new FileInputStream(objectFile);
	            } catch (IOException e2) {
	                throw new UserError(this, e, 302, new Object[] { objectFile, e2.getMessage() });
	            }
	        }
	        
			try {
				object = AbstractIOObject.read(in);
	            in.close();
			} catch (IOException e3) {
				throw new UserError(this, e, 302, new Object[] { objectFile, e3.getMessage() });
			}
		}

		if (object == null) {
			throw new UserError(this, 302, new Object[] { objectFile, "cannot load object file" });
		} else {
			return new IOObject[] { object };
		}
	}

	public Class[] getInputClasses() {
		return new Class[0];
	}
	
	public Class[] getOutputClasses() {
		return new Class[] { IOObject.class };
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		types.add(new ParameterTypeFile(PARAMETER_OBJECT_FILE, "Filename of the object file.", "ioo", false));
		return types;
	}
}
