// Type.java, created Wed Mar 19 13:05:01 2003 by cananian
// Copyright (C) 2003 C. Scott Ananian (cscott@cscott.net)
// Licensed under the terms of the GNU GPL; see COPYING for details.
package net.cscott.sinjdoc;

/**
 * The <code>Type</code> interface represents a java type.  A
 * <code>Type</code> can be a (possibly-parameterized) class type,
 * a primitive data type like int and char, or a type variable
 * declared in a generic class or method.
 * 
 * @author  C. Scott Ananian (cscott@cscott.net)
 * @version $Id: Type.java,v 1.9 2003/05/08 03:54:25 cananian Exp $
 * @see com.sun.javadoc.Type
 * @see java.lang.reflect.Type
 */
public interface Type {
    /** Return the canonical name of the <em>erasure</em> of this type.
     *  (Anonymous types and types contained within anonymous types do not
     *  have canonical names, but are unrepresented here in any case).
     *  See section 6.7 of the JLS for the definition of canonical names;
     *  using the erasure of the type allows this definition to extend to
     *  parameterized types and type variables.
     *  @see ExecutableMemberDoc#signature()
     */
    public String signature();

    /** Accept a visitor. */
    public <T> T accept(TypeVisitor<T> visitor);
}
