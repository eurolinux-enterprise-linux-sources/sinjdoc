// ProgramElementDoc.java, created Wed Mar 19 12:49:27 2003 by cananian
// Copyright (C) 2003 C. Scott Ananian (cscott@cscott.net)
// Licensed under the terms of the GNU GPL; see COPYING for details.
package net.cscott.sinjdoc;

/**
 * The <code>ProgramElementDoc</code> class represents a java program
 * element: class, interface, field, constructor, or method.  This is
 * an abstract class dealing with information common to these elements.
 * 
 * @author  C. Scott Ananian (cscott@cscott.net)
 * @version $Id: ProgramElementDoc.java,v 1.6 2003/06/25 01:51:52 cananian Exp $
 * @see com.sun.javadoc.ProgramElementDoc
 */
public interface ProgramElementDoc extends Doc {
    /**
     * Return the containing class of this program element.  If this
     * is a class item and there is no outer class, return
     * <code>null</code>. */
    public ClassDoc containingClass();
    /**
     * Return the package that this program element is contained in.
     * If in the unnamed package, then the <code>PackageDoc</code>
     * returned will have the name "". */
    public PackageDoc containingPackage();
    /**
     * Get the modifier specifier integer.
     * @see java.lang.reflect.Modifier
     */
    public int modifierSpecifier();
    /**
     * Return the fully-qualified name of this program element.
     * For example, for the class <code>java.util.Hashtable</code>,
     * return "java.util.Hashtable".  For the method
     * <code>bar(String str, int i)</code> in the class <code>Foo</code>
     * in the unnamed package, return "Foo.bar(java.lang.String,int)"
     * (where all parameter types are also given their canonical names).
     * See section 6.7 of the JLS for the difference between canonical
     * and fully-qualified names. */
    public String canonicalName();
    /** Return true if this program element is final. */
    public boolean isFinal();
    /** Return true if this program element is package private. */
    public boolean isPackage();
    /** Return true if this program element is private. */
    public boolean isPrivate();
    /** Return true if this program element is protected. */
    public boolean isProtected();
    /** Return true if this program element is public. */
    public boolean isPublic();
    /** Return true if this program element is static. */
    public boolean isStatic();
    /** Get the modifiers string.  For example, for
     *  <code>public abstract int foo()</code>
     *  return "public abstract". */
    // XXX do we spell out synchronized and native?
    public String modifiers();
}
