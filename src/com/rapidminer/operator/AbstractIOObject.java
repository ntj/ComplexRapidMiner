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
package com.rapidminer.operator;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.rapidminer.tools.LogService;
import com.rapidminer.tools.LoggingHandler;
import com.rapidminer.tools.XMLSerialization;


/** 
 * This is an abstract superclass for all IOObject. It provides basic implementations for all
 * methods of IOthe interface Object. In addition, it also provides static methods which can be 
 * used for reading IOObjects from XML strings and input streams / files containing the XML
 * serialization. 
 * 
 * @author Ingo Mierswa
 * @version $Id: AbstractIOObject.java,v 1.2 2007/07/10 20:50:51 ingomierswa Exp $
 */
public abstract class AbstractIOObject implements IOObject {

    /** The source of this IOObect. Might be null. */
    private String source = null;
    
    /** The current working operator. */
    private transient Operator currentWorkingOperator;
    
    /** Sets the source of this IOObject. */
    public void setSource(String sourceName) {
        this.source = sourceName;
    }

    /** Returns the source of this IOObject (might return null if the source is unknown). */
    public String getSource() {
        return source;
    }
    
    /** Gets the logging associated with the operator currently working on this 
     *  IOObject or the global log service if no operator was set. */
    public LoggingHandler getLog() {
        if (this.currentWorkingOperator != null) {
            return this.currentWorkingOperator.getLog();
        } else {
            return LogService.getGlobal();
        }
    }
    
    /** Sets the current working operator, i.e. the operator which is currently 
     *  working on this IOObject. This might be used for example for logging. */
    public void setWorkingOperator(Operator operator) {
        this.currentWorkingOperator = operator;
    }
    
	/**
	 * Returns not a copy but the very same object. This is ok for IOObjects
	 * which cannot be altered after creation. However, IOObjects which might be
	 * changed (e.g. {@link com.rapidminer.example.ExampleSet}s) should
	 * overwrite this method and return a proper copy.
	 */
	public IOObject copy() {
		return this;
	}
	
	/** Initializes the writing of this object. This method is invoked before
	 *  the actual writing is performed. The default implementation does nothing. 
     *  
     *  This method should also be used for clean up
     *  processes which should be performed before the actual writing is done. For 
     *  example, models might decide to keep the example set information directly
     *  after learning (e.g. for visualization reasons) but not to write them down.
     *  Please note that all fields will be written into files unless they are set
     *  to null in this method or they are marked as transient. */
	protected void initWriting() {}

	/** Just serializes this object with help of a {@link XMLSerialization}. 
     *  Initializes {@link #initWriting()} before the actual writing is performed. */
	public final void write(OutputStream out) throws IOException {
		initWriting();
		XMLSerialization.getXMLSerialization().writeXML(this, out);
	}

	/** Deserializes an IOObect from the given XML stream.
	 *
	 * @throws IOException if any IO error occurs.
	 * @throws IllegalStateException if {@link XMLSerialization#init(ClassLoader)} has never been called.
	 */
	public static IOObject read(InputStream in) throws IOException {
	    final XMLSerialization serializer = XMLSerialization.getXMLSerialization();
	    if (serializer == null)
	        throw new IllegalStateException("XMLSerialization not initialized, please invoke XMLSerialization.init(ClassLoader) before using this method.");
	    return (IOObject)serializer.fromXML(in);
	}
}
