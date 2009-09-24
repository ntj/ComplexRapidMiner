package de.tud.inf.operator.fingerprints.gsc;

import java.util.Iterator;
import java.util.List;

import JSci.maths.wavelet.FWTCoef;
import JSci.maths.wavelet.Filter;
import JSci.maths.wavelet.Signal;
import JSci.maths.wavelet.haar.MultiSplineHaar;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.parameter.conditions.EqualTypeCondition;
import com.rapidminer.tools.Ontology;

import de.tud.inf.example.set.ComplexExampleSet;
import de.tud.inf.example.set.attributevalues.MapValue;
import de.tud.inf.example.table.ConstantArrayAttribute;
import de.tud.inf.example.table.MapAttribute;

public class GlobalSlopeChange extends Operator{
	
	/*
	 * The number of beams
	 */
	public static String PARAMETER_NUM_OF_BEAMS = "number of beams";
	
	/*
	 * The way how the Global Slope Change will be determined
	 */
	public static String[] CATEGORIES = {
		"asc",
		"wavelet"
	};
	
	/*
	 * The category of the gsc
	 */
	public static String PARAMETER_CATEGORY = "category";
	
	/*
	 * The number of samples per beam
	 */
	public static String PARAMETER_SAMPLE_SIZE = "sample size";
	
	/*
	 * The level of the detail coefficient
	 */
	public static String PARAMETER_DETAIL_LEVEL = "detail level";
	
	/*
	 * The type of wavelet
	 */
	public static String PARAMETER_WAVELET_TYPE = "wavlet type";
	
	/*
	 * The available wavelet types
	 */
	public static String[] WAVELET_TYPES = {
		"Haar Wavelet"
	};
	
