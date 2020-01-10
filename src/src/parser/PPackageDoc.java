// PackageDoc.java, created Wed Mar 19 12:23:13 2003 by cananian
// Copyright (C) 2003 C. Scott Ananian (cscott@cscott.net)
// Licensed under the terms of the GNU GPL; see COPYING for details.
package net.cscott.sinjdoc.parser;

import net.cscott.sinjdoc.ClassDoc;
import net.cscott.sinjdoc.ClassType;
import net.cscott.sinjdoc.Doc;
import net.cscott.sinjdoc.Type;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
/**
 * The <code>PPackageDoc</code> class represents a java package.  It
 * provides access to information about the package, the package's
 * comment and tags, and the classes in the package.  It does *not*
 * necessarily represent a package included in the current SinjDoc run.
 * 
 * @author  C. Scott Ananian (cscott@cscott.net)
 * @version $Id: PPackageDoc.java,v 1.19 2003/08/01 00:01:26 cananian Exp $
 */
class PPackageDoc extends PDoc
    implements net.cscott.sinjdoc.PackageDoc {
    // package-local code can add classes to this array.
    final List<ClassType> classes = new ArrayList<ClassType>();
    final String name;
    final boolean isIncluded;
    final String packageText;
    final PSourcePosition packagePosition;
    final TypeContext packageContext;
    PPackageDoc(ParseControl pc, String packageName, boolean isIncluded) {
	super(pc);
	this.name = packageName;
	this.isIncluded = isIncluded;
	File packageTextFile = null;
	if (isIncluded) {
	    File packageDir = pc.sourcePath.findPackage(packageName);
	    packageTextFile = new File(packageDir, "package.html");
	    if (!packageTextFile.exists()) packageTextFile=null;
	}
	Pair<String,PSourcePosition> pair =
	    FileUtil.rawFileText(packageTextFile, pc.encoding,
				 false/*not unicode escaped*/, pc.reporter);
	this.packageText = pair.left;
	this.packagePosition = pair.right;
	this.packageContext = new TypeContext(pc, this);
    }
    // methods abstract in PDoc
    public String getRawCommentText() { return packageText; }
    public PSourcePosition getRawCommentPosition() { return packagePosition; }
    public TypeContext getCommentContext() { return packageContext; }
    public boolean isIncluded() { return isIncluded; }
    public String name() { return name; }
    // PackageDoc implementation:
    /** @return false */
    public final boolean shouldStripStars() { return false; }
    public List<ClassType> allClasses() {
	// XXX use reflection (via the ParseControl object) to create
	// EagerClassType objects if !isIncluded.
	return Collections.unmodifiableList(classes);
    }
    public List<ClassDoc> includedClasses() {
	List<ClassType> all = allClasses();
	List<ClassDoc> result = new ArrayList<ClassDoc>(all.size());
	for (ClassType ct : all) {
	    ClassDoc cd = ct.asClassDoc();
	    if (cd==null || !cd.isIncluded()) continue;
	    result.add(cd);
	}
	return Collections.unmodifiableList(result);
    }
    public List<ClassDoc> includedErrors() {
	List<ClassDoc> list = new ArrayList<ClassDoc>(includedClasses());
	for (Iterator<ClassDoc> it=list.iterator(); it.hasNext(); )
	    if (!it.next().isError()) it.remove();
	return Collections.unmodifiableList(list);
    }
    public List<ClassDoc> includedExceptions() {
	List<ClassDoc> list = new ArrayList<ClassDoc>(includedClasses());
	for (Iterator<ClassDoc> it=list.iterator(); it.hasNext(); )
	    if (!it.next().isException()) it.remove();
	return Collections.unmodifiableList(list);
    }
    public List<ClassDoc> includedInterfaces() {
	List<ClassDoc> list = new ArrayList<ClassDoc>(includedClasses());
	for (Iterator<ClassDoc> it=list.iterator(); it.hasNext(); )
	    if (!it.next().isInterface()) it.remove();
	return Collections.unmodifiableList(list);
    }
    public List<ClassDoc> includedOrdinaryClasses() {
	List<ClassDoc> list = new ArrayList<ClassDoc>(includedClasses());
	for (Iterator<ClassDoc> it=list.iterator(); it.hasNext(); )
	    if (!it.next().isOrdinaryClass()) it.remove();
	return Collections.unmodifiableList(list);
    }
    // xxx canonical name, or just partial name?  let's say either.
    public ClassDoc findClass(String className) {
	for (ClassDoc cd : includedClasses()) {
	    if (cd.name().equals(className) ||
		cd.canonicalName().equals(className))
		return cd;
	}
	return null; // not found.
    }
    public String toString() { return name(); }
}
