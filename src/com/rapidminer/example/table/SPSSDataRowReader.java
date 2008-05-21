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
package com.rapidminer.example.table;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.tools.LogService;
import com.rapidminer.tools.Ontology;
import com.rapidminer.tools.RandomGenerator;
import com.rapidminer.tools.Tools;
import com.rapidminer.tools.att.AttributeSet;


/**
 * SPSSDataRowReader implements a DataRowReader that reads DataRows from an SPSS
 * file.
 * 
 * @author Tobias Malbrecht, Ingo Mierswa
 * @version $Id: SPSSDataRowReader.java,v 1.5 2006/10/02 21:45:26 ingomierswa
 *          Exp $
 */
public class SPSSDataRowReader extends AbstractDataRowReader {

    private static class SPSSFileHeader {
    	
        private static final int RECORD_NR = 0x24464C32; // "$FL2"

        private static final int LENGTH = 176;

        private String productName;

        private int layoutCode;

        private int caseSize;

        private boolean compressed;

        private int weightIndex;

        private int numberOfCases;

        private double bias;

        private String date;

        private String time;

        private String fileLabel;
    }

    private static class Variable {
    	
        private static final int LENGTH = 32;

        private static final int NOMINAL = 1;

        private static final int ORDINAL = 2;

        private static final int CONTINUOUS = 3;

        private int type;

        private boolean labeled;

        private int numberOfMissingValues;

        private String name;

        private String label;

        private int length;

        private double[] missingValues;

        private ValueLabels valueLabels;

        private int measure;

        public Variable() {
            length = LENGTH;
        }
    }

    private static class ValueLabels extends LinkedHashMap<Double, String> {
    	
        private static final long serialVersionUID = 42L;

        private static final int RECORD_NR = 3;
    }

    private static class ValueLabelVariable {
        private static final int RECORD_NR = 4;
    }

    private static class Document {
        private static final int RECORD_NR = 6;
    }

    private static class DictionaryTermination {
        private static final int RECORD_NR = 999;
    }

    FileInputStream fileReader = null;

    byte[] readBuffer = new byte[500];

    boolean reverseEndian = false;

    SPSSFileHeader fileHeader = new SPSSFileHeader();

    List<Variable> variables = new LinkedList<Variable>();

    LinkedHashMap<Integer, Integer> variableNrTranslations = new LinkedHashMap<Integer, Integer>();

    AttributeSet attributeSet = null;

    Attribute[] attributes = null;

    Attribute weight = null;

    private boolean eof = false;

    private boolean lineRead = false;

    private double sampleRatio = 1.0d;

    private int sampleSize = -1;

    public static final int USE_VAR_NAME = 0;

    public static final int USE_VAR_LABEL = 1;

    public static final int USE_VAR_NAME_LABELED = 2;

    public static final int USE_VAR_LABEL_NAMED = 3;

    public static final String[] ATTRIBUTE_NAMING_MODES = { 
    	"Name", 
    	"Label",
        "Name (Label)", 
        "Label (Name)"
    };

    private boolean useValueLabels = false;

    private boolean recodeUserMissings = true;

    private int linesRead = 0;

    private int commandCodeCounter = 0;

    String[] data = null;

    //DataRowFactory dataRowFactory = null;

