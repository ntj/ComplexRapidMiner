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

/**
 * A parameter type for longer texts. In the GUI this might lead to a button opening
 * a text editor.
 * 
 * @author Ingo Mierswa
 * @version $Id: ParameterTypeText.java,v 1.3 2008/05/09 19:22:37 ingomierswa Exp $
 */
public class ParameterTypeText extends ParameterTypeString {
    
	private static final long serialVersionUID = 8056689512740292084L;
	
	private TextType type = TextType.PLAIN;
    
    /** Creates a new optional parameter type for longer texts. */
    public ParameterTypeText(String key, String description, TextType type) {
        super(key, description, true);
        setTextType(type);
    }
    
    /** Creates a new parameter type for longer texts with the given default value. */
    public ParameterTypeText(String key, String description, TextType type, String defaultValue) {
        super(key, description, defaultValue);
        setTextType(type);
    }
    
    /** Creates a new parameter type for longer texts. */
    public ParameterTypeText(String key, String description, TextType type, boolean optional) {
        super(key, description, optional);
        setTextType(type);
    }

    public void setTextType(TextType type) {
        this.type = type;
    }
    
    public TextType getTextType() {
        return this.type;
    }
}
