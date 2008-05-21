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
package com.rapidminer.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.net.URL;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.rapidminer.RapidMiner;
import com.rapidminer.gui.MainFrame;
import com.rapidminer.gui.RapidMinerGUI;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.tools.plugin.Plugin;

/**
 * Tools for RapidMiner.
 * 
 * @author Simon Fischer, Ingo Mierswa
 * @version $Id: Tools.java,v 1.24 2008/05/09 19:22:55 ingomierswa Exp $
 */
public class Tools {
    
    /** The line separator depending on the operating system. */
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
    
	/** Number smaller than this value are consideres as zero. */
	private static final double IS_ZERO = 1E-6;
	
	/** Number of post-comma digits needed to distinguish between display of numbers as integers or doubles. */
	private static final double IS_DISPLAY_ZERO = 1E-8;
	
	/** Used for formatting values in the {@link #formatNumber(double)} method. */
	private static final NumberFormat NUMBER_FORMAT = NumberFormat.getInstance(Locale.US);

	/** Used for formatting values in the {@link #formatPercent(double)} method. */
	private static final NumberFormat PERCENT_FORMAT = NumberFormat.getPercentInstance(Locale.US);

	/** Used for determining the symbols used in decimal formats. */
	private static final DecimalFormatSymbols FORMAT_SYMBOLS = new DecimalFormatSymbols(Locale.US);
	
	private static final LinkedList<ResourceSource> ALL_RESOURCES = new LinkedList<ResourceSource>();
	
	public static final String RESOURCE_PREFIX = "com/rapidminer/resources/";
	
	static {
		ALL_RESOURCES.add(new ResourceSource(Tools.class.getClassLoader()));
	}
	
	/**
	 * Returns a formatted string of the given number (percent format with two
	 * fraction digits).
	 */
	public static String formatPercent(double value) {
        if (Double.isNaN(value))
            return "?";
		String percentDigitsString = System.getProperty(RapidMiner.PROPERTY_RAPIDMINER_GENERAL_FRACTIONDIGITS_PERCENT);
		int percentDigits = 2;
		try {
			if (percentDigitsString != null)
				percentDigits = Integer.parseInt(percentDigitsString);
		} catch (NumberFormatException e) {
			LogService.getGlobal().log("Bad integer for property 'rapidminer.gui.fractiondigits.percent', using default number if digits (2).", LogService.WARNING);
		}
		PERCENT_FORMAT.setMaximumFractionDigits(percentDigits);
		PERCENT_FORMAT.setMinimumFractionDigits(percentDigits);
		return PERCENT_FORMAT.format(value);
	}

	/**
	 * Returns a formatted string of the given number (number format with
	 * usually three fraction digits).
	 */
	public static String formatNumber(double value) {
        if (Double.isNaN(value))
            return "?";
		int numberDigits = 3;
		try {
			String numberDigitsString = System.getProperty(RapidMiner.PROPERTY_RAPIDMINER_GENERAL_FRACTIONDIGITS_NUMBERS);
			numberDigits = Integer.parseInt(numberDigitsString);
		} catch (NumberFormatException e) {}
		NUMBER_FORMAT.setMaximumFractionDigits(numberDigits);
		NUMBER_FORMAT.setMinimumFractionDigits(numberDigits);
		return NUMBER_FORMAT.format(value);
	}

	/**
	 * Returns a formatted string of the given number (uses the property
	 * rapidminer.gui.fractiondigits.numbers if the given number of digits is 
	 * smaller than 0 (usually 3)).
	 */
	public static String formatNumber(double value, int numberOfDigits) {
        if (Double.isNaN(value))
            return "?";
        int numberDigits = numberOfDigits;
        if (numberDigits < 0) {
        	try {
        		String numberDigitsString = System.getProperty(RapidMiner.PROPERTY_RAPIDMINER_GENERAL_FRACTIONDIGITS_NUMBERS);
        		numberDigits = Integer.parseInt(numberDigitsString);
        	} catch (NumberFormatException e) {
        		numberDigits = 3;
        	}
        }
        NUMBER_FORMAT.setMaximumFractionDigits(numberDigits);
        NUMBER_FORMAT.setMinimumFractionDigits(numberDigits);
		return NUMBER_FORMAT.format(value);
	}

