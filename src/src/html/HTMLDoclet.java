// HTMLDoclet.java, created Tue Mar 18 14:31:10 2003 by cananian
// Copyright (C) 2003 C. Scott Ananian (cscott@cscott.net)
// Licensed under the terms of the GNU GPL; see COPYING for details.
package net.cscott.sinjdoc.html;

import net.cscott.sinjdoc.ClassDoc;
import net.cscott.sinjdoc.ClassType;
import net.cscott.sinjdoc.Doclet;
import net.cscott.sinjdoc.DocErrorReporter;
import net.cscott.sinjdoc.PackageDoc;
import net.cscott.sinjdoc.RootDoc;

import java.io.*;
import java.util.*;
/**
 * The <code>HTMLDoclet</code> is the standard doclet for SinjDoc.  It
 * generates HTML-format documentation.
 * 
 * @author  C. Scott Ananian (cscott@cscott.net)
 * @version $Id: HTMLDoclet.java,v 1.38 2003/08/01 00:01:15 cananian Exp $
 */
public class HTMLDoclet extends Doclet {
    HTMLOptions options = new HTMLOptions();
    public String getName() { return "Standard"; }

    void makeStylesheet(RootDoc root, HTMLUtil hu) {
	// get a reader for the style sheet.
	Reader styleReader = null;
	if (options.stylesheetFile!=null) try {
	    styleReader = new FileReader(options.stylesheetFile);
	} catch (FileNotFoundException e) {
	    root.printError("Couldn't open "+options.stylesheetFile);
	}
	if (styleReader==null) styleReader=hu.resourceReader("stylesheet.css");
	// get a writer for the emitted style sheet.
	TemplateContext context = new TemplateContext
	    (root, options, new URLContext("stylesheet.css"));
	TemplateWriter styleWriter=new TemplateWriter(styleReader,hu,context);
	// copy from template.
	styleWriter.copyRemainder(root);
    }
    void makeTopIndex(RootDoc root, HTMLUtil hu) {
	// THREE CASES:
	//   zero packages specified: use index-nopackages.html with
	//     first class page in main frame.
	//   one package specified: use index-nopackages.html with
	//     package overview in main frame.
	//   multiple packages specified: use index-packages.html
	//(remember to use <unnamed package> in package list where appropriate)
	TemplateContext context = new TemplateContext
	    (root, options, new URLContext("index.html"));
	int numPackages = root.specifiedPackages().size();
	TemplateWriter indexWriter;
	String mainURL;

	if (numPackages==0)
	    mainURL=HTMLUtil.toURL(Collections.min
				   (root.specifiedClasses(),
				    new DocComparator<ClassDoc>()));
	else if (numPackages==1 && root.specifiedClasses().size()==0)
	    mainURL=HTMLUtil.toURL(root.specifiedPackages().get(0),
				   "package-summary.html");
	else
	    mainURL=null;

	if (mainURL==null) {
	    makeOverviewFrame(root, hu);
	    makeOverviewSummary(root, hu);
	    indexWriter=new TemplateWriter("index-packages.html",hu,context);
	} else {
	    mainURL = context.curURL.makeRelative(mainURL);
	    indexWriter=new TemplateWriter("index-nopackages.html",hu,context);
	    indexWriter.copyToSplit(root);
	    indexWriter.print(mainURL);
	    indexWriter.copyToSplit(root);
	    indexWriter.print(mainURL);
	}
	indexWriter.copyRemainder(root);
	// done!
    }
    void makeOverviewFrame(RootDoc root, HTMLUtil hu) {
	// make overview-frame.
	TemplateContext context = new TemplateContext
	    (root, options, new URLContext("overview-frame.html"));
	TemplateWriter tw=new TemplateWriter("overview-frame.html",hu,context);
	tw.copyRemainder(root);
	// now emit package-frame.html for packages with included classes
	//  (not referenced except by this file)
	for (PackageDoc pd : hu.allDocumentedPackages(root))
	    makePackageFrame(root, hu, pd);
    }
    void makeOverviewSummary(RootDoc root, HTMLUtil hu) {
	TemplateContext context = new TemplateContext
	    (root, options, new URLContext("overview-summary.html"));
	TemplateWriter tw = new TemplateWriter
	    ("overview-summary.html"/*resource*/, hu, context);
	tw.copyRemainder(root); // done!
    }
    void makeAllClassesFrame(RootDoc root, HTMLUtil hu) {
	TemplateContext context = new TemplateContext
	    (root, options, new URLContext("allclasses-frame.html"));
	TemplateWriter tw = new TemplateWriter
	    ("allclasses-frame.html", hu, context);
	tw.copyRemainder(root);
    }
    void makeAllClassesNoFrame(RootDoc root, HTMLUtil hu) {
	TemplateContext context = new TemplateContext
	    (root, options, new URLContext("allclasses-noframe.html"));
	TemplateWriter tw = new TemplateWriter
	    ("allclasses-noframe.html", hu, context);
	tw.copyRemainder(root);
    }
    void makePackageFrame(RootDoc root, HTMLUtil hu, PackageDoc pd) {
	TemplateContext context = new TemplateContext
	    (root, options, new URLContext(hu.toURL(pd, "package-frame.html")),
	     pd);
	TemplateWriter tw = new TemplateWriter
	    ("package-frame.html"/*resource*/, hu, context);
	tw.copyRemainder(root);
    }
    void makePackageSummary(RootDoc root, HTMLUtil hu, PackageDoc pd) {
	TemplateContext context = new TemplateContext
	    (root,options, new URLContext(hu.toURL(pd,"package-summary.html")),
	     pd);
	TemplateWriter tw = new TemplateWriter
	    ("package-summary.html"/*resource*/, hu, context);
	tw.copyRemainder(root); // done!
    }
    void makePackageTree(RootDoc root, HTMLUtil hu, PackageDoc pd) {
	TemplateContext context = new TemplateContext
	    (root, options, new URLContext(hu.toURL(pd, "package-tree.html")),
	     pd);
	// xxx do me.
    }
    void makeClassPage(RootDoc root, HTMLUtil hu, ClassDoc cd) {
	TemplateContext context = new TemplateContext
	    (root, options, new URLContext(hu.toURL(cd)),
	     cd.containingPackage(), cd);
	TemplateWriter tw = new TemplateWriter
	    ("class-page.html"/*resource*/, hu, context);
	tw.copyRemainder(root); // done!
    }
    void makePackageList(RootDoc root, HTMLUtil hu) {
	URLContext context = new URLContext("package-list");
	// note that package-list will be in specified output encoding.
	PrintWriter pw = hu.fileWriter(context, options);
	for (PackageDoc pd : hu.allDocumentedPackages(root))
	    pw.println(pd.name());
	pw.close(); // done!
    }

