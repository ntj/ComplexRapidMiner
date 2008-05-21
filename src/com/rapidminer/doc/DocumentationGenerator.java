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
package com.rapidminer.doc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Iterator;

import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.tools.GroupTree;
import com.rapidminer.tools.LogService;
import com.rapidminer.tools.OperatorService;
import com.rapidminer.tools.ParameterService;
import com.rapidminer.tools.Tools;
import com.sun.javadoc.RootDoc;


/**
 * This is the main class of documentation generation for RapidMiner operators. The target format is LaTeX, therefore a
 * {@link LatexOperatorDocGenerator} is used. If no arguments are given to the main method, the LaTeX documentation of
 * the RapidMiner core is generated. If arguments are specified other documentation may be also generated, e.g. for plugin
 * operators.
 * 
 * @author Simon Fischer, Ingo Mierswa
 * @version $Id: DocumentationGenerator.java,v 1.4 2008/05/09 19:23:22 ingomierswa Exp $
 */
public class DocumentationGenerator {

	private OperatorDocGenerator generator;

	private static RootDoc rootDoc = null;

	public DocumentationGenerator(OperatorDocGenerator generator) {
		this.generator = generator;
	}

	/** Use only classes beneath the operator package. */
	private void getRootDoc() {
		getRootDoc(new File(ParameterService.getRapidMinerHome(), "src" + File.separator), "com.rapidminer.operator");
	}

	private void getRootDoc(File srcDir, String subpackages) {
		LogService.getGlobal().log("Starting javadoc!", LogService.STATUS);
		String[] javadocargs = { "-sourcepath", srcDir.getAbsolutePath(), "-doclet", this.getClass().getName(), "-breakiterator", "-subpackages",
				subpackages };
		com.sun.tools.javadoc.Main.execute(javadocargs);
		if (rootDoc == null)
			LogService.getGlobal().log("RootDoc not set!", LogService.ERROR);
	}

	public static boolean start(RootDoc rootDoc) {
		LogService.getGlobal().log("RootDoc generated!", LogService.STATUS);
		DocumentationGenerator.rootDoc = rootDoc;
		return true;
	}

	public void generateAll(PrintWriter out) {
		generateAll(out, false);
	}

	public void generateAll(PrintWriter out, boolean generateSubgroups) {
		GroupTree root = OperatorService.getGroups();
		if (root.getOperatorDescriptions().size() > 0) {
			// print main operators
			generator.beginGroup(null, out);
			generateOperators(out, root.getOperatorDescriptions());
			generator.endGroup(null, out);
		}

		// print subgroups
		Collection groups = root.getSubGroups();
		Iterator i = groups.iterator();
		while (i.hasNext()) {
			GroupTree group = (GroupTree) i.next();
			generateGroup(out, group, generateSubgroups);
		}
		out.println();
		out.flush();
	}

	public void generateGroup(PrintWriter out, GroupTree group, boolean generateSubgroups) {
		generator.beginGroup(group.getName(), out);
		if (generateSubgroups) {
			generateOperators(out, group.getOperatorDescriptions());
			Collection groups = group.getSubGroups();
			Iterator i = groups.iterator();
			while (i.hasNext()) {
				GroupTree subgroup = (GroupTree) i.next();
				generateGroup(out, subgroup, generateSubgroups);
			}
		} else {
			generateOperators(out, group.getAllOperatorDescriptions());
		}
		generator.endGroup(group.getName(), out);
	}

	public void generateOperators(PrintWriter out, Collection<OperatorDescription> operators) {
		Iterator<OperatorDescription> ops = operators.iterator();
		while (ops.hasNext()) {
			OperatorDescription description = ops.next();
			try {
				Operator operator = description.createOperatorInstance();
				generator.generateDoc(operator, rootDoc, out);
			} catch (Exception e) {
				e.printStackTrace(out);
				System.err.println("Error in " + description.getName() + ": " + e.getMessage());
			}

		}
		out.println();
	}

	/**
	 * If no arguments are given, the LaTeX documentation of the RapidMiner core is generated. Otherwise this documentation
	 * generator can be used to generated the documentation of arbitrary RapidMiner operators, e.g. for plugins. In this case
	 * the arguments are: <br/> &lt;operators.xml&gt; &lt;sourcedir&gt; &lt;packages&gt; &lt;with_subgroups&gt;
	 */
	public static void main(String[] argv) throws IOException {
		if (argv.length == 1) {
			OperatorDocGenerator opDocGen = null;
			if (argv[0].equals("LATEX"))
				 opDocGen = new LatexOperatorDocGenerator();
			else
				opDocGen = new ProgramHTMLOperatorDocGenerator();
			
			ParameterService.init();
			File file = new File(ParameterService.getRapidMinerHome(), "tutorial" + File.separator + "OperatorsGenerated.tex");
			LogService.getGlobal().log("Generating class documentation to '" + file + "'.", LogService.STATUS);
			DocumentationGenerator docGen = new DocumentationGenerator(opDocGen);
			docGen.getRootDoc();
			docGen.generateAll(new PrintWriter(new FileWriter(file)));
		} else if (argv.length == 2) {
			OperatorDocGenerator opDocGen = null;
			if (argv[0].equals("LATEX"))
				 opDocGen = new LatexOperatorDocGenerator();
			else
				opDocGen = new ProgramHTMLOperatorDocGenerator();
			
			ParameterService.init();
			File file = new File(argv[1]);
			LogService.getGlobal().log("Generating class documentation to '" + file + "'.", LogService.STATUS);
			DocumentationGenerator docGen = new DocumentationGenerator(opDocGen);
			docGen.getRootDoc();
			docGen.generateAll(new PrintWriter(new FileWriter(file)));
		} else if (argv.length >= 5) {
			OperatorDocGenerator opDocGen = null;
			if (argv[0].equals("LATEX"))
				 opDocGen = new LatexOperatorDocGenerator();
			else
				opDocGen = new ProgramHTMLOperatorDocGenerator();
			
			try {
				OperatorService.registerOperators(argv[1], new FileInputStream(argv[1]), null, true);
			} catch (IOException e) {
				LogService.getGlobal().log("Cannot read 'operators.xml'.", LogService.ERROR);
			}
			File file = new File(argv[4]);
			LogService.getGlobal().log("Generating class documentation to '" + file + "'.", LogService.STATUS);
			PrintWriter out = new PrintWriter(new FileWriter(file));

			DocumentationGenerator docGen = new DocumentationGenerator(opDocGen);
			boolean generateSubgroups = false;
			if (argv.length == 6) {
				if (argv[5].equals("true"))
					generateSubgroups = true;
			}
			docGen.getRootDoc(new File(argv[2]), argv[3]);
			docGen.generateAll(new PrintWriter(new FileWriter(file)), generateSubgroups);

			out.close();
		} else {
			LogService.getGlobal().log("usage: java com.rapidminer.doc.DocumentationGenerator or" + Tools.getLineSeparator()
					+ "       java com.rapidminer.doc.DocumentationGenerator operatordesc srcdir subpackages outputfile [generate subgroups (true/false)]",
					LogService.WARNING);
		}
	}
}