    /** Returns a number string with no fraction digits if possible. Otherwise the default 
     *  number of digits will be returned. */
    public static String formatIntegerIfPossible(double value) {
        int numberDigits = 3;
        try {
            String numberDigitsString = System.getProperty(RapidMiner.PROPERTY_RAPIDMINER_GENERAL_FRACTIONDIGITS_NUMBERS);
            numberDigits = Integer.parseInt(numberDigitsString);
        } catch (NumberFormatException e) {}
        return formatIntegerIfPossible(value, numberDigits);
    }
    
    /** Returns a number string with no fraction digits if possible. Otherwise the given 
     *  number of digits will be returned. */
    public static String formatIntegerIfPossible(double value, int numberOfDigits) {
        if (Double.isNaN(value))
            return "?";
        if (Double.isInfinite(value)) {
        	if (value < 0)
        		return "-" + FORMAT_SYMBOLS.getInfinity();
        	else
        		return FORMAT_SYMBOLS.getInfinity();
        }

        long longValue = Math.round(value);
        if (Math.abs(longValue - value) < IS_DISPLAY_ZERO) {
            return longValue + "";
        } else {
            return formatNumber(value, numberOfDigits);
        }
    }
    
	/** Returns the name for an ordinal number. */
	public static final String ordinalNumber(int n) {
		if ((n % 10 == 1) && (n % 100 != 11)) {
			return n + "st";
		}
		if ((n % 10 == 2) && (n % 100 != 12)) {
			return n + "nd";
		}
		if ((n % 10 == 3) && (n % 100 != 13)) {
			return n + "rd";
		}
		return n + "th";
	}

    /** Returns true if the difference between both numbers is smaller than IS_ZERO. */
    public static boolean isEqual(double d1, double d2) {
        return Math.abs(d1 - d2) < IS_ZERO;
    }
    
	/** Returns {@link #isEqual(double, double)} for d and 0. */
	public static boolean isZero(double d) {
		return isEqual(d, 0.0d);
	}

	/** Returns no {@link #isEqual(double, double)}. */
	public static boolean isNotEqual(double d1, double d2) {
		return !isEqual(d1, d2);
	}

    /** Returns true if the d1 is greater than d2 and they are not equal. */
    public static boolean isGreater(double d1, double d2) {
        return (d1 > d2) && isNotEqual(d1, d2);
    }

    /** Returns true if the d1 is greater than d1 or both are equal. */
    public static boolean isGreaterEqual(double d1, double d2) {
        return (d1 > d2) || isEqual(d1, d2);
    }

    /** Returns true if the d1 is less than d2 and they are not equal. */
    public static boolean isLess(double d1, double d2) {
        return !isGreaterEqual(d1, d2);
    }

    /** Returns true if the d1 is less than d1 or both are equal. */
    public static boolean isLessEqual(double d1, double d2) {
        return !isGreater(d1, d2);
    }
    
    // ====================================
    
    /** Returns the correct line separator for the current operating system. */
    public static String getLineSeparator() {
        return LINE_SEPARATOR;
    }
    
