// Main.java, created Sat Mar 15 19:14:23 2003 by cananian
// Copyright (C) 2003 C. Scott Ananian (cscott@cscott.net)
// Licensed under the terms of the GNU GPL; see COPYING for details.
package net.cscott.sinjdoc;

import net.cscott.sinjdoc.lexer.EscapedUnicodeReader;
import net.cscott.sinjdoc.parser.FileUtil;
import net.cscott.sinjdoc.parser.ParseControl;
import net.cscott.sinjdoc.parser.PRootDoc;
import net.cscott.sinjdoc.html.HTMLDoclet;

import java.lang.reflect.Modifier;
import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

/**
 * <code>Main</code> implements a javadoc-alike that properly handles
 * JSR-14 java code (-source 1.5).  The programmatic interface is
 * deliberately very similar to that of Sun's javadoc, although
 * slightly different option sets may be supported.
 * 
 * @author  C. Scott Ananian (cscott@cscott.net)
 * @version $Id: Main.java,v 1.18 2003/08/01 00:01:26 cananian Exp $
 */
public class Main {
    /** Command-line interface.  Exits using <code>System.exit()</code>.
     *  Doesn't return an exit code.
     * @param args The command line parameters.
     */
    public static void main(String[] args) {
	System.exit(execute(args));
    }
    /** Programmatic interface.
     * @param args The command line parameters.
     * @return the exit code.
     */
    public static int execute(String[] args) {
	return execute("sinjdoc", args);
    }
    /** Programmatic interface.
     * @param programName Name of the program, for error messages.
     * @param arg The command line parameters.
     * @return the exit code.
     */
    public static int execute(String programName, String[] args) {
	return execute(programName, HTMLDoclet.class.getName(), args);
    }
    /** Programmatic interface.
     * @param programName Name of the program, for error messages.
     * @param defaultDocletClassName ignored.
     * @param arg The command line parameters.
     * @return the exit code.
     */
    public static int execute(String programName,
			      String defaultDocletClassName, String[] args) {
	PrintWriter err = new PrintWriter(System.err, true);
	PrintWriter out = new PrintWriter(System.out, true);
	return execute(programName, err, err, out,
		       defaultDocletClassName, args);
    }
    /** Programmatic interface.
     * @param programName Name of the program, for error messages.
     * @param errWriter PrintWriter to receive error messages.
     * @param warnWriter PrintWriter to receive warning messages.
     * @param noticeWriter PrintWriter to receive notice messages.
     * @param defaultDocletClassName ignored.
     * @param arg The command line parameters.
     * @return the exit code.
     */
    public static int execute(String programName,
			      final PrintWriter errWriter,
			      final PrintWriter warnWriter,
			      final PrintWriter noticeWriter,
			      String defaultDocletClassName,
			      String[] args) {
	RunData runData = new RunData(programName,
				      errWriter, warnWriter, noticeWriter,
				      defaultDocletClassName);
	try {
	    List<String> nonOptionArgs = new ArrayList<String>();
	    List<List<String>> docletOptions = new ArrayList<List<String>>();
	    
	    // expand all '@' options first.
	    LinkedList<String> argList = new LinkedList<String>();
	    for (String anArg : args) {
		if (anArg.startsWith("@")) {
		    // process arg file.
		    try {
			Reader r = new BufferedReader
			    (new FileReader(anArg.substring(1)));
			StreamTokenizer st = new StreamTokenizer(r);
			st.resetSyntax(); // set up tokenizer.
			st.whitespaceChars(0, ' ');
			st.wordChars('!','!');
			st.quoteChar('"');
			st.commentChar('#');
			st.wordChars('$','&');
			st.quoteChar('\'');
			st.wordChars('(','~');
			st.wordChars(160, 255);// iso 8859-1 / unicode
			while (st.nextToken()!=st.TT_EOF) {
			    assert st.sval!=null;
			    argList.addLast(st.sval);
			}
			r.close();
		    } catch (IOException e) {
			runData.reporter.printError
			    ("Error processing "+anArg+": "+e);
		    }
		} else argList.addLast(anArg);
	    }
	    // parse all the java doc options.
	    while (!argList.isEmpty()) {
		String anArg = argList.removeFirst();
		assert anArg!=null;
		if (!anArg.startsWith("-"))
		    nonOptionArgs.add(anArg);
		else if (anArg.startsWith("-J")) {
		    // XXX Java option.
		    runData.reporter.printWarning("Ignoring option: "+anArg);
		} else if (stdOptions.containsKey(anArg.toLowerCase())) {
		    // standard option. (note that it is case-insensitive)
		    Option opt = stdOptions.get(anArg.toLowerCase());
		    assert opt.len>0;
		    List<String> optionWithArgs=new ArrayList<String>(opt.len);
		    optionWithArgs.add(anArg);
		    for (int j=1; j<opt.len; j++)
			// xxx handle array out of bounds here.
			optionWithArgs.add(argList.removeFirst());
		    // now process the arg.
		    opt.process(runData, optionWithArgs);
		    // special case for -help
		    if (anArg.toLowerCase().equals("-help"))
			return 0; // exit now.
		} else { // try as a doclet option.
		    Doclet doclet = runData.getDoclet();
		    int len = doclet.optionLength(anArg);
		    if (len==0)
			runData.reporter.printError("Unknown option: "+anArg);
		    else {
			List<String> optionWithArgs =
			    new ArrayList<String>(len);
			optionWithArgs.add(anArg);
			for (int j=1; j<len; j++)
			    // xxx handle array out of bounds here.
			    optionWithArgs.add(argList.removeFirst());
			docletOptions.add(optionWithArgs);
		    }
		}
	    }
	    // finish initializing standard options.
	    if (runData.sourcePath==null)
		runData.sourcePath =
		    (runData.classPath!=null) ? runData.classPath : ".";
	    if (runData.classPath==null)
		runData.classPath = System.getProperty("java.class.path");
	    runData.parseControl.setSourcePath(runData.sourcePath);
	    FileUtil fu = runData.parseControl.getSourcePath();

	    // check doclet options.
	    boolean success=false;
	    Doclet doclet = runData.getDoclet();
	    if (doclet.validOptions(docletOptions, runData.reporter)) {
		// separate nonOptionArgs into packages and source files.
		// note that '*' is accepted in the source file spec.
		List<String> packages = new ArrayList<String>();
		List<File> sourcefiles = new ArrayList<File>();
		for (String candidate : nonOptionArgs) {
		    if (candidate.indexOf('*')<0 &&
			!candidate.toLowerCase().endsWith(".java")) {
			// try as package name.
			if (fu.isValidPackageName(candidate))
			    packages.add(candidate);
		    } else {
			// try as source file name.
			for (File f : fu.expandFileStar(candidate)) {
			    if (fu.isValidClassName(f.getName()) &&
				f.isFile() && f.exists())
				sourcefiles.add(f);
			}
		    }
		}
		// expand subpackage spec.
		packages.addAll(fu.expandPackages(runData.subpackages,
						  runData.exclude));
		// okay.  have parsed all options.
		runData.parseControl.setSourceFiles(sourcefiles);
		runData.parseControl.setPackages(packages);
		PRootDoc rootDoc = runData.parseControl.parse();
		if (rootDoc!=null) rootDoc.setOptions(docletOptions);
		// check that we've got *some* packages or source files.
		if (rootDoc.specifiedClasses().size()==0 &&
		    rootDoc.specifiedPackages().size()==0)
		    runData.reporter.printError
			(programName+": No packages or classes specified.");
		// start the doclet! (if we haven't seen any errors yet)
		if (runData.reporter.errNum==0) {
		    runData.reporter.flush(); // show all pre-doclet messages
		    success = doclet.start(rootDoc);
		}
	    }
	    // okay, report on errors and warnings.
	    if (runData.reporter.errNum>0)
		runData.reporter.printNotice
		    (runData.reporter.errNum+" error"+
		     (runData.reporter.errNum==1?"":"s")+".");
	    if (runData.reporter.warnNum>0)
		runData.reporter.printNotice
		    (runData.reporter.warnNum+" warning"+
		     (runData.reporter.warnNum==1?"":"s")+".");
	    
	    // return an appropriate exit code.
	    return (success&&runData.reporter.errNum==0)?0:1;
	} catch (Throwable t) {
	    // if anything escapes, return w/ an error code.
	    runData.reporter.flush(); // make sure order of output is correct.
	    t.printStackTrace(errWriter);
	    return 1;
	} finally {
	    runData.reporter.flush(); // print all messages.
	}
    }

