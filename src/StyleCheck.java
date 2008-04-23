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
import java.io.Writer;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import com.rapidminer.tools.Tools;


/**
 * This class can be used to perform some style checks. In addition, this
 * program might give useful informations like the totalClasses number of lines or the
 * classes written by a particular author.
 * 
 * @author Ingo Mierswa
 * @version $Id: StyleCheck.java,v 2.14 2007/05/28 21:23:34 ingomierswa Exp $
 */
public class StyleCheck {

	/**
	 * This command indicates that all classes should be checked for maximal
	 * length.
	 */
	private static final String SIZE_CHECK = "size_check";

	/**
	 * This command indicates that the totalClasses number of all lines of code of all
	 * classes should be counted.
	 */
	private static final String LINES_COUNT = "total_lines";

	/**
	 * This command indicates that all classes should be checked for missing
	 * class comments.
	 */
	private static final String COMMENT_CHECK = "comment_check";

	/**
	 * This command indicates that all classes should be checked for missing
	 * version tags.
	 */
	private static final String VERSION_CHECK = "version_check";

	/**
	 * This command indicates that all classes should be checked for missing
	 * author tags.
	 */
	private static final String AUTHOR_CHECK = "author_check";

	/**
	 * This command indicates that the a list of all classes should be returned
	 * written by the given author.
	 */
	private static final String AUTHOR_LIST = "author_list";

	/**
	 * This command indicates that the number of all classes should be counted
	 * written by the given author.
	 */
	private static final String AUTHOR_COUNT = "author_count";

	/**
	 * This command indicates that the given author name should be replaced by
	 * the new one (e.g. 'ingo' by 'Ingo Mierswa').
	 */
	private static final String AUTHOR_REPLACE = "author_replace";

	/**
	 * This command indicates that a list of all author names should be printed
	 * with the number of class they have written.
	 */
	private static final String ALL_AUTHORS = "all_authors";

	/**
	 * This command indicates that all classes should be checked for the license
	 * text.
	 */
	private static final String LICENSE_CHECK = "license_check";

	/** Superclass for all Java file checker. */
	private static abstract class JavaFileChecker {

		/**
		 * Default implementation does nothing. Invoked after check and might be
		 * overridden by subclasses.
		 */
		public void printResult() {}

		/** Recursively checks all files. */
		public void checkAllFiles(File current) throws Exception {
			if (!current.isDirectory()) {
				if (current.getName().endsWith("java"))
					performCheck(current);
			} else {
				File[] children = current.listFiles();
				for (int i = 0; i < children.length; i++)
					checkAllFiles(children[i]);
			}
		}

		/** Must be implemented by subclasses. */
		public abstract void performCheck(File file) throws Exception;
	}

	/**
	 * Checks all Java files for a maximum size and prints a warning message if
	 * this number of lines is exceeded.
	 */
	private static class SizeChecker extends JavaFileChecker {

		private int maxLines;

		public SizeChecker(int maxLines) {
			this.maxLines = maxLines;
		}

		public void performCheck(File file) throws Exception {
			BufferedReader in = new BufferedReader(new FileReader(file));
			int counter = 0;
			String line = null;
			while ((line = in.readLine()) != null) {
				if (line.trim().length() > 0)
					counter++;
			}
			in.close();
			if (counter > maxLines) {
				System.out.println(file.getPath() + ": more than " + maxLines + " lines!");
			}
		}
	}

	/** Counts all lines and prints the totalClasses number of lines. */
	private static class LineCounter extends JavaFileChecker {

		private int total = 0;
		
		public void performCheck(File file) throws Exception {
			BufferedReader in = new BufferedReader(new FileReader(file));
			String line = null;
			while ((line = in.readLine()) != null) {
				if (line.trim().length() > 0)
					total++;
			}
			in.close();
		}

		public void printResult() {
			System.out.println("Total number of lines: " + total);
		}
	}

	/** Checks all Java files for a class comment. */
	private static class CommentChecker extends JavaFileChecker {