	public GlobalSlopeChange(OperatorDescription description) {
		super(description);
		
	}

	
	public IOObject[] apply() throws OperatorException {
		
		/**
		 * The ExampleSet must have a MapAttribute
		 */
		
		// get the map Attribute
		ComplexExampleSet input = getInput(ComplexExampleSet.class);
		Attributes attr = input.getAttributes();
		Attribute mapAttr = null;
		Iterator<Attribute> iter = attr.allAttributes();
		
		for(Attribute a : attr) {
			if(a instanceof MapAttribute) {
				mapAttr = a;
				break;
			}
		}
		
		if(mapAttr == null) {
			// throw a user error
		}
		
		// add new Attribute
		Attribute valueAttribute = AttributeFactory.createAttribute("Values", Ontology.NOMINAL);
		input.getExampleTable().addAttribute(valueAttribute);
		input = input.getExampleTable().createExampleSet();
		
		int numberOfBeams = this.getParameterAsInt(PARAMETER_NUM_OF_BEAMS);
		int detailLevel = -1;
		int maxDetaiLevel = -1;
		int numberOfCoefficients = 0;
		Filter wavelet = null;
		int sampleSize = getParameterAsInt(PARAMETER_SAMPLE_SIZE);
		int gscCategory = getParameterAsInt(PARAMETER_CATEGORY);
		
		// add new ComplexAttribute for the fingerprint
		String hint = Integer.toString(numberOfBeams) + "_";
		
		switch(gscCategory) {
		case 0:
			hint += "1";
			break;
		case 1:
			detailLevel = getParameterAsInt(PARAMETER_DETAIL_LEVEL);
			maxDetaiLevel = (int)Math.round((Math.log(sampleSize)/Math.log(2)));

			if(detailLevel > maxDetaiLevel)
				throw new OperatorException("detail level not feasible");
			if(getParameterAsInt(PARAMETER_WAVELET_TYPE) == 0)
				wavelet = new MultiSplineHaar();
			
			numberOfCoefficients = (int)Math.pow(2, detailLevel);
			hint += Integer.toString(numberOfCoefficients);
			break;
		default:
			hint += "1";
		}
		
		ConstantArrayAttribute fingerprint = (ConstantArrayAttribute)AttributeFactory.createAttribute("fingerprint", Ontology.ARRAY,hint);
		input.addComplexAttribute(fingerprint);
		// process every map in the Example Set
		for(Example e : input) {
			
			MapValue map = e.getMapValue(mapAttr);
			int[] dimensions = map.getDimension();
			
			// calculate the center of the beams
			double[] center = new double[2];
			
			for(int i = 0;i<dimensions.length;i++) {
				center[i] = Math.round((double)dimensions[i]/2);
			}
			
			// determine the start and endpoint of each beam
			// start point is always the center
			double[] startX = new double[numberOfBeams];
			double[] endX = new double[numberOfBeams];
			
			double[] startY = new double[numberOfBeams];
			double[] endY = new double[numberOfBeams];
			
			for(int i = 0;i<numberOfBeams;i++) {
				startX[i] = center[0];
				startY[i] = center[1];
			}
			if(numberOfBeams == 4) {
				endX = new double[]{1,center[0],dimensions[0],center[0]};
				endY = new double[]{center[1],dimensions[1],center[1],1};
			} else {
				endX[0] =endY[0] = 1;
				int beamIndex = 1;
				int j;
				
				for(;beamIndex<numberOfBeams/4+1;beamIndex++) {
					endX[beamIndex] = 1;
					endY[beamIndex] = Math.round(beamIndex*((double)dimensions[1] / (numberOfBeams/4)));
				}
				for(j = 0;j< numberOfBeams/4;j++) {
					endX[beamIndex+j] = Math.round((j+1)*((double)dimensions[0] / (numberOfBeams/4)));
					endY[beamIndex+j] = dimensions[1]; 
				}
				beamIndex += j;
				for(j = 0;j<numberOfBeams/4-1;j++) {
					endX[beamIndex+j] = dimensions[0];
					endY[beamIndex+j] = Math.round(((numberOfBeams/4-1)-j)*((double)dimensions[1]/(numberOfBeams/4)));
				}
				beamIndex += j;
				for(j=0;j<numberOfBeams/4;j++) {
					endX[beamIndex+j] = Math.round((numberOfBeams/4-j)*((double)dimensions[0]/(numberOfBeams/4)));
					endY[beamIndex+j] = 1;
				}
			}
			
			int category = getParameterAsInt(PARAMETER_CATEGORY);
			
			double[] xvalues = new double[sampleSize];
			double[] yvalues = new double[sampleSize];
			double[] zvalues; 
			double[][] slopes = new double[numberOfBeams][sampleSize];
			for(int k = 0;k<numberOfBeams;k++) {
				int numVals;
				zvalues = new double[sampleSize];
				double xStart = startX[k];
				double yStart = startY[k];
				double endBeamX = endX[k];
				double endBeamY = endY[k];
				double xDiff = Math.abs(xStart - endBeamX);
				double yDiff = Math.abs(yStart - endBeamY);
				
				double incX;
				double incY;
				
				int xStepSize = endBeamX - xStart >= 0 ? 1 : -1;
				int yStepSize = endBeamY - yStart >= 0 ? 1 : -1;
				
				if(xDiff == 0) {
					incY = yStepSize*yDiff/(sampleSize-1);
					for(int m = 0;m<sampleSize-1;m++) {
						xvalues[m] = xStart;
						yvalues[m] = yStart + m*incY;
						zvalues[m] = map.getValueAtId(xvalues[m]-1, yvalues[m]-1);
					}
				} else {
					if(yDiff == 0) {
						incX = xStepSize*xDiff/(sampleSize-1);
						for(int m = 0;m<sampleSize-1;m++) {
							xvalues[m] = xStart + m*incX;
							yvalues[m] = yStart;
							zvalues[m] = map.getValueAtId(xvalues[m]-1, yvalues[m]-1);
						}
					} else {
						double alpha = Math.atan(yDiff/xDiff);
						incX = xStepSize*Math.cos(alpha)*(Math.sqrt((xDiff*xDiff) + (yDiff*yDiff))/(sampleSize-1));
						incY = yStepSize*Math.sin(alpha)*(Math.sqrt((xDiff*xDiff) + (yDiff*yDiff))/(sampleSize-1));
						for(int m = 0;m<sampleSize-1;m++) {
							xvalues[m] = xStart+m*incX;
							yvalues[m] = yStart+m*incY;
							zvalues[m] = map.getValueAtId(xvalues[m]-1, yvalues[m]-1);
						}
						
					}
				}
				
				
				xvalues[sampleSize-1] = endBeamX;
				yvalues[sampleSize-1] = endBeamY;
				zvalues[sampleSize-1] = map.getValueAtId(xvalues[sampleSize-1]-1, yvalues[sampleSize-1]-1);
				
//				try {
//					
//					BufferedWriter out = new BufferedWriter(new FileWriter(new File("/media/drive/Studium/SHK/gsc/rapid/beams.csv"),true));
//					
//					for(int x = 0;x<xvalues.length;x++) {
//						out.write(String.valueOf(xvalues[x]) + ",");
//						out.write(String.valueOf(yvalues[x]));
//						out.newLine();
//						
//					}
//					out.close();
//					
//				} catch (IOException e1) {
//					// TODO Auto-generated catch block
//					e1.printStackTrace();
//				}
				
				slopes[k] = zvalues;
				//slopeList[k] = (double)Math.round(slopeList[k]*10000)/10000;
				
			}
			// create fingerprint attribute value
//			ConstantArrayValue val = new ConstantArrayValue(slopeList.length, 1);
//			val.setValues(slopeList);
//			e.setComplexValue(fingerprint, val);
			
			double[][] slope = null;
			switch(gscCategory) {
			case 0:
				// asc
				slope = new double[slopes.length][1];
				for(int i = 0;i<slopes.length;i++) {
					double slopesList[] = new double[sampleSize/2];
					int slopeindex = 0;
					for(int m=1;m<xvalues.length;m +=2) {
						//slopes[slopeindex] = map.getValueAtId(xvalues[m]-1, yvalues[m]-1) - map.getValueAtId(xvalues[m-1]-1,yvalues[m-1]-1);
						slopesList[slopeindex] = slopes[i][m]-slopes[i][m-1];
						slopeindex++;
					}
					
					for(int m=0;m<slopesList.length;m++) {
						slope[i][0] += slopesList[m];
					}
					slope[i][0] /= slopesList.length;
					
				}
				
				//slopeList[k] = new double[1];
				break;
			case 1:
				// wavelet
				slope = new double[slopes.length][numberOfCoefficients];
				for(int i = 0;i<slopes.length;i++) {
					Signal s = new Signal(slopes[i]);
					s.setFilter(wavelet);
					FWTCoef coeff = s.fwt(maxDetaiLevel - detailLevel);
					slope[i] = coeff.getCoefs()[maxDetaiLevel - detailLevel];
				}
		}
			
			
			String slopeString = "";
			for(double[] sl : slope) {
				for(double s : sl)
					slopeString += (double)Math.round(s*10000)/10000 + ":";
				slopeString += "#";
			}
			
			e.setValue(valueAttribute, slopeString);
		}
		
		return new IOObject[]{input.getExampleTable().createExampleSet()};
	}

	
	public Class<?>[] getInputClasses() {
		
		return new Class[]{ExampleSet.class};
	}

	
	public Class<?>[] getOutputClasses() {
		
		return new Class[]{ExampleSet.class};
	}


	
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		
		ParameterType type;
		type = new ParameterTypeInt(PARAMETER_NUM_OF_BEAMS, "The number of beams", 4, 16);
		type.setExpert(false);
		types.add(type);
		
		type = new ParameterTypeCategory(PARAMETER_CATEGORY, "The way how the gsc will be determined", CATEGORIES, 0);
		type.setExpert(false);
		types.add(type);
		
		type = new ParameterTypeCategory(PARAMETER_WAVELET_TYPE, "The type of wavelet", WAVELET_TYPES, 0);
		type.registerDependencyCondition(new EqualTypeCondition(this, PARAMETER_CATEGORY, true, 1));
		type.setExpert(false);
		types.add(type);
		
		type = new ParameterTypeInt(PARAMETER_DETAIL_LEVEL, "The level of the detail coefficient", 0, Integer.MAX_VALUE, 1);
		type.setExpert(false);
		type.registerDependencyCondition(new EqualTypeCondition(this, PARAMETER_CATEGORY, true, 1));
		types.add(type);
		
		type = new ParameterTypeInt(PARAMETER_SAMPLE_SIZE, "The number of samples pro beam", 1, Integer.MAX_VALUE);
		type.setExpert(false);
		types.add(type);
		
		
		
		return types;
	}

	
}
