// HTMLOptions.java, created Mon Mar 31 14:05:16 2003 by cananian
// Copyright (C) 2003 C. Scott Ananian (cscott@cscott.net)
// Licensed under the terms of the GNU GPL; see COPYING for details.
package net.cscott.sinjdoc.html;

import net.cscott.sinjdoc.DocErrorReporter;
import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * The <code>HTMLOptions</code> class encapsulates and parses all the
 * options given to the <code>HTMLDoclet</code>.
 *
 * @author  C. Scott Ananian (cscott@cscott.net)
 * @version $Id: HTMLOptions.java,v 1.11 2003/08/01 00:01:16 cananian Exp $
 */
class HTMLOptions {
    File docRoot=new File(".");
    File helpFile=null;
    File stylesheetFile=null;
    boolean emitAuthorTag=false;
    boolean emitDeprecatedTag=true;
    boolean emitDeprecatedPage=true;
    boolean emitHelpPage=true;
    boolean emitIndexPage=true;
    boolean emitNavBar=true;
    boolean emitSinceTag=true;
    boolean emitTreePage=true;
    boolean emitUsePage=false;
    boolean emitVersionTag=false;
    List<PackageGroup> groups = new ArrayList<PackageGroup>();
    String docTitle=null;
    String windowTitle=null;
    String header;
    String footer;
    String bottom;
    Charset charSet=null;

    public void parseOptions(List<List<String>> options) {
	for (List<String> anOption : options)
	    optionMap.get(anOption.get(0).toLowerCase()).process(anOption);
	// now deal with defaults. !J means that this logic is not implemented
	// in javadoc; it is specific to SinjDoc.
	if (windowTitle==null) windowTitle=docTitle;
	if (charSet==null) charSet = Charset.forName("UTF-8");
	if (windowTitle!=null && docTitle==null) docTitle=windowTitle;// !J
	if (windowTitle!=null && header==null) header=windowTitle;//!J
	if (header!=null && footer==null) footer=header;
	
	// add default/catch-remainder package group.
	groups.add(new PackageGroup
		   (groups.size()==0?"Packages":"Other Packages", "*"));
    }
    public boolean validOption(List<String> optionWithArgs,
			       DocErrorReporter reporter) {
	String option = optionWithArgs.get(0).toLowerCase();
	return optionMap.get(option).validate
	    (optionWithArgs, reporter);
    }
    public int optionLength(String option) {
	option = option.toLowerCase();
	if (optionMap.containsKey(option))
	    return optionMap.get(option).len;
	return 0;
    }
    public void printHelp(DocErrorReporter reporter) {
	List<Option> options = new ArrayList<Option>(optionMap.values());
	Collections.sort(options);
	for (Option opt : options) {
	    StringBuffer sb=new StringBuffer();
	    sb.append(opt.optionName);
	    sb.append(' ');
	    sb.append(opt.argSummary);
	    sb.append(' ');
	    while (sb.length()<34) sb.append(' ');
	    sb.append(opt.optionHelp);
	    reporter.printNotice(sb.toString());
	}
    }

