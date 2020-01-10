// MethodTypeVariable.java, created Wed Mar 19 15:05:26 2003 by cananian
// Copyright (C) 2003 C. Scott Ananian (cscott@cscott.net)
// Licensed under the terms of the GNU GPL; see COPYING for details.
package net.cscott.sinjdoc;

/**
 * The <code>MethodTypeVariable</code> interface represents a type
 * variable declared as a formal parameter to a generic method.
 * 
 * @author  C. Scott Ananian (cscott@cscott.net)
 * @version $Id: MethodTypeVariable.java,v 1.5 2003/05/08 03:54:25 cananian Exp $
 * @see java.lang.reflect.MethodTypeVariable
 */
public interface MethodTypeVariable extends TypeVariable {
    /** Return the generic method which declares this type variable. */
    public ExecutableMemberDoc declaringMethod();
}