    /**
     * @param factory A {@link DataRowFactory}, that creates {@link DataRow}s.
     * @param file SPSS file which is to be read.
     * @param attributeNamingMode Determines which SPSS variable properties should be used for attribute naming.
     * @param useValueLabels Use SPSS value labels as attribute values.
     * @param recodeUserMissings Recode SPSS user defined missings to RapidMiner missing values.
     * @param sampleRatio Ratio of examples that should be sampled.
     * @param sampleSize Total number of examples which should be read.
     */
    public SPSSDataRowReader(DataRowFactory factory, File file,
            int attributeNamingMode, boolean useValueLabels,
            boolean recodeUserMissings, double sampleRatio, int sampleSize)
            throws IOException {
        super(factory);

        //dataRowFactory = factory;
        fileReader = new FileInputStream(file);

        this.useValueLabels = useValueLabels;
        this.recodeUserMissings = recodeUserMissings;
        this.sampleRatio = sampleRatio;
        this.sampleSize = sampleSize;

        // read file header
        int readLength = fileReader.read(readBuffer, 0, SPSSFileHeader.LENGTH);
        checkForCorrectLength(readLength, SPSSFileHeader.LENGTH);
        if (extractInt(readBuffer, 0) == SPSSFileHeader.RECORD_NR) {
            fileHeader.productName = extractString(readBuffer, 4, 60); // IM
            fileHeader.layoutCode = extractInt(readBuffer, 64);
            if (fileHeader.layoutCode != 2) {
                reverseEndian = true;
                fileHeader.layoutCode = extractInt(readBuffer, 64);
            }
            fileHeader.caseSize = extractInt(readBuffer, 68);
            fileHeader.compressed = ((extractInt(readBuffer, 72) == 1) ? true
                    : false);
            fileHeader.weightIndex = extractInt(readBuffer, 76); // IM
            fileHeader.numberOfCases = extractInt(readBuffer, 80); // IM
            fileHeader.bias = extractDouble(readBuffer, 84);
            fileHeader.date = extractString(readBuffer, 92, 9); // IM
            fileHeader.time = extractString(readBuffer, 101, 8); // IM
            fileHeader.fileLabel = extractString(readBuffer, 109, 64); // IM
            StringBuffer logMessage = new StringBuffer("SPSSExampleSource reads "
                    + file.getAbsolutePath() + Tools.getLineSeparator());
            logMessage.append((fileHeader.compressed ? "" : "un")
                    + "compressed, written by  " + fileHeader.productName
                    + "  at " + fileHeader.time + ", " + fileHeader.date + Tools.getLineSeparator());
            if (fileHeader.fileLabel.length() == 0)
                logMessage.append("file label is " + fileHeader.fileLabel
                        + Tools.getLineSeparator());
            else
                logMessage.append("no file label, ");
            logMessage.append("contains " + fileHeader.numberOfCases
                    + " examples, case size is " + fileHeader.caseSize + "x8="
                    + fileHeader.caseSize * 8 + " Bytes" + Tools.getLineSeparator());
            logMessage.append("weight index is " + fileHeader.weightIndex
                    + Tools.getLineSeparator());
            LogService.getGlobal().log(logMessage.toString(), LogService.STATUS);
        }

        // read variables
        int currVarNr = 0;
        for (int i = 0; i < fileHeader.caseSize; i++) {
            readLength = fileReader.read(readBuffer, 0, Variable.LENGTH);
            checkForCorrectLength(readLength, Variable.LENGTH);
            if (extractInt(readBuffer, 0) != 2) {
                throw new IOException("File corrupt (missing variable definitions)");
            }
            Variable currentVar = new Variable();
            currentVar.type = extractInt(readBuffer, 4);
            currentVar.labeled = ((extractInt(readBuffer, 8) == 1) ? true
                    : false);
            currentVar.numberOfMissingValues = extractInt(readBuffer, 12);
            currentVar.name = extractString(readBuffer, 24, 8);
            if (currentVar.labeled) {
                readLength = fileReader.read(readBuffer, currentVar.length, 4);
                checkForCorrectLength(readLength, 4);
                int labelLength = extractInt(readBuffer, currentVar.length);
                currentVar.length += 4;
                int adjLabelLength = labelLength;
                if (labelLength % 4 != 0) {
                    adjLabelLength = labelLength + 4 - (labelLength % 4);
                }
                readLength = fileReader.read(readBuffer, currentVar.length, adjLabelLength);
                checkForCorrectLength(readLength, adjLabelLength);
                currentVar.label = extractString(readBuffer, currentVar.length, adjLabelLength);
                currentVar.length += adjLabelLength;
            }
            if (currentVar.numberOfMissingValues != 0) {
                readLength = fileReader.read(readBuffer, currentVar.length, currentVar.numberOfMissingValues * 8);
                checkForCorrectLength(readLength, currentVar.numberOfMissingValues * 8);
                currentVar.missingValues = new double[currentVar.numberOfMissingValues];
                for (int j = 0; j < currentVar.numberOfMissingValues; j++) {
                    currentVar.missingValues[j] = extractDouble(readBuffer, currentVar.length
                            + j * 8);
                }
                currentVar.length += currentVar.numberOfMissingValues * 8;
            }
            if (currentVar.type != -1) {
                variables.add(currentVar);
                variableNrTranslations.put(i, currVarNr);
                currVarNr++;
            }
        }

        // read other header records
        boolean valueLabelsRead = false;
        ValueLabels currentValLabels = null;
        for (;;) {
            int count = 0;
            boolean terminated = false;
            readLength = fileReader.read(readBuffer, 0, 4);
            checkForCorrectLength(readLength, 4);
            int recordType = extractInt(readBuffer, 0);

            switch (recordType) {
            case ValueLabels.RECORD_NR:
                readLength = fileReader.read(readBuffer, 4, 4);
                checkForCorrectLength(readLength, 4);
                count = extractInt(readBuffer, 4);
                currentValLabels = new ValueLabels();
                for (int i = 0; i < count; i++) {
                    readLength = fileReader.read(readBuffer, 0, 8);
                    checkForCorrectLength(readLength, 8);
                    double labelValue = extractDouble(readBuffer, 0);
                    readLength = fileReader.read(readBuffer, 8, 1);
                    checkForCorrectLength(readLength, 1);
                    int labelLength = readBuffer[8];
                    int adjLabelLength = labelLength + 8 - (labelLength % 8)
                            - 1;
                    readLength = fileReader.read(readBuffer, 9, adjLabelLength);
                    checkForCorrectLength(readLength, adjLabelLength);
                    String labelLabel = extractString(readBuffer, 9, adjLabelLength);
                    currentValLabels.put(labelValue, labelLabel);
                }
                valueLabelsRead = true;
                break;
            case ValueLabelVariable.RECORD_NR:
                if (!valueLabelsRead) {
                    throw new IOException("Value labels not read");
                }
                valueLabelsRead = false;
                readLength = fileReader.read(readBuffer, 0, 4);
                checkForCorrectLength(readLength, 4);
                count = extractInt(readBuffer, 0);
                for (int i = 0; i < count; i++) {
                    readLength = fileReader.read(readBuffer, 0, 4);
                    checkForCorrectLength(readLength, 4);
                    int varNr = variableNrTranslations.get(extractInt(readBuffer, 0) - 1);
                    if (varNr < variables.size()) {
                        Variable var = variables.get(varNr);
                        var.valueLabels = currentValLabels;
                    }
                }
                break;
            case Document.RECORD_NR:
                readLength = fileReader.read(readBuffer, 4, 4);
                checkForCorrectLength(readLength, 4);
                count = extractInt(readBuffer, 4);
                for (int i = 0; i < count; i++) {
                    readLength = fileReader.read(readBuffer, 0, 80); // ignored at the moment
                    checkForCorrectLength(readLength, 80);
                }
                break;
            case 7:
                readLength = fileReader.read(readBuffer, 4, 12);
                checkForCorrectLength(readLength, 12);
                int subType = extractInt(readBuffer, 4);
                int size = extractInt(readBuffer, 8);
                count = extractInt(readBuffer, 12);
                switch (subType) {
                case 3:
                    readLength = fileReader.read(readBuffer, 0, 32);
                    checkForCorrectLength(readLength, 32);
                    break;
                case 4:
                    readLength = fileReader.read(readBuffer, 0, 24);
                    checkForCorrectLength(readLength, 24);
                    break;
                case 11:
                    for (int i = 0; i < variables.size(); i++) {
                        readLength = fileReader.read(readBuffer, 0, 12);
                        checkForCorrectLength(readLength, 12);
                        Variable var = variables.get(i);
                        var.measure = extractInt(readBuffer, 0);
                        extractInt(readBuffer, 4); // IM
                        extractInt(readBuffer, 8); // IM
                    }
                    break;
                case 13:
                    readBuffer = new byte[count * size];
                    readLength = fileReader.read(readBuffer, 0, count * size);
                    checkForCorrectLength(readLength, count * size);
                    readBuffer = new byte[500];
                    break;
                default:
                    readLength = fileReader.read(readBuffer, 0, count * size);
                checkForCorrectLength(readLength, count * size);
                    break;
                }
                break;
            case DictionaryTermination.RECORD_NR:
                readLength = fileReader.read(readBuffer, 4, 4);
                checkForCorrectLength(readLength, 4);
                terminated = true;
                break;
            default:
                break;
            }
            if (terminated) {
                break;
            }
        }

        // create attributes from variables
        attributeSet = new AttributeSet();
        Attribute attribute = null;
        for (int i = 0; i < variables.size(); i++) {
            Variable var = variables.get(i);
            String varName = null;
            switch (attributeNamingMode) {
            case USE_VAR_NAME:
                varName = var.name;
                break;
            case USE_VAR_LABEL:
                varName = var.label;
                break;
            case USE_VAR_NAME_LABELED:
                varName = var.name + " (" + var.label + ")";
                break;
            case USE_VAR_LABEL_NAMED:
                varName = var.label + " (" + var.name + ")";
                break;
            default:
                varName = var.name;
            }
            if (var.type == 0) {
                switch (var.measure) {
                case Variable.NOMINAL:
                    attribute = AttributeFactory.createAttribute(varName, Ontology.NOMINAL);
                    break;
                case Variable.ORDINAL:
                    attribute = AttributeFactory.createAttribute(varName, Ontology.ORDERED);
                    break;
                case Variable.CONTINUOUS:
                    attribute = AttributeFactory.createAttribute(varName, Ontology.NUMERICAL);
                    break;
                default:
                    attribute = AttributeFactory.createAttribute(varName, Ontology.NUMERICAL);
                }
            } else {
                attribute = AttributeFactory.createAttribute(varName, Ontology.STRING);
            }

            // map strings to values for nominal attributes
            if (var.type == 0 && var.measure != Variable.CONTINUOUS) {
                if (var.valueLabels != null) {
                    Iterator<Double> iterator = var.valueLabels.keySet().iterator();
                    while (iterator.hasNext()) {
                        Double numValue = iterator.next();
                        boolean missing = false;
                        if (recodeUserMissings) {
                            for (int j = 0; j < var.numberOfMissingValues; j++) {
                                if (numValue == var.missingValues[j]) {
                                    missing = true;
                                    break;
                                }
                            }
                        }
                        if (!missing) {
                            if (useValueLabels) {
                                attribute.getMapping().mapString(var.valueLabels.get(numValue));
                            } else {
                            	attribute.getMapping().mapString(java.lang.Double.toString(numValue));
                            }
                        }
                    }
                }
            }
            attributeSet.addAttribute(attribute);
        }

        // store attributes into array
        Object[] obj = attributeSet.getAllAttributes().toArray();
        attributes = new Attribute[obj.length];
        for (int i = 0; i < obj.length; i++) {
            attributes[i] = (Attribute) obj[i];
        }

        if (fileHeader.weightIndex != 0) {
        	weight = attributes[variableNrTranslations.get(fileHeader.weightIndex - 1)];
        }
    }

