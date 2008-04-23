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
package com.rapidminer.gui.tools;

import java.awt.Color;
import java.awt.Component;
import java.awt.GradientPaint;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;

import com.rapidminer.gui.RapidMinerGUI;
import com.rapidminer.gui.tools.syntax.SyntaxStyle;
import com.rapidminer.gui.tools.syntax.SyntaxUtilities;
import com.rapidminer.gui.tools.syntax.TextAreaDefaults;
import com.rapidminer.gui.tools.syntax.Token;
import com.rapidminer.tools.LogService;
import com.rapidminer.tools.Tools;


/**
 * This helper class provides some static methods and properties which might be
 * useful for several GUI classes. These methods include
 * <ul>
 * <li>the creation of gradient paints</li>
 * <li>displaying (simple) error messages</li>
 * <li>creation of file chosers</li>
 * <li>creation of text panels</li>
 * <li>escaping HTML messages</li>
 * </ul>
 * 
 * @author Ingo Mierswa
 * @version $Id: SwingTools.java,v 1.13 2007/07/01 01:02:47 ingomierswa Exp $
 */
public class SwingTools {
	
	/** Defines the maximal length of characters in a line of the tool tip text. */
	private static final int TOOL_TIP_LINE_LENGTH = 100;
	
	/** Defines the extra height for each row in a table. */
	public static final int TABLE_ROW_EXTRA_HEIGHT = 4;
    
    /** Defines the extra height for rows in a table with components. If an
     *  {@link ExtendedJTable} is used, this amount can be added additionally
     *  to the amount of {@link #TABLE_ROW_EXTRA_HEIGHT} which is already 
     *  added in the constructor. */
    public static final int TABLE_WITH_COMPONENTS_ROW_EXTRA_HEIGHT = 10;
	
	/** Some color constants for Java Look and Feel. */
	public static final Color DARKEST_YELLOW = new Color(190, 170, 100);
	
	/** Some color constants for Java Look and Feel. */
	public static final Color DARK_YELLOW = new Color(220, 200, 130);

	/** Some color constants for Java Look and Feel. */
	public static final Color LIGHT_YELLOW = new Color(230, 215, 155);
    
    /** Some color constants for Java Look and Feel. */
    public static final Color LIGHTEST_YELLOW = new Color(250, 240, 190);

    /** Some color constants for Java Look and Feel. */
    public static final Color TRANSPARENT_YELLOW = new Color(250, 240, 190, 190);
    
	/** Some color constants for Java Look and Feel. */
	public static final Color DARKEST_BLUE = new Color(110, 130, 170);
	
	/** Some color constants for Java Look and Feel. */
	public static final Color DARK_BLUE = new Color(140, 160, 200);

	/** Some color constants for Java Look and Feel. */
	public static final Color LIGHT_BLUE = new Color(196, 213, 255);

    /** Some color constants for Java Look and Feel. */
    public static final Color LIGHTEST_BLUE = new Color(232, 239, 255);
    
    /** Some color constants for Java Look and Feel. */
    public static final Color LIGHTEST_RED = new Color(255, 195, 195);
    
    /** A brown font color. */
	public static final Color BROWN_FONT_COLOR = new Color(63,53,24);
	
    /** A brown font color. */
	public static final Color LIGHT_BROWN_FONT_COLOR = new Color(113,103,74);
	
	/** Contains the small frame icons in all possible sizes. */
	private static final List<Image> ALL_FRAME_ICONS = new LinkedList<Image>();
	
	private static final String[] FRAME_ICON_SIZES = {
		"16", "24", "32", "48", "64", "128"
	};
	
	static {
		try {
			for (String size : FRAME_ICON_SIZES) {
				URL url = Tools.getResource("rapidminer_frame_icon_" + size + ".png");
				if (url != null) {
					ALL_FRAME_ICONS.add(ImageIO.read(url));
				}
			}
		} catch (IOException e) {
			// ignore this and do not use frame icons
			LogService.getGlobal().logWarning("Cannot load frame icons. Skipping...");
		}
	}
    
