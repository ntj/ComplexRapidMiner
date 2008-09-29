package com.rapidminer.gui.wizards;

import java.util.Map;

public abstract class AbstractConfigurationWizardCreator implements ConfigurationWizardCreator {

	private static final long serialVersionUID = 3622980797331677255L;
	
	private Map<String, String> parameters;

	public Map<String, String> getParameters() {
		return this.parameters;
	}

	public void setParameters(Map<String, String> parameters) {
		this.parameters = parameters;
	}
}
