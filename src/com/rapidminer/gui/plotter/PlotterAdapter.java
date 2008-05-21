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
package com.rapidminer.gui.plotter;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JPanel;

import com.rapidminer.datatable.DataTable;
import com.rapidminer.datatable.DataTableRow;
import com.rapidminer.gui.MainFrame;
import com.rapidminer.gui.plotter.conditions.BasicPlotterCondition;
import com.rapidminer.gui.plotter.conditions.PlotterCondition;
import com.rapidminer.tools.LogService;
import com.rapidminer.tools.Tools;
import com.rapidminer.tools.math.MathFunctions;


/** This adapter class can be used for simple plotter implementation which only need to overwrite the methods
 *  they need. Most method implementations are rather restrictive and need to be overwritten for the more
 *  sophisticated plotter possibilities. The complete plotting has to be done in the {@link #paintComponent(Graphics)}
 *  method (which must be invoked by super.paintComponent in order to get the correct color schemes), 
 *  plotter updates should be intitiated by invoking {@link #repaint()}.
 *  
 *  Subclasses should at least react to {@link #setDataTable(DataTable)} in order to properly update 
 *  the plotter. Another method usually overridden is {@link #setPlotColumn(int, boolean)}. Other overridden
 *  methods might include the methods for plot column and axis column handling.
 *  
 *  @author Ingo Mierswa
 *  @version $Id: PlotterAdapter.java,v 1.14 2008/05/09 19:22:51 ingomierswa Exp $
 */
public class PlotterAdapter extends JPanel implements Plotter {
	
	private static final long serialVersionUID = -8994113034200480325L;

    public static final double POINTSIZE = 7.0d;
    
	private static final int[] TICS = { 1, 2, 5 };
	
	public static final int MARGIN = 20;
	
	public static final int WEIGHT_BORDER_WIDTH = 5;
	
	public static final Font LABEL_FONT = new Font("Lucida Sans", Font.PLAIN, 11);

	protected static final Color GRID_COLOR = Color.lightGray;
	
	protected static final Color TOOLTIP_COLOR = new Color(170, 150, 240, 210);

	protected static final PointStyle ELLIPSOID_POINT_STYLE = new EllipsoidPointStyle();
	protected static final PointStyle RECTANGLE_POINT_STYLE = new RectanglePointStyle();
	protected static final PointStyle TRIANGUALAR_POINT_STYLE = new TriangularPointStyle();
	protected static final PointStyle TURNED_TRIANGUALAR_POINT_STYLE = new TurnedTriangularPointStyle();
	protected static final PointStyle STAR_POINT_STYLE = new StarPointStyle();

	//protected static final Color[] LINE_COLORS = { Color.black, Color.red, Color.blue, Color.orange, new Color(0, 200, 0), Color.pink, Color.yellow, Color.gray };
	protected static final Color[] LINE_COLORS = { 
		new Color(255, 0, 0),
		new Color(0, 255, 0),
		new Color(0, 0, 255),
		new Color(255, 0, 255),
		Color.ORANGE,
		new Color(255, 255, 0),
		new Color(0, 255, 255),
		new Color(200, 100, 0),
		new Color(100, 200, 0),
		new Color(0, 100, 200),
    };

	protected static final PointStyle[] KNOWN_POINT_STYLES = {
		ELLIPSOID_POINT_STYLE,
		RECTANGLE_POINT_STYLE,
		TRIANGUALAR_POINT_STYLE,
		TURNED_TRIANGUALAR_POINT_STYLE,
		STAR_POINT_STYLE,
		ELLIPSOID_POINT_STYLE,
		RECTANGLE_POINT_STYLE,
		TRIANGUALAR_POINT_STYLE,
		TURNED_TRIANGUALAR_POINT_STYLE,
		STAR_POINT_STYLE
	};
	
	// stroked lines are very slow!!!
	protected static final Stroke[] LINE_STROKES = {
		new BasicStroke(2.0f)
		/*
		new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND),
		new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f, new float[] { 8, 8 }, 0),
		new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f, new float[] { 12, 12 }, 0),
		new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f, new float[] { 16, 16 }, 0),
		new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f, new float[] { 12, 8, 4, 8 }, 0)
		*/
	};

	protected static final LineStyle[] LINE_STYLES = new LineStyle[LINE_COLORS.length * LINE_STROKES.length];
	