    Map<String,Option> optionMap=new HashMap<String,Option>();
    void addOption(Option o) { optionMap.put(o.optionName.toLowerCase(), o); }
    {
	// options will be printed for help in the order in which they
	// are created here.
	addOption(new Option("-d", "<directory>", 2,
			     "Destination directory for output files") {
		void process(List<String> optionWithArgs) {
		    docRoot = new File(optionWithArgs.get(1));
		}
	    });
	addOption(new Option("-use", "", 1,
			     "Create class and package usage pages") {
		void process(List<String> optionWithArgs) {
		    emitUsePage = true;
		}
	    });
	addOption(new Option("-version", "", 1,
			     "Include @version paragraphs") {
		void process(List<String> optionWithArgs) {
		    emitVersionTag = true;
		}
	    });
	addOption(new Option("-author", "", 1,
			     "Include @author paragraphs") {
		void process(List<String> optionWithArgs) {
		    emitAuthorTag = true;
		}
	    });
	addOption(new IgnoreOption("-docfilessubdirs", "", 1,
				   "Recursively copy doc-file "+
				   "subdirectories"));
	addOption(new IgnoreOption("-splitindex", "", 1,
				   "Split index into one page per letter"));
	addOption(new Option("-windowtitle", "<text>", 2,
			     "Browser window title for the documentation") {
		void process(List<String> optionWithArgs) {
		    windowTitle = optionWithArgs.get(1);
		}
	    });
	addOption(new Option("-doctitle", "<html-code>", 2,
			     "Title for the overview page") {
		void process(List<String> optionWithArgs) {
		    docTitle = optionWithArgs.get(1);
		}
	    });
	addOption(new Option("-header", "<html-code>", 2,
			     "Header text to include on each page") {
		void process(List<String> optionWithArgs) {
		    header = optionWithArgs.get(1);
		}
	    });
	addOption(new Option("-footer", "<html-code>", 2,
			     "Footer text to include on each page") {
		void process(List<String> optionWithArgs) {
		    footer = optionWithArgs.get(1);
		}
	    });
	addOption(new Option("-bottom", "<html-code>", 2,
			     "Bottom text to include on each page") {
		void process(List<String> optionWithArgs) {
		    bottom = optionWithArgs.get(1);
		}
	    });
	addOption(new IgnoreOption("-link", "<url>", 2,
				   "Create links to javadoc output at <url>"));
	addOption(new IgnoreOption("-linkoffline", "<url> <url2>", 3,
				   "Link to docs at <url> using package list "+
				   "at <url2>"));
	addOption(new IgnoreOption("-excludedocfilessubdir", "<name1>:..", 2,
				   "Exclude any doc-files subdirectories "+
				   "with given name"));
	addOption(new Option("-group", "<name> <p1>:<p2>..", 3,
			     "Group specified packages together on "+
			     "overview page") {
		void process(List<String> optionWithArgs) {
		    groups.add(new PackageGroup(optionWithArgs.get(1),
						optionWithArgs.get(2)));
		}
	    });
	addOption(new IgnoreOption("-nocomment", "", 1,
				   "Suppress description and tags, generate "+
				   "only declarations"));
	addOption(new Option("-nodeprecated", "", 1,
			     "Do not include @deprecated information") {
		void process(List<String> optionWithArgs) {
		    emitDeprecatedTag=false;
		}
	    });
	addOption(new IgnoreOption("-noqualifier", "<name1>:<name2>:...", 2,
				   "Exclude the list of qualifiers from the "+
				   "output"));
	addOption(new Option("-nosince", "", 1,
			     "Do not include @since information") {
		void process(List<String> optionWithArgs) {
		    emitSinceTag=false;
		}
	    });
	addOption(new Option("-nodeprecatedlist", "", 1,
			     "Do not generate page listing deprecated API") {
		void process(List<String> optionWithArgs) {
		    emitDeprecatedPage=false;
		}
	    });
	addOption(new Option("-notree", "", 1,
			     "Do not generate class hierarchy") {
		void process(List<String> optionWithArgs) {
		    emitTreePage=false;
		}
	    });
	addOption(new Option("-noindex", "", 1,
			     "Do not generate index") {
		void process(List<String> optionWithArgs) {
		    emitIndexPage=false;
		}
	    });
	addOption(new Option("-nohelp", "", 1,
			     "Do not generate help link") {
		void process(List<String> optionWithArgs) {
		    emitHelpPage=false;
		}
	    });
	addOption(new Option("-nonavbar", "", 1,
				   "Do not generate navigation bar") {
		void process(List<String> optionWithArgs) {
		    emitNavBar=false;
		}
	    });
	addOption(new IgnoreOption("-serialwarn", "", 1,
				   "Generate warning about @serial tag"));
	addOption(new IgnoreOption("-tag", "<name>:<locations>:<header>", 2,
				   "Specify single argument custom tags"));
	addOption(new IgnoreOption("-taglet", "", 2,
				   "Fully-qualified name of a Taglet class"));
	addOption(new IgnoreOption("-tagletpath", "<pathlist>", 2,
				   "Classpath for taglets"));
	addOption(new CharsetOption("-charset", "<charset>", 2,
				    "Charset for cross-platform viewing of "+
				    "generated documentation") {
		void process(List<String> optionWithArgs) {
		    charSet = Charset.forName(optionWithArgs.get(1));
		}
	    });
	addOption(new FileOption("-helpfile", "<file>", 2,
				 "Source document for help page") {
		void process(List<String> optionWithArgs) {
		    helpFile = new File(optionWithArgs.get(1));
		}
	    });
	addOption(new IgnoreOption("-linksource", "", 1,
				   "Generate HTML for annotated java source"));
	addOption(new FileOption("-stylesheetfile", "<path>", 2,
				 "Alternate style sheet for generated "+
				 "documentation") {
		void process(List<String> optionWithArgs) {
		    stylesheetFile = new File(optionWithArgs.get(1));
		}
	    });
	addOption(new CharsetOption("-docencoding", "<name>", 2,
			     "Output encoding name (synonym of -charset)") {
		void process(List<String> optionWithArgs) {
		    charSet = Charset.forName(optionWithArgs.get(1));
		}
	    });
	addOption(new Option("-title", "<name>", 2,
				   "Deprecated synonym for -doctitle") {
		void process(List<String> optionWithArgs) {
		    docTitle = optionWithArgs.get(1);
		}
	    });
    }