		public void performCheck(File file) throws Exception {
			BufferedReader in = new BufferedReader(new FileReader(file));
			String line = null;
			boolean commentFound = false;
			boolean inComment = false;
			boolean packageFound = false;
			while ((line = in.readLine()) != null) {
				// ignore everything before the package starts
				if (line.trim().startsWith("package"))
					packageFound = true;
				if (!packageFound)
					continue;

				if (line.trim().startsWith("/**")) {
					commentFound = true;
					inComment = true;
				}
				if (inComment && (line.indexOf("*/") > 0))
					inComment = false;

				if ((!inComment) && ((line.indexOf(" class ") > 0) || (line.indexOf(" interface ") > 0)))
					break;
			}
			in.close();
			if (!commentFound) {
				System.out.println(file.getPath() + ": it seems that there is no class comment (or no package...)!");
			}
		}
	}

	/** Checks all Java files for a version tag. */
	private static class VersionChecker extends JavaFileChecker {

		public void performCheck(File file) throws Exception {
			BufferedReader in = new BufferedReader(new FileReader(file));
			String line = null;
			boolean versionFound = false;
			while ((line = in.readLine()) != null) {
				if (line.indexOf("@version") > 0) {
					if (line.indexOf("$Id") > 0) {
						versionFound = true;
						break;
					}
				}
				// if ((line.indexOf(" class ") > 0) || (line.indexOf("
				// interface ") > 0))
				// break;
			}
			in.close();
			if (!versionFound) {
				System.out.println(file.getPath() + ": missing or non-conform @version tag!");
			}
		}
	}

	/** Checks all Java files for an author tag. */
	private static class AuthorChecker extends JavaFileChecker {

		public void performCheck(File file) throws Exception {
			BufferedReader in = new BufferedReader(new FileReader(file));
			String line = null;
			boolean authorFound = false;
			while ((line = in.readLine()) != null) {
				if (line.indexOf("@author") > 0) {
					authorFound = true;
					break;
				}
				// if ((line.indexOf(" class ") > 0) || (line.indexOf("
				// interface ") > 0))
				// break;
			}
			in.close();
			if (!authorFound) {
				System.out.println(file.getPath() + ": missing @author tag!");
			}
		}
	}

	/** Collects all Java files written by the given author (and not by the second, optionally). */
	private static class AuthorList extends JavaFileChecker {

		private String author;

        private String exceptAuthor;
        
		private List<String> result = new LinkedList<String>();

		public AuthorList(String author) {
			this(author, null);
		}

        public AuthorList(String author, String exceptAuthor) {
            this.author = author;
            this.exceptAuthor = exceptAuthor;
        }
        
		public void performCheck(File file) throws Exception {
			BufferedReader in = new BufferedReader(new FileReader(file));
			String line = null;
			while ((line = in.readLine()) != null) {
                int index = line.indexOf("@author"); 
				if (index >= 0) {
                    String authorLine = line.substring(index + "@author".length());
                    String[] authors = authorLine.split(",");
                    boolean authorFound = false;
                    boolean exceptAuthorFound = false;
                    for (String a : authors) {
                        if (a.trim().toLowerCase().indexOf(author.toLowerCase()) >= 0) {
                            authorFound = true;
                        }
                        if ((exceptAuthor != null) && (a.trim().toLowerCase().indexOf(exceptAuthor.trim().toLowerCase()) >= 0)) {
                            exceptAuthorFound = true;
                        }
                    }
                    if (authorFound && (!exceptAuthorFound))
                        result.add(file.getPath());
                    break;
                }
			}
			in.close();
		}

		public void printResult() {
			System.out.println(result.size() + " classes written by " + author + (exceptAuthor != null ? " and not by " + exceptAuthor + ":" : ":"));
			Iterator<String> i = result.iterator();
			while (i.hasNext()) {
				System.out.println(i.next());
			}
		}
	}

	/** Counts all Java files written by the given author. */
	private static class AuthorCounter extends JavaFileChecker {