	protected static final PointStyle[] POINT_STYLES = new PointStyle[LINE_COLORS.length * LINE_STROKES.length];
	
	protected final static Icon[] LINE_STYLE_ICONS = new LineStyleIcon[LINE_STYLES.length];
	
    private static Color MIN_LEGEND_COLOR = Color.BLUE;
    
    private static Color MAX_LEGEND_COLOR = Color.RED;
    
	static {
		for (int i = 0; i < LINE_STROKES.length; i++) {
			for (int j = 0; j < LINE_COLORS.length; j++) {
				LINE_STYLES[i * LINE_COLORS.length + j] = new LineStyle(LINE_COLORS[j], LINE_STROKES[i]);
			}
		}

		for (int i = 0; i < LINE_STYLE_ICONS.length; i++) {
			LINE_STYLE_ICONS[i] = new LineStyleIcon(i);
		}
		
		for (int i = 0; i < LINE_STROKES.length; i++) {
			for (int j = 0; j < LINE_COLORS.length; j++) {
				POINT_STYLES[i * LINE_COLORS.length + j] = KNOWN_POINT_STYLES[j];
			}
		}
	}

	/**
	 * This icon is displayed before the columns to indicate the color and line
	 * style (as a legend or key).
	 */
	protected static class LineStyleIcon implements Icon {

		private int index;

		private LineStyleIcon(int index) {
			this.index = index;
		}

		public int getIconWidth() {
			return 20;
		}

		public int getIconHeight() {
			return 2;
		}

		public void paintIcon(Component c, Graphics g, int x, int y) {
			LINE_STYLES[index].set((Graphics2D) g);
			g.drawLine(x, y, x + 20, y);
		}
	}

	/**
	 * The line style that should be used for plotting lines. Please note that
	 * rendering dashed lines might be very slow which was the reason that only
	 * solid lines can be used in recent releases.
	 */
	protected static class LineStyle {

		private Color color;

		private Stroke stroke;

		private LineStyle(Color color, Stroke stroke) {
			this.color = color;
			this.stroke = stroke;
		}

		public void set(Graphics2D g) {
			g.setColor(color);
			g.setStroke(stroke);
		}
		
		public Color getColor() {
			return color;
		}
		
		public Stroke getStroke() {
			return this.stroke;
		}
	}

	/**
	 * The point style that should be used for plotting points.
	 */
	protected static interface PointStyle {

		public Shape createShape(double x, double y);
		
	}

	/**
	 * The point style that should be used for plotting points.
	 */
	protected static class EllipsoidPointStyle implements PointStyle {

		public Shape createShape(double x, double y) {
			return new Ellipse2D.Double(x - POINTSIZE / 2.0d, y - POINTSIZE / 2.0d, POINTSIZE, POINTSIZE);
		}
	}

	/**
	 * The point style that should be used for plotting points.
	 */
	protected static class RectanglePointStyle implements PointStyle {

		public Shape createShape(double x, double y) {
			return new Rectangle2D.Double(x - POINTSIZE / 2.0d, y - POINTSIZE / 2.0d, POINTSIZE - 1, POINTSIZE - 1);
		}
	}

	/**
	 * The point style that should be used for plotting points.
	 */
	protected static class TriangularPointStyle implements PointStyle {

		public Shape createShape(double x, double y) {
			int[] xPoints = new int[] { (int)Math.ceil(x - POINTSIZE / 2.0d), (int)Math.ceil(x), (int)Math.ceil(x + POINTSIZE / 2.0d) };
			int[] yPoints = new int[] { (int)Math.ceil(y + POINTSIZE / 2.0d), (int)Math.ceil(y - POINTSIZE / 2.0d), (int)Math.ceil(y + POINTSIZE / 2.0d) };
			return new Polygon(xPoints, yPoints, xPoints.length);
		}
	}

	/**
	 * The point style that should be used for plotting points.
	 */
	protected static class TurnedTriangularPointStyle implements PointStyle {

		public Shape createShape(double x, double y) {
			int[] xPoints = new int[] { (int)Math.ceil(x - POINTSIZE / 2.0d), (int)Math.ceil(x), (int)Math.ceil(x + POINTSIZE / 2.0d) };
			int[] yPoints = new int[] { (int)Math.ceil(y - POINTSIZE / 2.0d), (int)Math.ceil(y + POINTSIZE / 2.0d), (int)Math.ceil(y - POINTSIZE / 2.0d) };
			return new Polygon(xPoints, yPoints, xPoints.length);
		}
	}

