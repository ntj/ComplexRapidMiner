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
package com.rapidminer.operator.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.learner.clustering.ClusterModel;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeFile;


/**
 * Write a single cluster model to a file.
 * 
 * @author Ingo Mierswa
 * @version $Id: ClusterModelWriter.java,v 1.2 2007/06/15 16:58:37 ingomierswa Exp $
 * 
 */
public class ClusterModelWriter extends Operator {


	/** The parameter name for &quot;the file to which the cluster model is stored&quot; */
	public static final String PARAMETER_CLUSTER_MODEL_FILE = "cluster_model_file";
	public ClusterModelWriter(OperatorDescription description) {
		super(description);
	}

	public IOObject[] apply() throws OperatorException {
		ClusterModel cm = getInput(ClusterModel.class);
		File file = getParameterAsFile(PARAMETER_CLUSTER_MODEL_FILE);

		try {
			OutputStream out = new GZIPOutputStream(new FileOutputStream(file));
			cm.write(out);
			out.close();
		} catch (IOException e) {
			throw new UserError(this, e, 303, new Object[] { file, e.getMessage() });
		}

		return new IOObject[] { cm };
	}

	public Class[] getInputClasses() {
		return new Class[] { ClusterModel.class };
	}

	public Class[] getOutputClasses() {
		return new Class[0];
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		ParameterType type = new ParameterTypeFile(PARAMETER_CLUSTER_MODEL_FILE, "the file to which the cluster model is stored", "clm", false);
		type.setExpert(false);
		types.add(type);
		return types;
	}
}