    /** Create a doclet instance given its classname.
     *  XXX Note that we don't do anything with the docletPath here.
     */
    private static Doclet findDoclet(String docletPath, String className) {
	try {
	    Class docClass = Class.forName(className);
	    return (Doclet) docClass.newInstance();
	} catch (ClassNotFoundException e) {
	    throw new RuntimeException
		("Couldn't find doclet "+className);
	} catch (InstantiationException e) {
	    throw new RuntimeException
		("Couldn't instantiate doclet "+className);
	} catch (IllegalAccessException e) {
	    throw new RuntimeException
		("Access exception creating doclet: "+e);
	} catch (ClassCastException e) {
	    throw new RuntimeException
		(className+" is not a Doclet.");
	}
    }
    static void doHelp(DocErrorReporter reporter,
		       String programName, Doclet doclet) {
	reporter.printNotice("usage: "+programName+
			     " [options] [packagenames] [sourcefiles] "+
			     "[classnames] [@files]");
	// now iterate over standard options.
	List<Option> options = new ArrayList<Option>(stdOptions.values());
	Collections.sort(options);
	for (Option opt : options) {
	    StringBuffer sb=new StringBuffer();
	    sb.append(opt.optionName);
	    sb.append(' ');
	    sb.append(opt.argSummary);
	    sb.append(' ');
	    while (sb.length()<26) sb.append(' ');
	    sb.append(opt.optionHelp);
	    reporter.printNotice(sb.toString());
	}
	reporter.printNotice("");
	reporter.printNotice("Provided by "+doclet.getName()+
			     " doclet:");
	doclet.optionHelp(reporter);
    }
    static Map<String,Option> stdOptions=new HashMap<String,Option>();
    static void addOption(Option o) { stdOptions.put(o.optionName, o); }
    static {
	// options will be printed for help in the order in which they
	// are created here.
	addOption(new Option
		  ("-overview", "<file>", 2,
		   "Read overview documentation from HTML file") {
		void process(RunData rd, List<String> args) {
		    File f = new File(args.get(1));
		    if (f.exists() && f.isFile())
			rd.parseControl.setOverviewFile(f);
		    else
			rd.reporter.printError("Can't find overview file: "+f);
		}
	    });
	addOption(new Option
		  ("-public", "", 1, 
		   "Show only public classes and members") {
		void process(RunData rd, List<String> args) {
		    rd.parseControl.setAccess(Modifier.PUBLIC);
		}
	    });
	addOption(new Option
		  ("-protected", "", 1, 
		   "Show protected/public classes and members (default)") {
		void process(RunData rd, List<String> args) {
		    rd.parseControl.setAccess(Modifier.PROTECTED);
		}
	    });
	addOption(new Option
		  ("-package", "", 1,
		   "Show package/protected/public classes and members") {
		void process(RunData rd, List<String> args) {
		    rd.parseControl.setAccess(0);
		}
	    });
	addOption(new Option
		  ("-private", "", 1, 
		   "Show all classes and members") {
		void process(RunData rd, List<String> args) {
		    rd.parseControl.setAccess(Modifier.PRIVATE);
		}
	    });
	addOption(new Option("-help", "", 1,
			     "Display command-line options and exit") {
		void process(RunData rd, List<String> args) {
		    rd.reporter.quiet=false;
		    doHelp(rd.reporter, rd.programName, rd.getDoclet());
		}
	    });
	addOption(new Option("-doclet", "<class>", 2,
			     "Generate output via alternate doclet") {
		void process(RunData rd, List<String> args) {
		    if (rd.doclet!=null)
			throw new RuntimeException
			    ("-doclet given after doclet option.");
		    rd.defaultDocletClassName=args.get(1);
		}
	    });
	addOption(new Option
		  ("-docletpath", "<path>", 2, 
		   "Specify where to find doclet class files") {
		void process(RunData rd, List<String> args) {
		    rd.docletPath=args.get(1);
		}
	    });
	addOption(new Option("-sourcepath", "<pathlist>", 2,
			     "Specify where to find source files") {
		void process(RunData rd, List<String> args) {
		    rd.sourcePath = args.get(1);
		}
	    });
	addOption(new Option
		  ("-classpath", "<pathlist>", 2, 
		   "Specify where to find user class files") {
		void process(RunData rd, List<String> args) {
		    rd.classPath = args.get(1);
		}
	    });
	addOption(new Option("-exclude", "<pkglist>", 2,
			     "Specify a list of packages to exclude") {
		void process(RunData rd, List<String> args) {
		    rd.exclude.addAll(FileUtil.splitColon(args.get(1)));
		}
	    });
	addOption(new Option("-subpackages", "<subpkglist>", 2,
			     "Specify subpackages to recursively load") {
		void process(RunData rd, List<String> args) {
		    rd.subpackages.addAll(FileUtil.splitColon(args.get(1)));
		}
	    });
	addOption(new Option
		  ("-breakiterator", "", 1, 
		   "Compute 1st sentence with BreakIterator") {
		void process(RunData rd, List<String> args) { /* nop */ }
	    });
	addOption(new IgnoreOption
		  ("-bootclasspath", "<pathlist>", 2, 
		   "Override location of class files loaded by the "+
		   "bootstrap class loader") {
	    });
	addOption(new Option("-source", "<release>", 2,
			     "Provide source compatibility with the "+
			     "specified release") {
		void process(RunData rd, List<String> args) {
		    String release = args.get(1);
		    if (!release.startsWith("1."))
			rd.reporter.printError("Bad -source option: "+release);
		    else {
			int val = Integer.parseInt(release.substring(2));
			rd.parseControl.setSourceVersion(val);
		    }
		}
	    });
	addOption(new IgnoreOption
		  ("-extdirs", "<dirlist>", 2,
		   "Override location of installed extensions") {
	    });
	addOption(new Option("-verbose", "", 1,
			     "Output messages about what Javadoc is doing") {
		void process(RunData rd, List<String> args) {
		    rd.parseControl.setVerbose(true);
		}
	    });
	addOption(new Option
		  ("-locale", "<name>", 2,
		   "Locale to be used, e.g. en_US or en_US_WIN") {
		final Pattern UNDERSCORE = Pattern.compile("_");
		void process(RunData rd, List<String> args) {
		    // split into three parts separated by underscores.
		    String[] codes = UNDERSCORE.split(args.get(1), 3);
		    String language = codes.length>0?codes[0]:"";
		    String country = codes.length>1?codes[1]:"";
		    String variant = codes.length>2?codes[2]:"";
		    rd.parseControl.setLocale
			(new Locale(language, country, variant));
		}
	    });
	addOption(new Option("-encoding", "<name>", 2, 
			     "Source file encoding name") {
		void process(RunData rd, List<String> args) {
		    rd.parseControl.setEncoding(args.get(1));
		    rd.reporter.encoding=args.get(1);
		}
	    });
	addOption(new Option
		  ("-J<flag>", "", 1,
		   "Pass <flag> directly to the runtime system") {
		void process(RunData rd, List<String> args) {
		    assert false : "should be caught before getting here";
		}
	    });
	// Not documented by javadoc -help
	addOption(new Option("-1.1", "", 1, "[no effect]") {
		void process(RunData rd, List<String> args) { /* nop */ }
	    });
	addOption(new Option("-quiet", "", 1, "Be quiet (default)") {
		void process(RunData rd, List<String> args) {
		    rd.reporter.quiet=true;
		}
	    });
    }
    private abstract static class Option implements Comparable<Option> {
	private final int order;
	public final String optionName;
	public final int len;
	public final String argSummary;
	public final String optionHelp;
	Option(String optionName, String argSummary, int len,
	       String optionHelp) {
	    this.order = counter++;
	    this.optionName = optionName;
	    this.argSummary = argSummary;
	    this.len = len;
	    this.optionHelp = optionHelp;
	}
	private static int counter=0;
	public int compareTo(Option o) { return this.order-o.order; }

