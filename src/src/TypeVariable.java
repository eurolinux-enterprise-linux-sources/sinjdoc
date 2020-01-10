// TypeVariable.java, created Wed Mar 19 15:03:51 2003 by cananian
// Copyright (C) 2003 C. Scott Ananian (cscott@cscott.net)
// Licensed under the terms of the GNU GPL; see COPYING for details.
package net.cscott.sinjdoc;

import java.util.List;
/**
 * The <code>TypeVariable</code> interface represents a type
 * variable declared as a formal parameter to a generic class,
 * interface, or method.  Every actual type variable supports
 * exact one of the two subinterfaces <code>MethodTypeVariable</code> or
 * <code>ClassTypeVariable</code>.
 * 
 * @author  C. Scott Ananian (cscott@cscott.net)
 * @version $Id: TypeVariable.java,v 1.6 2003/05/08 03:54:25 cananian Exp $
 * @see java.lang.reflect.TypeVariable
 */
public interface TypeVariable extends Type {
    /**
     * Return the bounds on this type variable.  If there are no bounds
     * specified, returns an array of length one containing the
     * <code>ClassType</code> for <code>java.lang.Object</code>. */
    public List<Type> getBounds();
    /** Return the name of this type variable. */
    public String getName();
}
