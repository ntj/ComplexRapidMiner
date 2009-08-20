package de.tud.inf.operator.fingerprints.lnf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.table.DataRow;
import com.rapidminer.example.table.DataRowFactory;
import com.rapidminer.example.table.MemoryExampleTable;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.parameter.ParameterTypeString;
import com.rapidminer.parameter.conditions.EqualTypeCondition;
import com.rapidminer.tools.Ontology;

import de.tud.inf.example.set.ComplexExampleSet;
import de.tud.inf.example.set.attributevalues.MapValue;
import de.tud.inf.example.table.MapAttribute;
import de.tud.inf.operator.fingerprints.ProcessStatistics;

public class LnfCreation extends Operator {

	public static final String PARAMETER_MAP_NAME = "map_attribute_name";
	public static final String PARAMETER_WINDOW_SIZE = "window_size";
	public static final String PARAMETER_STEP_SIZE = "step_size";
	public static final String PARAMETER_NEIGHBOURHOOD = "neighbourhood";
	public static final String PARAMETER_RADIUS = "radius";
	public static final String PARAMETER_ROTATION = "rotation";

	public LnfCreation(OperatorDescription description) {
		super(description);
		// TODO Auto-generated constructor stub
	}

	@Override
	public IOObject[] apply() throws OperatorException {
		//exampleSet of all map values
		ComplexExampleSet inputSet = getInput(ComplexExampleSet.class);
		MapValue mv = null;
		MapAttribute mapAttr = (MapAttribute) inputSet.getAttributes().get(getParameterAsString(PARAMETER_MAP_NAME));
		
		//lnf attribute
		List<Attribute> attributeList = new ArrayList<Attribute>(1);
		Attribute lnfAttribute = AttributeFactory.createAttribute("LNF", Ontology.NOMINAL);
		attributeList.add(lnfAttribute);
		MemoryExampleTable outputTable = new MemoryExampleTable(attributeList);
		DataRowFactory factory = new DataRowFactory(DataRowFactory.TYPE_BYTE_ARRAY, ',');
		
		Iterator<Example> it = inputSet.iterator();
		Example e;
		
		while (it.hasNext()) {
			e = it.next();
			mv = e.getMapValue(mapAttr);
			// calculate x and y size (map sorted by x,y)
			TreeMap<String, Integer> symbolMap = new TreeMap<String, Integer>();
			int windowSize;
			if (getParameterAsInt(PARAMETER_NEIGHBOURHOOD) < 3)
				windowSize = getParameterAsInt(PARAMETER_WINDOW_SIZE);
			else
				windowSize = 2 * getParameterAsInt(PARAMETER_RADIUS) + 1;
			int stepSize = getParameterAsInt(PARAMETER_STEP_SIZE);
			// nr of x values
			int nrow = mv.getDimension()[0];
			// nr of y values
			int ncol = mv.getDimension()[1];
			for (int i = 0; i <= nrow - windowSize; i += stepSize)
				for (int j = 0; j <= ncol - windowSize; j += stepSize) {
					//symbolList for each step through one array
					List<String> symbolList = new ArrayList<String>();
					// line 91
					// boundary neighbourhood
					//here numeric values are are read from map 
					if (getParameterAsInt("neighbourhood") == 2) {
						String surroundSymbols = "";
						// get only surrounding pixels
						for (int m = 0; m < windowSize; m++)
							surroundSymbols = surroundSymbols
									+ mv.getStringValueAtId(i, j + m);
						for (int m = 1; m < windowSize; m++)
							surroundSymbols = surroundSymbols
									+ mv.getStringValueAtId(i + m, j + windowSize
													- 1);
						for (int m = 1; m < windowSize; m++)
							surroundSymbols = surroundSymbols
									+ mv.getStringValueAtId(i + windowSize - 1, j
											+ windowSize - 1 - m);
						for (int m = 1; m < windowSize - 1; m++)
							surroundSymbols = surroundSymbols
									+ mv.getStringValueAtId(i + windowSize - 1
													- m, j);
						symbolList.add(surroundSymbols);
						// line 108
						// rotate pixels
						if (getParameterAsInt(PARAMETER_ROTATION) == 1) {
							int stringLength = surroundSymbols.length();
							String s = "";
							for (int l = 0; l < stringLength - 1; l++) {
								s = "";
								for (int k = 1; k <= stringLength; k++) {
									s = s
											+ surroundSymbols.charAt(k
													% stringLength);
								}
								symbolList.add(s);
								surroundSymbols = s;
							}
						}
					}
					// ring neighbourhood
					//for neighbourhood > 3 string values are are read from map
					else if (getParameterAsInt("neighbourhood") == 3) {
						int radius = getParameterAsInt(PARAMETER_RADIUS);
						int numberOfPoints = getParameterAsInt("points");

						int k = i + radius;
						int l = j + radius;

						double[] points = getCircle(mv, k, l, radius,
								(double) numberOfPoints);

						// quantizise
						String symbols = "";
						double q = getParameterAsDouble("quantization step size");
						char startSymbol = 'm';
						for (int m = 0; m < points.length; m++) {
							double z = points[m];
							if (z >= 0) {
								double quantile = q;
								char symbol = startSymbol;
								while (z >= quantile) {
									quantile += q;
									symbol++;
								}
								symbols += new Character(symbol).toString();
							} else if (z < 0) {
								double quantile = 0 - q;
								char symbol = startSymbol;
								symbol--;
								while (z <= quantile) {
									quantile -= q;
									symbol--;
								}
								symbols += new Character(symbol).toString();
							}
						}
						symbolList.add(symbols);
					}
					// circle neighbourhood
					//for neighbourhood > 3 string values are are read from map
					else if (getParameterAsInt("neighbourhood") == 4) {
						int radius = getParameterAsInt("radius");
						int numberOfPoints = getParameterAsInt("points");

						int k = i + radius;
						int l = j + radius;

						List<double[]> pointsList = new ArrayList<double[]>();
						for (int a = 1; a <= radius; a++) {
							double[] points = getCircle(mv, k, l, a,
									(double) numberOfPoints);
							pointsList.add(points);
						}

						// add center pixel
						pointsList.add(new double[] { mv.getValueAtId(k, l) });

						// quantizise
						List<String> qList = new ArrayList<String>();
						for (double[] points : pointsList) {
							String symbols = "";
							double q = getParameterAsDouble("quantization step size");
							char startSymbol = 'm';
							for (int m = 0; m < points.length; m++) {
								double z = points[m];
								if (z >= 0) {
									double quantile = q;
									char symbol = startSymbol;
									while (z >= quantile) {
										quantile += q;
										symbol++;
									}
									symbols += new Character(symbol).toString();
								} else if (z < 0) {
									double quantile = 0 - q;
									char symbol = startSymbol;
									symbol--;
									while (z <= quantile) {
										quantile -= q;
										symbol--;
									}
									symbols += new Character(symbol).toString();
								}
							}
							qList.add(symbols);
						}
						String s = "";
						for (String symbols : qList)
							s += symbols;
						symbolList.add(s);

						// rotate
						if (getParameterAsInt("rotation") == 1) {
							for (int a = 0; a < numberOfPoints - 1; a++) {
								String stringCon = "";
								for (int b = 0; b < qList.size() - 1; b++)
									stringCon += qList.get(b).substring(a + 1,
											numberOfPoints)
											+ qList.get(b).substring(0, a + 1);
								symbolList.add(stringCon
										+ qList.get(qList.size() - 1));
							}
						}
					}
					else {
						String symbols = "";
						if ((windowSize == 3) && (getParameterAsInt("neighbourhood") == 0)) {
							symbols = symbols + mv.getStringValueAtId(i+1,j) + mv.getStringValueAtId(i,j+1) + mv.getStringValueAtId(i+1,j+1) + mv.getStringValueAtId(i+2,j+1) + mv.getStringValueAtId(i+1,j+2);
						} else {
							for (int k=0; k<windowSize; k++)
							{
								for (int l=0; l<windowSize; l++)
								{
									symbols = symbols + mv.getStringValueAtId(i+k,j+l);
								}
							}
						}	
						symbolList.add(symbols);
					
						// rotate window 4 times	
						if (getParameterAsInt("rotation") == 1)
						{
							//180 degrees
							String symbols2 = "";
							for (int z = (symbols.length() - 1); z >= 0; z--)
								symbols2 += symbols.charAt(z);
							symbolList.add(symbols2);
							// 270 degrees
							String symbols3 = "";
							if ((getParameterAsInt("neighbourhood") == 0) && (windowSize == 3)) {
								symbols3 += "" + symbols.charAt(1) + symbols.charAt(4) + symbols.charAt(2) + symbols.charAt(0) + symbols.charAt(3);
							}
							else {
								for (int y = windowSize - 1; y >= 0; y--){
									for (int z = 0; z < Math.pow(windowSize,2); z += windowSize)
										symbols3 += symbols.charAt(z+y);
								}
							}
							symbolList.add(symbols3);
							// 90 degrees
							String symbols4 = "";
							for (int z = (symbols3.length() - 1); z >= 0; z--)
								symbols4 += symbols3.charAt(z);
							symbolList.add(symbols4);
						
							// rotate 3x3 window (and eight-neighbourhood) 8 times
							if ((windowSize == 3) && (getParameterAsInt("neighbourhood") == 1))
							{
								String symbols5 = "" + symbols.charAt(3) + symbols.charAt(0) + symbols.charAt(1) + symbols.charAt(6) + symbols.charAt(4) + symbols.charAt(2) + symbols.charAt(7) + symbols.charAt(8) + symbols.charAt(5);
								symbolList.add(symbols5);
								
								String symbols6 = "";
								for (int z = (symbols5.length() - 1); z >= 0; z--)
									symbols6 += symbols5.charAt(z);
								symbolList.add(symbols6);
								
								String symbols7 = "" + symbols.charAt(7) + symbols.charAt(6) + symbols.charAt(3) + symbols.charAt(8) + symbols.charAt(4) + symbols.charAt(0) + symbols.charAt(5) + symbols.charAt(2) + symbols.charAt(1);
								symbolList.add(symbols7);
								
								String symbols8 = "";
								for (int z = (symbols7.length() - 1); z >= 0; z--)
									symbols8 += symbols7.charAt(z);
								symbolList.add(symbols8);
							}
						}
					} //end if (default neighbourhood)
					Collections.sort(symbolList);
					String symbols = symbolList.get(0);
					
					Integer number;
					if ((number = symbolMap.get(symbols)) != null)
					{
						number++;
						symbolMap.put(symbols, number);
					}
					else
						symbolMap.put(symbols, 1);
				} // end iteration through one map
			
			// calculate relative frequencies
			int xSize = nrow;
			int ySize = ncol;
			double totalNum = ((xSize - windowSize) + 1) * ((ySize - windowSize) + 1);
			StringBuffer fingerprintBuffer = new StringBuffer("");
			double minFreq = getParameterAsDouble("min relative frequency");
			double relValue = 0;
			for (Map.Entry<String, Integer> mapEntry: symbolMap.entrySet())
			{
				relValue = (double)mapEntry.getValue()/totalNum;
				if (relValue > minFreq)
				{
					fingerprintBuffer.append(mapEntry.getKey() + "!" + relValue + "#");			
				}
			}
			
			//create new dataRow in outputexampleTable
			DataRow dataRow = factory.create(1);
			dataRow.set(lnfAttribute, lnfAttribute.getMapping().mapString(fingerprintBuffer.toString()));
			outputTable.addDataRow(dataRow);
			// some statistics
			ProcessStatistics.getInstance().addFingerprintStringLength(fingerprintBuffer.length());
			ProcessStatistics.getInstance().addNumSymbolVectors(symbolMap.size());
		} // end iteration through all maps in exampleSet
	
		ExampleSet output = outputTable.createExampleSet();
		return new IOObject[] {output};
	}