    private abstract class Option implements Comparable<Option> {
	private final int order;
	public final String optionName;
	public final int len;
	public final String argSummary;
	public final String optionHelp;
	Option(String optionName, String argSummary, int len,
	       String optionHelp) {
	    this.order = optionCounter++;
	    this.optionName = optionName;
	    this.argSummary = argSummary;
	    this.len = len;
	    this.optionHelp = optionHelp;
	}
	public int compareTo(Option o) { return this.order-o.order; }

	// intended to be overridden
	abstract void process(List<String> optionWithArgs);
	/** Returns true if the specified option & arguments are valid. */
	boolean validate(List<String> optionWithArgs,
			 DocErrorReporter reporter) {
	    /* do no validation by default. */
	    return true;
	}
    }
    private int optionCounter=0;
    private abstract class FileOption extends Option {
	FileOption(String optionName, String argSummary, int len,
		   String optionHelp) {
	    super(optionName, argSummary, len, optionHelp);
	    assert len==2;
	}
	boolean validate(List<String> optionWithArgs,
			 DocErrorReporter reporter) {
	    File f = new File(optionWithArgs.get(1));
	    if (f.exists() && f.isFile()) return true;
	    reporter.printError("Can't read "+f);
	    return false;
	}
    }
    private abstract class CharsetOption extends Option {
	CharsetOption(String optionName, String argSummary, int len,
		      String optionHelp) {
	    super(optionName, argSummary, len, optionHelp);
	    assert len==2;
	}
	boolean validate(List<String> optionWithArgs,
			 DocErrorReporter reporter) {
	    try {
		Charset cs = Charset.forName(optionWithArgs.get(1));
		if (cs.canEncode()) {
		    if (!cs.isRegistered())
			reporter.printWarning("Selected charset is not "+
					      "IANA-registered; may not be "+
					      "valid for HTML.");
		    return true;
		}
		reporter.printError("Can't encode with charset "+cs);
	    } catch (IllegalArgumentException e) {
		reporter.printError("Invalid charset: "+optionWithArgs.get(1));
	    }
	    return false;
	}
    }
    private final class IgnoreOption extends Option {
	IgnoreOption(String optionName, String argSummary, int len,
		     String optionHelp) {
	    super(optionName, argSummary, len, "[ignored]");
	}
	boolean validate(List<String> optionWithArgs,
			 DocErrorReporter reporter){
	    reporter.printWarning("IGNORING OPTION: "+optionWithArgs);
	    return true;
	}
	void process(List<String> optionWithArgs) { /* ignore */ }
    }
}// HTMLOptions