	/**
	 * The point style that should be used for plotting points.
	 */
	protected static class StarPointStyle implements PointStyle {

		public Shape createShape(double x, double y) {
			double pointSize = POINTSIZE - 1.0d;
			int[] xPoints = new int[] { 
					(int)Math.ceil(x - pointSize / 6.0d), 
					(int)Math.ceil(x + pointSize / 6.0d), 
					(int)Math.ceil(x + pointSize / 6.0d),
					(int)Math.ceil(x + pointSize / 2.0d),
					(int)Math.ceil(x + pointSize / 2.0d),
					(int)Math.ceil(x + pointSize / 6.0d),
					(int)Math.ceil(x + pointSize / 6.0d),
					(int)Math.ceil(x - pointSize / 6.0d),
					(int)Math.ceil(x - pointSize / 6.0d),
					(int)Math.ceil(x - pointSize / 2.0d),
					(int)Math.ceil(x - pointSize / 2.0d),
					(int)Math.ceil(x - pointSize / 6.0d)
			};
			int[] yPoints = new int[] { 
					(int)Math.ceil(y - pointSize / 2.0d), 
					(int)Math.ceil(y - pointSize / 2.0d), 
					(int)Math.ceil(y - pointSize / 6.0d),
					(int)Math.ceil(y - pointSize / 6.0d),
					(int)Math.ceil(y + pointSize / 6.0d),
					(int)Math.ceil(y + pointSize / 6.0d),
					(int)Math.ceil(y + pointSize / 2.0d),
					(int)Math.ceil(y + pointSize / 2.0d),
					(int)Math.ceil(y + pointSize / 6.0d),
					(int)Math.ceil(y + pointSize / 6.0d),
					(int)Math.ceil(y - pointSize / 6.0d),
					(int)Math.ceil(y - pointSize / 6.0d)
			};
			return new Polygon(xPoints, yPoints, xPoints.length);
		}
	}
	
	// ===============================================================================================
    
	private static void setMinLegendColor(Color minColor) {
		MIN_LEGEND_COLOR = minColor;
	}

	private static void setMaxLegendColor(Color maxColor) {
		MAX_LEGEND_COLOR = maxColor;
	}
	