    /** Returns the correct line separator for the current operating system concatenated 
     *  for the given number of times. */
    public static String getLineSeparators(int number) {
        if (number < 0)
            number = 0;
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < number; i++)
            result.append(LINE_SEPARATOR);
        return result.toString();
    }
    
    /** Replaces all possible line feed character combinations by &quot;\n&quot;. This
     *  might be important for GUI purposes like tool tip texts which do not support
     *  carriage return combinations. */
    public static String transformAllLineSeparators(String text) {
        Pattern crlf = Pattern.compile("(\r\n|\r|\n|\n\r)");
        Matcher m = crlf.matcher(text);
        if (m.find()) {
            text = m.replaceAll("\n");
        }
        return text;
    }
    
    /** Removes all possible line feed character combinations. This
     *  might be important for GUI purposes like tool tip texts which do not support
     *  carriage return combinations. */
    public static String removeAllLineSeparators(String text) {
        Pattern crlf = Pattern.compile("(\r\n|\r|\n|\n\r)");
        Matcher m = crlf.matcher(text);
        if (m.find()) {
            text = m.replaceAll(" ");
        }
        return text;
    }
    
	/**
	 * Returns the class name of the given class without the package
	 * information.
	 */
	public static String classNameWOPackage(Class c) {
		return c.getName().substring(c.getName().lastIndexOf(".") + 1);
	}

    // ====================================
    
	/**
	 * Reads the output of the reader and delivers it as string.
	 */
	public static String readOutput(BufferedReader in) throws IOException {
		StringBuffer output = new StringBuffer();
		String line = null;
		while ((line = in.readLine()) != null) {
			output.append(line);
			output.append(Tools.getLineSeparator());
		}
		return output.toString();
	}
    
	/**
	 * Creates a file relative to the given parent if name is not an absolute
	 * file name. Returns null if name is null.
	 */
	public static File getFile(File parent, String name) {
		if (name == null)
			return null;
		File file = new File(name);
		if (file.isAbsolute())
			return file;
		else
			return new File(parent, name);
	}

    /** This method checks if the given file is a Zip file containing one entry (in case of file extension .zip). 
     *  If this is the case, a reader based on a ZipInputStream for this entry is returned. 
     *  Otherwise, this method checks if the file has the extension .gz. If this applies, a gzipped
     *  stream reader is returned. Otherwise, this method just returns a BufferedReader
     *  for the given file (file was not zipped at all). */
    public static BufferedReader getReader(File file, Charset encoding) throws IOException {
        // handle zip files if necessary
        if (file.getAbsolutePath().endsWith(".zip")) {
        	ZipFile zipFile = new ZipFile(file);	                 
            if (zipFile.size() == 0) {	 
                throw new IOException("Input of Zip file failed: the file archive does not contain any entries.");	 
            }	 
            if (zipFile.size() > 1) {	 
                throw new IOException("Input of Zip file failed: the file archive contains more than one entry.");	 
            }	 
            Enumeration<? extends ZipEntry> entries = zipFile.entries();	 
            InputStream zipIn = zipFile.getInputStream(entries.nextElement());	 
            return new BufferedReader(new InputStreamReader(zipIn, encoding));        	
        } else if (file.getAbsolutePath().endsWith(".gz")) {
        	return new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(file)), encoding));
        } else {
            //return new BufferedReader(new FileReader(file));
        	return new BufferedReader(new InputStreamReader(new FileInputStream(file), encoding));
        }
    }
    
    /** This method tries to identify the encoding if a GUI is running and a process
     *  is defined. In this case, the encoding is taken from the process. 
     *  Otherwise, the method tries to identify the encoding via the property
     *  {@link RapidMiner#PROPERTY_RAPIDMINER_GENERAL_DEFAULT_ENCODING}. If this is not 
     *  possible, this method just returns the default system encoding. */
    public static Charset getDefaultEncoding() {
    	Charset result = null;

    	// try GUI setting
    	MainFrame mainFrame = RapidMinerGUI.getMainFrame();
    	if (mainFrame != null) {
    		com.rapidminer.Process process = mainFrame.getProcess();
    		if (process != null) {
    			Operator rootOperator = process.getRootOperator();
    			if (rootOperator != null) {
    				result = rootOperator.getEncoding();
    			}
    		}
    	}
    	
    	// try property setting
    	if (result == null) {
            String encoding = System.getProperty(RapidMiner.PROPERTY_RAPIDMINER_GENERAL_DEFAULT_ENCODING);
            if ((encoding != null) && (encoding.trim().length() > 0)) {
                if (RapidMiner.SYSTEM_ENCODING_NAME.equals(encoding)) {
                	result = Charset.defaultCharset();
                } else {
                	result = Charset.forName(encoding);
                }
            }
    	}
    	
    	// still not found? try default charset
    	if (result == null) {
    		result = Charset.defaultCharset();
    	}
    	
    	return result;
    }
    
	/**
	 * Creates a directory including parent directories.
	 * 
	 * @return true, if operation was successful.
	 */
	public static boolean mkdir(File dir) {
		if (dir == null)
			return true;
		if (dir.exists())
			return true;
		File parent = dir.getParentFile();
		if (parent == null) {
			return true;
		} else if (!parent.exists()) {
			if (!mkdir(parent))
				return false;
		}
		return dir.mkdir();
	}

	/** Returns the relative path of the first file resolved against the second. */
	public static String getRelativePath(File firstFile, File secondFile) throws IOException {
		String canonicalFirstPath = firstFile.getCanonicalPath();
		String canonicalSecondPath = secondFile.getCanonicalPath();

		int minLength = Math.min(canonicalFirstPath.length(), canonicalSecondPath.length());
		int index = 0;
		for (index = 0; index < minLength; index++) {
			if (canonicalFirstPath.charAt(index) != canonicalSecondPath.charAt(index)) {
				break;
			}
		}

		String relPath = canonicalFirstPath;
		int lastSeparatorIndex = canonicalFirstPath.substring(0, index).lastIndexOf(File.separator);
		if (lastSeparatorIndex != -1) {
			String absRest = canonicalSecondPath.substring(lastSeparatorIndex + 1);
			StringBuffer relPathBuffer = new StringBuffer();
			while (absRest.indexOf(File.separator) >= 0) {
				relPathBuffer.append(".." + File.separator);
				absRest = absRest.substring(absRest.indexOf(File.separator) + 1);
			}
			relPathBuffer.append(canonicalFirstPath.substring(lastSeparatorIndex + 1));
			relPath = relPathBuffer.toString();
		}
		return relPath;
	}

	/**
	 * Waits for process to die and writes log messages. Terminates if exit
	 * value is not 0.
	 */
	public static void waitForProcess(Operator operator, Process process, String name) throws OperatorException {
		try {
			LogService.getGlobal().log("Waiting for process '" + name + "' to die.", LogService.MINIMUM);
			int value = process.waitFor();
			if (value == 0) {
				LogService.getGlobal().log("Process '" + name + "' terminated successfully.", LogService.STATUS);
			} else {
				throw new UserError(operator, 306, new Object[] { name, value });
			}
		} catch (InterruptedException e) {
			throw new RuntimeException("Interrupted waiting for process '" + name + "' to die.", e);
		}
	}

	/**
	 * Sends a mail to the given address, using the specified subject and
	 * contents. Subject must contain no whitespace!
	 */
	public static void sendEmail(String address, String subject, String content) {
		try {
			// subject = subject.replaceAll("\\s", "_"); // replace whitespace
			String command = System.getProperty(RapidMiner.PROPERTY_RAPIDMINER_TOOLS_SENDMAIL_COMMAND);
			if (command != null) {
				// command = command.replaceAll("\\$A", address);
				// command = command.replaceAll("\\$S", subject);
				LogService.getGlobal().log("Executing '" + command + "'", LogService.MINIMUM);
				Process sendmail = Runtime.getRuntime().exec(new String[] { command, address });
				PrintStream out = null;
				try {
					out = new PrintStream(sendmail.getOutputStream());
					out.println("Subject: " + subject);
					out.println("From: RapidMiner");
					out.println("To: " + address);
					out.println();
					out.println(content);
				} catch (Exception e) {
					throw e;
				} finally {
					if (out != null)
						out.close();
				}
				waitForProcess(null, sendmail, command);
			}
		} catch (Throwable e) {
            LogService.getGlobal().log("Cannot send mail to " + address + ": " + e.getMessage(), LogService.ERROR);
		}
	}

    /** Adds a new resource source. Might be used by plugins etc. */
	public static void addResourceSource(ResourceSource source) {
		ALL_RESOURCES.add(source);
	}

	/** Adds a new resource source before the others. Might be used by plugins etc. */
	public static void prependResourceSource(ResourceSource source) {
		ALL_RESOURCES.addFirst(source);
	}

    public static URL getResource(ClassLoader loader, String name) {	 
        return getResource(loader, RESOURCE_PREFIX, name);	 
    }

    public static URL getResource(ClassLoader loader, String prefix, String name) {	 
        return loader.getResource(prefix + name);	 
    }
    
    /** Returns the desired resource. Tries first to find a resource in the core RapidMiner resources
     *  directory. If no resource with the given name is found, it is tried to load with help of
     *  the ResourceSource which might have been added by plugins. Please note that resource names
     *  are only allowed to use '/' as separator instead of File.separator! */
	public static URL getResource(String name) {
		Iterator<ResourceSource> i = ALL_RESOURCES.iterator();
		while (i.hasNext()) {
			ResourceSource source = i.next();
			URL url = source.getResource(name);
			if (url != null) {
				return url;
			}
		}
		
		URL resourceURL = getResource(Tools.class.getClassLoader(), name);
		if (resourceURL != null) {
			return resourceURL;
		} else {
			return null;
		}
	}
    
	public static String readTextFile(File file) throws IOException {
		return readTextFile(new FileReader(file));
	}

	public static String readTextFile(Reader r) throws IOException {
		StringBuffer contents = new StringBuffer();
		BufferedReader reader = new BufferedReader(r);
		String line = "";
		while ((line = reader.readLine()) != null) {
			contents.append(line + Tools.getLineSeparator());
		}
		reader.close();
		return contents.toString();
	}

	public static final String[] TRUE_STRINGS = { "true", "on", "yes", "y" };

	public static final String[] FALSE_STRINGS = { "false", "off", "no", "n" };

	public static boolean booleanValue(String string, boolean deflt) {
		if (string == null)
			return deflt;
		string = string.toLowerCase().trim();
		for (int i = 0; i < TRUE_STRINGS.length; i++) {
			if (TRUE_STRINGS[i].equals(string)) {
				return true;
			}
		}
		for (int i = 0; i < FALSE_STRINGS.length; i++) {
			if (FALSE_STRINGS[i].equals(string)) {
				return false;
			}
		}
		return deflt;
	}

	public static File findSourceFile(StackTraceElement e) {
		try {
			Class clazz = Class.forName(e.getClassName());
			while (clazz.getDeclaringClass() != null)
				clazz = clazz.getDeclaringClass();
			String filename = clazz.getName().replace('.', File.separatorChar);
			return ParameterService.getSourceFile(filename + ".java");
		} catch (Throwable t) {}
		String filename = e.getClassName().replace('.', File.separatorChar);
		return ParameterService.getSourceFile(filename + ".java");
	}

	public static Process launchFileEditor(File file, int line) throws IOException {
		String editor = System.getProperty(RapidMiner.PROPERTY_RAPIDMINER_TOOLS_EDITOR);
		if (editor == null)
			throw new IOException("Property 'rapidminer.tools.editor' undefined.");
		editor = editor.replaceAll("%f", file.getAbsolutePath());
		editor = editor.replaceAll("%l", line + "");
		return Runtime.getRuntime().exec(editor);
	}
	
	/** Replaces angle brackets by html entities. */
	public static String escapeXML(String string) {
		if (string == null)
			return "null";
		string = string.replaceAll("&",  "&amp;");
		string = string.replaceAll("\"", "&quot;");
        string = string.replaceAll("'",  "&#39;");
		string = string.replaceAll("<",  "&lt;");
		string = string.replaceAll(">",  "&gt;");
		string = transformAllLineSeparators(string);
		string = string.replaceAll("\n",  "&#10;");
		string = string.replaceAll("\t",  "&#09;");
		return string;
	}
	
	public static void findImplementationsInJar(JarFile jar, Class superClass, List<String> implementations) {
		findImplementationsInJar(Tools.class.getClassLoader(), jar, superClass, implementations);
	}

	public static void findImplementationsInJar(ClassLoader loader, JarFile jar, Class<?> superClass, List<String> implementations) {
		Enumeration<JarEntry> e = jar.entries();
		while (e.hasMoreElements()) {
			JarEntry entry = e.nextElement();
			String name = entry.getName();
			int dotClass = name.lastIndexOf(".class");
			if (dotClass < 0)
				continue;
			name = name.substring(0, dotClass);
			name = name.replaceAll("/", "\\.");
			try {
				Class<?> c = loader.loadClass(name);
				if (superClass.isAssignableFrom(c)) {
					if (!java.lang.reflect.Modifier.isAbstract(c.getModifiers())) {
						implementations.add(name);
					}
				}
			} catch (Throwable t) {}
		}
	}

	public static Class classForName(String className) throws ClassNotFoundException {
		try {
			return Class.forName(className);
		} catch (ClassNotFoundException e) {}
		try {
			return ClassLoader.getSystemClassLoader().loadClass(className);
		} catch (ClassNotFoundException e) {}	
		Iterator i = Plugin.getAllPlugins().iterator();
		while (i.hasNext()) {
			Plugin p = (Plugin) i.next();
			try {
				return p.getClassLoader().loadClass(className);
			} catch (ClassNotFoundException e) {
				// TODO: do nothing?
			}
		}
		throw new ClassNotFoundException(className);
	}
    
    /**
     * This method merges quoted splits, e.g. if a string line should be splitted by comma and
     * commas inside of a quoted string should not be used as splitting point.
     *  
     * @param line the original line
     * @param splittedTokens the tokens as they were originally splitted
     * @param quoteString the string which should be used as quote indicator, e.g. &quot; or '
     * @return the array of strings where the given quoteString was regarded
     * @throws IOException if an open quote was not ended
     */
    public static String[] mergeQuotedSplits(String line, String[] splittedTokens, String quoteString) throws IOException {
    	int[] tokenStarts = new int[splittedTokens.length];
    	int currentCounter = 0;
    	int currentIndex = 0;
    	for (String currentToken : splittedTokens) {
    		tokenStarts[currentIndex] = line.indexOf(currentToken, currentCounter);
    		currentCounter = tokenStarts[currentIndex] + currentToken.length() + 1;
    		currentIndex++;
    	}
    	
        List<String> tokens = new LinkedList<String>();
        int start = -1;
        int end = -1;
        for (int i = 0; i < splittedTokens.length; i++) {
            if (splittedTokens[i].trim().startsWith(quoteString)) {
                start = i;
            }
            if (start >= 0) {
                StringBuffer current = new StringBuffer();
                while ((end < 0) && (i < splittedTokens.length)) {
                    if (splittedTokens[i].endsWith(quoteString)) {
                        end = i;
                        break;
                    }
                    i++;
                }
                if (end < 0)
                    throw new IOException("Error during reading: open quote \" is not ended!");
                String lastToken = null;
                for (int a = start; a <= end; a++) {
                    String nextToken = splittedTokens[a];
                    if (nextToken.length() == 0)
                        continue;
                    if (a == start) {
                        nextToken = nextToken.substring(quoteString.length());
                    }
                    if (a == end) {
                        nextToken = nextToken.substring(0, nextToken.length() - quoteString.length());
                    }
                    // add correct separator
                    if (lastToken != null) {
                        //int lastIndex = line.indexOf(lastToken, totalCounter - lastToken.length()) + lastToken.length();
                    	int lastIndex = tokenStarts[a-1] + lastToken.length();
                        int thisIndex = tokenStarts[a];
                        if (lastIndex >= 0 && thisIndex >= lastIndex) {
                            String separator = line.substring(lastIndex, thisIndex);
                            current.append(separator);
                        }
                    }
                    current.append(nextToken);
                    lastToken = splittedTokens[a];
                }
                tokens.add(current.toString());
                start = -1;
                end = -1;
            } else {
                tokens.add(splittedTokens[i]);
            }
        }
        String[] quoted = new String[tokens.size()];
        tokens.toArray(quoted);
        return quoted;
    }
    
    /** Delivers the next token and skip empty lines. */
    public static void getFirstToken(StreamTokenizer tokenizer) throws IOException {
        // skip empty lines
        while (tokenizer.nextToken() == StreamTokenizer.TT_EOL) {};
        
        if ((tokenizer.ttype == '\'') || (tokenizer.ttype == '"')) {
            tokenizer.ttype = StreamTokenizer.TT_WORD;
        } else if ((tokenizer.ttype == StreamTokenizer.TT_WORD) && (tokenizer.sval.equals("?"))) {
            tokenizer.ttype = '?';
        }
    }
    
    /** Delivers the next token and checks if its the end of line. */
    public static void getLastToken(StreamTokenizer tokenizer, boolean endOfFileOk) throws IOException {
        if ((tokenizer.nextToken() != StreamTokenizer.TT_EOL) && ((tokenizer.ttype != StreamTokenizer.TT_EOF) || !endOfFileOk)) {
            throw new IOException("expected the end of the line " + tokenizer.lineno());
        }
    }

    /** Delivers the next token and checks for an unexpected end of line or file. */
    public static void getNextToken(StreamTokenizer tokenizer) throws IOException {
        if (tokenizer.nextToken() == StreamTokenizer.TT_EOL) {
            throw new IOException("unexpected end of line " + tokenizer.lineno());
        }

        if (tokenizer.ttype == StreamTokenizer.TT_EOF) {
            throw new IOException("unexpected end of file in line " + tokenizer.lineno());
        } else if ((tokenizer.ttype == '\'') || (tokenizer.ttype == '"')) {
            tokenizer.ttype = StreamTokenizer.TT_WORD;
        } else if ((tokenizer.ttype == StreamTokenizer.TT_WORD) &&
                (tokenizer.sval.equals("?"))){
            tokenizer.ttype = '?';
        }
    }
    
    /** Skips all tokens before next end of line (EOL). */
    public static void waitForEOL(StreamTokenizer tokenizer) throws IOException {
        // skip everything until EOL
        while (tokenizer.nextToken() != StreamTokenizer.TT_EOL) {};
        tokenizer.pushBack();
    }
      
    public static void delete(File file) {
    	if (file.isDirectory()) {
			File[] files = file.listFiles();
			for (File child : files) {
				delete(child);
			}
			boolean result = file.delete();
			if (!result)
				LogService.getGlobal().logWarning("Unable to delete file " + file);
    	} else {
			boolean result = file.delete();
			if (!result)
				LogService.getGlobal().logWarning("Unable to delete file " + file);
    	}
    }
    
    public static void copy(File srcPath, File dstPath) throws IOException {
		if (srcPath.isDirectory()) {
			if (!dstPath.exists()) {
				boolean result = dstPath.mkdir();
				if (!result)
					throw new IOException("Unable to create directoy: " + dstPath);
			}

			String[] files = srcPath.list();
			for (int i = 0; i < files.length; i++) {
				copy(new File(srcPath, files[i]), new File(dstPath, files[i]));
			}
		} else {
			if (srcPath.exists()) {
				FileChannel in = null;
				FileChannel out = null;
				try {
					in = new FileInputStream(srcPath).getChannel();
					out = new FileOutputStream(dstPath).getChannel();
					long size = in.size();
					MappedByteBuffer buf = in.map(FileChannel.MapMode.READ_ONLY, 0, size);
					out.write(buf);
				} finally {
					if (in != null)
						in.close();
					if (out != null)
						out.close();
				}
			}
		}
	}
}
