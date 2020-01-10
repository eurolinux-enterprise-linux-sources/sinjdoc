// TypeArgument.java, created Mon Jul 28 22:33:21 2003 by cananian
// Copyright (C) 2003 C. Scott Ananian <cscott@cscott.net>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package net.cscott.sinjdoc;

/**
 * A <code>TypeArgument</code> is either a type or a wildcard
 * specification such as "<code>&#63;&nbsp;extends&nbsp;Number</code>",
 * "<code>&#63;&nbsp;super&nbsp;Integer</code>", or "<code>&#63;</code>".
 * 
 * @author  C. Scott Ananian (cscott@cscott.net)
 * @version $Id: TypeArgument.java,v 1.1 2003/07/29 16:02:37 cananian Exp $
 */
public interface TypeArgument {
    /** Return the <code>Type</code> bound corresponding to this
     *  <code>TypeArgument</code>.  Bivariant type arguments
     *  should return the <code>Type</code> corresponding to
     *  <code>java.lang.Object</code>.  Covariant, contravariant, or
     *  invariant type arguments should return the type forming
     *  the bound on the argument type. */
    public Type getType();
    /** Return true if this type argument is covariant; for example,
     *    <code>Set&lt;&#63;&nbsp;extends&nbsp;Number&gt;</code>.
     *  Also returns true if the type argument is bivariant; for example,
     *    <code>Set&lt;&#63;&gt;</code>.
     */
    public boolean isCovariant();
    /** Return true if this type argument is contravariant; for example,
     *    <code>Comparable&lt;&#63;&nbsp;super&nbsp;Integer&gt;</code>.
     *  Also returns true if the type argument is bivariant; for example,
     *    <code>Comparable&lt;&#63;&gt;</code>.
     */
    public boolean isContravariant();
}