	/** Returns the list of all possible frame icons. */
	public static void setFrameIcon(JFrame frame) {
		Method iconImageMethod = null;
		try {
			iconImageMethod = frame.getClass().getMethod("setIconImages", new Class[] { List.class });
		} catch (Throwable e) {
			// ignore this and use single small icon below
		}
		
		if (iconImageMethod != null) {
			try {
				iconImageMethod.invoke(frame, new Object[] { ALL_FRAME_ICONS });
			} catch (Throwable e) {
				// ignore this and use single small icon
				if (ALL_FRAME_ICONS.size() > 0)
					frame.setIconImage(ALL_FRAME_ICONS.get(0));
			}
		} else {
			if (ALL_FRAME_ICONS.size() > 0)
				frame.setIconImage(ALL_FRAME_ICONS.get(0));
		}
	}
	
	/** Creates a blue gradient paint. */
	public static GradientPaint makeBluePaint(double width, double height) {
		return new GradientPaint(0f, 0f, LIGHT_BLUE, (float) width / 2, (float) height / 2, LIGHTEST_BLUE, true);
	}

	/** Creates a yellow gradient paint. */
	public static GradientPaint makeYellowPaint(double width, double height) {
		return new GradientPaint(0f, 0f, LIGHT_YELLOW, (float) width / 2, (float) height / 2, LIGHTEST_YELLOW, true);
	}

	/** Tries to load the icon for the given resource. Returns null (and writes a warning) if the 
	 *  resource file cannot be loaded. */
	public static ImageIcon createIcon(String resourceName) {
		URL url = Tools.getResource(resourceName);
		if (url != null) {
			return new ImageIcon(url);
		} else {
            LogService.getGlobal().log("Cannot load image '" + resourceName + "': icon will not be displayed", LogService.WARNING);
			return null;
		}		
	}
	
	/** This method transforms the given tool tip text into HTML. Lines are splitted at linebreaks
	 *  and additional line breaks are added after ca. {@link #TOOL_TIP_LINE_LENGTH} characters. */
	public static String transformToolTipText(String description) {
		return transformToolTipText(description, true, TOOL_TIP_LINE_LENGTH);
	}
	
	/** This method transforms the given tool tip text into HTML. Lines are splitted at linebreaks
	 *  and additional line breaks are added after ca. {@link #TOOL_TIP_LINE_LENGTH} characters. */
	public static String transformToolTipText(String description, boolean addHTMLTags, int lineLength) {
		String completeText = description.trim();
		StringBuffer result = new StringBuffer();
		if (addHTMLTags)
			result.append("<html>");
        // line.separator does not work here (transform and use \n)
        completeText = Tools.transformAllLineSeparators(completeText);
		String[] lines = completeText.split("\n");
		for (String text : lines) {
			boolean first = true;
			while (text.length() > lineLength) {
				int spaceIndex = text.indexOf(" ", lineLength);
				if (!first) {
					result.append("<br>");
				}
				first = false;
				if (spaceIndex >= 0) {
					result.append(text.substring(0, spaceIndex));
					text = text.substring(spaceIndex + 1);
				} else {
					result.append(text);
					text = "";
				}
			}
			if ((!first) && (text.length() > 0)) {
				result.append("<br>");
			}
			result.append(text);
			result.append("<br>");
		}
		if (addHTMLTags)
			result.append("</html>");
		return result.toString();
	}
	
	/** Transforms the given class name array into a comma separated string of the short class names. */
	public static String getStringFromClassArray(Class[] classes) {
		StringBuffer outputString = new StringBuffer();
		if (classes == null)
			outputString.append("none");
		else {
			for (int i = 0; i < classes.length; i++) {
				if (i != 0)
					outputString.append(", ");
				outputString.append(Tools.classNameWOPackage(classes[i]));
			}
		}
		if (outputString.length() == 0)
			outputString.append("none");
		return outputString.toString();
	}
	
	/** Adds linebreaks after {@link #TOOL_TIP_LINE_LENGTH} letters. */
	public static String addLinebreaks(String message) {
		String completeText = message.trim();
		StringBuffer result = new StringBuffer();
        // line.separator does not work here (transform and use \n)
        completeText = Tools.transformAllLineSeparators(completeText);
		String[] lines = completeText.split("\n");
		for (String text : lines) {
			boolean first = true;
			while (text.length() > TOOL_TIP_LINE_LENGTH) {
				int spaceIndex = text.indexOf(" ", TOOL_TIP_LINE_LENGTH);
				if (!first) {
					result.append(Tools.getLineSeparator());
				}
				first = false;
				if (spaceIndex >= 0) {
					result.append(text.substring(0, spaceIndex));
					text = text.substring(spaceIndex + 1);
				} else {
					result.append(text);
					text = "";
				}
			}
			if ((!first) && (text.length() > 0)) {
				result.append(Tools.getLineSeparator());
			}
			result.append(text);
			result.append(Tools.getLineSeparator());
		}
		return result.toString();
	}
	
