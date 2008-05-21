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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.rapidminer.RapidMiner;
import com.rapidminer.datatable.DataTable;
import com.rapidminer.datatable.SimpleDataTable;
import com.rapidminer.datatable.SimpleDataTableRow;
import com.rapidminer.gui.plotter.PlotterPanel;
import com.rapidminer.gui.tools.SwingTools;
import com.rapidminer.gui.viewer.DataTableViewer;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.tools.Tools;
import com.rapidminer.tools.math.AverageVector;


/**
 * AttributeWeights holds the information about the weights of attributes of an
 * example set. It is delivered by several feature weighting algorithms or
 * learning schemes. The use of a linked hash map ensures that the added
 * features are stored in the same sequence they were added.
 * 
 * @author Ingo Mierswa
 * @version $Id: AttributeWeights.java,v 2.25 2006/04/05 08:57:22 ingomierswa
 *          Exp $
 */
public class AttributeWeights extends AverageVector {

	private static final long serialVersionUID = 7000978931118131854L;

	private static final String RESULT_ICON_NAME = "transform.png";
	
	private static Icon resultIcon = null;
	
	static {
		resultIcon = SwingTools.createIcon("16/" + RESULT_ICON_NAME);
	}
	
	/** Indicates that the weights should not be sorted at all. */
	public static final int NO_SORTING = 0;

	/** Indicates that the weights should be sorted in descending order. */
	public static final int DECREASING = -1;
    
    /** Indicates that the weights should be sorted in ascending order. */
    public static final int INCREASING = 1;

    /** The names for the sorting orders. */
    private static final String[] SORTING_ORDER_NAMES = {
        "no sorting", "decreasing", "increasing"
    };
    
	/** Indicates that the the actual weights should be used for sorting. */
	public static final int ORIGINAL_WEIGHTS = 0;

	/** Indicates that the the absolute weights should be used for sorting. */
	public static final int ABSOLUTE_WEIGHTS = 1;

    /** The names for the sorting value type. */
    private static final String[] SORTING_TYPE_NAMES = {
        "original weights", "absolute weights"
    };
    
	/** This comparator sorts the names of attributes according to their weights. */
	private class WeightComparator implements Comparator<String>, Serializable {

		private static final long serialVersionUID = 5013281668316451984L;

		/** Indicates if absolute weights should be used for sorting. */
		private int comparatorWeightType;

		/** Indicates the sorting direction. */
		private int direction;

		/** Creates a new weight comparator. */
		public WeightComparator(int direction, int comparatorWeightType) {
			this.comparatorWeightType = comparatorWeightType;
			this.direction = direction;
		}

		/** Creates two attribute weights. */
		public int compare(String o1, String o2) {
			double w1 = weightMap.get(o1).getWeight();
			double w2 = weightMap.get(o2).getWeight();

			if (comparatorWeightType == ABSOLUTE_WEIGHTS) {
				w1 = Math.abs(w1);
				w2 = Math.abs(w2);
			}

			return Double.compare(w2, w1) * direction;
		}
	}
    
	// ================================================================================

	/** Indicates the type of sorting. */
	private int sortType = NO_SORTING;

	/** Indicates if absolute or actual weights should be used for sorting. */
	private int weightType = ORIGINAL_WEIGHTS;

	/** Maps the name of an attribute to the corresponding attribute weight. */
	private Map<String, AttributeWeight> weightMap = new LinkedHashMap<String, AttributeWeight>();

	/** Creates a new empty attribute weights object. */
	public AttributeWeights() {}

	/**
	 * Creates a new attribute weights object containing a weight of 1 for each
	 * of the given input attributes.
	 */
	public AttributeWeights(ExampleSet exampleSet) {
		for (Attribute attribute : exampleSet.getAttributes())
			setWeight(attribute.getName(), 1.0d);
	}

	/** Clone constructor. */
	private AttributeWeights(AttributeWeights weights) {
		Iterator i = weights.getAttributeNames().iterator();
		while (i.hasNext()) {
			String name = (String) i.next();
			this.setWeight(name, weights.getWeight(name));
		}
	}

	/** Returns the name of this AverageVector. */
	public String getName() {
		return "AttributeWeights";
	}

	/** Sets the weight for the attribute with the given name. */
	public void setWeight(String name, double weight) {
		AttributeWeight oldWeight = weightMap.get(name);
		if (Double.isNaN(weight)) {
			weightMap.remove(name);
			super.removeAveragable(oldWeight);
		} else if (oldWeight == null) {
			AttributeWeight attWeight = new AttributeWeight(this, name, weight);
			super.addAveragable(attWeight);
			weightMap.put(name, attWeight);
		} else {
			oldWeight.setWeight(weight);
		}
	}

	/**
	 * Returns the weight for the attribute with the given name. Returns
	 * Double.NaN if the weight for the queried attribute is not known.
	 */
	public double getWeight(String name) {
		AttributeWeight weight = weightMap.get(name);
		if (weight == null)
			return Double.NaN;
		else
			return weight.getWeight();
	}

