// PackageDoc.java, created Wed Mar 19 12:23:13 2003 by cananian
// Copyright (C) 2003 C. Scott Ananian (cscott@cscott.net)
// Licensed under the terms of the GNU GPL; see COPYING for details.
package net.cscott.sinjdoc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
/**
 * The <code>PackageDoc</code> class represents a java package.  It
 * provides access to information about the package, the package's
 * comment and tags, and the classes in the package.  It does *not*
 * necessarily represent a package included in the current SinjDoc run.
 * 
 * @author  C. Scott Ananian (cscott@cscott.net)
 * @version $Id: PackageDoc.java,v 1.7 2003/05/08 03:54:25 cananian Exp $
 * @see com.sun.javadoc.PackageDoc
 */
public interface PackageDoc extends Doc {
    /** Return all classes in this package, including exceptions, errors, 
     *  and interfaces, and classes which aren't being documented. */
    public List<ClassType> allClasses();
    /**
     * Return all included classes and interfaces in this package,
     * including exceptions and errors. */
    public List<ClassDoc> includedClasses();
    /** Return all included error classes in this package. */
    public List<ClassDoc> includedErrors();
    /** Return all included exception classes in this package. */
    public List<ClassDoc> includedExceptions();
    /** Return all included interfaces in this package. */
    public List<ClassDoc> includedInterfaces();
    /** Return all included non-interface classes in this package which
     *  are not errors or exceptions. */
    public List<ClassDoc> includedOrdinaryClasses();
    // xxx fully qualified name, or just partial name?  let's say either.
    /** Lookup a class within this package.  Returns <code>null</code>
     *  if the class is not found. */
    public ClassDoc findClass(String className);
}