	/** Shows a very simple error message without any Java exception hints. */
	public static void showVerySimpleErrorMessage(String message) {
		JOptionPane.showMessageDialog(RapidMinerGUI.getMainFrame(), addLinebreaks(message), "Error", JOptionPane.ERROR_MESSAGE);
	}

	/** This is the normal method which could be used by GUI classes for errors caused by 
	 *  some exception (e.g. IO issues). Of course these erro message methods should never be 
	 *  invoked by operators or similar. */
	public static void showSimpleErrorMessage(String message, Throwable e) {
		JOptionPane.showMessageDialog(RapidMinerGUI.getMainFrame(), addLinebreaks(message) + Tools.classNameWOPackage(e.getClass()) + " caught:" + Tools.getLineSeparator() + addLinebreaks(e.getMessage()), "Error", JOptionPane.ERROR_MESSAGE);
	}

	/** Shows the final error message dialog. This dialog also allows to send a bugreport if 
	 *  the error was not (definitely) a user error. */
	public static void showFinalErrorMessage(String message, Throwable e) {
		JDialog dialog = ErrorDialog.create(message, e);
		dialog.setLocationRelativeTo(RapidMinerGUI.getMainFrame());
		dialog.setVisible(true);
	}

	/** Opens a file chooser with a reasonable start directory. If the extension is null, no file filters will be used. */
	public static File chooseFile(Component parent, File file, boolean open, String extension, String extensionDescription) {
		return chooseFile(parent, file, open, false, extension, extensionDescription);
	}

	/** Opens a file chooser with a reasonable start directory. If the extension is null, no file filters will be used. 
	 *  This method allows choosing directories. */
	public static File chooseFile(Component parent, File file, boolean open, boolean onlyDirs, String extension, String extensionDescription) {
		return chooseFile(parent, 
				          file, 
				          open, 
				          onlyDirs, 
				          extension == null ? null : new String[] { extension }, 
				          extensionDescription == null ? null : new String[] { extensionDescription });
	}
	
	/** Returns the user selected file. */
	private static File chooseFile(Component parent, File file, boolean open, boolean onlyDirs, String[] extensions, String[] extensionDescriptions) {
		FileFilter[] filters = null;
		if (extensions != null) {
			filters = new FileFilter[extensions.length];
			for (int i = 0; i < extensions.length; i++) {
				filters[i] = new SimpleFileFilter(extensionDescriptions[i] + " (*." + extensions[i] + ")", "." + extensions[i]);
			}
		}
		return chooseFile(parent, file, open, onlyDirs, filters);
	}
	
	/**
	 * Opens a file chooser with a reasonable start directory. onlyDirs
	 * indidcates if only files or only can be selected.
	 * 
	 * @param file
	 *            The initially selected value of the file chooser dialog
	 * @param open
	 *            Open or save dialog?
	 * @param onlyDirs
	 *            Only allow directories to be selected
	 * @param fileFilters
	 *            List of FileFilters to use
	 */
	private static File chooseFile(Component parent, File file, boolean open, boolean onlyDirs, FileFilter[] fileFilters) {
		if (parent == null)
			parent = RapidMinerGUI.getMainFrame();
		JFileChooser fileChooser = createFileChooser(file, onlyDirs, fileFilters);        
		int returnValue = open ? fileChooser.showOpenDialog(parent) : fileChooser.showSaveDialog(parent);
		switch (returnValue) {
			case JFileChooser.APPROVE_OPTION:
				// check extension
				File selectedFile = fileChooser.getSelectedFile();
				FileFilter selectedFilter = fileChooser.getFileFilter();
				String extension = null;
				if (selectedFilter instanceof SimpleFileFilter) {
					SimpleFileFilter simpleFF = (SimpleFileFilter)selectedFilter;
					extension = simpleFF.getExtension();
				}
				if (extension != null) {
					if (!selectedFile.getAbsolutePath().endsWith(extension)) {
						selectedFile = new File(selectedFile.getAbsolutePath() + extension); 
					}
				}
				return selectedFile;
			default:
				return null;
		}
	}

