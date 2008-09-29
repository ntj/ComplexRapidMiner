package com.rapidminer.gui.renderer;

import com.rapidminer.operator.IOContainer;
import com.rapidminer.report.Reportable;

public abstract class NonGraphicalRenderer extends AbstractRenderer {

	public abstract Reportable createReportable(Object renderable, IOContainer ioContainer);
	
	public Reportable createReportable(Object renderable, IOContainer ioContainer, int desiredWidth, int desiredHeight) {
		return createReportable(renderable, ioContainer);
	}
}