		private String author;

		private int counter = 0;

		private int total = 0;

		public AuthorCounter(String author) {
			this.author = author;
		}

		public void performCheck(File file) throws Exception {
			total++;
			BufferedReader in = new BufferedReader(new FileReader(file));
			String line = null;
			while ((line = in.readLine()) != null) {
				if (line.indexOf("@author") > 0) {
					if (line.toLowerCase().indexOf(author.toLowerCase()) > 0) {
						counter++;
						break;
					}
				}
			}
			in.close();
		}

		public void printResult() {
			System.out.println("Number of classes written by " + author + ": " + counter + " / " + total);
		}
	}

	/** Counts all Java files written by the given author. */
	private static class AuthorReplace extends JavaFileChecker {

		private String oldName, newName;

		private int counter = 0;

		public AuthorReplace(String oldName, String newName) {
			this.oldName = oldName;
			this.newName = newName;
		}

		public void performCheck(File file) throws Exception {
			boolean replace = false;
			StringBuffer result = new StringBuffer();
			BufferedReader in = new BufferedReader(new FileReader(file));
			String line = null;
			while ((line = in.readLine()) != null) {
				if (line.indexOf("@author") > 0) {
					String newAuthorLine = line;
					if (line.toLowerCase().indexOf(newName.toLowerCase()) < 0) {
						int start = line.toLowerCase().indexOf(oldName.toLowerCase());
						if (start > 0) {
							newAuthorLine = line.substring(0, start) + newName;
							newAuthorLine += line.substring(start + oldName.length());
							replace = true;
						}
					}
					result.append(newAuthorLine + Tools.getLineSeparator());
				} else {
					result.append(line + Tools.getLineSeparator());
				}
			}
			in.close();

			if (replace) {
				counter++;
				Writer out = new FileWriter(file);
				out.write(result.toString().toCharArray());
				out.close();
			}
		}

		public void printResult() {
			System.out.println("Number of classes where " + oldName + " was replaced by " + newName + ": " + counter);
		}
	}

	/** Counts all Java files written by all authors and prints them. */
	private static class AllAuthors extends JavaFileChecker {

        private int totalLines = 0;
        
		private int totalClasses = 0;

		private SortedMap<String, Integer> authorClassNumberMap = new TreeMap<String, Integer>();

		private SortedMap<String, Integer> authorLineNumberMap = new TreeMap<String, Integer>();

		public void performCheck(File file) throws Exception {
			totalClasses++;
			String[] authors = null;
			BufferedReader in = new BufferedReader(new FileReader(file));
			String line = null;
			int lines = 0;
			while ((line = in.readLine()) != null) {
				lines++;
				int index = line.indexOf("@author");
				if (index > 0) {
					String authorLine = line.substring(index + "@author".length());
					authors = authorLine.split(",");
				}
			}
            totalLines += lines;
			if (authors != null) {
				for (int i = 0; i < authors.length; i++) {
					String currentAuthor = authors[i].trim();

					Integer classCounter = authorClassNumberMap.get(currentAuthor);
					if (classCounter == null) {
						authorClassNumberMap.put(currentAuthor, 1);
					} else {
						authorClassNumberMap.put(currentAuthor, classCounter.intValue() + 1);
					}

					Integer lineCounter = authorLineNumberMap.get(currentAuthor);
					if (lineCounter == null) {
						authorLineNumberMap.put(currentAuthor, lines);
					} else {
						authorLineNumberMap.put(currentAuthor, lineCounter.intValue() + lines);
					}
				}
			}
			in.close();
		}