	/**
	 * Creates file chooser with a reasonable start directory. You may use the
	 * following code snippet in order to retrieve the file:
	 * 
	 * <pre>
	 * 	if (fileChooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION)
	 * 	    File selectedFile = fileChooser.getSelectedFile();
	 * </pre>
	 * 
	 * Usually, the method
	 * {@link #chooseFile(Component, File, boolean, boolean, FileFilter[])} or
	 * one of the convenience wrapper methods can be used to do this. This
	 * method is only useful if one is interested, e.g., in the selected file
	 * filter.
	 * 
	 * @param file
	 *            The initially selected value of the file chooser dialog
	 * @param onlyDirs
	 *            Only allow directories to be selected
	 * @param fileFilters
	 *            List of FileFilters to use
	 */
	public static JFileChooser createFileChooser(File file, boolean onlyDirs, FileFilter[] fileFilters) {
		File directory = null;

		if (file != null) {
			if (file.isDirectory()) {
				directory = file;
			} else {
				directory = file.getAbsoluteFile().getParentFile();
			}
		} else {
			File processFile = (RapidMinerGUI.getMainFrame().getProcess() != null) ? RapidMinerGUI.getMainFrame().getProcess().getProcessFile() : null;
			if (processFile != null) {
				directory = processFile.getAbsoluteFile().getParentFile();
			} else {
				directory = new File(System.getProperty("user.dir"));
			}
		}

		JFileChooser fileChooser = new JFileChooser(directory);
		if (onlyDirs)
			fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		if (fileFilters != null) {
			fileChooser.setAcceptAllFileFilterUsed(true);
			for (int i = 0; i < fileFilters.length; i++)
				fileChooser.addChoosableFileFilter(fileFilters[i]);
		}
		if (file != null)
			fileChooser.setSelectedFile(file);
        
        // for special options of Trendy LaF
        try {        	
            Class configurationClazz = Class.forName("com.Trendy.swing.plaf.TrendyConfiguration");
            
            Field favouritesField = configurationClazz.getField("FILECHOOSER_FAVOURITES");
            String favouritesString = (String)favouritesField.get(null);
            fileChooser.putClientProperty(favouritesString, Boolean.TRUE);
            
            Field viewField = configurationClazz.getField("FILECHOOSER_VIEW");
            String viewString = (String)viewField.get(null);
            Field detailsField = configurationClazz.getField("FILECHOOSER_VIEW_DETAILS");
            String detailsString = (String)detailsField.get(null);
            
            fileChooser.putClientProperty(viewString, detailsString);
            
            
        } catch (Throwable e) {
            // does nothing: trendy look and feel is simply not available --> use usual file chooser
        	LogService.getGlobal().log("Cannot use trendy file chooser, use normal one...", LogService.MINIMUM);
        }
        
		return fileChooser;
	}

	/** Creates a panel with title and text. The panel has a border layout and the text 
     *  is placed into the NORTH section. */
	public static JPanel createTextPanel(String title, String text) {
		JPanel panel = new JPanel(new java.awt.BorderLayout());
		JLabel label = new JLabel("<html><h2>" + title + "</h2><p>" + text + "</p></html>");
		label.setBorder(BorderFactory.createEmptyBorder(11, 11, 11, 11));
		label.setFont(label.getFont().deriveFont(java.awt.Font.PLAIN));
		panel.add(label, java.awt.BorderLayout.NORTH);
		return panel;
	}

	// ================================================================================

	/**
	 * Replaces simple html tags and quotes by RapidMiner specific text elements.
	 * These can be used in XML files without confusing an XML parser.
	 */
	public static String html2RapidMinerText(String html) {
		if (html == null)
			return null;
		String result = html.replaceAll("<", "#ylt#");
		result = result.replaceAll(">", "#ygt#");
		result = result.replaceAll("\"", "#yquot#");
        result = result.replaceAll(Tools.getLineSeparator(), "");
		return result;
	}