	// intended to be overridden
	abstract void process(RunData rd, List<String> optionWithArgs);
    }
    private static class IgnoreOption extends Option {
	IgnoreOption(String optionName, String argSummary, int len,
	       String optionHelp) {
	    super(optionName, argSummary, len, "[ignored]");
	}
	void process(RunData rd, List<String> optionWithArgs) {
	    rd.reporter.printWarning("IGNORING OPTION: "+optionWithArgs);
	}
    }
    private static class RunData {
	final String programName;
	final Reporter reporter;
	final ParseControl parseControl;
	String docletPath=null;
	String defaultDocletClassName;
	Doclet doclet=null;

	String sourcePath=null, classPath=null;
	List<String> subpackages = new ArrayList<String>();
	List<String> exclude = new ArrayList<String>();

	RunData(String programName,
		PrintWriter errWriter, PrintWriter warnWriter,
		PrintWriter noticeWriter,
		String defaultDocletClassName) {
	    this.programName = programName;
	    this.reporter = new Reporter(errWriter, warnWriter, noticeWriter);
	    this.parseControl = new ParseControl(reporter);
	    this.defaultDocletClassName = defaultDocletClassName;
	}
	Doclet getDoclet() {
	    if (doclet==null)
		doclet=findDoclet(docletPath, defaultDocletClassName);
	    return doclet;
	}
    }
    private static class Reporter implements DocErrorReporter {
	final PrintWriter errWriter, warnWriter, noticeWriter;
	String encoding=null;
	int errNum=0, warnNum=0;
	boolean quiet=false;
	public void printError(String msg) { printError(null, msg); }
	public void printWarning(String msg) { printWarning(null, msg); }
	public void printNotice(String msg) { printNotice(null, msg); }
	public void printError(SourcePosition pos, String msg) {
	    print(errWriter, pos, msg);
	    errNum++;
	}
	public void printWarning(SourcePosition pos, String msg) {
	    print(warnWriter, pos, msg);
	    warnNum++;
	}
	public void printNotice(SourcePosition pos, String msg) {
	    if (!quiet) print(noticeWriter, pos, msg);
	}
	private void print(PrintWriter pw, SourcePosition pos, String msg) {
	    if (pos!=null) {
		msg=pos.toString()+": "+msg;
	    }
	    pw.println(msg);
	    if (pos!=null && pos.file()!=null && pos.line()>0) {
		// print the appropriate line of the file.
		String line=null;
		try {
		    BufferedReader reader = new BufferedReader
			(new EscapedUnicodeReader
			 (FileUtil.fileReader(pos.file(), encoding, this)));
		    for (int i=1; i<pos.line(); i++)
			if (null==reader.readLine()) break;
		    line = reader.readLine();
		    reader.close();
		} catch (java.io.IOException e) { /* ignore */ }
		if (line!=null) {
		    pw.println(line);
		    if (pos.column()>0) {
			// print a carat at the appropriate position.
			for (int i=1; i<pos.column(); i++)
			    pw.print(' ');
			pw.println('^');
		    }
		}
	    }
	}
	void flush() {
	    noticeWriter.flush();
	    warnWriter.flush();
	    errWriter.flush();
	}
	Reporter(PrintWriter errWriter, PrintWriter warnWriter,
		 PrintWriter noticeWriter) {
	    this.errWriter = errWriter;
	    this.warnWriter = warnWriter;
	    this.noticeWriter = noticeWriter;
	}
    }
}