    public boolean start(RootDoc root) {
	// create our HTMLUtil object.
	HTMLUtil hu = new HTMLUtil(root);
	// parse options.
	options.parseOptions(root.options());
	PackageGroup.groupPackages(options.groups,
				   hu.allDocumentedPackages(root));
	// put the stylesheet where it belongs.
	makeStylesheet(root, hu);
	// top-level index.
	makeTopIndex(root, hu);
	// list all documented classes.
	makeAllClassesFrame(root, hu);
	makeAllClassesNoFrame(root, hu);
	// top-level class hierarchy.
	if (options.emitTreePage) {
	    // XXX create overview-tree.html
	}
	// for each package to be documented...
	for (PackageDoc pd : hu.allDocumentedPackages(root)) {
	    // create package pages.
	    makePackageSummary(root, hu, pd);
	    if (options.emitTreePage)
		makePackageTree(root, hu, pd);
	    // XXX copy doc-files.
	}
	// for each class to be documented...
	for (ClassDoc cd : root.classes())
	    // create class page.
	    makeClassPage(root, hu, cd);
	// make package list
	makePackageList(root, hu);
	if (options.emitUsePage) {
	    // XXX create class-use
	}
	if (options.emitIndexPage) {
	    // XXX create index pages
	}
	if (options.emitHelpPage) {
	    // XXX create help-doc.html
	}
	if (options.emitDeprecatedPage) {
	    // XXX create deprecated-list.html
	}
	// XXX create constant-values.html
	// XXX create serialized-form.html
	// XXX create annotated source code.
	return true; /* do nothing */
    }

    public int optionLength(String option) {
	return options.optionLength(option);
    }
    public boolean validOptions(List<List<String>> optionList,
				DocErrorReporter reporter) {
	for (List<String> anOption : optionList )
	    if (!options.validOption(anOption, reporter))
		return false;
	// all options valid.
	return true;
    }
    public void optionHelp(DocErrorReporter reporter) {
	options.printHelp(reporter);
    }
}
