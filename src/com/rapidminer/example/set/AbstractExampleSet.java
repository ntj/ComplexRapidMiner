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
package com.rapidminer.example.set;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import com.rapidminer.datatable.DataTable;
import com.rapidminer.datatable.DataTableExampleSetAdapter;
import com.rapidminer.example.Attribute;
import com.rapidminer.example.AttributeRole;
import com.rapidminer.example.AttributeWeights;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.Statistics;
import com.rapidminer.example.table.NumericalAttribute;
import com.rapidminer.example.table.SparseFormatDataRowReader;
import com.rapidminer.gui.RapidMinerGUI;
import com.rapidminer.gui.plotter.PlotterPanel;
import com.rapidminer.gui.tools.SwingTools;
import com.rapidminer.gui.viewer.DataViewer;
import com.rapidminer.gui.viewer.MetaDataViewer;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.MissingIOObjectException;
import com.rapidminer.operator.ProcessRootOperator;
import com.rapidminer.operator.ResultObjectAdapter;
import com.rapidminer.tools.Ontology;
import com.rapidminer.tools.Tools;


/**
 * Implements wrapper methods of abstract example set. Implements all
 * ResultObject methods.<br>
 * 
 * Apart from the interface methods the implementing classes must have a public
 * single argument clone constructor. This constructor is invoked by reflection
 * from the clone method. Do not forget to call the superclass method.
 * 
 * @author Ingo Mierswa, Simon Fischer
 * @version $Id: AbstractExampleSet.java,v 2.74 2006/03/27 13:21:58 ingomierswa
 *          Exp $
 */
public abstract class AbstractExampleSet extends ResultObjectAdapter implements ExampleSet {

    /** Maps attribute names to list of statistics objects. */
    private Map<String, List<Statistics>> statisticsMap = new HashMap<String, List<Statistics>>();
    
    /** This method overrides the implementation of ResultObjectAdapter and returns "ExampleSet". */
    public String getName() {
        return "ExampleSet";
    }
	
    
    
	// --- Visualisation and toString() methods ---

	public String toString() {
		StringBuffer str = new StringBuffer(Tools.classNameWOPackage(this.getClass()) + ":" + Tools.getLineSeparator());
		str.append(size() + " examples," + Tools.getLineSeparator());		
		str.append(getAttributes().size() + " regular attributes," + Tools.getLineSeparator());
		
		boolean first = true;
		Iterator<AttributeRole> s = getAttributes().specialAttributes();
		while (s.hasNext()) {
			if (first) {
				str.append("special attributes = {" + Tools.getLineSeparator());
				first = false;
			}
			AttributeRole special = s.next();
			str.append("    " + special.getSpecialName() + " = " + special.getAttribute() + Tools.getLineSeparator());
		}
		
		if (!first) {
			str.append("}");
		} else {
			str.append("no special attributes" + Tools.getLineSeparator());
		}
        
		return str.toString();
	}

    /** This method is used to create a {@link DataTable} from this example set. The default implementation
     *  returns an instance of {@link DataTableExampleSetAdapter}. The given IOContainer is used to check if 
     *  there are compatible attribute weights which would used as column weights of the returned table. 
     *  Subclasses might want to override this method in order to allow for other data tables. */
    private final DataTable createDataTable(IOContainer container) {
        AttributeWeights weights = null;
        if (container != null) {
            try {
                weights = container.get(AttributeWeights.class);
                for (Attribute attribute : getAttributes()) {
                    double weight = weights.getWeight(attribute.getName());
                    if (Double.isNaN(weight)) { // not compatible
                        weights = null;
                        break;
                    }
                }
            } catch (MissingIOObjectException e) {}
        }
        return  new DataTableExampleSetAdapter(this, weights);
    }
    
