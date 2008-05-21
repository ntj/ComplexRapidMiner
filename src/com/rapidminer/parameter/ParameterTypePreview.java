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
package com.rapidminer.parameter;

import com.rapidminer.gui.wizards.PreviewCreator;
import com.rapidminer.gui.wizards.PreviewListener;
import com.rapidminer.tools.LogService;


/**
 * This parameter type will lead to a GUI element which can be used as initialization for a 
 * results preview. This might be practical especially for complex operators which often also
 * provide a configuration wizard.
 * 
 * @author Ingo Mierswa
 * @version $Id: ParameterTypePreview.java,v 1.4 2008/05/09 19:22:37 ingomierswa Exp $
 */
public class ParameterTypePreview extends ParameterType {

	private static final long serialVersionUID = 6538432700371374278L;

	private Class<? extends PreviewCreator> previewCreatorClass;
    
    private transient PreviewListener previewListener;
    
    public ParameterTypePreview(Class<? extends PreviewCreator> previewCreatorClass, PreviewListener previewListener) {
        super("preview", "Shows a preview for the results which will be achieved by the current configuration.");
        this.previewCreatorClass = previewCreatorClass;
        this.previewListener = previewListener;
    }
    
    /** Returns a new instance of the wizard creator. If anything does not work this method will return null. */
    public PreviewCreator getPreviewCreator() {
    	PreviewCreator creator = null;
    	try {
    		creator = previewCreatorClass.newInstance();
    	} catch (InstantiationException e) {
            LogService.getGlobal().log("Problem during creation of previewer: " + e.getMessage(), LogService.WARNING);
    	} catch (IllegalAccessException e) {
            LogService.getGlobal().log("Problem during creation of previewer: " + e.getMessage(), LogService.WARNING);
    	}
    	return creator;
    }
    
    public PreviewListener getPreviewListener() {
        return previewListener;
    }
    
    public Object checkValue(Object value) {
        return null;
    }

    /** Returns null. */
    public Object getDefaultValue() {
        return null;
    }

    /** Does nothing. */
    public void setDefaultValue(Object defaultValue) {}
    
	public Object copyValue(Object value) {
		return value;
	}
    
    /** Returns null. */
    public String getRange() {
        return null;
    }

    /** Returns an empty string since this parameter cannot be used in XML description but is only used for
     *  GUI purposes. */
    public String getXML(String indent, String key, Object value, boolean hideDefault) {
        return "";
    }

    public boolean isNumerical() {
        return false;
    }
}
