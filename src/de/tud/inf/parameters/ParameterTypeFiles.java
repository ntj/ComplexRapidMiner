package de.tud.inf.parameters;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeFile;
import com.rapidminer.parameter.ParameterTypeList;
import com.rapidminer.tools.Tools;

public class ParameterTypeFiles extends ParameterTypeFile{
	
	private Set defaultFiles = new HashSet();
	
	@Override
	public void setDefaultValue(Object defaultValue) {
		this.defaultFiles = (Set)defaultValue;
	}

	@Override
	public String getXML(String indent, String key, Object value,
			boolean hideDefault) {
		
		StringBuffer result = new StringBuffer();
		result.append(indent + "<parameter key=\"" + key + "\"\tvalue=\"");
		if(value instanceof String)
			result.append(value.toString());
		else {
			StringBuffer files = new StringBuffer();
			for(File f : ((Set<File>) value)) {
				files.append(f.getPath() + ";");
			}
			result.append(files.toString());
		}
		
		result.append("\"/>" + Tools.getLineSeparator());
		return result.toString();
	}

	@Override
	public Object getDefaultValue() {
		// TODO Auto-generated method stub
		return "";
	}

	@Override
	public Object copyValue(Object value) {
		if(value == null)
			return null;
		Set<File> valFiles = (Set<File>)value;
		Set<File> newFiles = new HashSet<File>(valFiles.size());
		for(File f : valFiles)
			newFiles.add(f);
		
		return newFiles;
	}

	public ParameterTypeFiles(String key, String description, String extension,
			boolean optional) {
		super(key, description, extension, optional);
		
	}

	public ParameterTypeFiles(String key, String description, String extension,
			String defaultFileName) {
		super(key, description, extension, defaultFileName);
		
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 655150517880229268L;

	


}