	/** Returns the currently used weight type. */
	public int getWeightType() {
		return weightType;
	}

	/** Returns the currently used weight type. */
	public void setWeightType(int weightType) {
		this.weightType = weightType;
	}
	
	/** Returns the currently used sorting type. */
	public int getSortingType() {
		return sortType;
	}
	
	/** Sets the currently used sorting type. */
	public void setSortingType(int sortingType) {
		this.sortType = sortingType;
	}

	/** Returns the number of features in this map. */
	public int size() {
		return weightMap.size();
	}

	/**
	 * Returns an set of attribute names in this map ordered by their insertion
	 * time.
	 */
	public Set<String> getAttributeNames() {
		return weightMap.keySet();
	}

	/**
	 * Since this average vector cannot be compared this method always returns
	 * 0.
	 */
	public int compareTo(Object o) {
		return 0;
	}
	
	/** Returns true if both objects have the same weight map. */
	public boolean equals(Object o) {
		if (!(o instanceof AttributeWeights)) {
			return false;
		} else {
			AttributeWeights other = (AttributeWeights)o;
			return this.weightMap.equals(other.weightMap);
		}
	}
	
	/** Returns the hash code of the weight map. */
	public int hashCode() {
		return this.weightMap.hashCode();
	}
	
	/**
	 * Sorts the given array of attribute names according to their weight,
	 * the sorting direction (ascending or descending), and with respect
     * to the fact if original or absolute weights should be used.
	 * 
	 * @param direction
	 *            <code>ASCENDING</code> or <code>DESCENDING</code>
	 * @param comparatorType
	 *            <code>WEIGHT</code> or <code>WEIGHT_ABSOLUTE</code>.
	 */
	public void sortByWeight(String[] attributeNames, int direction, int comparatorType) {
		Arrays.sort(attributeNames, new WeightComparator(direction, comparatorType));
	}
    
	/** Saves the attribute weights into an XML file. */
	public void save(File file) throws IOException {
	    writeAttributeWeights(file, Tools.getDefaultEncoding());
	}
    
    public void writeAttributeWeights(File file, Charset encoding) throws IOException {
        PrintWriter out = null;
        try {
        	out = new PrintWriter(new FileWriter(file));
        	out.println("<?xml version=\"1.0\" encoding=\"" + encoding + "\"?>");
        	out.println("<attributeweights version=\"" + RapidMiner.getVersion() + "\">");
        	Iterator i = weightMap.keySet().iterator();
        	while (i.hasNext()) {
        		String key = (String) i.next();
        		double weight = weightMap.get(key).getWeight();
        		out.println("    <weight name=\"" + key + "\" value=\"" + weight + "\"/>");
        	}
        	out.println("</attributeweights>");
        } catch (IOException e) {
        	throw e;
        } finally {
        	if (out != null) {
                out.close();		
        	}
        }
    }
    
	/** Loads a new AttributeWeights object from the given XML file. */
	public static AttributeWeights load(File file) throws IOException {
		AttributeWeights result = new AttributeWeights();
		Document document = null;
		try {
			document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
		} catch (SAXException e1) {
			throw new IOException(e1.getMessage());
		} catch (ParserConfigurationException e1) {
			throw new IOException(e1.getMessage());
		}

		Element attributeWeightsElement = document.getDocumentElement();
		if (!attributeWeightsElement.getTagName().equals("attributeweights")) {
			throw new IOException("Outer tag of attribute weights file must be <attributeweights>");
		}
		
		NodeList weights = attributeWeightsElement.getChildNodes();
		for (int i = 0; i < weights.getLength(); i++) {
			Node node = weights.item(i);
			if (node instanceof Element) {
				Element weightTag = (Element)node;
				String tagName = weightTag.getTagName();
				if (!tagName.equals("weight"))
					throw new IOException("Only tags <weight> are allowed, was " + tagName);
				String name = weightTag.getAttribute("name");
				String value = weightTag.getAttribute("value");
				double weight = 1.0d;
				try {
					weight = Double.parseDouble(value);
				} catch (NumberFormatException e) {
					throw new IOException("Only numerical weights are allowed for the 'value' attribute.");
				}
				result.setWeight(name, weight);
			}
		}
		return result;
	}

    public String getExtension() { return "wgt"; }
    
    public String getFileDescription() { return "attribute weights file"; }

    /** Returns a string representation of this object. */
    public String toString() {
        return "AttributeWeights (containing weights for " + weightMap.size() + " attributes)";
    }
    
	/**
	 * Returns a deep clone of the attribute weights which provides the same
	 * sequence of attribute names.
	 */
	public Object clone() {
		return new AttributeWeights(this);
	}

