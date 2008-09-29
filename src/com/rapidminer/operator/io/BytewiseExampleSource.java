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
package com.rapidminer.operator.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.DataRowFactory;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeFile;
import com.rapidminer.parameter.UndefinedParameterError;


/**
 * Superclass for file data source operators which read
 * the file byte per byte into a byte array and extract
 * the actual data from that array. This class provides
 * some methods to extract integer and floating point
 * values from such an array.
 * 
 * @author Tobias Malbrecht
 * @version $Id: BytewiseExampleSource.java,v 1.3 2008/08/27 16:14:45 tobiasmalbrecht Exp $
 */
public abstract class BytewiseExampleSource extends Operator {
	
	/** The parameter name for &quot;Name of the file to read the data from.&quot; */
	public static final String PARAMETER_FILENAME = "filename";

	/** The parameter name for &quot;Determines, how the data is represented internally.&quot; */
	public static final String PARAMETER_DATAMANAGEMENT = "datamanagement";
	
	/** A generic wrong file format error message. */
	protected static final String GENERIC_ERROR_MESSAGE = "Wrong file format";
	
	/** A even more generic error message. */
	protected static final String UNSPECIFIED_ERROR_MESSAGE = "Unspecified error";
	
	/** The length of a byte measured in bytes. */
	protected static final int LENGTH_BYTE = 1;
	
	/** The length of an int measured in bytes. */
	protected static final int LENGTH_INT_32 = 4;
	
	/** The length of a double measured in bytes. */
	protected static final int LENGTH_DOUBLE = 8;
	
	private static final Class[] INPUT_CLASSES = {};

    private static final Class[] OUTPUT_CLASSES = { ExampleSet.class };
    
	public BytewiseExampleSource(OperatorDescription description) {
		super(description);
	}

    public IOObject[] apply() throws OperatorException {
        File file = getParameterAsFile(PARAMETER_FILENAME);
    	DataRowFactory dataRowFactory = new DataRowFactory(getParameterAsInt(PARAMETER_DATAMANAGEMENT), '.');
    	ExampleSet result = null;
        
    	// read file and construct example set
    	try {
        	result = readFile(file, dataRowFactory);
    	} catch (IOException e) {
    		throw new UserError(this, 302, file, e.getMessage());
    	}

        // verify that the result is not null
        if (result == null) {
        	throw new UserError(this, 302, file, UNSPECIFIED_ERROR_MESSAGE);
        }
        
        // verify that the resulting example set is not empty
        if (result.size() == 0) {
            throw new UserError(this, 117);
        }
       	return new IOObject[] { result };
    }

    /**
     * Returns the suffix of the files which should be read
     * by the input operator. 
     */
    protected abstract String getFileSuffix();
    
	/**
	 * Reads the given file and constructs an example set from the
	 * read data.
	 */
    protected abstract ExampleSet readFile(File file, DataRowFactory dataRowFactory) throws IOException, UndefinedParameterError;
	
	/**
	 * Reads a number (specified by length) of bytes from a given
	 * file reader into a byte array beginning at index 0.
	 */
    protected int read(FileInputStream fileReader, byte[] buffer, int length) throws IOException {
    	final int offset = 0;
    	return read(fileReader, buffer, offset, length);
    }
    
	/**
	 * Reads a number (specified by length) of bytes from a given
	 * file reader into a byte array beginning at the given offset.
	 */
    protected int read(FileInputStream fileReader, byte[] buffer, int offset, int length) throws IOException {
    	int readLength = fileReader.read(buffer, offset, length);
        if (readLength != length)
        	throw new IOException("wrong byte length");
        return readLength;
    }

	/**
	 * Reads a number (specified by length) of bytes from a given
	 * file reader into a byte array beginning at index 0. No read
	 * length verification is performed.
	 */
    protected int readWithoutLengthCheck(FileInputStream fileReader, byte[] buffer, int length) throws IOException {
    	return fileReader.read(buffer, 0, length);
    }
    
    /**
     * Reads bytes from a given file reader until either a certain 
     * character is read, the buffer is completely filled or the
     * end of file is reached. 
     */
    protected int read(FileInputStream fileReader, byte[] buffer, char divider) throws IOException {
    	int index = 0;
    	do {
    		byte readByte = (byte) (0x000000FF & fileReader.read());
    		if (readByte == -1 || readByte == (byte) divider) {
        		index++;
    			return index;
    		}
    		buffer[index] = readByte;
    		index++;
    	} while (index < buffer.length);
    	return index;
    }

