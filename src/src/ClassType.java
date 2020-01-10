// ClassType.java, created Wed Mar 19 13:05:01 2003 by cananian
// Copyright (C) 2003 C. Scott Ananian (cscott@cscott.net)
// Licensed under the terms of the GNU GPL; see COPYING for details.
package net.cscott.sinjdoc;

import java.util.List;
/**
 * The <code>ClassType</code> interface represents a concrete java class or
 * primitive data type.  It does not represent type variables.
 * 
 * @author  C. Scott Ananian (cscott@cscott.net)
 * @version $Id: ClassType.java,v 1.6 2003/06/25 01:40:10 cananian Exp $
 * @see com.sun.javadoc.Type
 */
public interface ClassType extends Type {
    /**
     * Return the <code>ClassDoc</code> corresponding to this type,
     * ignoring any array dimensions or parameters.  Returns
     * <code>null</code> if it is a primitive type, or if the
     * type is not included. */
    public ClassDoc asClassDoc();
    /**
     * Return the canonical name of the type.  For example,
     * "java.lang.String".  See section 6.7 of the JLS. */
    public String canonicalTypeName();
    /**
     * Return the unqualified name of this type excluding any
     * package information.  For example, "String".   Note that inner class
     * specifications <i>are</i> included in this name; i.e. the returned
     * string may contain dots. */
    public String typeName();
    /**
     * Return the simple name of this type, not including the names of
     * any declaring classes. */
    public String name();
}
