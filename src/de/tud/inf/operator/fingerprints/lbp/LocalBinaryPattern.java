package de.tud.inf.operator.fingerprints.lbp;



import java.util.ArrayList;
import java.util.HashMap;
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
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.parameter.ParameterTypeString;
import com.rapidminer.tools.Ontology;

import de.tud.inf.example.set.ComplexExampleSet;
import de.tud.inf.example.set.attributevalues.ComplexValueFactory;
import de.tud.inf.example.set.attributevalues.DataMapValue;
import de.tud.inf.example.set.attributevalues.MapValue;
import de.tud.inf.example.table.ComplexAttribute;
import de.tud.inf.example.table.MapAttribute;
import de.tud.inf.operator.capabilites.AttributeCapability;
import de.tud.inf.operator.capabilites.Capability;
import de.tud.inf.operator.capabilites.ExampleSetCapability;
import de.tud.inf.operator.capabilites.RegularAttributesCapability;

public class LocalBinaryPattern extends Operator {

	public static final String PARAMETER_MAP_NAME = "map_attribute_name";
	
	public LocalBinaryPattern(OperatorDescription description) {
		super(description);
	}

	@Override
	public IOObject[] apply() throws OperatorException {
		ExampleSet output = null;
		Map<Integer, Integer> map = null;
		try {
			ComplexExampleSet inputSet = getInput(ComplexExampleSet.class);
			MapValue mv = null;
			MapAttribute mapAttr = (MapAttribute) inputSet.getAttributes().get(getParameterAsString(PARAMETER_MAP_NAME));
			
			Iterator<Example> it = inputSet.iterator();
			Example e;
			DataMapValue dmValue = (DataMapValue)ComplexValueFactory.getComplexValueFunction(Ontology.DATA_MAP,"");
			while (it.hasNext()) {
				e = it.next();
				mv = e.getMapValue(mapAttr);
				// calculate x and y size (map sorted by x,y)		
				//nr of x values
				int xSize =  mv.getDimension()[0];
				//nr of y values
				int ySize =  mv.getDimension()[1];
				
				// count symbols
				int radius = getParameterAsInt("radius");
				int numberOfPoints = getParameterAsInt("points");
				int nrow = xSize;
				int ncol = ySize;
				
				map = new TreeMap<Integer, Integer>();		
				for (int i=0; i<=nrow-(2*radius+1); i++)
				{
					for (int j=0; j<=ncol-(2*radius+1); j++)
					{					
						int k = i + radius;
						int l = j + radius;
								
						double[] points = getCircle(mv, k, l, radius, (double) numberOfPoints);
						
						int result = 0;
						for (int a=0; a<points.length; a++)
							result = (int) (result + (Math.pow(2,a)* ((points[a] >= mv.getValueAtId(k,l))?1:0)));
														
						// rotate
						if (getParameterAsInt("rotation") == 1)
						{
							for (int m=1; m<points.length; m++) {
								int r = 0;							
								for (int a=0; a<points.length; a++)
									r = (int) (r + (Math.pow(2,a) * ((points[(a+m) % points.length] >= mv.getValueAtId(k,l))?1:0)));

								if (r < result)
									result = r;
							}
						}
						
						Integer number;
						if ((number = map.get(result)) != null)
						{
							number++;
							map.put(result, number);
						}
						else
							map.put(result, 1);
					}
				}
				
				// normalize
				double totalNum = ((xSize - (2*radius+1)) + 1) * ((ySize - (2*radius+1)) + 1);

				// create output map, here we do not have string keys
				Map<Integer, Double> resultMap = new HashMap<Integer,Double>();
				
				for (Map.Entry<Integer, Integer> mapEntry: map.entrySet()) {
					if (getParameterAsBoolean("normalize"))
							resultMap.put(mapEntry.getKey(), mapEntry.getValue()/totalNum);			
					else
							resultMap.put(mapEntry.getKey(), mapEntry.getValue().doubleValue());	
				}
				
				dmValue.setMap(resultMap);
				// create output example set
				List<Attribute> attributeList = new ArrayList<Attribute>(1);
				ComplexAttribute lnfAttribute = (ComplexAttribute)AttributeFactory.createAttribute("LBP", Ontology.DATA_MAP);
				attributeList.add(lnfAttribute);
				MemoryExampleTable exampleTable = new MemoryExampleTable(attributeList);
				DataRowFactory factory = new DataRowFactory(DataRowFactory.TYPE_BYTE_ARRAY, ',');
				
				DataRow dataRow = factory.create(1);
				dataRow.set(lnfAttribute, dmValue);
				exampleTable.addDataRow(dataRow);
				
				output = exampleTable.createExampleSet();
				
				// some statistics
				//ProcessStatistics.getInstance().addFingerprintStringLength(fingerprintBuffer.length());


			}
			
		
			
			
						
		} catch (Exception e) {
			System.out.println("Exception in FingerprintCreation " + e.toString());
		}
		
		return new IOObject[] {output};
	}
	
