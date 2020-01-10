// Parameter.java, created Wed Mar 19 12:38:54 2003 by cananian
// Copyright (C) 2003 C. Scott Ananian (cscott@cscott.net)
// Licensed under the terms of the GNU GPL; see COPYING for details.
package net.cscott.sinjdoc;

/**
 * The <code>Parameter</code> interface represents parameter information,
 * including parameter type and parameter name.
 * 
 * @author  C. Scott Ananian (cscott@cscott.net)
 * @version $Id: Parameter.java,v 1.5 2003/07/29 16:02:37 cananian Exp $
 * @see com.sun.javadoc.Parameter
 */
public interface Parameter {
    /** Return the local name of this parameter.   For example, if this
     *  parameter is <code>short index</code>, returns "index". */
    public abstract String name();
    /** Return the type of this parameter.  For example, if this
     *  parameter is <code>short index</code>, then
     *  <code>type().typeName()</code> would be the string "short". */
    public abstract Type type();
    /** Returns <code>true</code> if this is a "varargs" parameter. */
    public abstract boolean isVarArgs();
    /** Return the "printable type" -- this is the component type
     *  of the type of a varargs parameter, otherwise it is the
     *  type of the parameter.
     */
    public abstract Type printableType();
    /** Return a human-readable string representing this parameter.
     *  For example, if this parameter is <code>short index</code>,
     *  returns "short index". */
    public abstract String toString();
}
