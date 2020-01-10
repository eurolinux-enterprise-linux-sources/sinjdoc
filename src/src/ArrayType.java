// ArrayType.java, created Wed Mar 19 13:05:01 2003 by cananian
// Copyright (C) 2003 C. Scott Ananian (cscott@cscott.net)
// Licensed under the terms of the GNU GPL; see COPYING for details.
package net.cscott.sinjdoc;

/**
 * The <code>ArrayType</code> interface represents a java array type.
 * 
 * @author  C. Scott Ananian (cscott@cscott.net)
 * @version $Id: ArrayType.java,v 1.2 2003/05/08 03:54:25 cananian Exp $
 */
public interface ArrayType extends Type {
    /** The base type of the array. */
    public Type baseType();
    /** The dimension of the array type. */
    public int dimension();
    /** Returns a string representation of the type.  Returns the
     *  string representation of the base type followed by
     *  dimension information.  For example, a two-dimensional array
     *  of <code>String</code> returns "String[][]". */
    public String toString();
}