    /**
     * Reads bytes from a given file reader until either a specified
     * character sequence is read, the buffer is completely filled or the
     * end of file is reached. 
     */
    protected int read(FileInputStream fileReader, byte[] buffer, char[] divider) throws IOException {
    	int index = 0;
    	int dividerIndex = 0;
    	do {
    		byte readByte = (byte) (0x000000FF & fileReader.read());
    		if (readByte == -1) {
    			index++;
    			return index;
    		}
    		if (readByte == divider[dividerIndex]) {
    			dividerIndex++;
    		}
    		if (dividerIndex == divider.length) {
    			index -= dividerIndex - 1;
        		for (int i = index; i < index + dividerIndex; i++) {
        			if (i >= buffer.length) {
        				break;
        			}
        			buffer[i] = 0;
        		}
        		return index;
    		}
    		buffer[index] = readByte;
    		index++;
    	} while (index < buffer.length);
    	return index;
    }

	/**
	 * Extracts a 2-byte (short) int from a byte array.
	 */
	protected int extract2ByteInt(byte[] buffer, int offset, boolean reverseEndian) {
    	int r = 0;
    	if (reverseEndian) {
    		r = (buffer[offset + 1] << 8) + (0x000000FF & buffer[offset]);
        } else {
        	r = (buffer[offset] << 8) + (0x000000FF & buffer[offset + 1]); 
        }
        return r;
    }
    
    /**
     * Extracts an int from a byte array.
     */
    protected int extractInt(byte[] buffer, int offset, boolean reverseEndian) {
        int r = 0;
        if (reverseEndian) {
        	for (int i = offset + 3; i >= offset; i--) {
                r = r << 8;
                r += 0x000000FF & buffer[i];
            }
        } else {
            for (int i = offset; i < offset + 4; i++) {
                r = r << 8;
                r += 0x000000FF & buffer[i];
            }
        }
        return r;
    }

    /**
     * Extracts a float from a byte array.
     */
    protected float extractFloat(byte[] value, int offset, boolean reverseEndian) {
        int bits = 0;
        if (reverseEndian) {
            for (int i = offset + 3; i >= offset; i--) {
                bits = bits << 8;
                bits += 0x000000FF & value[i];
            }
        } else {
            for (int i = offset; i < offset + 4; i++) {
                bits = bits << 8;
                bits += 0x000000FF & value[i];
            }
        }
        return java.lang.Float.intBitsToFloat(bits);
    }
    
    /**
     * Extracts a double from a byte array.
     */
    protected double extractDouble(byte[] value, int offset, boolean reverseEndian) {
        long bits = 0;
        if (reverseEndian) {
            for (int i = offset + 7; i >= offset; i--) {
                bits = bits << 8;
                bits += 0x000000FF & value[i];
            }
        } else {
            for (int i = offset; i < offset + 8; i++) {
                bits = bits << 8;
                bits += 0x000000FF & value[i];
            }
        }
        return java.lang.Double.longBitsToDouble(bits);
    }
    
    /**
     * Extracts string from byte array.
     */
    protected String extractString(byte[] value, int offset, int length) {
		/* TODO: Shevek suggests this use a Charset for safety. */
        return (new String(value, offset, length)).trim();
    }
    
    public Class<?>[] getInputClasses() {
        return INPUT_CLASSES;
    }

    public Class<?>[] getOutputClasses() {
        return OUTPUT_CLASSES;
    }
	
    public List<ParameterType> getParameterTypes() {
        List<ParameterType> types = super.getParameterTypes();
        ParameterType type = new ParameterTypeFile(PARAMETER_FILENAME, "Name of the file to read the data from.", getFileSuffix(), false);
        type.setExpert(false);
        types.add(type);
        type = new ParameterTypeCategory(PARAMETER_DATAMANAGEMENT, "Determines, how the data is represented internally.", DataRowFactory.TYPE_NAMES, DataRowFactory.TYPE_DOUBLE_ARRAY);
        type.setExpert(true);
        types.add(type);
        return types;
    }
}