		public void printResult() {
			System.out.println("Total number of classes: " + totalClasses);
            System.out.println("Total number of lines:   " + totalLines);
            System.out.println("----------------------------------------------------------");
            
            class AuthorCounter implements Comparable<AuthorCounter> {
                private String name;
                private int lines;
                private int classes;
                private NumberFormat formatter;
                
                public AuthorCounter(String name, int lines, int classes, NumberFormat formatter) {
                    this.name = name;
                    this.lines = lines;
                    this.classes = classes;
                    this.formatter = formatter;
                }
                public int compareTo(AuthorCounter a) {
                    return -1 * Double.compare(this.classes, a.classes);
                }
                public boolean equals(Object o) {
                	if (!(o instanceof AuthorCounter)) {
                		return false;
                	} else {
                		return this.name.equals(((AuthorCounter)o).name);
                	}
                }
                public int hashCode() {
                	return this.name.hashCode();
                }
                public int getStringLength() {
                    return (name + ": " + classes + " (" + lines + " lines)").length();
                }
                public String toString(int numberOfSpaces) {
                    StringBuffer spaceString = new StringBuffer();
                    for (int i = 0; i < numberOfSpaces; i++)
                        spaceString.append(" ");
                    double classPercent = (double)classes / (double)totalClasses; 
                    double linesPercent = (double)lines / (double)totalLines; 
                    return 
                        name + ": " + classes + " (" + lines + " lines) " + spaceString + " --> " + 
                        formatter.format(classPercent) + " (" + formatter.format(linesPercent) + ")";
                }
            }
            
            NumberFormat formatter = NumberFormat.getPercentInstance();
            formatter.setMaximumFractionDigits(1);
            formatter.setMinimumFractionDigits(1);
            List<AuthorCounter> authorCounters = new LinkedList<AuthorCounter>();
            Iterator<String> i = authorClassNumberMap.keySet().iterator();
            int maxLength = 0;
			while (i.hasNext()) {
				String author = i.next();
                int classNumber =  authorClassNumberMap.get(author);
                int lineNumber  = authorLineNumberMap.get(author);
                AuthorCounter newCounter = new AuthorCounter(author, lineNumber, classNumber, formatter); 
                authorCounters.add(newCounter);
                maxLength = Math.max(maxLength, newCounter.getStringLength());
			}
            Collections.sort(authorCounters);
            Iterator<AuthorCounter> a = authorCounters.iterator();
            while (a.hasNext()) {
                AuthorCounter current = a.next();
                System.out.println(current.toString(maxLength - current.getStringLength()));
            }
            
            System.out.println("----------------------------------------------------------");
		}
	}

	/** Checks all Java files for a version tag. */
	private static class LicenseChecker extends JavaFileChecker {

		private static String LICENSE_LINE = "Copyright (C) 2001-2006";

		public void performCheck(File file) throws Exception {
			BufferedReader in = new BufferedReader(new FileReader(file));
			String line = null;
			boolean licenseFound = false;
			while ((line = in.readLine()) != null) {
				if (line.indexOf(LICENSE_LINE) > 0) {
					licenseFound = true;
					break;
				}
			}
			in.close();
			if (!licenseFound) {
				System.out.println(file.getPath() + ": missing or non-conform license");
			}
		}
	}

	// ================================================================================

	public static void checkSize(String[] argv) throws Exception {
		if (argv.length != 3) {
			System.err.println("usage: java StyleCheck " + SIZE_CHECK + " max_lines path");
			System.exit(1);
		}
		JavaFileChecker checker = new SizeChecker(Integer.parseInt(argv[1]));
		checker.checkAllFiles(new File(argv[2]));
		checker.printResult();
	}

	public static void countLines(String[] argv) throws Exception {
		if (argv.length != 2) {
			System.err.println("usage: java StyleCheck " + LINES_COUNT + " path");
			System.exit(1);
		}
		JavaFileChecker checker = new LineCounter();
		checker.checkAllFiles(new File(argv[1]));
		checker.printResult();
	}

	public static void checkComment(String[] argv) throws Exception {
		if (argv.length != 2) {
			System.err.println("usage: java StyleCheck " + COMMENT_CHECK + " path");
			System.exit(1);
		}
		JavaFileChecker checker = new CommentChecker();
		checker.checkAllFiles(new File(argv[1]));
		checker.printResult();
	}