	private double interpolate(double x1, double x2, double y1, double y2, double q11, double q21, double q12, double q22, double x, double y) {
		double result = 0.0;
		result += q11 * (x2-x) * (y2-y);
		result += q21 * (x-x1) * (y2-y);
		result += q12 * (x2-x) * (y-y1);
		result += q22 * (x-x1) * (y-y1);
		return result;
	}
	
	/**
	 * @param mapArray array with pixel values
	 * @param i current x position
	 * @param j current y position
	 * @param r radius of circle
	 * @param p number of points
	 * @return
	 */
	private double[] getCircle(MapValue mv, int i, int j, double R, double P) {
		double[] result = new double[(int)P];
		for (double p=1; p<=P; p++) {
			double x = -R * Math.sin(2.0*Math.PI*p/P);
			double y = R * Math.cos(2.0*Math.PI*p/P);

			if ((int)(x*10000.0) == 0) x=0;
			if ((int)(y*10000.0) == 0) y=0;
			
			double x1 = Math.floor(x);
			double x2 = Math.ceil(x);
			double y1 = Math.floor(y);
			double y2 = Math.ceil(y);
			
			// hack for -0
			if (x1==-0) x1=0;
			if (x2==-0) x2=0;
			if (y1==-0) y1=0;
			if (y2==-0) y2=0;
			
			if ((x1 == x2) && (y1 == y2))
				result[(int)p-1] = mv.getValueAtId(i+(int)x,j+(int)y);
			else {
				result[(int)p-1] = interpolate(x1,x2,y1,y2,mv.getValueAtId((int)(i+x1),(int)(j+y1)),
														   mv.getValueAtId((int)(i+x2),(int)(j+y1)),
														   mv.getValueAtId((int)(i+x1),(int)(j+y2)),
														   mv.getValueAtId((int)(i+x2),(int)(j+y2)),
														   x,y);
			}
		}
		return result;
	}
	
	@Override
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		
		types.add(new ParameterTypeString(PARAMETER_MAP_NAME, "", "map"));
		types.add(new ParameterTypeCategory("rotation", "", new String[]{"no", "yes"}, 0));
		types.add(new ParameterTypeInt("radius", "", 1, Integer.MAX_VALUE, 1));
		types.add(new ParameterTypeInt("points", "", 4, Integer.MAX_VALUE, 8));
		
		types.add(new ParameterTypeBoolean("normalize", "", false));
		
		return types;
	}
	

	@Override
	public Class<?>[] getInputClasses() {
		return new Class[] {ComplexExampleSet.class}; 
	}

	@Override
	public Class<?>[] getOutputClasses() {
		return new Class[] {ExampleSet.class}; 
	}

	@Override
	public List<Capability> getDeliveredOutputCapabilities() {
		AttributeCapability ac = new RegularAttributesCapability();
		ac.setInnerTypes(new int[]{Ontology.DATA_MAP}, true);
		
		ExampleSetCapability result = new ExampleSetCapability();
		result.addCapability(ac);
		
		List<Capability> list = new ArrayList<Capability>();
		list.add(result);
		return list;
	}

	@Override
	public List<Capability> getInputCapabilities() {
		AttributeCapability ac = new RegularAttributesCapability();
		ac.setInnerTypes(new int[]{Ontology.MAP}, true);
		
		ExampleSetCapability result = new ExampleSetCapability();
		result.addCapability(ac);
		
		List<Capability> list = new ArrayList<Capability>();
		list.add(result);
		return list;
	}
	
	
	
}