	/**
	 * Replaces the RapidMiner specific tag elements by normal html tags.
	 * The given text is also embedded in an html and body tag with an
     * appropriated style sheet definition.
	 */
	public static String text2DisplayHtml(String text) {
		String result = "<html><head><style type=text/css>body { font-family:sans-serif; font-size:12pt; }</style></head><body>" + text + "</body></html>";
		result = text2SimpleHtml(result);
		result = result.replaceAll("#yquot#", "&quot;");
		while (result.indexOf("<icon>") != -1) {
			int startIndex = result.indexOf("<icon>");
			int endIndex = result.indexOf("</icon>");
			String start = result.substring(0, startIndex);
			String end = result.substring(endIndex + 7);
			String icon = result.substring(startIndex + 6, endIndex).trim().toLowerCase();
			java.net.URL url = Tools.getResource("icons/" + icon + ".png");
			if (url != null)
				result = start + "<img src=\"" + url + "\">" + end;
            else
                result = start + end;
		}
		return result;
	}

	/**
	 * Replaces the RapidMiner specific tag elements by normal html tags. This method
	 * does not embed the given text in a html tag.
	 */
	public static String text2SimpleHtml(String htmlText) {
		if (htmlText == null)
			return null;
		String replaceString = htmlText.replaceAll("#ygt#", ">");
		replaceString = replaceString.replaceAll("#ylt#", "<");
		
		StringBuffer result = new StringBuffer();
		boolean afterClose = true;
		int currentLineLength = 0;
		for (int i = 0; i < replaceString.length(); i++) {
			char c = replaceString.charAt(i);
			// skip white space after close
			if (afterClose)
				if (c == ' ')
					continue;
			
			// opening bracket
			if (c == '<') {
				if (!afterClose) {
					result.append(Tools.getLineSeparator());
					currentLineLength = 0;
				}
			} 
			
			// apend char
			afterClose = false;
			result.append(c);
			currentLineLength++;
			
			// break long lines
			if ((currentLineLength > 70) && (c == ' ')) {
				result.append(Tools.getLineSeparator());
				currentLineLength = 0;				
			}
			
			// closing bracket
			if (c == '>') {
				result.append(Tools.getLineSeparator());
				currentLineLength = 0;
				afterClose = true;
			}		
		}
		return result.toString();
	}
	
	/**
	 * Returns a color equivalent to the value of <code>value</code>. The value
	 * has to be normalized between 0 and 1.
	 */
	public static Color getPointColor(double value) {
		return new Color(Color.HSBtoRGB((float) (0.68 * (1.0d - value)), 1.0f, 1.0f)); // all
   																					// colors
	}
	
	/**
	 * Returns a color equivalent to the value of <code>value</code>. The value
	 * will be normalized between 0 and 1 using the parameters max and min. Which 
	 * are the minimum and maximum of the complete dataset.
	 */
	public static Color getPointColor(double value, double max, double min){
		 value = (value - min) / (max - min);
		 return getPointColor(value);
	}
	
	/** Returns JEditTextArea defaults with adapted syntax color styles. */
	public static TextAreaDefaults getTextAreaDefaults() {
		TextAreaDefaults defaults = TextAreaDefaults.getDefaults();
		defaults.styles = getSyntaxStyles();
		return defaults;
	}
	
	/**
	 * Returns adapted syntax color and font styles matching RapidMiner colors.
	 */
	public static SyntaxStyle[] getSyntaxStyles() {
		SyntaxStyle[] styles = SyntaxUtilities.getDefaultSyntaxStyles();
		styles[Token.COMMENT1] = new SyntaxStyle(new Color(0x990033), true, false);
		styles[Token.COMMENT2] = new SyntaxStyle(Color.black, true, false);
		styles[Token.KEYWORD1] = new SyntaxStyle(Color.black, false, true);
		styles[Token.KEYWORD2] = new SyntaxStyle(new Color(255,51,204), false, false);
		styles[Token.KEYWORD3] = new SyntaxStyle(new Color(255,51,204), false, false);
		styles[Token.LITERAL1] = new SyntaxStyle(new Color(51,51,255), false, false);
		styles[Token.LITERAL2] = new SyntaxStyle(new Color(51,51,255), false, false);
		styles[Token.LABEL] = new SyntaxStyle(new Color(0x990033), false, true);
		styles[Token.OPERATOR] = new SyntaxStyle(Color.black, false, true);
		styles[Token.INVALID] = new SyntaxStyle(Color.red, false, true);
		return styles;
	}
}