	public static void checkVersion(String[] argv) throws Exception {
		if (argv.length != 2) {
			System.err.println("usage: java StyleCheck " + VERSION_CHECK + " path");
			System.exit(1);
		}
		JavaFileChecker checker = new VersionChecker();
		checker.checkAllFiles(new File(argv[1]));
		checker.printResult();
	}

	public static void checkAuthor(String[] argv) throws Exception {
		if (argv.length != 2) {
			System.err.println("usage: java StyleCheck " + AUTHOR_CHECK + " path");
			System.exit(1);
		}
		JavaFileChecker checker = new AuthorChecker();
		checker.checkAllFiles(new File(argv[1]));
		checker.printResult();
	}

	public static void createAuthorList(String[] argv) throws Exception {
		if ((argv.length < 3) || (argv.length > 4)) {
			System.err.println("usage: java StyleCheck " + AUTHOR_LIST + " path authorname [exceptauthor]");
			System.exit(1);
		}
		JavaFileChecker checker = null;
        if (argv.length == 3)
            checker = new AuthorList(argv[2]);
        else
            checker = new AuthorList(argv[2], argv[3]);
        
		checker.checkAllFiles(new File(argv[1]));
		checker.printResult();
	}

	public static void createAuthorCount(String[] argv) throws Exception {
		if (argv.length != 3) {
			System.err.println("usage: java StyleCheck " + AUTHOR_COUNT + " authorName path");
			System.exit(1);
		}
		JavaFileChecker checker = new AuthorCounter(argv[1]);
		checker.checkAllFiles(new File(argv[2]));
		checker.printResult();
	}

	public static void replaceAuthor(String[] argv) throws Exception {
		if (argv.length != 4) {
			System.err.println("usage: java StyleCheck " + AUTHOR_REPLACE + " oldAuthorName newAuthorName path");
			System.exit(1);
		}
		JavaFileChecker checker = new AuthorReplace(argv[1], argv[2]);
		checker.checkAllFiles(new File(argv[3]));
		checker.printResult();
	}

	public static void listAllAuthors(String[] argv) throws Exception {
		if (argv.length != 2) {
			System.err.println("usage: java StyleCheck " + ALL_AUTHORS + " path");
			System.exit(1);
		}
		JavaFileChecker checker = new AllAuthors();
		checker.checkAllFiles(new File(argv[1]));
		checker.printResult();
	}

	public static void checkForLicenseText(String[] argv) throws Exception {
		if (argv.length != 2) {
			System.err.println("usage: java StyleCheck " + LICENSE_CHECK + " path");
			System.exit(1);
		}
		JavaFileChecker checker = new LicenseChecker();
		checker.checkAllFiles(new File(argv[1]));
	}

	// ================================================================================

	public static void main(String[] argv) throws Exception {
		if (argv.length < 2) {
			System.err.println("usage: java StyleCheck command <arguments> path");
			System.exit(1);
		}
		if (argv[0].equals(SIZE_CHECK)) {
			checkSize(argv);
		} else if (argv[0].equals(LINES_COUNT)) {
			countLines(argv);
		} else if (argv[0].equals(COMMENT_CHECK)) {
			checkComment(argv);
		} else if (argv[0].equals(VERSION_CHECK)) {
			checkVersion(argv);
		} else if (argv[0].equals(AUTHOR_CHECK)) {
			checkAuthor(argv);
		} else if (argv[0].equals(AUTHOR_LIST)) {
			createAuthorList(argv);
		} else if (argv[0].equals(AUTHOR_COUNT)) {
			createAuthorCount(argv);
		} else if (argv[0].equals(AUTHOR_REPLACE)) {
			replaceAuthor(argv);
		} else if (argv[0].equals(ALL_AUTHORS)) {
			listAllAuthors(argv);
		} else if (argv[0].equals(LICENSE_CHECK)) {
			checkForLicenseText(argv);
		} else {
			System.err.println("Unknown command: " + argv[0]);
			System.exit(1);
		}
	}
}