	/**
	 * @param mv
	 *            map value
	 * @param i
	 *            current x position
	 * @param j
	 *            current y position
	 * @param r
	 *            radius of circle
	 * @param p
	 *            number of points
	 * @return
	 */
	private double[] getCircle(MapValue mv, int i, int j, double R, double P) {
		double[] result = new double[(int) P];
		for (double p = 1; p <= P; p++) {
			double x = -R * Math.sin(2.0 * Math.PI * p / P);
			double y = R * Math.cos(2.0 * Math.PI * p / P);

			if ((int) (x * 10000.0) == 0)
				x = 0;
			if ((int) (y * 10000.0) == 0)
				y = 0;

			double x1 = Math.floor(x);
			double x2 = Math.ceil(x);
			double y1 = Math.floor(y);
			double y2 = Math.ceil(y);

			// hack for -0
			if (x1 == -0)
				x1 = 0;
			if (x2 == -0)
				x2 = 0;
			if (y1 == -0)
				y1 = 0;
			if (y2 == -0)
				y2 = 0;

			if ((x1 == x2) && (y1 == y2))
				result[(int) p - 1] = mv.getValueAtId(i + (int) x,
						(j + (int) y));
			else
				result[(int) p - 1] = interpolate(x1, x2, y1, y2, mv
						.getValueAtId((int) (i + x1), (int) (j + y1)), mv
						.getValueAtId((int) (i + x2), (int) (j + y1)), mv
						.getValueAtId((int) (i + x1), (int) (j + y2)), mv
						.getValueAtId((int) (i + x2), (int) (j + y2)), x, y);
		}
		return result;
	}

