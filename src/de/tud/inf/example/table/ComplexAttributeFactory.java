package de.tud.inf.example.table;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class ComplexAttributeFactory {
	
	public static final String[] SYMBOLS = {
		"gauss",
		"uniform",
		"matrix",
		"simple_matrix",
		"sparse_matrix",
		"sparse_binary_matrix",
	};
	
	public static final Class[] CLASSES = {
		GaussAttributeDescription.class,
		UniformAttributeDescription.class,
		MatrixAttributeDescription.class,
		SimpleMatrixAttributeDescription.class,
		SparseMatrixAttributeDescription.class,
		SparseBinaryMatrixAttributeDescription.class
	};
	
	public static ComplexAttributeDescription createAttributeDescription(int[] attIds, int[] paramIds, String symbol, String name, String hint) {
		
		ComplexAttributeDescription instance = null;
		Object[] args = new Object[]{attIds,paramIds,symbol,name,hint};
		Class cl = null;
		Constructor constr;
		for(int i=0;i<SYMBOLS.length;i++) {
			if(SYMBOLS[i].equals(symbol)) {
				cl = CLASSES[i];
				break;
			}
		}
		if(cl == null)
			cl = ComplexAttributeDescription.class;
		
		constr = cl.getConstructors()[0];
		try {
			instance = (ComplexAttributeDescription)constr.newInstance(args);
			
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		
		return instance;
	}

}
