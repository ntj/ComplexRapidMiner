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
package com.rapidminer.tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.GregorianCalendar;

import com.rapidminer.Process;
import com.rapidminer.operator.ProcessRootOperator;
import com.rapidminer.operator.ResultObject;
import com.rapidminer.parameter.UndefinedParameterError;


/**
 * Some statical methods to write results in a file. The class must be set up
 * properly before you can use these methods. Therefore the destination file is
 * read from the global part of the process configuration file.
 * 
 * @author Ingo Mierswa
 * @version $Id: ResultService.java,v 1.5 2007/06/23 00:09:30 ingomierswa Exp $
 */
public class ResultService {

	private static PrintWriter out;

	private static boolean systemStream = true;
	
	/** Without initialization stdout is used. */
	static {
		out = new PrintWriter(System.out);
	}

	/**
	 * Initializes the ResultService.
	 * 
	 * @param filename
	 *            destination file.
	 * @param process
	 *            the process which is used to resolve the filename
	 */
	public static void init(String filename, Process process) {
		if (filename == null) {
            process.getLog().log("No filename given for result file, using stdout for logging results!", LogService.NOTE);
			init(new PrintWriter(System.out));
		} else if (filename.equals("stderr")) {
			init(new PrintWriter(System.err));
		} else if (filename.equals("stdout")) {
			init(new PrintWriter(System.out));
		} else {
			File file = process.resolveFileName(filename);
			PrintWriter out;
			String encoding = process.getRootOperator().getEncoding();
			try {
				out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), encoding));
			} catch (IOException e) {
                process.getLog().log("Cannot create resultfile '" + filename + "': " + e.getClass() + ":" + e.getMessage(), LogService.MAXIMUM);
                process.getLog().log("using stdout", LogService.MAXIMUM);
				out = new PrintWriter(System.out);
			}
			systemStream = false;
			init(out);
		}
	}

	/**
	 * Initializes the ResultService.
	 * 
	 * @param outStream
	 *            stream to write the results to.
	 */
	public static void init(PrintWriter outWriter) {
		ResultService.out = outWriter;
	}

	/** Initializes the ResultService. */
	public static void init(Process process) {
		String filename = null;
		try {
			filename = process.getRootOperator().getParameterAsString(ProcessRootOperator.PARAMETER_RESULTFILE);
		} catch (UndefinedParameterError e) {
			// tries to read result file
			// if no file was specified use 'null' --> use System.out
		}
		init(filename, process);
	}

	/** Closes the stream. */
	public static void close() {
		if (!systemStream)
			out.close();
	}

	/** Writes the string in the general result file. */
	public static void logResult(String result) {
        logResult(result, out);
	}

    /** Writes the string in the given stream. */
    public static void logResult(String result, PrintWriter localOut) {
        localOut.println(getTime() + " " + result);
    }

	/** Writes the result string from the given result object in the result file. */
	public static void logResult(ResultObject resultObject) {
		logResult(resultObject, out);
	}

    /** Writes the result string from the given result object in the result file. */
    public static void logResult(ResultObject resultObject, PrintWriter localOut) {
        logResult(resultObject.toResultString(), localOut);
    }

	/** Returns the current date and time as formatted string. */
	private static String getTime() {
		GregorianCalendar cal = (GregorianCalendar) Calendar.getInstance();
		String time = getTwoDigits(cal.get(Calendar.DAY_OF_MONTH)) + "." + getTwoDigits((cal.get(Calendar.MONTH) + 1)) + "." + cal.get(Calendar.YEAR) + " " + getTwoDigits(cal.get(Calendar.HOUR_OF_DAY)) + ":" + getTwoDigits(cal.get(Calendar.MINUTE)) + ":"
				+ getTwoDigits(cal.get(Calendar.SECOND));
		return time;
	}

	/** Adds a leading zero. */
	static String getTwoDigits(int i) {
		return (i < 10 ? "0" : "") + i;
	}
}