	/**
	 * Returns component with several views controlled by radio buttons. The first view is a meta data viewer, the
     * second a data viewer and the last one a plotter panel. For this plotter the data table created by {@link #createDataTable(IOContainer)}
     * is used.
	 */
	public Component getVisualizationComponent(final IOContainer container) {
		final JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
        
		// meta data html table view
        final MetaDataViewer metaDataViewer = new MetaDataViewer(this);
        
		// data html table view
		final DataViewer dataViewer = new DataViewer(this);

		// statistics plotter view
		DataTable dataTable = createDataTable(container);

		final PlotterPanel plotterComponent = new PlotterPanel(dataTable);
		
		// toggle radio button for views
		final JRadioButton metaDataButton = new JRadioButton("Meta Data View", true);
		metaDataButton.setToolTipText("Changes to a table showing information about all attributes.");
		metaDataButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				if (metaDataButton.isSelected()) {
					mainPanel.remove(1);
					mainPanel.add(metaDataViewer, BorderLayout.CENTER);
					mainPanel.repaint();
				}
			}
		});

		final JRadioButton dataButton = new JRadioButton("Data View", true);
		dataButton.setToolTipText("Changes to a table showing the complete example set.");
		dataButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				if (dataButton.isSelected()) {
					mainPanel.remove(1);
					mainPanel.add(dataViewer, BorderLayout.CENTER);
					mainPanel.repaint();
				}
			}
		});
		final JRadioButton plotButton = new JRadioButton("Plot View", false);
		plotButton.setToolTipText("Changes to a plot view of the example data.");
		plotButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (plotButton.isSelected()) {
					mainPanel.remove(1);
					mainPanel.add(plotterComponent, BorderLayout.CENTER);
					mainPanel.repaint();
				}
			}
		});
		ButtonGroup group = new ButtonGroup();
		group.add(metaDataButton);
		group.add(dataButton);
		group.add(plotButton);
		JPanel togglePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		togglePanel.add(metaDataButton);
		togglePanel.add(dataButton);
		togglePanel.add(plotButton);

		mainPanel.add(togglePanel, BorderLayout.NORTH);
		mainPanel.add(metaDataViewer, BorderLayout.CENTER);
		return mainPanel;
	}

    public List<Action> getActions() {
        List<Action> result = new LinkedList<Action>();

        result.add(new AbstractAction("Save...") {

            private static final long serialVersionUID = 763183727596275786L;

            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null, "In the following, you can save both a data file and an attribute description file.", "Save data and meta data", JOptionPane.INFORMATION_MESSAGE);
                File dataFile = SwingTools.chooseFile(null, null, false, "dat", "example set data file");
                try {
                    if (dataFile != null) {
                    	String encoding = "UTF-8";
                        writeDataFile(dataFile, NumericalAttribute.UNLIMITED_NUMBER_OF_DIGITS, true, false, false, encoding);
                        File attFile = SwingTools.chooseFile(null, dataFile, false, "aml", "attribute description file");
                        if (attFile != null) {
                            writeAttributeFile(attFile, dataFile, RapidMinerGUI.getMainFrame().getProcess().getRootOperator().getParameterAsString(ProcessRootOperator.PARAMETER_ENCODING)); 
                        }
                    }
                } catch (Exception ex) {
                    SwingTools.showSimpleErrorMessage("Cannot write example set to file '" + dataFile + "'", ex);
                }
            }
        });        
        return result;
    }
    	
	// -------------------- File Writing --------------------
    
	public void writeDataFile(File dataFile, int fractionDigits, boolean quoteWhitespace, boolean zipped, boolean append, String encoding) throws IOException {
		OutputStream outStream = null;
		if (zipped) {
			outStream = new GZIPOutputStream(new FileOutputStream(dataFile, append));
		} else {
			outStream = new FileOutputStream(dataFile, append);
		}
		PrintWriter out = new PrintWriter(new OutputStreamWriter(outStream, encoding));
		Iterator<Example> reader = iterator();
		while (reader.hasNext()) {
			out.println(reader.next().toDenseString(fractionDigits, quoteWhitespace));
		}
		out.close();
		outStream.close();
	}

    /** Writes the data into a sparse file format. */
	public void writeSparseDataFile(File dataFile, int format, int fractionDigits, boolean quoteWhitespace, boolean zipped, boolean append, String encoding) throws IOException {
		OutputStream outStream = null;
		if (zipped) {
			outStream = new GZIPOutputStream(new FileOutputStream(dataFile, append));
		} else {
			outStream = new FileOutputStream(dataFile, append);
		}
		PrintWriter out = new PrintWriter(new OutputStreamWriter(outStream, encoding));
		Iterator<Example> reader = iterator();
		while (reader.hasNext()) {
			out.println(reader.next().toSparseString(format, fractionDigits, quoteWhitespace));
		}
		out.close();
		outStream.close();
	}
    
	/**
	 * Writes the attribute descriptions for all examples. Writes first all
	 * regular attributes and then the special attributes (just like the data
	 * write format of {@link Example#toString()}. Please note that the given 
     * data file will only be used to determine the relative position.
	 */
	public void writeAttributeFile(File attFile, File dataFile, String encoding) throws IOException {
		// determine relative path
        if (dataFile == null)
            throw new IOException("ExampleSet writing: cannot determine path to data file: data file was not given!");
		String relativePath = Tools.getRelativePath(dataFile, attFile);
		PrintWriter aout = new PrintWriter(new OutputStreamWriter(new FileOutputStream(attFile), encoding));
        aout.println("<?xml version=\"1.0\" encoding=\""+encoding+"\"?>");
		aout.println("<attributeset default_source=\"" + relativePath + "\">" + Tools.getLineSeparator());
		int sourcecol = 1;
        Iterator<AttributeRole> i = getAttributes().allAttributeRoles();
		while (i.hasNext()) {
			if (sourcecol != 1) {
				aout.println();
			}
			writeAttributeMetaData(i.next(), sourcecol, aout, false);
			sourcecol++;
		}
		aout.println(Tools.getLineSeparator() + "</attributeset>");
		aout.close();
	}

	/**
	 * Writes the attribute descriptions for all examples. Writes only the
	 * special attributes which are supported by the sparse format of the method
	 * {@link Example#toSparseString(int, int, boolean)}. Please note that the given data 
     * file is only be used to determine the relative position.
	 */
	public void writeSparseAttributeFile(File attFile, File dataFile, int format, String encoding) throws IOException {
        if (dataFile == null)
            throw new IOException("ExampleSet sparse writing: cannot determine path to data file: data file was not given!");
        
		PrintWriter aout = new PrintWriter(new OutputStreamWriter(new FileOutputStream(attFile), encoding));
        String relativePath = Tools.getRelativePath(dataFile, attFile);
        aout.println("<?xml version=\"1.0\" encoding=\""+encoding+"\"?>");
		aout.println("<attributeset default_source=\"" + relativePath + "\">");
		// some of special attributes
		AttributeRole labelRole = getAttributes().getRole(Attributes.LABEL_NAME);
		if ((labelRole != null) && (format != SparseFormatDataRowReader.FORMAT_NO_LABEL))
			writeAttributeMetaData(labelRole, 0, aout, true);
		AttributeRole idRole = getAttributes().getRole(Attributes.ID_NAME);
		if (idRole != null)
			writeAttributeMetaData(idRole, 0, aout, true);
		AttributeRole weightRole = getAttributes().getRole(Attributes.WEIGHT_NAME);
		if (weightRole != null)
			writeAttributeMetaData(weightRole, 0, aout, true);
        
		// regular attributes
		int sourcecol = 1;
		for (Attribute attribute : getAttributes()) {
			writeAttributeMetaData("attribute", attribute, sourcecol, aout, true);
			sourcecol++;
		}

		aout.println("</attributeset>");
		aout.close();
	}
    
    /** Writes the data of this attribute in the given stream. */
    private void writeAttributeMetaData(AttributeRole attributeRole, int sourcecol, PrintWriter aout, boolean sparse) {
        String tag = "attribute";
        if (attributeRole.isSpecial())
            tag = attributeRole.getSpecialName();
        Attribute attribute = attributeRole.getAttribute();
        writeAttributeMetaData(tag, attribute, sourcecol, aout, sparse);
    }
    
	/** Writes the data of this attribute in the given stream. */
	private void writeAttributeMetaData(String tag, Attribute attribute, int sourcecol, PrintWriter aout, boolean sparse) {
		aout.println("  <" + tag);
		aout.println("    name         = \"" + attribute.getName() + "\"");
		if (!sparse || tag.equals("attribute")) {
			aout.println("    sourcecol    = \"" + sourcecol + "\"");
		}
		aout.print("    valuetype    = \"" + Ontology.ATTRIBUTE_VALUE_TYPE.mapIndex(attribute.getValueType()) + "\"");
        if (!Ontology.ATTRIBUTE_BLOCK_TYPE.isA(attribute.getBlockType(), Ontology.SINGLE_VALUE))
            aout.print(Tools.getLineSeparator() + "    blocktype  = \"" + Ontology.ATTRIBUTE_BLOCK_TYPE.mapIndex(attribute.getBlockType()) + "\"");

        if ((Ontology.ATTRIBUTE_VALUE_TYPE.isA(attribute.getValueType(), Ontology.NOMINAL)) && 
            (!tag.equals(Attributes.KNOWN_ATTRIBUTE_TYPES[Attributes.TYPE_ID]))) {
            aout.println(">");
            Iterator i = attribute.getMapping().getValues().iterator();
            while (i.hasNext()) {
                aout.println("       <value>"+i.next()+"</value>");
            }
            aout.println("  </"+tag+">");
        } else { // no values, simply end this attribute
            aout.println("/>");
        }
	}
	
    public String getExtension() { return "aml"; }
    
    public String getFileDescription() { return "attribute description file"; }
	
	/**
	 * Returns true, if all attributes including labels and other special
	 * attributes are equal.
	 */
	public boolean equals(Object o) {
		if (!(o instanceof ExampleSet)) {
			return false;
		}
		ExampleSet es = (ExampleSet) o;
		return getAttributes().equals(es.getAttributes());
	}

    /** Returns the hash code of all attributes. */
	public int hashCode() {
		return getAttributes().hashCode();
	}

	public IOObject copy() {
		return (IOObject)clone();
	}

	/** Clones the example set by invoking a single argument clone constructor. Please note that a cloned
     *  example set has no information about the attribute statistics. That means, that attribute statistics
     *  must be (re-)calculated after the clone was created. */
	public Object clone() {
		try {
			Class<? extends AbstractExampleSet> clazz = getClass();
			java.lang.reflect.Constructor cloneConstructor = clazz.getConstructor(new Class[] { clazz });
			return cloneConstructor.newInstance(new Object[] { this });
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Cannot clone ExampleSet: " + e.getMessage());
		} catch (NoSuchMethodException e) {
			throw new RuntimeException("'" + getClass().getName() + "' does not implement clone constructor!");
		} catch (java.lang.reflect.InvocationTargetException e) {
			throw new RuntimeException("Cannot clone " + getClass().getName() + ": " + e + ". Target: " + e.getCause() + ". Cause: " + e.getTargetException() + ".");
		} catch (InstantiationException e) {
			throw new RuntimeException("Cannot clone " + getClass().getName() + ": " + e);
		}
	}
	
	/**
	 * Recalculates the attribute statistics for all attributes. They are
	 * average value, variance, minimum, and maximum. For nominal attributes the
	 * occurences for all values are counted. This method collects all
	 * attributes (regular and special) in a list and invokes
	 * <code>recalculateAttributeStatistics(List attributes)</code> and
	 * performs only one data scan.
	 */
	public void recalculateAllAttributeStatistics() {
		List<Attribute> allAttributes = new ArrayList<Attribute>();
		Iterator<Attribute> a = getAttributes().allAttributes();
		while (a.hasNext()) {
			allAttributes.add(a.next());
		}
		recalculateAttributeStatistics(allAttributes);
	}

	/** Recalculate the attribute statistics of the given attribute. */
	public void recalculateAttributeStatistics(Attribute attribute) {
		List<Attribute> allAttributes = new ArrayList<Attribute>();
		allAttributes.add(attribute);
		recalculateAttributeStatistics(allAttributes);
	}
	
	/**
	 * Here the Example Set is parsed only once, all the information is retained
	 * for each example set.
	 */
	public void recalculateAttributeStatistics(List<Attribute> attributeList) {
        // calculate statistics
		Iterator<Example> reader = iterator();
		Example example = null;
        boolean first = true;
		while ((example = reader.next()) != null) {
			for (Attribute attribute : attributeList) {
				double value = example.getValue(attribute);
				Iterator<Statistics> stats = attribute.getAllStatistics();
                while (stats.hasNext()) {
                    Statistics statistics = stats.next();
                    if (first)
                        statistics.startCounting(attribute);
                    statistics.count(value);
                }
			}
            first = false;
		}
        
        // store cloned statistics
        for (Attribute attribute : attributeList) {
            List<Statistics> statisticsList = statisticsMap.get(attribute.getName());
            // no stats known for this attribute at all --> new list
            if (statisticsList == null) {
                statisticsList = new LinkedList<Statistics>();
                statisticsMap.put(attribute.getName(), statisticsList);
            }            
            
            // in all cases: clear the list before adding new stats (clone of the calculations)
            statisticsList.clear();
            
            Iterator<Statistics> stats = attribute.getAllStatistics();
            while (stats.hasNext()) {
                Statistics statistics = (Statistics)stats.next().clone();
                statisticsList.add(statistics);
            }
        }
	}
    
    /** Returns the desired statistic for the given attribute. This method should be 
     *  preferred over the deprecated method {@link Attribute#getStatistics(String)}
     *  since it correctly calculates and keep the statistics for the current example
     *  set and does not overwrite the statistics in the attribute. 
     *  Invokes the method {@link #getStatistics(Attribute, String, String)} with a null 
     *  statistics parameter. */
    public double getStatistics(Attribute attribute, String statisticsName) {
        return getStatistics(attribute, statisticsName, null);
    }
    
    /** Returns the desired statistic for the given attribute. This method should be 
     *  preferred over the deprecated method {@link Attribute#getStatistics(String)}
     *  since it correctly calculates and keep the statistics for the current example
     *  set and does not overwrite the statistics in the attribute. If the statistics 
     *  were not calculated before (via one of the recalculate methods) this method
     *  will return NaN. If no statistics is available for the given name, also NaN
     *  is returned. */
    public double getStatistics(Attribute attribute, String statisticsName, String statisticsParameter) {
        List<Statistics> statisticsList = statisticsMap.get(attribute.getName());
        if (statisticsList == null)
            return Double.NaN;
        
        for (Statistics statistics : statisticsList) {
            if (statistics.handleStatistics(statisticsName)) {
                return statistics.getStatistics(statisticsName, statisticsParameter);
            }
        }
        
        return Double.NaN;
    }
}