	/** This method normalizes all weights to the range 0 to 1. */
	public void normalize() {
	    double weightMin = Double.POSITIVE_INFINITY;
	    double weightMax = Double.NEGATIVE_INFINITY;
	    for (String name : getAttributeNames()) {
	    	double weight = Math.abs(getWeight(name));
	    	weightMin = Math.min(weightMin, weight);
	    	weightMax = Math.max(weightMax, weight);
	    }
        Iterator<AttributeWeight> w = weightMap.values().iterator();
        double diff = weightMax - weightMin;
        while (w.hasNext()) { 
            AttributeWeight attributeWeight = w.next();
	    	double newWeight = 1.0d;
            if (diff != 0.0d)
                newWeight = (Math.abs(attributeWeight.getWeight()) - weightMin) / diff;
            attributeWeight.setWeight(newWeight);
	    }
	}
	
	/**
	 * Returns a visualisation component which allows sorting of the attribute
	 * weights and several weight plots.
	 */
	public Component getVisualizationComponent(IOContainer container) {
        // create plotter panel
        DataTable dataTable = createSortedDataTable();
        final DataTableViewer dataTableViewer = new DataTableViewer(dataTable, PlotterPanel.WEIGHT_PLOTTER_SELECTION);
        dataTableViewer.getTable().setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        
        // main panel
		final JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());   
        
        
		GridBagLayout layout = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 0;
		c.insets = new Insets(5,5,5,5);
        JPanel settingsPanel = new JPanel(layout);
        mainPanel.add(settingsPanel, BorderLayout.NORTH);
		
        // sorting settings
        final JComboBox sortingDirectionBox = new JComboBox(SORTING_ORDER_NAMES);        
        sortingDirectionBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int selectedIndex = sortingDirectionBox.getSelectedIndex();
                switch (selectedIndex) {
                    case 0: sortType = NO_SORTING; break;
                    case 1: sortType = DECREASING; break;
                    case 2: sortType = INCREASING; break;
                    default: sortType = NO_SORTING; break;
                }
                DataTable sortedDataTable = createSortedDataTable();
                dataTableViewer.setDataTable(sortedDataTable);
            }
        });
        JLabel sortingLabel = new JLabel("sorting direction:");
        layout.setConstraints(sortingLabel, c);
        settingsPanel.add(sortingLabel);
        layout.setConstraints(sortingDirectionBox, c);
        settingsPanel.add(sortingDirectionBox);

        final JComboBox sortingValueBox = new JComboBox(SORTING_TYPE_NAMES);        
        sortingValueBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int selectedIndex = sortingValueBox.getSelectedIndex();
                switch (selectedIndex) {
                    case 0: weightType = ORIGINAL_WEIGHTS; break;
                    case 1: weightType = ABSOLUTE_WEIGHTS; break;
                    default: weightType = ORIGINAL_WEIGHTS; break;
                }
                DataTable sortedDataTable = createSortedDataTable();
                dataTableViewer.setDataTable(sortedDataTable);
            }
        });
        JLabel valueTypeLabel = new JLabel("value type:");
        layout.setConstraints(valueTypeLabel, c);
        settingsPanel.add(valueTypeLabel);
        c.gridwidth = GridBagConstraints.REMAINDER;
        layout.setConstraints(sortingValueBox, c);
        settingsPanel.add(sortingValueBox);   
        
        mainPanel.add(dataTableViewer, BorderLayout.CENTER);
		return mainPanel;
	}
	
	public Icon getResultIcon() {
		return resultIcon;
	}
    
    public DataTable createSortedDataTable() {
        DataTable dataTable = new SimpleDataTable("Attribute Weights", new String[] { "attribute", "weight" });
        Iterator<AttributeWeight> iter = getSortedWeights();
        while (iter.hasNext()) {
            AttributeWeight attWeight = iter.next();
            String attName = attWeight.getName();
            attWeight = weightMap.get(attName);
            double index = dataTable.mapString(0, attName);
            double weightValue = attWeight.getWeight();
            if (weightType == ABSOLUTE_WEIGHTS) 
                weightValue = Math.abs(weightValue);
            double[] data = new double[] { index, weightValue };
            dataTable.add(new SimpleDataTableRow(data, attName));
        }        
        return dataTable;
    }
    
    /**
     * Returns an iterator over all AttributeWeight objects according to the
     * current sorting settings.
     */
    private Iterator<AttributeWeight> getSortedWeights() {
        List<AttributeWeight> allWeights = new LinkedList<AttributeWeight>();
        Iterator<String> i = weightMap.keySet().iterator();
        while (i.hasNext()) {
            AttributeWeight attWeight = new AttributeWeight(weightMap.get(i.next()));
            if (weightType == ABSOLUTE_WEIGHTS) {
                attWeight.setWeight(Math.abs(attWeight.getWeight()));
            }
            allWeights.add(attWeight);
        }
        if (this.sortType != 0) {
            Collections.sort(allWeights);
        }
        return allWeights.iterator();
    }
}
