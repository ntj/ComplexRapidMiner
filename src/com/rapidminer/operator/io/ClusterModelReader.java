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
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.List;
import java.util.zip.GZIPInputStream;

import com.rapidminer.operator.AbstractIOObject;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.learner.clustering.ClusterModel;
import com.rapidminer.operator.learner.clustering.FlatClusterModel;
import com.rapidminer.operator.learner.clustering.FlattendClusterModel;
import com.rapidminer.operator.learner.clustering.HierarchicalClusterModel;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeFile;


/**
 * Reads a single cluster model from a file.
 * 
 * @author Michael Wurst, Ingo Mierswa
 * @version $Id: ClusterModelReader.java,v 1.5 2008/05/09 19:22:37 ingomierswa Exp $
 * 
 */
public class ClusterModelReader extends Operator {

	/** The parameter name for &quot;the file from which the cluster model is read&quot; */
	public static final String PARAMETER_CLUSTER_MODEL_FILE = "cluster_model_file";

	/** The parameter name for &quot;load a flat model or flatten it&quot; */
	public static final String PARAMETER_FLAT = "flat";

	/** The parameter name for &quot;if true, new ids are generated for each cluster model, otherwise, the ids in the file are used&quot; */
	public static final String PARAMETER_ADD_IDS = "add_ids";

	/** The parameter name for &quot;if true, all non-letter characters are replaced in cluster descriptions&quot; */
	public static final String PARAMETER_CONVERT_LABELS = "convert_labels";
	
	public ClusterModelReader(OperatorDescription description) {
		super(description);
	}

	public IOObject[] apply() throws OperatorException {
		File file = getParameterAsFile(PARAMETER_CLUSTER_MODEL_FILE);
        
		ClusterModel model = null;
		try {
            // try if the model was written as a serializable model
			ObjectInputStream objectIn = new ObjectInputStream(new FileInputStream(file));
			model = (ClusterModel)objectIn.readObject();
			objectIn.close();
		} catch (Exception e) {
            // if not serialized, then try the usual model serialization (xml)
			InputStream in = null;
	        try {
	            in = new GZIPInputStream(new FileInputStream(file));
	        } catch (IOException e1) {
	            try {
	                // maybe already uncompressed?
	                in = new FileInputStream(file);
	            } catch (IOException e2) {
	                throw new UserError(this, e, 302, new Object[] { file, e2.getMessage() });
	            }
	        }
	        
			try {
				model = (ClusterModel)AbstractIOObject.read(in);
	            in.close();
			} catch (IOException e3) {
				throw new UserError(this, e, 302, new Object[] { file, e3.getMessage() });
			}
		}
		
		ClusterModel result = model;
		if (getParameterAsBoolean(PARAMETER_FLAT)) {
			if (model instanceof HierarchicalClusterModel)
			result = new FlattendClusterModel((HierarchicalClusterModel)model);
		}
		return new IOObject[] { result };
	}

	public Class[] getInputClasses() {
		return new Class[0];
	}

	public Class[] getOutputClasses() {
		if (getParameterAsBoolean(PARAMETER_FLAT)) {
			return new Class[] { FlatClusterModel.class };
		} else {
			return new Class[] { HierarchicalClusterModel.class };
		}
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		ParameterType type = new ParameterTypeFile(PARAMETER_CLUSTER_MODEL_FILE, "the file from which the cluster model is read", "clm", false);
		type.setExpert(false);
		types.add(type);
		type = new ParameterTypeBoolean(PARAMETER_FLAT, "load a flat model or flatten it", false);
		type.setExpert(false);
		types.add(type);
		types.add(new ParameterTypeBoolean(PARAMETER_ADD_IDS, "if true, new ids are generated for each cluster model, otherwise, the ids in the file are used", false));
		types.add(new ParameterTypeBoolean(PARAMETER_CONVERT_LABELS, "if true, all non-letter characters are replaced in cluster descriptions", false));
		return types;
	}
}