    /** Invokes super method and sets correct color schemes. Should be overwritten by children, but invokation of
     *  this super method must still be performed in order to get correct color schemes. */
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        setMinLegendColor(getColorFromProperty("rapidminer.gui.plotter.legend.mincolor", Color.BLUE));
        setMaxLegendColor(getColorFromProperty("rapidminer.gui.plotter.legend.maxcolor", Color.RED));
    }
    
    private Color getColorFromProperty(String propertyName, Color errorColor) {
        String propertyString = System.getProperty(propertyName);
        if (propertyString != null) {
        	String[] colors = propertyString.split(",");
        	Color color = new Color(Integer.parseInt(colors[0]), Integer.parseInt(colors[1]), Integer.parseInt(colors[2]));
        	return color;      
        } else {
        	return errorColor;
        }
    }
    
	/** Returns false. Subclasses should overwrite this method if they want to allow jittering. 
	 *  Subclasses overriding this method should also override {@link #setJitter(int)}. */
	public boolean canHandleJitter() { return false; }
	
	/** Returns false. Subclasses should overwrite this method if they want to allow zooming. 
	 *  Subclasses overriding this method should also override {@link #setZooming(int)}.*/
	public boolean canHandleZooming() { return false; }

	/** Returns -1. Subclasses overriding this method should also override {@link #getAxisName(int)}, 
	 *  {@link #setAxis(int, int)}, and {@link #getNumberOfAxes()}. */
	public int getAxis(int axis) { return -1; }
	
	/** Returns null. Subclasses overriding this method should also override {@link #getAxis(int)}, 
	 *  {@link #setAxis(int, int)}, and {@link #getNumberOfAxes()}. */
	public String getAxisName(int index) { return null;	}
	
	/** Returns a line icon depending on the index. */
	public Icon getIcon(int index) {
		return LINE_STYLE_ICONS[index % LINE_STYLE_ICONS.length];
	}
	
	/** Returns null. Subclasses which are able to derive a point from a mouse position should
	 *  return a proper Id which can be used for object visualizers. */
	public String getIdForPos(int x, int y) { return null; }
	
	/** Returns 1. Subclasses might want to deliver another initial zoom factor between 1 and 100. */
	public int getInitialZoomFactor() { return 1; }
	
	/** Returns 0. Subclasses overriding this method should also override {@link #getAxisName(int)}, 
	 *  {@link #setAxis(int, int)}, and {@link #getAxis(int)}. */
	public int getNumberOfAxes() { return 0; }
	
	/** Returns null. Subclasses might override this method in order to provide additional option
	 *  components. */
	public JComponent getOptionsComponent(int index) { return null; }
	
	/** Returns false. Subclasses should override this method and return true for the columns which should
	 *  be plotted. */
	public boolean getPlotColumn(int dimension) { return false;	}
	
	/** Returns null. Subclasses might return another name more fitting the plot selection box or list. */
	public String getPlotName() { return null; }
	
	/** Returns this. Subclasses which do not want to use this object (JPanel) for plotting should directly
	 *  implement {@link Plotter}. */
	public final JComponent getPlotter() {
		return this;
	}
	
    /** Returns a {@link BasicPlotterCondition} allowing for all {@link DataTable}s. Subclasses
     *  should override this method in order to indicate that they might not be able to handle
     *  certain data tables. */
    public PlotterCondition getPlotterCondition() {
        return new BasicPlotterCondition();
    }
    
	/** Returns null. Subclasses which are able to calculate the position in data space from a position
	 *  in screen space should return the proper position. Please note that you have to override the
	 *  method {@link #isProvidingCoordinates()}, too. */
	public Point2D getPositionInDataSpace(Point p) { return null; }
	
	/** Returns {@link #SINGLE_SELECTION}. Subclasses might override this method and return
	 *  {@link #NO_SELECTION} or {@link #MULTIPLE_SELECTION}. */
	public int getValuePlotSelectionType() {
		return SINGLE_SELECTION;
	}
	
	/** Returns false. Subclasses should override this method if they want to provide an options dialog. */
	public boolean hasOptionsDialog() { return false; }
	
	/** Returns false. Subclasses might want to indicate that this plotter has an save (export) 
	 *  image button of its own by returning true. */
	public boolean hasSaveImageButton() { return false; }
	
	/** Returns false. Subclasses might override this method in order to indicate that this plotter is
	 *  able to deliver plot coordinates. Please note that overriding subclasses should also override
	 *   {@link #getPositionInDataSpace(Point)}. */
	public boolean isProvidingCoordinates() { return false;	}
	
	/** Returns false. Subclasses might want to override this method to indicate that they are able to 
	 *  save the data into a file. In this case, the method {@link #save()} should also be overridden. */
	public boolean isSaveable() { return false;	}
	
	/** Does nothing. Please note that subclasses which want to allow saving should also override the method
	 *  {@link #isSaveable()}. */
	public void save() {}
	
	/** Does nothing. Subclasses overriding this method should also override {@link #getAxis(int)}, 
	 *  {@link #getAxisName(int)}, and {@link #getNumberOfAxes()}. */
	public void setAxis(int plotterAxis, int dimension) {}
	
	/** Does nothing. Can be used for setting the current drag bounds in screen space. */
	public void setDragBounds(int x, int y, int w, int h) {}
	
	/** Does nothing. Subclasses might override this method if they want to allow setting the actual draw 
	 *  range which might be different from the data range. */
	public void setDrawRange(double x, double y, double w, double h) {}
	
	/** Does nothing. Subclasses should overwrite this method if they want to allow jittering. 
	 *  Subclasses overriding this method should also override {@link #canHandleJitter()}. */
	public void setJitter(int jitter) {}
	
	/** Does nothing. Subclasses might override this method if they want to allow a key (legend). */
	public void setKey(String key) {}
	
	/** Does nothing. This method might be used by subclasses if they want to react on mouse moves, 
     *  e.g. by showing tool tips. */
	public void setMousePosInDataSpace(int mouseX, int mouseY) {}
	
	/** Does nothing. Subclasses should override this method if they want to allow plot column selection. 
	 *  In this case, the method {@link #getPlotColumn(int)} should also be overriden. */
	public void setPlotColumn(int dimension, boolean plot) {}
	
	/** Since this method already adds this object as a listener, all methods
	 *  overriding this one should invoke the super method. */
	public void setDataTable(DataTable dataTable) {
		dataTable.addDataTableListener(this);
	}
	
	/** Does nothing. Subclasses should overwrite this method if they want to allow zooming. 
	 *  Subclasses overriding this method should also override {@link #canHandleZooming()}. */
	public void setZooming(int zooming) {}
	
	/** Does nothing. Subclasses might implement this method in order to provide an options dialog. */
	public void showOptionsDialog() {}
    
	/** Returns true if a log scale for this column is supported. The default implementation returns false. */
	public boolean isSupportingLogScale(int axis) { return false; }
	
	/** Sets if the given axis should be plotted with log scale. The default implementation does nothing. */
	public void setLogScale(int axis, boolean logScale) {}
	
	/** Invokes {@link #repaint()}. Will be invoked since all plotters are {@link com.rapidminer.datatable.DataTableListener}s. */
	public final void dataTableUpdated(DataTable source) {
        int maxRowNumber = PlotterPanel.DEFAULT_MAX_NUMBER_OF_DATA_POINTS;
        String maxRowNumberString = System.getProperty(MainFrame.PROPERTY_RAPIDMINER_GUI_PLOTTER_ROWS_MAXIMUM);
        if ((maxRowNumberString != null) && (maxRowNumberString.trim().length() > 0)) {
            try {
                int newMaxRows = Integer.parseInt(maxRowNumberString);
                maxRowNumber = newMaxRows;
            } catch (NumberFormatException e) {
                LogService.getGlobal().logWarning("Plotter: cannot read maximum number of plotter points (was '" + maxRowNumberString + "').");
            }
        }
        if (source.getNumberOfRows() > maxRowNumber) {
            source.sample(maxRowNumber);
            LogService.getGlobal().logWarning("Cannot plot all data points, using only a sample of " + maxRowNumber + " rows.");
        }
		repaint();
	}
	
	// ===================================================================================
	//  Helper methods
	// ===================================================================================
	
	/** Helper methods which can be used to deliver a value for the point color. For nominal values with two classes,
	 *  this method tries to search another column with a name xxx(name) and changes the color a bit to the opponent
	 *  color if the values are not the same. This might be nice for example in case of a predicted value and a 
	 *  real value. */
	protected double getPointColorValue(DataTable table, DataTableRow row, int column, double min, double max) {
		double colorValue = row.getValue(column);
		double normalized = (colorValue - min) / (max - min);
		if (!Double.isNaN(colorValue)) {
			if ((table.isNominal(column)) && (table.getNumberOfValues(column) == 2)) {
				String columnName = table.getColumnName(column);
				int startParIndex = columnName.indexOf("(") + 1;
				if (startParIndex >= 0) {
					int endParIndex = columnName.indexOf(")", startParIndex);
					if (endParIndex >= 0) {
						String otherColumnName = columnName.substring(startParIndex, endParIndex);
						int otherColumnIndex = table.getColumnIndex(otherColumnName);
						if (otherColumnIndex >= 0) {
							if (table.isNominal(otherColumnIndex)) {
								double compareValue = row.getValue(otherColumnIndex);
								if (!Double.isNaN(compareValue)) {
									int compareIndex = (int)compareValue;
									String compareString = table.mapIndex(otherColumnIndex, compareIndex);
									compareIndex = table.mapString(column, compareString);
									if (colorValue != compareIndex) {
										// both values are different --> change color
										if (normalized > 0.8)
											normalized = 0.8;
										else if (normalized < 0.2)
											normalized = 0.2;
									}
								}
							}
						}
					}
				}
			}
		}
		return normalized;
	}
	
    protected Color getPointBorderColor(DataTable table, DataTableRow row, int column) {
        Color result = Color.BLACK;
        if (table.isNominal(column)) { // nominal --> try to find compare column
        	double colorValue = row.getValue(column);
        	if (!Double.isNaN(colorValue)) {
        		int colorIndex = (int)colorValue;
        		String columnName = table.getColumnName(column);
        		int startParIndex = columnName.indexOf("(") + 1;
        		if (startParIndex >= 0) {
        			int endParIndex = columnName.indexOf(")", startParIndex);
        			if (endParIndex >= 0) {
        				String otherColumnName = columnName.substring(startParIndex, endParIndex);
        				int otherColumnIndex = table.getColumnIndex(otherColumnName);
        				if (otherColumnIndex >= 0) {
        					if (table.isNominal(otherColumnIndex)) {
        						double compareValue = row.getValue(otherColumnIndex);
        						if (!Double.isNaN(compareValue)) {
        							int compareIndex = (int)compareValue;
        							String compareString = table.mapIndex(otherColumnIndex, compareIndex);
        							compareIndex = table.mapString(column, compareString);
        							if (colorIndex != compareIndex) {
        								// both values are different --> change color
        								result = Color.RED;
        							} 
        						}
        					}
        				}
        			}
        		}
        	}
        }
        return result;
    }
    
	/**
	 * Returns a color for the given value. The value must be normalized, i.e.
	 * between zero and one.
	 */
	public static Color getPointColor(double value) {
		return getPointColor(value, 255);
	}
	
	/**
	 * Returns a color for the given value. The value must be normalized, i.e.
	 * between zero and one. Please note that high alpha values are more transparent. 
	 */
	public static Color getPointColor(double value, int alpha) {
        if (Double.isNaN(value))
            return Color.LIGHT_GRAY;
        float[] minCol = Color.RGBtoHSB(MIN_LEGEND_COLOR.getRed(), MIN_LEGEND_COLOR.getGreen(), MIN_LEGEND_COLOR.getBlue(), null);
        float[] maxCol = Color.RGBtoHSB(MAX_LEGEND_COLOR.getRed(), MAX_LEGEND_COLOR.getGreen(), MAX_LEGEND_COLOR.getBlue(), null);
        //double hColorDiff = 1.0f - 0.68f;
        double hColorDiff = maxCol[0] - minCol[0];
        double sColorDiff = maxCol[1] - minCol[1];
        double bColorDiff = maxCol[2] - minCol[2];
        
		Color color = new Color(Color.HSBtoRGB((float)(minCol[0] + hColorDiff * value), (float)(minCol[1] + value * sColorDiff), (float)(minCol[2] + value * bColorDiff)));
		if (alpha < 255)
			color = new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
		return color;
	}

	protected PointStyle getPointStyle(int styleIndex) {
		return POINT_STYLES[styleIndex % POINT_STYLES.length];
	}
	
	/** This helper method can be used to draw a point in the given graphics object. */
	protected void drawPoint(Graphics2D g, double x, double y, Color color, Color borderColor) {
		drawPoint(g, ELLIPSOID_POINT_STYLE, x, y, color, borderColor);
	}
	
	/** This helper method can be used to draw a point in the given graphics object. */
	protected void drawPoint(Graphics2D g, PointStyle pointStyle, double x, double y, Color color, Color borderColor) {
		Shape pointShape = pointStyle.createShape(x, y);
		g.setColor(color);
		g.fill(pointShape);
		g.setColor(borderColor);
		g.draw(pointShape);
	}
	
	/** This method can be used to draw a legend on the given graphics context. */
	protected void drawLegend(Graphics graphics, DataTable table, int legendColumn) {
		drawLegend(graphics, table, legendColumn, 0, 255);
	}
	
	/** This method can be used to draw a legend on the given graphics context. */
	protected void drawLegend(Graphics graphics, DataTable table, int legendColumn, int xOffset, int alpha) {
		if ((legendColumn < 0) || (legendColumn > table.getNumberOfColumns() - 1))
			return;
		if (table.isNominal(legendColumn)) {
			String maxNominalValuesString = System.getProperty(MainFrame.PROPERTY_RAPIDMINER_GUI_PLOTTER_LEGEND_CLASSLIMIT);
			int maxNumberOfNominalValues = 10;
			try {
				if (maxNominalValuesString != null)
					maxNumberOfNominalValues = Integer.parseInt(maxNominalValuesString);
			} catch (NumberFormatException e) {
                LogService.getGlobal().logWarning("Plotter: cannot parse maximal number of nominal values for legend ("+maxNominalValuesString+")! Using 10...");
			}
			if ((maxNumberOfNominalValues == -1) || (table.getNumberOfValues(legendColumn) <= maxNumberOfNominalValues)) {
				drawNominalLegend(graphics, table, legendColumn, xOffset, alpha);		
			} else {
                LogService.getGlobal().logWarning("Plotter: cannot draw nominal legend since number of different values is too high (more than "+maxNominalValuesString+")! Using numerical legend instead.");
				drawNumericalLegend(graphics, table, legendColumn, alpha);				
			}
		} else {
			drawNumericalLegend(graphics, table, legendColumn, alpha);
		}
	}
	
	private void drawNominalLegend(Graphics graphics, DataTable table, int legendColumn, int xOffset, int alpha) {
		Graphics2D g = (Graphics2D)graphics.create();
		g.translate(xOffset, 0);
		int numberOfValues = table.getNumberOfValues(legendColumn);
		int currentX = MARGIN;
		for (int i = 0; i < numberOfValues; i++) {
			if (currentX > getWidth())
				break;
			String nominalValue = table.mapIndex(legendColumn, i);
			if (nominalValue.length() > 16)
				nominalValue = nominalValue.substring(0, 16) + "...";
			Shape colorBullet = new Ellipse2D.Double(currentX, 7, 7.0d, 7.0d);
			Color color = getPointColor((double)i / (double)(numberOfValues - 1), alpha);
			g.setColor(color);
			g.fill(colorBullet);
			g.setColor(Color.black);
			g.draw(colorBullet);
			currentX += 12;
			g.drawString(nominalValue, currentX, 15);
			Rectangle2D stringBounds = LABEL_FONT.getStringBounds(nominalValue, g.getFontRenderContext());
			currentX += stringBounds.getWidth() + 15;
		}
	}
	
	private void drawNumericalLegend(Graphics graphics, DataTable table, int legendColumn, int alpha) {
		double min = Double.POSITIVE_INFINITY;
		double max = Double.NEGATIVE_INFINITY;
		synchronized (table) {
			Iterator<DataTableRow> i = table.iterator();
			while (i.hasNext()) {
				DataTableRow row = i.next();
				double colorValue = row.getValue(legendColumn);
				min = MathFunctions.robustMin(min, colorValue);
				max = MathFunctions.robustMax(max, colorValue);
			}
		}
		drawNumericalLegend(graphics, min, max, alpha);		
	}
	
	/** This method can be used to draw a legend on the given graphics context. */
	private void drawNumericalLegend(Graphics graphics, double minColor, double maxColor, int alpha) {
		// key or legend
		String minColorString = Tools.formatNumber(minColor);
		String maxColorString = Tools.formatNumber(maxColor);
		Graphics2D g = (Graphics2D)graphics.create();
		Rectangle2D minStringBounds = LABEL_FONT.getStringBounds(minColorString, g.getFontRenderContext());
		Rectangle2D maxStringBounds = LABEL_FONT.getStringBounds(maxColorString, g.getFontRenderContext());
		int legendWidth = (int) (minStringBounds.getWidth() + 114 + maxStringBounds.getWidth());
		int keyX = MARGIN + getWidth() / 2 - legendWidth / 2;;
		int keyY = (int) (MARGIN + 2 - minStringBounds.getHeight() / 2);
		g.setColor(Color.black);
		g.drawString(minColorString, keyX, keyY);
		keyX += minStringBounds.getWidth() + 10;
		for (int i = 0; i < 100; i++) {
			double scaledColor = i / 100.0d;
			Color lineColor = getPointColor(scaledColor, alpha);
			g.setColor(lineColor);
			g.drawLine(keyX, keyY, keyX, keyY - 10);
			keyX++;
		}
		g.setColor(Color.black);
		Rectangle2D frame = new Rectangle2D.Double(keyX - 101, keyY - 11, 101, 11);
		g.draw(frame);
		keyX += 4;
		g.drawString(maxColorString, keyX, keyY);
	}
    
    protected void drawGenericNominalLegend(Graphics graphics, String[] names, PointStyle[] pointStyles, Color[] colors, int xOffset, int alpha) {
        Graphics2D g = (Graphics2D)graphics.create();
        g.translate(xOffset, 0);
        int numberOfValues = names.length;
        int currentX = MARGIN;
        for (int i = 0; i < numberOfValues; i++) {
            if (currentX > getWidth())
                break;
            String nominalValue = names[i];
            if (nominalValue.length() > 16)
                nominalValue = nominalValue.substring(0, 16) + "...";
            Shape shape = pointStyles[i].createShape(currentX, 11);
            Color color = colors[i];
            g.setColor(color);
            g.fill(shape);
            g.setColor(Color.black);
            g.draw(shape);
            currentX += 8;
            g.drawString(nominalValue, currentX, 15);
            Rectangle2D stringBounds = LABEL_FONT.getStringBounds(nominalValue, g.getFontRenderContext());
            currentX += stringBounds.getWidth() + 15;
        }
    }
	
	protected void drawToolTip(Graphics2D g, ToolTip toolTip) {
		if (toolTip != null) {
			g.setFont(LABEL_FONT);
			Rectangle2D stringBounds = LABEL_FONT.getStringBounds(toolTip.getText(), g.getFontRenderContext());
			g.setColor(TOOLTIP_COLOR);
			Rectangle2D bg = new Rectangle2D.Double(toolTip.getX()- stringBounds.getWidth() / 2 - 4, toolTip.getY() - stringBounds.getHeight() / 2, stringBounds.getWidth() + 5, stringBounds.getHeight() + 3);
			g.fill(bg);
			g.setColor(Color.black);
			g.draw(bg);
			g.drawString(toolTip.getText(), (int)(toolTip.getX() - stringBounds.getWidth() / 2 - 2), toolTip.getY() + 6);
		}
	}
	
	protected int getNumberOfPlots(DataTable table) {
		int counter = 0;
		for (int i = 0; i < table.getNumberOfColumns(); i++) {
			if (getPlotColumn(i))
				counter++;
		}
		return counter;
	}
	
	protected double getTicSize(DataTable dataTable, int column, double min, double max) {
		if (column < 0)
			return Double.NaN;
		if ((getNumberOfPlots(dataTable) == 1) && (dataTable.isNominal(column))) {
			if (dataTable.getNumberOfValues(column) <= 10) {
				return 1;
			} else {
				return getNumericalTicSize(min, max);				
			}
		} else {
			return getNumericalTicSize(min, max);
		}
	}
	
	protected double getNumericalTicSize(double min, double max) {
		double delta = (max - min) / 5;
		double e = Math.floor(Math.log(delta) / Math.log(10));
		double factor = Math.pow(10, e);
		for (int i = TICS.length - 1; i >= 0; i--) {
			if (TICS[i] * factor <= delta)
				return TICS[i] * factor;
		}
		return factor;		
	}
    
    protected double getMaxWeight(DataTable dataTable) {
        double maxWeight = Double.NaN;
        if (dataTable.isSupportingColumnWeights()) {
            maxWeight = Double.NEGATIVE_INFINITY;
            for (int c = 0; c < dataTable.getNumberOfColumns(); c++) {
                double weight = dataTable.getColumnWeight(c);
                if (!Double.isNaN(weight))
                    maxWeight = Math.max(Math.abs(weight), maxWeight);
            }
        }
        return maxWeight;
    }
    
    /**
     * Returns a color for the given weight. If weight or maxWeight are Double.NaN, just Color.white will be returned.
     */
    protected Color getWeightColor(double weight, double maxWeight) {
        Color weightColor = Color.white;
        if (!Double.isNaN(weight) && !Double.isNaN(maxWeight))
            weightColor = new Color(255, 255, 0, (int)((Math.abs(weight) / maxWeight) * 100));
        return weightColor;
    }
    
    protected void drawWeightRectangle(Graphics2D newSpace, DataTable dataTable, int column, double maxWeight, int plotterSize) {
    	if (dataTable.isSupportingColumnWeights()) {
    		newSpace.setColor(getWeightColor(dataTable.getColumnWeight(column), maxWeight));
    		Rectangle2D weightRect = new Rectangle2D.Double(1, 1, plotterSize-2, plotterSize-2);
    		newSpace.fill(weightRect);
    		newSpace.setColor(Color.WHITE);
    		int weightBorder = WEIGHT_BORDER_WIDTH + 1;
    		weightRect = new Rectangle2D.Double(weightBorder, weightBorder, 
    				plotterSize-2*weightBorder, plotterSize-2*weightBorder);
    		newSpace.fill(weightRect);
    	}
    }

	public int getRenderHeight(int preferredHeight) {
		return getHeight();
	}

	public int getRenderWidth(int preferredWidth) {
		return getWidth();
	}

	public void render(Graphics graphics, int width, int height) {
		paint(graphics);
	}
}
