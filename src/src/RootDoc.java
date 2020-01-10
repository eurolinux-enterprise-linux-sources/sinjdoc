// RootDoc.java, created Wed Mar 19 12:56:45 2003 by cananian
// Copyright (C) 2003 C. Scott Ananian (cscott@cscott.net)
// Licensed under the terms of the GNU GPL; see COPYING for details.
package net.cscott.sinjdoc;

import java.util.Collection;
import java.util.List;
/**
 * The <code>RootDoc</code> class holds the information from one run of
 * SinjDoc; in particular the packages, classes, and options specified by
 * the user.
 * 
 * @author  C. Scott Ananian (cscott@cscott.net)
 * @version $Id: RootDoc.java,v 1.7 2003/05/08 03:54:25 cananian Exp $
 * @see com.sun.javadoc.RootDoc
 */
public interface RootDoc extends Doc, DocErrorReporter {
    /**
     * Return the classes and interfaces to be documented.  This includes
     * both the classes returned by <code>specifiedClasses()</code> as well
     * as classes within the packages returned by
     * <code>specifiedPackages()</code>. */
    public Collection<ClassDoc> classes();
    /**
     * Return a <code>ClassDoc</code> for the specified class or interface
     * name.
     * @param canonicalName canonical class name.
     * @return a <code>ClassDoc</code> representing the specified class,
     *  or <code>null</code> if this class is not referenced.
     */
    public ClassDoc classNamed(String canonicalName);
    /** Command-line options.
     *  Each complete option is in its own list.
     */
    public List<List<String>> options();
    /**
     * Return a <code>PackageDoc</code> for the specified fully-qualified
     * package name.
     * @param name a fully-qualified package name.
     * @return a <code>PackageDoc</code> holding the specified package,
     *   or <code>null</code> if this package is not referenced.
     */
    public PackageDoc packageNamed(String name);
    /**
     * Return the classes and interfaces specified on the command line.
     * These are source files which were mentioned explicitly.
     */
    public List<ClassDoc> specifiedClasses();
    /**
     * Return the packages specified on the command-line, either directly
     * or via a <code>-subpackages</code> option.
     */
    public List<PackageDoc> specifiedPackages();
}
