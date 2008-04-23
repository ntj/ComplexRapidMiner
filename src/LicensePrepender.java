/*
 *  RapidMiner
 *
 *  Copyright (C) 2001-2006 by Rapid-I and the contributors
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import com.rapidminer.tools.Tools;


/**
 * Prepends the license text before the package statement. Replaces all existing
 * comments before. Ignores files without package statement.
 * 
 * @author Simon Fischer, Ingo Mierswa
 * @version $Id: LicensePrepender.java,v 2.11 2007/05/28 21:23:34 ingomierswa Exp $
 */
public class LicensePrepender {

	private char[] license;

	private char[] readFile(File file, String from) throws IOException {
		StringBuffer contents = new StringBuffer((int) file.length());

		BufferedReader in = new BufferedReader(new FileReader(file));
		try {
			String line = null;
			while (((line = in.readLine()) != null) && (!line.startsWith("package")));
			if (line == null) {
				System.err.println("'package' not found in file '" + file + "'.");
				return null;
			}

			do {
				contents.append(line);
				contents.append(Tools.getLineSeparator());
			} while ((line = in.readLine()) != null);
		}
		finally {
			/* Close the stream even if we return early. */
			in.close();
		}
		return contents.toString().toCharArray();
	}

	private void readLicense(File file, String additionalAdmin) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(file));
		String line = null;
		StringBuffer licenseText = new StringBuffer(); 
		while ((line = in.readLine()) != null) {
			licenseText.append(line + Tools.getLineSeparator());
			/*
			if ((additionalAdmin != null) && (line.toLowerCase().indexOf("administrator") >= 0)) {
				licenseText.append(" *  " + additionalAdmin + Tools.getLineSeparator());
			}
			*/
		}
		in.close();
		this.license = licenseText.toString().toCharArray();
	}

	private void prependLicense(File file) throws IOException {
		System.out.print(file + "...");

		char[] fileContents = readFile(file, "package");
		if (fileContents == null)
			return;

		Writer out = new FileWriter(file);
		out.write(license);
		out.write(fileContents);
		out.close();
		System.out.println("ok");
	}

	private void recurse(File file) {
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			for (int i = 0; i < files.length; i++) {
				recurse(files[i]);
			}
		} else {
			if (file.getName().endsWith(".java")) {
				try {
					prependLicense(file);
				} catch (IOException e) {
					System.err.println("failed: " + e.getClass().getName() + ": " + e.getMessage());
				}
			}
		}

	}

	public static void main(String[] argv) throws Exception {

		LicensePrepender lp = new LicensePrepender();

		if ((argv.length < 1) || (argv[0].equals("-help"))) {
			System.out.println("Usage: java " + lp.getClass().getName() + " licensefile [additional_admin]");
			System.exit(1);
		}

		String additionalAdmin = null;
		if (argv.length >= 3) {
			StringBuffer additionalAdminBuffer = new StringBuffer();
			for (int i = 2; i < argv.length; i++)
				additionalAdminBuffer.append(argv[i] + " ");
			additionalAdmin = additionalAdminBuffer.toString();
		}
		
		lp.readLicense(new File(argv[0]), additionalAdmin);
		System.out.println("Prepending license:");
		System.out.print(lp.license);

		lp.recurse(new File(argv[1]));
	}
}
