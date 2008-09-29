package com.rapidminer.gui.renderer;

import java.awt.Component;
import java.awt.Graphics;

import com.rapidminer.gui.plotter.PlotterPanel;
import com.rapidminer.report.Renderable;

/**
 * The default component renderer.
 * 
 * @author Ingo Mierswa
 * @version $Id: DefaultComponentRenderable.java,v 1.3 2008/08/25 14:06:02 homburg Exp $
 */
public class DefaultComponentRenderable implements Renderable {

	private Component component;
	
	public DefaultComponentRenderable(Component component) {
		this.component = component;
	}

	public int getRenderHeight(int preferredHeight) {
		return preferredHeight;
	}

	public int getRenderWidth(int preferredWidth) {
		return preferredWidth;
	}

	public void prepareRendering() {}

	public void render(Graphics graphics, int width, int height) {
		component.setSize(width, height);
		if (component instanceof PlotterPanel) {
			((PlotterPanel)component).getSelectedPlotter().render(graphics, width, height);
		} else {
			component.paint(graphics);
		}
	}
}
