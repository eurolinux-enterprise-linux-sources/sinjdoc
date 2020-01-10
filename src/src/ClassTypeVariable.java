// ClassTypeVariable.java, created Wed Mar 19 15:01:41 2003 by cananian
// Copyright (C) 2003 C. Scott Ananian (cscott@cscott.net)
// Licensed under the terms of the GNU GPL; see COPYING for details.
package net.cscott.sinjdoc;

/**
 * The <code>ClassTypeVariable</code> interface represents a type variable
 * declared as a formal parameter to a generic class or interface.
 * 
 * @author  C. Scott Ananian (cscott@cscott.net)
 * @version $Id: ClassTypeVariable.java,v 1.4 2003/05/08 03:54:25 cananian Exp $
 * @see java.lang.reflect.ClassTypeVariable
 */
public interface ClassTypeVariable extends TypeVariable {
    /** Return the class where this type variable was declared. */
    public ClassDoc declaringClass();
}
