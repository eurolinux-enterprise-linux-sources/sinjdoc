// ParameterizedType.java, created Wed Mar 19 15:06:55 2003 by cananian
// Copyright (C) 2003 C. Scott Ananian (cscott@cscott.net)
// Licensed under the terms of the GNU GPL; see COPYING for details.
package net.cscott.sinjdoc;

import java.util.List;
/**
 * The <code>ParameterizedType</code> interface represents a parameterized
 * type such as <code>Collection&lt;Integer&gt;</code> or
 * <code>HashMap&lt;String,Double&gt;</code>.
 * 
 * @author  C. Scott Ananian (cscott@cscott.net)
 * @version $Id: ParameterizedType.java,v 1.8 2003/07/29 16:02:37 cananian Exp $
 * @see java.lang.reflect.ParameterizedType
 */
public interface ParameterizedType extends Type {
    /** Return the <code>Type</code> corresponding to the "raw class"
     *  of this parameterized type; that is, the type without parameters. */
    public ClassType getBaseType();
    /** Return the <code>Type</code> corresponding to the (possibly
     *  parameterized) declaring class of this parameterized type, or
     *  <code>null</code> if this type does not represent an inner
     *  class.  Note that this method will return a <code>ClassType</code>
     *  if this type represents a static inner class, even if the
     *  declaring class is parameterized.
     */
    public Type getDeclaringType();
    /** Return the type arguments that this parameterized type has been
     *  instantiated with.  Note that for nested parameterized types,
     *  the size of the returned list will always be equal to the size of
     *  of <code>getBaseType().typeParameters()</code>; however, the
     *  <code>getDeclaringType()</code> method may return a
     *  parameterized type.  For example, for the type:
     *  <code>A&lt;Integer&gt;.B&lt;String&gt;.C</code>, this method will
     *  return a zero-length list, and <code>getDeclaringType()</code>
     *  will return the <code>ParameterizedType</code> for
     *  <code>A&lt;Integer&gt;.B&lt;String&gt;</code>.
     */
    public List<TypeArgument> getActualTypeArguments();
}