    private void checkForCorrectLength(int actualLength, int targetLength) throws IOException {
        if (actualLength != targetLength)
        	throw new IOException("SPSSReader: wrong byte length");
    }
    
    /** Extracts int from byte array. */
    private int extractInt(byte[] value, int offset) {
        int r = 0;
        if (reverseEndian) {
            for (int i = offset + 3; i >= offset; i--) {
                r = r << 8;
                r += 0x000000FF & value[i];
            }
        } else {
            for (int i = offset; i < offset + 4; i++) {
                r = r << 8;
                r += 0x000000FF & value[i];
            }
        }
        return r;
    }

    /** Extracts double from byte array. */
    private double extractDouble(byte[] value, int offset) {
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

    /** Extracts string from byte array. */
    private String extractString(byte[] value, int offset, int length) {
		/* TODO: Shevek suggests this use a Charset for safety. */
        return (new String(value, offset, length)).trim();
    }

    /** Reads another line. */
    private String[] readLine() throws IOException {
        int bytesRead = 0;
        String[] strings = new String[variables.size()];
        if (fileHeader.compressed) {
            for (int i = 0; i < variables.size(); i++) {
                boolean readValue = false;
                String value = null;

                for (;;) {
                    if (commandCodeCounter % 8 == 0) {
                        bytesRead = fileReader.read(readBuffer, 0, 8);
                        if (bytesRead == -1) {
                            eof = true;
                            strings = null;
                            break;
                        }
                        commandCodeCounter = 0;
                    }
                    int commandCode = 0x000000FF & readBuffer[commandCodeCounter];
                    switch (commandCode) {
                    case 0:
                        break;
                    case 252:
                        // clear rest of readBuffer
                        for (int j = commandCodeCounter + 1; j < 8; j++) {
                            readBuffer[j] = (byte) 0;
                        }
                        eof = true;
                        break;
                    case 253:
                        bytesRead = fileReader.read(readBuffer, 8, 8);
                        if (bytesRead == -1) {
                            throw new IOException("File corrupt (data inconsistency)");
                        }
                        if (variables.get(i).type == 0) {
                            value = java.lang.Double.toString(extractDouble(readBuffer, 8));
                            readValue = true;
                        } else {
                            if (value == null) {
                                value = new String(readBuffer, 8, 8);
                            } else {
                                value = value + new String(readBuffer, 8, 8);
                            }
                            if (value.length() >= variables.get(i).type) {
                                value = value.trim();
                                readValue = true;
                            }
                        }
                        break;
                    case 254:
                        if (value == null) {
                            value = "        ";
                        } else {
                            value = value + "        ";
                        }
                        if (value.length() >= variables.get(i).type) {
                            value = value.trim();
                            readValue = true;
                        }
                        break;
                    case 255:
                        value = null;
                        readValue = true;
                        break;
                    default:
                        double numValue = commandCode - fileHeader.bias;
                        Variable currVar = variables.get(i);
                        value = java.lang.Double.toString(numValue);
                        if (currVar.measure != Variable.CONTINUOUS) {
                            if (useValueLabels) {
                                if (currVar.valueLabels != null) {
                                    String label = currVar.valueLabels.get(numValue);
                                    value = label;
                                }
                            }
                        }
                        if (recodeUserMissings) {
                            for (int j = 0; j < currVar.numberOfMissingValues; j++) {
                                if (Tools.isEqual(numValue, currVar.missingValues[j])) {
                                    value = null;
                                }
                            }
                        }
                        readValue = true;
                        break;
                    }
                    commandCodeCounter++;
                    if (readValue) {
                        strings[i] = value;
                        break;
                    }
                    if (eof) {
                        break;
                    }
                }
            }
        } else {
            for (int i = 0; i < variables.size(); i++) {
                bytesRead = fileReader.read(readBuffer, 0, 8);
                strings[i] = java.lang.Double.toString(extractDouble(readBuffer, 8));
            }
        }
        return strings;
    }

    /**
     * Checks if another line exists and reads. The next line is only read once
     * even if this method is invoked more than once.
     */
    public boolean hasNext() {
        if (lineRead)
            return !eof;
        if (sampleSize > -1 && linesRead >= sampleSize)
            return false;
        try {
            boolean ok = false;
            while (!ok) {
                data = readLine();
                if (eof) {
                    try {
                        fileReader.close();
                    } catch (IOException e) {
                        LogService.getGlobal().log(e.getMessage(), LogService.ERROR);
                    }
                }
                if ((eof)
                        || (sampleSize != -1)
                        || (sampleRatio == 1.0d)
                        || (RandomGenerator.getGlobalRandomGenerator().nextDouble() < sampleRatio))
                    ok = true;

            }
        } catch (IOException e) {
            LogService.getGlobal().log(e.getMessage(), LogService.ERROR);
            return false;
        }
        lineRead = true;
        return (!eof);
    }

    /** Returns the next Example. */
    public DataRow next() {
        if (eof)
            return null;
        if (!lineRead)
            if (!hasNext())
                return null;
        DataRow dataRow = getFactory().create(data, attributes);
        linesRead++;
        lineRead = false;
        return dataRow;
    }

    /** Returns the example table which is read from file. */
    public ExampleSet getExampleSet() {
        ExampleTable table = new MemoryExampleTable(attributeSet.getAllAttributes(), this);
        ExampleSet exampleSet = table.createExampleSet(attributeSet);
        if (weight != null) {
        	exampleSet.getAttributes().setWeight(weight);
        }
        return exampleSet;
    }
}