	private double interpolate(double x1, double x2, double y1, double y2,
			double q11, double q21, double q12, double q22, double x, double y) {
		// double quo = (x2 - x1)*(y2 -y1);
		double result = 0.0;
		result += q11 * (x2 - x) * (y2 - y);
		result += q21 * (x - x1) * (y2 - y);
		result += q12 * (x2 - x) * (y - y1);
		result += q22 * (x - x1) * (y - y1);
		return result;
	}

	@Override
	public Class<?>[] getInputClasses() {
		return new Class[] { ComplexExampleSet.class };
	}

	@Override
	public Class<?>[] getOutputClasses() {
		return new Class[] { ExampleSet.class };
	}

	/**
	 * Returns a list of ParameterTypes describing the parameters of this
	 * operator.
	 */
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		types.add(new ParameterTypeString(PARAMETER_MAP_NAME, "", "map"));
		types.add(new ParameterTypeCategory(PARAMETER_ROTATION, "",new String[] { "no", "yes" }, 0));

		types.add(new ParameterTypeCategory(PARAMETER_NEIGHBOURHOOD, "",new String[] { "four-neighbourhood", "eight-neighbourhood","boundary", "ring", "circle" }, 1));

		ParameterType type = new ParameterTypeInt(PARAMETER_WINDOW_SIZE, "", 2,100, 3);
		type.registerDependencyCondition(new EqualTypeCondition(this,PARAMETER_NEIGHBOURHOOD, true, 0, 1, 2));
		types.add(type);

		type = new ParameterTypeInt(PARAMETER_RADIUS, "", 1, Integer.MAX_VALUE,1);
		type.registerDependencyCondition(new EqualTypeCondition(this,PARAMETER_NEIGHBOURHOOD, true, 3, 4));
		types.add(type);

		
		type = new ParameterTypeInt("points", "", 4, Integer.MAX_VALUE, 8);
		type.registerDependencyCondition(new EqualTypeCondition(this,
						PARAMETER_NEIGHBOURHOOD, true, 3,4)); types.add(type);
		
		type = new ParameterTypeDouble("quantization step size","only for ring/circle neighbourhoods", 0, 10, 1);
		type.registerDependencyCondition(new EqualTypeCondition(this,PARAMETER_NEIGHBOURHOOD, true, 3, 4));
		types.add(type);

		types.add(new ParameterTypeInt(PARAMETER_STEP_SIZE, "", 1, 1000, 1));
		types.add(new ParameterTypeDouble("min relative frequency", "", 0,1, 0));
		return types;
	}

}
